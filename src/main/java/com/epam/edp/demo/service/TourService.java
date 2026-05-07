package com.epam.edp.demo.service;
import com.epam.edp.demo.dto.DestinationListResponseDTO;
import com.epam.edp.demo.dto.TourListResponseDTO;
import com.epam.edp.demo.model.Tour;
import com.epam.edp.demo.repository.TourRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.bson.Document;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final MongoTemplate mongoTemplate;

    public TourService(TourRepository tourRepository, MongoTemplate mongoTemplate) {
        this.tourRepository = tourRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // ─────────────────────────────────────────────
    // US4 — Destination autocomplete
    // ─────────────────────────────────────────────
    public DestinationListResponseDTO searchDestinations(String query) {
        if (query == null || query.length() < 3) {
            throw new IllegalArgumentException(
                    "Query must be at least 3 characters"
            );
        }

        List<String> destinations = mongoTemplate.findDistinct(
                new Query(Criteria.where("destination")
                        .regex(query, "i")),
                "destination",
                Tour.class,
                String.class
        );

        return DestinationListResponseDTO.builder()
                .destinations(destinations)
                .build();
    }

    // ─────────────────────────────────────────────
    // US4 — Available tours with filters
    // ─────────────────────────────────────────────
    public TourListResponseDTO getAvailableTours(
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String duration,
            Integer adults,
            Integer children,
            String mealPlan,
            String tourType,
            String sortBy,
            int page,
            int pageSize
    ) {
        Query query = buildTourQuery(
                destination, startDate, endDate, duration,
                adults, children, mealPlan, tourType
        );

        Sort sort = applySortOrder(sortBy);

        // count total matching docs for pagination metadata
        long totalItems = mongoTemplate.count(query, Tour.class);

        // apply pagination + sort
        query.with(PageRequest.of(page - 1, pageSize, sort));

        // execute query
        List<Tour> tours = mongoTemplate.find(query, Tour.class);

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        List<TourListResponseDTO.TourItem> tourItems = tours.stream()
                .map(this::mapToTourItem)
                .collect(Collectors.toList());

        return TourListResponseDTO.builder()
                .tours(tourItems)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalItems((int) totalItems)
                .build();
    }







    // ─────────────────────────────────────────────
    // Private — query builder
    // ─────────────────────────────────────────────
    private Query buildTourQuery(
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            String duration,
            Integer adults,
            Integer children,
            String mealPlan,
            String tourType
    ) {
        Query query = new Query();

        // always applied — never show fully booked tours
        query.addCriteria(
                Criteria.where("$expr").is(
                        new Document("$lt", Arrays.asList("$bookedCount", "$totalCapacity"))
                )
        );

        if (destination != null
                && !destination.isBlank()
                && !destination.equalsIgnoreCase("Any destination")) {
            query.addCriteria(
                    Criteria.where("destination").regex(destination, "i")
            );
        }

        if (startDate != null) {
            if (endDate != null) {
                // Filter tours with start dates within the range
                Criteria dateCriteria = new Criteria().gte(startDate).lte(endDate);
                query.addCriteria(
                        Criteria.where("startDates").elemMatch(dateCriteria)
                );
            } else {
                // Single date - exact match
                query.addCriteria(
                        Criteria.where("startDates").in(startDate)
                );
            }
        }

        if (duration != null) {
            query.addCriteria(
                    Criteria.where("durations").in(duration)
            );
        }

        if (mealPlan != null) {
            query.addCriteria(
                    Criteria.where("mealPlans").in(mealPlan)
            );
        }

        if (tourType != null) {
            query.addCriteria(
                    Criteria.where("tourType").is(tourType)
            );
        }

        if (adults != null || children != null) {
            int totalGuests = (adults  != null ? adults  : 1)
                    + (children != null ? children : 0);
            query.addCriteria(
                    Criteria.where("guestQuantity.totalMaxValue").gte(totalGuests)
            );
        }

        return query;
    }

    // ─────────────────────────────────────────────
    // Private — sort helpers
    // ─────────────────────────────────────────────
    private Sort applySortOrder(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "rating");
        }
        return switch (sortBy) {
            case "RATING_ASC" -> Sort.by(Sort.Direction.ASC,  "rating");
            case "PRICE_DESC" -> Sort.by(Sort.Direction.DESC, "basePrice");
            case "PRICE_ASC"  -> Sort.by(Sort.Direction.ASC,  "basePrice");
            default           -> Sort.by(Sort.Direction.DESC, "rating");
        };
    }

    private Sort applyReviewSortOrder(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "rate");
        }
        return switch (sortBy) {
            case "RATING_ASC" -> Sort.by(Sort.Direction.ASC,  "rate");
            case "NEWEST"     -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "OLDEST"     -> Sort.by(Sort.Direction.ASC,  "createdAt");
            default           -> Sort.by(Sort.Direction.DESC, "rate");
        };
    }

<<<<<<< Updated upstream
    // ─────────────────────────────────────────────
    // Private — mappers
    // ─────────────────────────────────────────────
=======
>>>>>>> Stashed changes
    private TourListResponseDTO.TourItem mapToTourItem(Tour tour) {
        LocalDate earliestDate = tour.getStartDates().stream()
                .min(Comparator.naturalOrder())
                .orElse(null);

        String lowestPrice = tour.getPricePerDuration().values().stream()
                .min(Comparator.naturalOrder())
                .map(p -> "from " + p + " for 1 person")
                .orElse("Price on request");

        LocalDate freeCancellation = earliestDate != null
                ? earliestDate.minusDays(tour.getFreeCancellationDaysBefore())
                : null;

        List<String> formattedMealPlans = tour.getMealPlans().stream()
                .map(this::formatMealPlan)
                .collect(Collectors.toList());

        return TourListResponseDTO.TourItem.builder()
                .id(tour.getId())
                .name(tour.getName())
                .destination(tour.getDestination())
                .startDate(earliestDate)
                .durations(tour.getDurations())
                .mealPlans(formattedMealPlans)
                .price(lowestPrice)
                .rating(tour.getRating())
                .reviews(tour.getReviewCount())
                .freeCancellation(freeCancellation)
                .build();
    }





    private String formatMealPlan(String code) {
        return switch (code) {
            case "BB" -> "Breakfast (BB)";
            case "HB" -> "Half-board (HB)";
            case "FB" -> "Full-board (FB)";
            case "AI" -> "All inclusive (AI)";
            default   -> code;
        };
    }
}