package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.BookedTourListResponseDTO;
import com.epam.edp.demo.dto.CreateBookingRequestDTO;
import com.epam.edp.demo.dto.CreateBookingResponseDTO;
import com.epam.edp.demo.dto.UpdateBookingRequestDTO;
import com.epam.edp.demo.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * POST /api/v1/bookings
     * Create a new booking — authenticated users only.
     */
    @PostMapping
    public ResponseEntity<CreateBookingResponseDTO> createBooking(
            @Valid @RequestBody CreateBookingRequestDTO request
    ) {
        String userId = getAuthenticatedUserId();
        CreateBookingResponseDTO response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/bookings?userId=...
     * Retrieve all bookings for a specific user — authenticated users only.
     */
    @GetMapping
    public ResponseEntity<BookedTourListResponseDTO> getBookings(
            @RequestParam String userId
    ) {
        if (userId == null || userId.isBlank() || !OBJECT_ID_PATTERN.matcher(userId.trim()).matches()) {
            throw new IllegalArgumentException("Invalid userId format");
        }
        String authenticatedUserId = getAuthenticatedUserId();
        BookedTourListResponseDTO response = bookingService.getBookingsForUser(userId, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/bookings/{id}/cancel
     * Cancel a booking — authenticated users only.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable String id,
            @RequestParam(required = false) String cancelReason
    ) {
        String authenticatedUserId = getAuthenticatedUserId();
        LocalDate freeCancelDeadline = bookingService.cancelBooking(id, authenticatedUserId, cancelReason);

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("message", "Booking cancelled successfully");
        if (freeCancelDeadline != null) {
            body.put("freeCancelation", freeCancelDeadline.toString());
        }
        return ResponseEntity.ok(body);
    }

    /**
     * PUT /api/v1/bookings/{id}
     * Update a booking — authenticated users only.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBooking(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingRequestDTO request
    ) {
        String authenticatedUserId = getAuthenticatedUserId();
        Map<String, Object> response = bookingService.updateBooking(id, authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // Helper: extract userId from JWT context
    // ─────────────────────────────────────────────
    private String getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return auth.getName();
    }
}

