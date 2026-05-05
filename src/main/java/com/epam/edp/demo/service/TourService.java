package com.epam.edp.demo.service;
import com.epam.edp.demo.dto.DestinationListResponseDTO;
import com.epam.edp.demo.dto.ReviewListResponseDTO;
import com.epam.edp.demo.dto.TourDetailResponseDTO;
import com.epam.edp.demo.dto.TourListResponseDTO;
import com.epam.edp.demo.model.Review;
import com.epam.edp.demo.model.Tour;
import com.epam.edp.demo.repository.ReviewRepository;
import com.epam.edp.demo.repository.TourRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.bson.Document;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    public TourService(TourRepository tourRepository,
                       ReviewRepository reviewRepository,
                       MongoTemplate mongoTemplate) {
        this.tourRepository = tourRepository;
        this.reviewRepository = reviewRepository;
        this.mongoTemplate = mongoTemplate;
    }

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

    public TourListResponseDTO getAvailableTours(
            String destination,
            LocalDate startDate,
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
                destination, startDate, duration,
                adults, children, mealPlan, tourType
        );

        Sort sort = applySortOrder(sortBy);
        long totalItems = mongoTemplate.count(query, Tour.class);
        query.with(PageRequest.of(page - 1, pageSize, sort));
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

    private Query buildTourQuery(
            String destination,
            LocalDate startDate,
            String duration,
            Integer adults,
            Integer children,
            String mealPlan,
            String tourType
    ) {
        Query query = new Query();

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
            query.addCriteria(
                    Criteria.where("startDates").in(startDate)
            );
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

    public TourDetailResponseDTO getTourById(String id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tour not found: " + id));

        LocalDate earliest = tour.getStartDates() == null ? null :
                tour.getStartDates().stream()
                        .min(Comparator.naturalOrder())
                        .orElse(null);

        LocalDate freeCancellationDeadline = (earliest != null
                && tour.getFreeCancellationDaysBefore() != null)
                ? earliest.minusDays(tour.getFreeCancellationDaysBefore())
                : null;

        List<String> formattedMealPlans = tour.getMealPlans() == null ? List.of() :
                tour.getMealPlans().stream()
                        .map(this::formatMealPlan)
                        .collect(Collectors.toList());

        TourDetailResponseDTO.GuestQuantityDTO guestQuantityDTO = null;
        if (tour.getGuestQuantity() != null) {
            guestQuantityDTO = new TourDetailResponseDTO.GuestQuantityDTO(
                    tour.getGuestQuantity().getAdultsMaxValue(),
                    tour.getGuestQuantity().getChildrenMaxValue(),
                    tour.getGuestQuantity().getTotalMaxValue()
            );
        }

        return TourDetailResponseDTO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .destination(tour.getDestination())
                .summary(tour.getSummary())
                .imageUrls(tour.getImageUrls())
                .rating(tour.getRating())
                .reviewCount(tour.getReviewCount())
                .startDates(tour.getStartDates())
                .durations(tour.getDurations())
                .pricePerDuration(tour.getPricePerDuration())
                .mealPlans(formattedMealPlans)
                .mealSupplementsPerDay(tour.getMealSupplementsPerDay())
                .hotelName(tour.getHotelName())
                .hotelDescription(tour.getHotelDescription())
                .accommodation(tour.getAccommodation())
                .tourType(tour.getTourType())
                .customDetails(tour.getCustomDetails())
                .guestQuantity(guestQuantityDTO)
                .freeCancellationDaysBefore(tour.getFreeCancellationDaysBefore())
                .freeCancellationDeadline(freeCancellationDeadline)
                .build();
    }

    public ReviewListResponseDTO getReviews(String tourId, String sortBy, int page, int pageSize) {
        if (!tourRepository.existsById(tourId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + tourId);
        }

        Sort sort = switch (sortBy == null ? "TOP_RATED_FIRST" : sortBy) {
            case "LOW_RATED_FIRST" -> Sort.by(Sort.Direction.ASC,  "rate");
            case "NEWEST_FIRST"   -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "OLDEST_FIRST"   -> Sort.by(Sort.Direction.ASC,  "createdAt");
            default               -> Sort.by(Sort.Direction.DESC, "rate"); // TOP_RATED_FIRST
        };

        Query countQuery = new Query(Criteria.where("tourId").is(tourId));
        long totalItems = mongoTemplate.count(countQuery, Review.class);

        Query pagedQuery = new Query(Criteria.where("tourId").is(tourId))
                .with(PageRequest.of(page - 1, pageSize, sort));
        List<Review> reviews = mongoTemplate.find(pagedQuery, Review.class);

        List<Review> allReviews = reviewRepository.findByTourId(tourId);
        OptionalDouble avg = allReviews.stream()
                .filter(r -> r.getRate() != null)
                .mapToDouble(Review::getRate)
                .average();
        Double averageRating = avg.isPresent()
                ? Math.round(avg.getAsDouble() * 100.0) / 100.0
                : null;

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        List<ReviewListResponseDTO.ReviewItem> items = reviews.stream()
                .map(r -> ReviewListResponseDTO.ReviewItem.builder()
                        .id(r.getId())
                        .userName(r.getUserName())
                        .userAvatarUrl(r.getUserAvatarUrl())
                        .rate(r.getRate())
                        .comment(r.getComment())
                        .reviewDate(r.getReviewDate())
                        .build())
                .collect(Collectors.toList());

        return ReviewListResponseDTO.builder()
                .reviews(items)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalItems((int) totalItems)
                .averageRating(averageRating)
                .build();
    }
}