package com.epam.edp.demo.service;

import com.epam.edp.demo.dto.BookedTourListResponseDTO;
import com.epam.edp.demo.dto.CreateBookingRequestDTO;
import com.epam.edp.demo.dto.CreateBookingResponseDTO;
import com.epam.edp.demo.dto.PersonalDetailDTO;
import com.epam.edp.demo.dto.UpdateBookingRequestDTO;
import com.epam.edp.demo.enums.BookingState;
import com.epam.edp.demo.model.Booking;
import com.epam.edp.demo.model.Tour;
import com.epam.edp.demo.model.User;
import com.epam.edp.demo.repository.BookingRepository;
import com.epam.edp.demo.repository.TourRepository;
import com.epam.edp.demo.repository.UserRepository;
import com.epam.edp.demo.util.MealPlanFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final String BOOKING_NOT_FOUND = "Booking not found: ";
    private static final String ADULT_LABEL       = " adult";
    private static final String PRICE_ON_REQUEST  = "Price on request";

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ConcurrentMap<String, ReentrantLock> tourBookingLocks = new ConcurrentHashMap<>();

    public BookingService(BookingRepository bookingRepository,
                          TourRepository tourRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────────
    // Create booking (US6)
    // ─────────────────────────────────────────────
    public CreateBookingResponseDTO createBooking(CreateBookingRequestDTO req, String authenticatedUserId) {

        ReentrantLock tourLock = tourBookingLocks.computeIfAbsent(req.getTourId(), ignored -> new ReentrantLock(true));
        tourLock.lock();
        try {

        // Verify the authenticated user matches the requested userId
        if (!authenticatedUserId.equals(req.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only create bookings for yourself");
        }

        // Validate personalDetails count matches total guests
        int totalGuests = req.getGuests().getAdult() + req.getGuests().getChildren();
        if (req.getPersonalDetails() != null && req.getPersonalDetails().size() != totalGuests) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "personalDetails count (" + req.getPersonalDetails().size()
                            + ") must match total guests (" + totalGuests + ")");
        }

        // Load tour
        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tour not found: " + req.getTourId()));

        // Load user to confirm existence
        userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + req.getUserId()));

        // Calculate total price
        String totalPrice = calculateTotalPrice(tour, req.getDuration(), req.getMealPlan(),
                req.getGuests().getAdult());

        // Build Booking entity
        Booking booking = new Booking();
        booking.setUserId(req.getUserId());
        booking.setTourId(req.getTourId());
        booking.setDate(req.getDate());
        booking.setDuration(req.getDuration());
        booking.setMealPlan(req.getMealPlan());
        booking.setAdults(req.getGuests().getAdult());
        booking.setChildren(req.getGuests().getChildren());
        booking.setState(BookingState.BOOKED);
        booking.setTotalPrice(totalPrice);
        booking.setDocumentCount(0);
        booking.setFreeCancellationDaysBefore(tour.getFreeCancellationDaysBefore());

        // Store tour snapshot so data survives if tour is deleted/re-seeded
        booking.setTourName(tour.getName());
        booking.setDestination(tour.getDestination());
        if (tour.getImageUrls() != null && !tour.getImageUrls().isEmpty()) {
            booking.setTourImageUrl(tour.getImageUrls().get(0));
        }

        // Map personal details
        if (req.getPersonalDetails() != null) {
            booking.setPersonalDetails(mapPersonalDetails(req.getPersonalDetails()));
        }

        long updated = tourRepository.incrementBookedCountIfCapacityAvailable(tour.getId());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This tour is fully booked");
        }

        try {
            bookingRepository.save(booking);
        } catch (RuntimeException ex) {
            // Best-effort rollback of reserved seat if booking persistence fails.
            tourRepository.decrementBookedCountIfPositive(tour.getId());
            throw ex;
        }

        // Free cancellation deadline
        LocalDate freeCancelation = null;
        if (tour.getFreeCancellationDaysBefore() != null) {
            freeCancelation = req.getDate().minusDays(tour.getFreeCancellationDaysBefore());
        }

        // Build confirmation message
        String details = buildConfirmationMessage(tour, req);

        return CreateBookingResponseDTO.builder()
                .freeCancelation(freeCancelation)
                .details(details)
                .build();
        } finally {
            tourLock.unlock();
        }
    }

    // ─────────────────────────────────────────────
    // Get bookings for a user (US6)
    // ─────────────────────────────────────────────
    public BookedTourListResponseDTO getBookingsForUser(String userId, String authenticatedUserId) {

        if (!authenticatedUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own bookings");
        }

        List<Booking> bookings = bookingRepository.findByUserId(userId);

        List<BookedTourListResponseDTO.BookingItem> items = bookings.stream()
                .map(this::mapToBookingItem)
                .collect(Collectors.toList());

        return BookedTourListResponseDTO.builder()
                .bookings(items)
                .build();
    }

    // ─────────────────────────────────────────────
    // Cancel booking (US6)
    // ─────────────────────────────────────────────
    public LocalDate cancelBooking(String bookingId, String authenticatedUserId, String cancelReason) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        BOOKING_NOT_FOUND + bookingId));

        // Only the booking owner can cancel
        if (!booking.getUserId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only cancel your own bookings");
        }

        if (booking.getState() == BookingState.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already cancelled");
        }
        if (booking.getState() == BookingState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel a finished booking");
        }

        // Determine free cancellation deadline
        Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);
        LocalDate freeCancelDeadline = null;
        if (tour != null && tour.getFreeCancellationDaysBefore() != null) {
            freeCancelDeadline = booking.getDate().minusDays(tour.getFreeCancellationDaysBefore());
        }

        booking.setState(BookingState.CANCELLED);
        booking.setCanceledBy(authenticatedUserId);
        booking.setCancelReason(cancelReason);
        bookingRepository.save(booking);

        // Decrement tour's bookedCount
        if (tour != null) {
            tourRepository.decrementBookedCountIfPositive(tour.getId());
        }

        return freeCancelDeadline;
    }

    // ─────────────────────────────────────────────
    // Update booking (Edit)
    // ─────────────────────────────────────────────
    public Map<String, Object> updateBooking(
            String bookingId, String authenticatedUserId, UpdateBookingRequestDTO req) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        BOOKING_NOT_FOUND + bookingId));

        validateBookingForEdit(booking, authenticatedUserId);

        List<String> changes = new ArrayList<>();
        Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);

        applyGuestUpdate(booking, req, changes);
        applyPersonalDetailsIfUnchanged(booking, req);
        applyMealPlanUpdate(booking, req, changes);
        applyDateUpdate(booking, req, changes);
        applyDurationUpdate(booking, req, changes);

        if (tour != null) {
            String newPrice = calculateTotalPrice(
                    tour, booking.getDuration(), booking.getMealPlan(), booking.getAdults());
            booking.setTotalPrice(newPrice);
        }

        bookingRepository.save(booking);

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", "Booking updated successfully");
        response.put("changes", changes);
        response.put("bookingId", bookingId);
        response.put("newTotalPrice", booking.getTotalPrice());
        return response;
    }

    private void validateBookingForEdit(Booking booking, String authenticatedUserId) {
        if (!booking.getUserId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only edit your own bookings");
        }
        if (booking.getState() == BookingState.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot edit a cancelled booking");
        }
        if (booking.getState() == BookingState.FINISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot edit a finished booking");
        }
    }

    private void applyGuestUpdate(Booking booking, UpdateBookingRequestDTO req, List<String> changes) {
        if (req.getGuests() == null) {
            return;
        }
        int newAdults = req.getGuests().getAdult();
        int newChildren = req.getGuests().getChildren();
        if (booking.getAdults() == newAdults && booking.getChildren() == newChildren) {
            return;
        }
        String oldGuestsStr = formatGuests(booking);
        booking.setAdults(newAdults);
        booking.setChildren(newChildren);
        if (req.getPersonalDetails() != null) {
            booking.setPersonalDetails(mapPersonalDetails(req.getPersonalDetails()));
        }
        changes.add("Number of tourists: " + oldGuestsStr + " → " + formatGuests(booking));
    }

    private void applyPersonalDetailsIfUnchanged(Booking booking, UpdateBookingRequestDTO req) {
        if (req.getPersonalDetails() == null) {
            return;
        }
        boolean guestsUnchanged = req.getGuests() == null
                || (booking.getAdults() == req.getGuests().getAdult()
                    && booking.getChildren() == req.getGuests().getChildren());
        if (guestsUnchanged) {
            booking.setPersonalDetails(mapPersonalDetails(req.getPersonalDetails()));
        }
    }

    private void applyMealPlanUpdate(Booking booking, UpdateBookingRequestDTO req, List<String> changes) {
        if (req.getMealPlan() == null || req.getMealPlan().equals(booking.getMealPlan())) {
            return;
        }
        String oldMeal = MealPlanFormatter.format(booking.getMealPlan());
        booking.setMealPlan(req.getMealPlan());
        changes.add("Meal plan: " + oldMeal + " → " + MealPlanFormatter.format(req.getMealPlan()));
    }

    private void applyDateUpdate(Booking booking, UpdateBookingRequestDTO req, List<String> changes) {
        if (req.getDate() != null && !req.getDate().equals(booking.getDate())) {
            booking.setDate(req.getDate());
            changes.add("Start date updated");
        }
    }

    private void applyDurationUpdate(Booking booking, UpdateBookingRequestDTO req, List<String> changes) {
        if (req.getDuration() == null || req.getDuration().equals(booking.getDuration())) {
            return;
        }
        String oldDur = booking.getDuration();
        booking.setDuration(req.getDuration());
        changes.add("Duration: " + oldDur + " → " + req.getDuration());
    }


    // ─────────────────────────────────────────────
    // Scheduled: auto-finish tours past their end date
    // Runs every day at midnight
    // ─────────────────────────────────────────────
    @Scheduled(cron = "0 0 0 * * *")
    public void markFinishedBookings() {
        List<Booking> bookedList = bookingRepository.findByState(BookingState.BOOKED);
        LocalDate today = LocalDate.now();

        List<Booking> toFinish = new ArrayList<>();
        for (Booking b : bookedList) {
            if (b.getDate() != null && b.getDuration() != null) {
                int days = parseDurationDays(b.getDuration());
                LocalDate endDate = b.getDate().plusDays(days);
                if (!today.isBefore(endDate)) {
                    b.setState(BookingState.FINISHED);
                    toFinish.add(b);
                }
            }
        }

        if (!toFinish.isEmpty()) {
            bookingRepository.saveAll(toFinish);
        }
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    private BookedTourListResponseDTO.BookingItem mapToBookingItem(Booking booking) {
        Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);

        String tourName     = resolveTourName(booking, tour);
        String destination  = resolveDestination(booking, tour);
        String tourImageUrl = resolveTourImageUrl(booking, tour);
        double rating       = (tour != null && tour.getRating() != null) ? tour.getRating() : 0.0;

        return BookedTourListResponseDTO.BookingItem.builder()
                .id(booking.getId())
                .tourId(booking.getTourId())
                .state(booking.getState() != null ? booking.getState().name() : null)
                .tourImageUrl(tourImageUrl)
                .name(tourName)
                .destination(destination)
                .rating(rating)
                .tourDetails(buildTourDetailsDTO(booking))
                .travelAgent(buildTravelAgentDTO(tour))
                .canceledBy(booking.getCanceledBy())
                .cancelReason(booking.getCancelReason())
                .rawDate(booking.getDate() != null ? booking.getDate().toString() : null)
                .rawDuration(booking.getDuration())
                .rawMealPlan(booking.getMealPlan())
                .rawAdults(booking.getAdults())
                .rawChildren(booking.getChildren())
                .freeCancellationDate(resolveFreeCancellationDate(booking, tour))
                .personalDetails(mapPersonalDetailItems(booking))
                .build();
    }

    private String resolveTourName(Booking booking, Tour tour) {
        return (tour != null) ? tour.getName() : booking.getTourName();
    }

    private String resolveDestination(Booking booking, Tour tour) {
        return (tour != null) ? tour.getDestination() : booking.getDestination();
    }

    private String resolveTourImageUrl(Booking booking, Tour tour) {
        if (tour != null && tour.getImageUrls() != null && !tour.getImageUrls().isEmpty()) {
            return tour.getImageUrls().get(0);
        }
        return booking.getTourImageUrl();
    }

    private String resolveFreeCancellationDate(Booking booking, Tour tour) {
        if (booking.getDate() == null) {
            return null;
        }
        Integer daysBefore = resolveDaysBefore(booking, tour);
        if (daysBefore == null) {
            return null;
        }
        return booking.getDate().minusDays(daysBefore).toString();
    }

    private Integer resolveDaysBefore(Booking booking, Tour tour) {
        if (tour != null && tour.getFreeCancellationDaysBefore() != null) {
            return tour.getFreeCancellationDaysBefore();
        }
        return booking.getFreeCancellationDaysBefore();
    }

    private List<BookedTourListResponseDTO.PersonalDetailItem> mapPersonalDetailItems(Booking booking) {
        if (booking.getPersonalDetails() == null) {
            return null;
        }
        return booking.getPersonalDetails().stream()
                .map(pd -> BookedTourListResponseDTO.PersonalDetailItem.builder()
                        .firstName(pd.getFirstName())
                        .lastName(pd.getLastName())
                        .build())
                .collect(Collectors.toList());
    }

    private BookedTourListResponseDTO.TourDetailsDTO buildTourDetailsDTO(Booking booking) {
        // Format date: "Jan 17, 2025 (7 days)"
        String dateStr = null;
        if (booking.getDate() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
            dateStr = booking.getDate().format(fmt) + " (" + booking.getDuration() + ")";
        }

        // Format meal plan
        String mealPlanFormatted = MealPlanFormatter.format(booking.getMealPlan());

        // Format guests: "Jhonson Doe (1 adult)" or "Jhonson Doe (2 adults, 1 child)"
        String guestsStr = formatGuests(booking);

        // Documents: "0 items"
        String documents = booking.getDocumentCount() + " item" + (booking.getDocumentCount() == 1 ? "" : "s");

        return BookedTourListResponseDTO.TourDetailsDTO.builder()
                .date(dateStr)
                .mealPlan(mealPlanFormatted)
                .guests(guestsStr)
                .totalPrice(booking.getTotalPrice())
                .documents(documents)
                .build();
    }

    private BookedTourListResponseDTO.TravelAgentDTO buildTravelAgentDTO(Tour tour) {
        if (tour == null || tour.getAssignedAgentId() == null) {
            return null;
        }
        User agent = userRepository.findById(tour.getAssignedAgentId()).orElse(null);
        if (agent == null) {
            return null;
        }
        String agentName = (agent.getFirstName() + " " + agent.getLastName()).trim();
        return BookedTourListResponseDTO.TravelAgentDTO.builder()
                .name(agentName)
                .email(agent.getEmail())
                .phone(agent.getPhone())
                .messenger(agent.getMessengerLink())
                .build();
    }

    private List<Booking.PersonalDetail> mapPersonalDetails(List<PersonalDetailDTO> dtos) {
        return dtos.stream()
                .map(pd -> {
                    Booking.PersonalDetail d = new Booking.PersonalDetail();
                    d.setFirstName(pd.getFirstName());
                    d.setLastName(pd.getLastName());
                    return d;
                })
                .collect(Collectors.toList());
    }

    private String formatGuests(Booking booking) {
        String leadName = "";
        if (booking.getPersonalDetails() != null && !booking.getPersonalDetails().isEmpty()) {
            Booking.PersonalDetail lead = booking.getPersonalDetails().get(0);
            leadName = (lead.getFirstName() + " " + lead.getLastName()).trim() + " ";
        }
        int adults = booking.getAdults();
        int children = booking.getChildren();
        StringBuilder sb = new StringBuilder(leadName);
        sb.append("(");
        sb.append(adults).append(ADULT_LABEL).append(adults != 1 ? "s" : "");
        if (children > 0) {
            sb.append(", ").append(children).append(" child").append(children != 1 ? "ren" : "");
        }
        sb.append(")");
        return sb.toString();
    }

    private String calculateTotalPrice(Tour tour, String duration, String mealPlan, int adults) {
        if (tour.getPricePerDuration() == null) {
            return PRICE_ON_REQUEST;
        }

        String basePriceStr = tour.getPricePerDuration().get(duration);
        if (basePriceStr == null) {
            return PRICE_ON_REQUEST;
        }

        try {
            long basePrice = parsePriceLong(basePriceStr);
            long supplement = 0;
            if (tour.getMealSupplementsPerDay() != null) {
                String suppStr = tour.getMealSupplementsPerDay().get(mealPlan);
                if (suppStr != null) {
                    supplement = parsePriceLong(suppStr);
                }
            }
            int days = parseDurationDays(duration);
            long totalPerPerson = basePrice + (supplement * days);
            long total = totalPerPerson * Math.max(adults, 1);
            return "$" + total;
        } catch (NumberFormatException e) {
            return basePriceStr;
        }
    }

    private long parsePriceLong(String price) {
        // Strip everything except digits
        String cleaned = price.replaceAll("[^0-9]", "");
        return Long.parseLong(cleaned);
    }

    private int parseDurationDays(String duration) {
        // e.g. "7 days" → 7, "10 days" → 10
        if (duration == null) {
            return 0;
        }
        String[] parts = duration.trim().split("\\s+");
        try {
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String buildConfirmationMessage(Tour tour, CreateBookingRequestDTO req) {
        String hotelOrDest = (tour.getHotelName() != null && !tour.getHotelName().isBlank())
                ? tour.getHotelName()
                : tour.getDestination();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
        String formattedDate = req.getDate().format(fmt);
        String mealPlanFormatted = MealPlanFormatter.format(req.getMealPlan());
        int adults = req.getGuests().getAdult();
        String adultStr = adults + ADULT_LABEL + (adults != 1 ? "s" : "");

        return "You have booked at " + hotelOrDest
                + ", starting date " + formattedDate
                + " (" + req.getDuration() + "), "
                + mealPlanFormatted
                + " for " + adultStr
                + " successfully. Please upload your travel documents to the booking on the"
                + " 'My Tours' page and wait for the Travel Agent to contact you.";
    }
}

