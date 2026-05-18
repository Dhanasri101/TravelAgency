package com.epam.edp.demo.service;

import com.epam.edp.demo.dto.FeedbackRequest;
import com.epam.edp.demo.dto.FeedbackResponse;
import com.epam.edp.demo.dto.FeedbackUpdateRequest;
import com.epam.edp.demo.enums.BookingState;
import com.epam.edp.demo.enums.FeedbackStatus;
import com.epam.edp.demo.event.FeedbackSubmittedEvent;
import com.epam.edp.demo.event.FeedbackUpdatedEvent;
import com.epam.edp.demo.exception.DuplicateFeedbackException;
import com.epam.edp.demo.exception.FeedbackNotAllowedException;
import com.epam.edp.demo.exception.FeedbackNotFoundException;
import com.epam.edp.demo.model.Booking;
import com.epam.edp.demo.model.Feedback;
import com.epam.edp.demo.model.Tour;
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

    // Basic keyword list for comment moderation
    private static final Set<String> INAPPROPRIATE_KEYWORDS = Set.of(
            "spam", "scam", "fraud", "fake", "cheat", "scammer",
            "idiot", "stupid", "moron", "dumb", "loser",
            "hate", "terrible", "worst", "disgusting", "horrible",
            "shit", "crap", "damn", "hell", "ass"
    );

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
    // Submit new feedback for a booking
    // ─────────────────────────────────────────────
    public FeedbackResponse submitFeedback(String bookingId, String customerId, FeedbackRequest request) {
        Booking booking = loadBooking(bookingId);

        validateFeedbackRules(booking, customerId, request.getRating(), request.getComment());

        if (feedbackRepository.existsByBookingId(bookingId)) {
            throw new DuplicateFeedbackException("Feedback already exists for booking: " + bookingId);
        }

        Feedback feedback = new Feedback();
        feedback.setBookingId(bookingId);
        feedback.setTourId(booking.getTourId());
        feedback.setCustomerId(customerId);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setStatus(FeedbackStatus.PENDING);

        Feedback saved = feedbackRepository.save(feedback);

        // Publish event — synchronous listener will apply moderation and update status
        eventPublisher.publishEvent(
                new FeedbackSubmittedEvent(saved.getId(), bookingId, booking.getTourId(), customerId));

        // Reload to reflect status set by the event listener
        return toResponse(feedbackRepository.findById(saved.getId()).orElse(saved));
    }

    // ─────────────────────────────────────────────
    // Update existing feedback for a booking
    // ─────────────────────────────────────────────
    public FeedbackResponse updateFeedback(String bookingId, String customerId, FeedbackUpdateRequest request) {
        Booking booking = loadBooking(bookingId);

        validateFeedbackRules(booking, customerId, request.getRating(), request.getComment());

        Feedback feedback = feedbackRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new FeedbackNotFoundException("No feedback found for booking: " + bookingId));

        // Only the customer who submitted it can update it
        if (!feedback.getCustomerId().equals(customerId)) {
            throw new FeedbackNotAllowedException("You are not authorized to update this feedback");
        }

        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setStatus(FeedbackStatus.PENDING);

        Feedback saved = feedbackRepository.save(feedback);

        // Publish event — synchronous listener will re-apply moderation
        eventPublisher.publishEvent(
                new FeedbackUpdatedEvent(saved.getId(), bookingId, booking.getTourId(), customerId));

        return toResponse(feedbackRepository.findById(saved.getId()).orElse(saved));
    }

    // ─────────────────────────────────────────────
    // Get feedback for a specific booking (owner only)
    // ─────────────────────────────────────────────
    public FeedbackResponse getFeedbackByBooking(String bookingId, String customerId) {
        Booking booking = loadBooking(bookingId);

        if (!booking.getUserId().equals(customerId)) {
            throw new FeedbackNotAllowedException("You are not authorized to view this booking's feedback");
        }

        Feedback feedback = feedbackRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new FeedbackNotFoundException("No feedback found for booking: " + bookingId));

        return toResponse(feedback);
    }

    // ─────────────────────────────────────────────
    // Get all approved feedbacks for a tour (public)
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
    // Validate all business rules before save
    // ─────────────────────────────────────────────
    public void validateFeedbackRules(Booking booking, String customerId, Integer rating, String comment) {
        // Only the booking owner can submit/update feedback
        if (!booking.getUserId().equals(customerId)) {
            throw new FeedbackNotAllowedException("You can only submit feedback for your own booking");
        }

        // Feedback is allowed only when booking is STARTED or FINISHED
        BookingState state = booking.getState();
        if (state == null || (state != BookingState.STARTED && state != BookingState.FINISHED)) {
            throw new FeedbackNotAllowedException(
                    "Feedback is only allowed when booking status is Started or Finished");
        }

        // Comment is mandatory when rating is 1, 2, or 3
        if (rating != null && rating <= 3 && (comment == null || comment.isBlank())) {
            throw new IllegalArgumentException(
                    "A comment is required when rating is 3 or below");
        }
    }

    // ─────────────────────────────────────────────
    // Moderate comment — returns APPROVED or FLAGGED
    // ─────────────────────────────────────────────
    public FeedbackStatus moderateComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return FeedbackStatus.APPROVED;
        }
        String lower = comment.toLowerCase();
        boolean flagged = INAPPROPRIATE_KEYWORDS.stream()
                .anyMatch(lower::contains);
        return flagged ? FeedbackStatus.FLAGGED : FeedbackStatus.APPROVED;
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────
    private Booking loadBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking not found: " + bookingId));
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .status(feedback.getStatus() != null ? feedback.getStatus().name() : null)
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .customerId(feedback.getCustomerId())
                .bookingId(feedback.getBookingId())
                .tourId(feedback.getTourId())
                .build();
    }
}
