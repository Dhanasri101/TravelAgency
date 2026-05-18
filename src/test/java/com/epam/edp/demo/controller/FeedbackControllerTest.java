package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.FeedbackRequest;
import com.epam.edp.demo.dto.FeedbackResponse;
import com.epam.edp.demo.dto.FeedbackUpdateRequest;
import com.epam.edp.demo.exception.DuplicateFeedbackException;
import com.epam.edp.demo.exception.FeedbackNotAllowedException;
import com.epam.edp.demo.exception.FeedbackNotFoundException;
import com.epam.edp.demo.service.FeedbackService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService;

    private FeedbackController feedbackController;

    private static final String BOOKING_ID  = "booking-1";
    private static final String TOUR_ID     = "tour-1";
    private static final String CUSTOMER_ID = "customer-1";

    @BeforeEach
    void setUp() {
        feedbackController = new FeedbackController(feedbackService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── submitFeedback ───────────────────────────────────────────────────────

    @Test
    void submitFeedback_returns201WithBody() {
        authenticate(CUSTOMER_ID);
        FeedbackRequest req = new FeedbackRequest(4, "Nice tour");
        FeedbackResponse expected = response();

        when(feedbackService.submitFeedback(BOOKING_ID, CUSTOMER_ID, req)).thenReturn(expected);

        ResponseEntity<FeedbackResponse> result = feedbackController.submitFeedback(BOOKING_ID, req);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expected, result.getBody());
    }

    @Test
    void submitFeedback_passesCorrectArgumentsToService() {
        authenticate(CUSTOMER_ID);
        FeedbackRequest req = new FeedbackRequest(5, "Amazing!");
        when(feedbackService.submitFeedback(eq(BOOKING_ID), eq(CUSTOMER_ID), any()))
                .thenReturn(response());

        feedbackController.submitFeedback(BOOKING_ID, req);

        verify(feedbackService).submitFeedback(BOOKING_ID, CUSTOMER_ID, req);
    }

    @Test
    void submitFeedback_throws401WhenNotAuthenticated() {
        FeedbackRequest req = new FeedbackRequest(4, "Nice");

        assertThrows(ResponseStatusException.class,
                () -> feedbackController.submitFeedback(BOOKING_ID, req));
    }

    @Test
    void submitFeedback_propagatesDuplicateFeedbackException() {
        authenticate(CUSTOMER_ID);
        FeedbackRequest req = new FeedbackRequest(4, "Nice");
        when(feedbackService.submitFeedback(any(), any(), any()))
                .thenThrow(new DuplicateFeedbackException("Already exists"));

        assertThrows(DuplicateFeedbackException.class,
                () -> feedbackController.submitFeedback(BOOKING_ID, req));
    }

    // ─── updateFeedback ───────────────────────────────────────────────────────

    @Test
    void updateFeedback_returns200WithBody() {
        authenticate(CUSTOMER_ID);
        FeedbackUpdateRequest req = new FeedbackUpdateRequest(5, "Even better");
        FeedbackResponse expected = response();

        when(feedbackService.updateFeedback(BOOKING_ID, CUSTOMER_ID, req)).thenReturn(expected);

        ResponseEntity<FeedbackResponse> result = feedbackController.updateFeedback(BOOKING_ID, req);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
    }

    @Test
    void updateFeedback_propagatesFeedbackNotFoundException() {
        authenticate(CUSTOMER_ID);
        FeedbackUpdateRequest req = new FeedbackUpdateRequest(5, null);
        when(feedbackService.updateFeedback(any(), any(), any()))
                .thenThrow(new FeedbackNotFoundException("Not found"));

        assertThrows(FeedbackNotFoundException.class,
                () -> feedbackController.updateFeedback(BOOKING_ID, req));
    }

    // ─── getFeedbackByBooking ─────────────────────────────────────────────────

    @Test
    void getFeedbackByBooking_returns200WithBody() {
        authenticate(CUSTOMER_ID);
        FeedbackResponse expected = response();
        when(feedbackService.getFeedbackByBooking(BOOKING_ID, CUSTOMER_ID)).thenReturn(expected);

        ResponseEntity<FeedbackResponse> result = feedbackController.getFeedbackByBooking(BOOKING_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expected, result.getBody());
    }

    @Test
    void getFeedbackByBooking_propagatesFeedbackNotAllowedException() {
        authenticate(CUSTOMER_ID);
        when(feedbackService.getFeedbackByBooking(any(), any()))
                .thenThrow(new FeedbackNotAllowedException("Not allowed"));

        assertThrows(FeedbackNotAllowedException.class,
                () -> feedbackController.getFeedbackByBooking(BOOKING_ID));
    }

    // ─── getFeedbacksByTour ───────────────────────────────────────────────────

    @Test
    void getFeedbacksByTour_returns200WithList() {
        List<FeedbackResponse> expected = List.of(response());
        when(feedbackService.getFeedbacksByTour(TOUR_ID)).thenReturn(expected);

        ResponseEntity<List<FeedbackResponse>> result = feedbackController.getFeedbacksByTour(TOUR_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getFeedbacksByTour_returnsEmptyListWhenNoFeedbacks() {
        when(feedbackService.getFeedbacksByTour(TOUR_ID)).thenReturn(List.of());

        ResponseEntity<List<FeedbackResponse>> result = feedbackController.getFeedbacksByTour(TOUR_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().size());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private void authenticate(String userId) {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(userId, null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private FeedbackResponse response() {
        return FeedbackResponse.builder()
                .id("feedback-1")
                .rating(4)
                .comment("Good experience")
                .status("APPROVED")
                .customerId(CUSTOMER_ID)
                .bookingId(BOOKING_ID)
                .tourId(TOUR_ID)
                .build();
    }
}
