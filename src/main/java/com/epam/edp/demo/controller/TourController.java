package com.epam.edp.demo.controller;

import com.epam.edp.demo.dto.DestinationListResponseDTO;
import com.epam.edp.demo.dto.ReviewListResponseDTO;
import com.epam.edp.demo.dto.TourDetailResponseDTO;
import com.epam.edp.demo.dto.TourListResponseDTO;
import com.epam.edp.demo.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/destinations")
    public ResponseEntity<DestinationListResponseDTO> getDestinations(
            @RequestParam String destination
    ) {
        DestinationListResponseDTO response =
                tourService.searchDestinations(destination);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<TourListResponseDTO> getAvailableTours(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDate startDate,
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

    @GetMapping("/{id}")
    public ResponseEntity<TourDetailResponseDTO> getTourById(@PathVariable String id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ReviewListResponseDTO> getReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "TOP_RATED_FIRST") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int pageSize
    ) {
        return ResponseEntity.ok(tourService.getReviews(id, sortBy, page, pageSize));
    }
}