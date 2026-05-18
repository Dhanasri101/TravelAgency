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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock private FeedbackRepository feedbackRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private TourRepository tourRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private FeedbackService feedbackService;

    private static final String BOOKING_ID  = "booking-1";
    private static final String TOUR_ID     = "tour-1";
    private static final String CUSTOMER_ID = "customer-1";
    private static final String FEEDBACK_ID = "feedback-1";

    @BeforeEach
    void setUp() {
        feedbackService = new FeedbackService(
                feedbackRepository, bookingRepository, tourRepository, eventPublisher);
    }

    // ─── submitFeedback ───────────────────────────────────────────────────────

    @Test
    void submitFeedback_createsAndReturnsFeedback() {
        Booking booking = startedBooking();
        Feedback saved = feedback(FeedbackStatus.PENDING);
        Feedback approved = feedback(FeedbackStatus.APPROVED);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(feedbackRepository.save(any())).thenReturn(saved);
        when(feedbackRepository.findById(FEEDBACK_ID)).thenReturn(Optional.of(approved));

        FeedbackResponse response = feedbackService.submitFeedback(
                BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(4, "Great tour!"));

        assertNotNull(response);
        assertEquals(FEEDBACK_ID, response.getId());
        verify(feedbackRepository).save(any(Feedback.class));
        verify(eventPublisher).publishEvent(any(FeedbackSubmittedEvent.class));
    }

    @Test
    void submitFeedback_throwsWhenBookingNotFound() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> feedbackService.submitFeedback(BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(4, null)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void submitFeedback_throwsWhenCustomerDoesNotOwnBooking() {
        Booking booking = startedBooking();
        booking.setUserId("other-user");
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThrows(FeedbackNotAllowedException.class,
                () -> feedbackService.submitFeedback(BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(4, null)));

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void submitFeedback_throwsWhenBookingStateIsBooked() {
        Booking booking = bookingWithState(BookingState.BOOKED);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThrows(FeedbackNotAllowedException.class,
                () -> feedbackService.submitFeedback(BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(4, null)));
    }

    @Test
    void submitFeedback_throwsWhenBookingIsCancelled() {
        Booking booking = bookingWithState(BookingState.CANCELLED);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThrows(FeedbackNotAllowedException.class,
                () -> feedbackService.submitFeedback(BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(4, null)));
    }

    @Test
    void submitFeedback_allowedWhenBookingIsFinished() {
        Booking booking = bookingWithState(BookingState.FINISHED);
        Feedback saved = feedback(FeedbackStatus.PENDING);
        Feedback approved = feedback(FeedbackStatus.APPROVED);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.existsByBookingId(BOOKING_ID)).thenReturn(false);
        when(feedbackRepository.save(any())).thenReturn(saved);
        when(feedbackRepository.findById(FEEDBACK_ID)).thenReturn(Optional.of(approved));

        FeedbackResponse response = feedbackService.submitFeedback(
                BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(5, null));

        assertNotNull(response);
    }

    @Test
    void submitFeedback_throwsWhenFeedbackAlreadyExists() {
        Booking booking = startedBooking();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.existsByBookingId(BOOKING_ID)).thenReturn(true);

        assertThrows(DuplicateFeedbackException.class,
                () -> feedbackService.submitFeedback(BOOKING_ID, CUSTOMER_ID, new FeedbackRequest(4, null)));
    }

    // ─── validateFeedbackRules ────────────────────────────────────────────────

    @Test
    void validateFeedbackRules_throwsWhenRatingIsLowAndCommentIsNull() {
        Booking booking = startedBooking();

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.validateFeedbackRules(booking, CUSTOMER_ID, 2, null));
    }

    @Test
    void validateFeedbackRules_throwsWhenRatingIsLowAndCommentIsBlank() {
        Booking booking = startedBooking();

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.validateFeedbackRules(booking, CUSTOMER_ID, 3, "   "));
    }

    @Test
    void validateFeedbackRules_passesWhenRatingIsHighAndCommentIsNull() {
        Booking booking = startedBooking();
        // Should not throw
        feedbackService.validateFeedbackRules(booking, CUSTOMER_ID, 4, null);
        feedbackService.validateFeedbackRules(booking, CUSTOMER_ID, 5, null);
    }

    @Test
    void validateFeedbackRules_passesWhenRatingIsLowAndCommentPresent() {
        Booking booking = startedBooking();
        // Should not throw
        feedbackService.validateFeedbackRules(booking, CUSTOMER_ID, 1, "Bad experience");
    }

    // ─── moderateComment ─────────────────────────────────────────────────────

    @Test
    void moderateComment_returnsApprovedForCleanComment() {
        assertEquals(FeedbackStatus.APPROVED, feedbackService.moderateComment("Wonderful tour!"));
    }

    @Test
    void moderateComment_returnsFlaggedForInappropriateComment() {
        assertEquals(FeedbackStatus.FLAGGED, feedbackService.moderateComment("This is total spam"));
    }

    @Test
    void moderateComment_returnsApprovedForNullComment() {
        assertEquals(FeedbackStatus.APPROVED, feedbackService.moderateComment(null));
    }

    @Test
    void moderateComment_returnsApprovedForBlankComment() {
        assertEquals(FeedbackStatus.APPROVED, feedbackService.moderateComment("   "));
    }

    // ─── updateFeedback ───────────────────────────────────────────────────────

    @Test
    void updateFeedback_updatesAndReturnsFeedback() {
        Booking booking = startedBooking();
        Feedback existing = feedback(FeedbackStatus.APPROVED);
        Feedback saved = feedback(FeedbackStatus.PENDING);
        Feedback approved = feedback(FeedbackStatus.APPROVED);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(existing));
        when(feedbackRepository.save(any())).thenReturn(saved);
        when(feedbackRepository.findById(FEEDBACK_ID)).thenReturn(Optional.of(approved));

        FeedbackResponse response = feedbackService.updateFeedback(
                BOOKING_ID, CUSTOMER_ID, new FeedbackUpdateRequest(5, "Even better!"));

        assertNotNull(response);
        verify(eventPublisher).publishEvent(any(FeedbackUpdatedEvent.class));
    }

    @Test
    void updateFeedback_throwsWhenFeedbackNotFound() {
        Booking booking = startedBooking();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());

        assertThrows(FeedbackNotFoundException.class,
                () -> feedbackService.updateFeedback(
                        BOOKING_ID, CUSTOMER_ID, new FeedbackUpdateRequest(5, null)));
    }

    @Test
    void updateFeedback_throwsWhenNotFeedbackOwner() {
        Booking booking = startedBooking();
        Feedback existing = feedback(FeedbackStatus.APPROVED);
        existing.setCustomerId("other-customer");

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(existing));

        assertThrows(FeedbackNotAllowedException.class,
                () -> feedbackService.updateFeedback(
                        BOOKING_ID, CUSTOMER_ID, new FeedbackUpdateRequest(5, null)));
    }

    // ─── getFeedbacksByTour ───────────────────────────────────────────────────

    @Test
    void getFeedbacksByTour_returnsOnlyApprovedFeedbacks() {
        Tour tour = new Tour();
        tour.setId(TOUR_ID);
        Feedback approved = feedback(FeedbackStatus.APPROVED);

        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(tour));
        when(feedbackRepository.findByTourIdAndStatus(TOUR_ID, FeedbackStatus.APPROVED))
                .thenReturn(List.of(approved));

        List<FeedbackResponse> responses = feedbackService.getFeedbacksByTour(TOUR_ID);

        assertEquals(1, responses.size());
        assertEquals("APPROVED", responses.get(0).getStatus());
    }

    @Test
    void getFeedbacksByTour_throwsWhenTourNotFound() {
        when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> feedbackService.getFeedbacksByTour(TOUR_ID));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ─── getFeedbackByBooking ─────────────────────────────────────────────────

    @Test
    void getFeedbackByBooking_returnsFeedbackForOwner() {
        Booking booking = startedBooking();
        Feedback existing = feedback(FeedbackStatus.APPROVED);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(feedbackRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(existing));

        FeedbackResponse response = feedbackService.getFeedbackByBooking(BOOKING_ID, CUSTOMER_ID);

        assertNotNull(response);
        assertEquals(FEEDBACK_ID, response.getId());
    }

    @Test
    void getFeedbackByBooking_throwsWhenNotOwner() {
        Booking booking = startedBooking();
        booking.setUserId("someone-else");
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThrows(FeedbackNotAllowedException.class,
                () -> feedbackService.getFeedbackByBooking(BOOKING_ID, CUSTOMER_ID));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Booking startedBooking() {
        return bookingWithState(BookingState.STARTED);
    }

    private Booking bookingWithState(BookingState state) {
        Booking b = new Booking();
        b.setId(BOOKING_ID);
        b.setUserId(CUSTOMER_ID);
        b.setTourId(TOUR_ID);
        b.setState(state);
        return b;
    }

    private Feedback feedback(FeedbackStatus status) {
        Feedback f = new Feedback();
        f.setId(FEEDBACK_ID);
        f.setBookingId(BOOKING_ID);
        f.setTourId(TOUR_ID);
        f.setCustomerId(CUSTOMER_ID);
        f.setRating(4);
        f.setComment("Good experience");
        f.setStatus(status);
        return f;
    }
}
