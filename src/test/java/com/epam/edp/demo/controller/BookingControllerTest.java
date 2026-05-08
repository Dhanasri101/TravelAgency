package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.BookedTourListResponseDTO;
import com.epam.edp.demo.dto.CreateBookingRequestDTO;
import com.epam.edp.demo.dto.CreateBookingResponseDTO;
import com.epam.edp.demo.service.BookingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(bookingService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBooking_returnsCreatedWithBody() {
        authenticate("user-1", true);
        CreateBookingRequestDTO req = request();
        CreateBookingResponseDTO expected = CreateBookingResponseDTO.builder()
                .freeCancelation(LocalDate.of(2026, 6, 1))
                .details("ok")
                .build();
        when(bookingService.createBooking(req, "user-1")).thenReturn(expected);

        ResponseEntity<CreateBookingResponseDTO> response = bookingController.createBooking(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void createBooking_passesAuthenticatedUserIdToService() {
        authenticate("auth-user", true);
        CreateBookingRequestDTO req = request();
        when(bookingService.createBooking(any(), eq("auth-user")))
                .thenReturn(CreateBookingResponseDTO.builder().details("ok").build());

        bookingController.createBooking(req);

        verify(bookingService).createBooking(req, "auth-user");
    }

    @Test
    void createBooking_propagatesServiceExceptions() {
        authenticate("user-1", true);
        CreateBookingRequestDTO req = request();
        when(bookingService.createBooking(req, "user-1"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "full"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.createBooking(req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createBooking_returnsCreatedEvenWhenServiceResponseContainsNulls() {
        authenticate("user-1", true);
        CreateBookingRequestDTO req = request();
        when(bookingService.createBooking(req, "user-1"))
                .thenReturn(CreateBookingResponseDTO.builder().build());

        ResponseEntity<CreateBookingResponseDTO> response = bookingController.createBooking(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDetails());
        assertNull(response.getBody().getFreeCancelation());
    }

    @Test
    void getBookings_returnsOkWithBody() {
        authenticate("user-1", true);
        BookedTourListResponseDTO expected = BookedTourListResponseDTO.builder()
                .bookings(List.of())
                .build();
        when(bookingService.getBookingsForUser("user-1", "user-1")).thenReturn(expected);

        ResponseEntity<BookedTourListResponseDTO> response = bookingController.getBookings("user-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getBookings_passesRequestAndAuthenticatedUserIds() {
        authenticate("auth-user", true);
        when(bookingService.getBookingsForUser("query-user", "auth-user"))
                .thenReturn(BookedTourListResponseDTO.builder().bookings(List.of()).build());

        bookingController.getBookings("query-user");

        verify(bookingService).getBookingsForUser("query-user", "auth-user");
    }

    @Test
    void getBookings_propagatesServiceExceptions() {
        authenticate("user-1", true);
        when(bookingService.getBookingsForUser("user-2", "user-1"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "no"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.getBookings("user-2"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void cancelBooking_returnsOkAndIncludesDeadlineWhenPresent() {
        authenticate("user-1", true);
        when(bookingService.cancelBooking("b-1", "user-1", "reason"))
                .thenReturn(LocalDate.of(2026, 6, 1));

        ResponseEntity<Map<String, Object>> response = bookingController.cancelBooking("b-1", "reason");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking cancelled successfully", response.getBody().get("message"));
        assertEquals("2026-06-01", response.getBody().get("freeCancelation"));
    }

    @Test
    void cancelBooking_returnsOkWithoutDeadlineWhenNull() {
        authenticate("user-1", true);
        when(bookingService.cancelBooking("b-1", "user-1", "reason")).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = bookingController.cancelBooking("b-1", "reason");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().containsKey("freeCancelation"));
    }

    @Test
    void cancelBooking_passesNullCancelReason() {
        authenticate("user-1", true);
        when(bookingService.cancelBooking("b-1", "user-1", null)).thenReturn(null);

        bookingController.cancelBooking("b-1", null);

        verify(bookingService).cancelBooking("b-1", "user-1", null);
    }

    @Test
    void cancelBooking_passesProvidedCancelReason() {
        authenticate("user-1", true);
        when(bookingService.cancelBooking("b-1", "user-1", "changed plans")).thenReturn(null);

        bookingController.cancelBooking("b-1", "changed plans");

        verify(bookingService).cancelBooking("b-1", "user-1", "changed plans");
    }

    @Test
    void cancelBooking_propagatesServiceExceptions() {
        authenticate("user-1", true);
        when(bookingService.cancelBooking("b-1", "user-1", "reason"))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "done"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.cancelBooking("b-1", "reason"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createBooking_unauthorizedWhenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.createBooking(request()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void createBooking_unauthorizedWhenAuthenticationNotAuthenticated() {
        authenticate("user-1", false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.createBooking(request()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void createBooking_unauthorizedWhenPrincipalIsNull() {
        setAuthentication(new TestingAuthenticationToken(null, null, "ROLE_USER"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.createBooking(request()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getBookings_unauthorizedWhenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.getBookings("user-1"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getBookings_unauthorizedWhenAuthenticationNotAuthenticated() {
        authenticate("user-1", false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.getBookings("user-1"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getBookings_unauthorizedWhenPrincipalIsNull() {
        setAuthentication(new TestingAuthenticationToken(null, null, "ROLE_USER"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.getBookings("user-1"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void cancelBooking_unauthorizedWhenAuthenticationMissing() {
        SecurityContextHolder.clearContext();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.cancelBooking("b-1", "reason"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void cancelBooking_unauthorizedWhenAuthenticationNotAuthenticated() {
        authenticate("user-1", false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.cancelBooking("b-1", "reason"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void cancelBooking_unauthorizedWhenPrincipalIsNull() {
        setAuthentication(new TestingAuthenticationToken(null, null, "ROLE_USER"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.cancelBooking("b-1", "reason"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void createBooking_unauthorizedContainsExpectedReasonText() {
        SecurityContextHolder.clearContext();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> bookingController.createBooking(request()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Authentication required", ex.getReason());
    }

    private static void authenticate(String userId, boolean authenticated) {
        TestingAuthenticationToken token = new TestingAuthenticationToken(userId, null, "ROLE_USER");
        token.setAuthenticated(authenticated);
        setAuthentication(token);
    }

    private static void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static CreateBookingRequestDTO request() {
        CreateBookingRequestDTO req = new CreateBookingRequestDTO();
        req.setUserId("user-1");
        req.setTourId("tour-1");
        req.setDate(LocalDate.of(2026, 6, 10));
        req.setDuration("7 days");
        req.setMealPlan("BB");

        CreateBookingRequestDTO.GuestsDTO guests = new CreateBookingRequestDTO.GuestsDTO();
        guests.setAdult(1);
        guests.setChildren(0);
        req.setGuests(guests);

        CreateBookingRequestDTO.PersonalDetailDTO detail = new CreateBookingRequestDTO.PersonalDetailDTO();
        detail.setFirstName("John");
        detail.setLastName("Doe");
        req.setPersonalDetails(List.of(detail));

        return req;
    }
}
