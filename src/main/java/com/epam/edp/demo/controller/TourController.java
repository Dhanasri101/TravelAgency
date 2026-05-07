package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.DestinationListResponseDTO;
import com.epam.edp.demo.dto.TourListResponseDTO;
import com.epam.edp.demo.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    // ─────────────────────────────────────────────
    // US4 — Destination autocomplete
    // GET /tours/destinations?destination=Pun
    // ─────────────────────────────────────────────
    @GetMapping("/destinations")
    public ResponseEntity<DestinationListResponseDTO> getDestinations(
            @RequestParam String destination
    ) {
        DestinationListResponseDTO response =
                tourService.searchDestinations(destination);

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // US4 — Available tours with filters
    // GET /tours/available?destination=...&tourType=...
    // ─────────────────────────────────────────────
    @GetMapping("/available")
    public ResponseEntity<TourListResponseDTO> getAvailableTours(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) Integer adults,
            @RequestParam(required = false) Integer children,
            @RequestParam(required = false) String mealPlan,
            @RequestParam(required = false) String tourType,
            @RequestParam(defaultValue = "RATING_DESC") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize
    ) {
        TourListResponseDTO response = tourService.getAvailableTours(
                destination,
                startDate,
                endDate,
                duration,
                adults,
                children,
                mealPlan,
                tourType,
                sortBy,
                page,
                pageSize
        );

        return ResponseEntity.ok(response);
    }
}