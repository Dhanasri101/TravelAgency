package com.epam.edp.demo.service;

import com.epam.edp.demo.dto.FeedbackRequest;
import com.epam.edp.demo.dto.FeedbackResponse;
import com.epam.edp.demo.dto.FeedbackSubmittedEvent;
import com.epam.edp.demo.dto.FeedbackUpdateRequest;
import com.epam.edp.demo.dto.FeedbackUpdatedEvent;
import com.epam.edp.demo.enums.BookingState;
import com.epam.edp.demo.enums.FeedbackStatus;
import com.epam.edp.demo.exception.FeedbackNotAllowedException;
import com.epam.edp.demo.exception.FeedbackNotFoundException;
import com.epam.edp.demo.model.Booking;
import com.epam.edp.demo.model.Feedback;
import com.epam.edp.demo.repository.BookingRepository;
import com.epam.edp.demo.repository.FeedbackRepository;
import com.epam.edp.demo.repository.TourRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private static final Set<BookingState> FEEDBACK_ALLOWED_STATES =
            Set.of(BookingState.STARTED, BookingState.FINISHED);

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           BookingRepository bookingRepository,
                           TourRepository tourRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─────────────────────────────────────────────
    // Submit feedback (one per booking)
    // ─────────────────────────────────────────────
    public FeedbackResponse submitFeedback(String bookingId, String customerId, FeedbackRequest request) {
        Booking booking = resolveBooking(bookingId);
        validateOwnership(booking, customerId);
        validateBookingStateAllowsFeedback(booking);
        validateFeedbackRules(request.getRating(), request.getComment());

        if (feedbackRepository.existsByBookingId(bookingId)) {
            throw new FeedbackNotAllowedException(
                    "Feedback already submitted for booking " + bookingId + ". Use PUT to update.");
        }

        Feedback feedback = new Feedback();
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setCustomerId(customerId);
        feedback.setBookingId(bookingId);
        feedback.setTourId(booking.getTourId());

        feedback = feedbackRepository.save(feedback);

        eventPublisher.publishEvent(new FeedbackSubmittedEvent(
                feedback.getId(), bookingId, booking.getTourId(),
                customerId, feedback.getRating(), feedback.getComment()));

        return toResponse(feedback);
    }

    // ─────────────────────────────────────────────
    // Update feedback
    // ─────────────────────────────────────────────
    public FeedbackResponse updateFeedback(String bookingId, String customerId, FeedbackUpdateRequest request) {
        Booking booking = resolveBooking(bookingId);
        validateOwnership(booking, customerId);
        validateBookingStateAllowsFeedback(booking);
        validateFeedbackRules(request.getRating(), request.getComment());

        Feedback feedback = feedbackRepository.findByBookingIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new FeedbackNotFoundException(
                        "No feedback found for booking " + bookingId));

        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        // Reset to PENDING so moderation runs again on updated content
        feedback.setStatus(FeedbackStatus.PENDING);

        feedback = feedbackRepository.save(feedback);

        eventPublisher.publishEvent(new FeedbackUpdatedEvent(
                feedback.getId(), bookingId, booking.getTourId(),
                customerId, feedback.getRating(), feedback.getComment()));

        return toResponse(feedback);
    }

    // ─────────────────────────────────────────────
    // Get feedback for a specific booking
    // ─────────────────────────────────────────────
    public FeedbackResponse getFeedbackByBooking(String bookingId, String customerId) {
        Booking booking = resolveBooking(bookingId);
        validateOwnership(booking, customerId);

        return feedbackRepository.findByBookingId(bookingId)
                .map(this::toResponse)
                .orElseThrow(() -> new FeedbackNotFoundException(
                        "No feedback found for booking " + bookingId));
    }

    // ─────────────────────────────────────────────
    // Get approved feedbacks for a tour (public)
    // ─────────────────────────────────────────────
    public List<FeedbackResponse> getFeedbacksByTour(String tourId) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tour not found: " + tourId));

        return feedbackRepository.findByTourIdAndStatus(tourId, FeedbackStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // Validation helpers
    // ─────────────────────────────────────────────

    /**
     * Validates rating range and mandatory comment rule.
     * Rating must be 1–5; comment is required when rating <= 3.
     */
    public void validateFeedbackRules(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (rating <= 3 && (comment == null || comment.isBlank())) {
            throw new FeedbackNotAllowedException(
                    "A comment is required when the rating is 3 or below");
        }
    }

    /**
     * Basic keyword-based moderation. Returns APPROVED if clean, FLAGGED otherwise.
     * The list can be externalised to configuration when requirements grow.
     */
    public FeedbackStatus moderateComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return FeedbackStatus.APPROVED;
        }
        String lower = comment.toLowerCase();
        List<String> bannedKeywords = List.of(
                "spam", "scam", "fraud", "fake", "hate", "racist", "sexist",
                "offensive", "abuse", "violent", "idiot", "stupid", "moron",
                "damn", "crap", "shit", "fuck", "bitch", "ass");
        boolean flagged = bannedKeywords.stream().anyMatch(lower::contains);
        return flagged ? FeedbackStatus.FLAGGED : FeedbackStatus.APPROVED;
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    private Booking resolveBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found: " + bookingId));
    }

    private void validateOwnership(Booking booking, String customerId) {
        if (!booking.getUserId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only manage feedback for your own bookings");
        }
    }

    private void validateBookingStateAllowsFeedback(Booking booking) {
        if (!FEEDBACK_ALLOWED_STATES.contains(booking.getState())) {
            throw new FeedbackNotAllowedException(
                    "Feedback is only allowed when the booking is in STARTED or FINISHED state. "
                            + "Current state: " + booking.getState());
        }
    }

    private FeedbackResponse toResponse(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .rating(f.getRating())
                .comment(f.getComment())
                .status(f.getStatus() != null ? f.getStatus().name() : null)
                .customerId(f.getCustomerId())
                .bookingId(f.getBookingId())
                .tourId(f.getTourId())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
