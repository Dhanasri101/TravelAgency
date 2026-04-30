package com.epam.edp.demo.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "tours")
public class Tour {

    @Id
    private String id;
    private String name;
    private String destination;
    private String summary;
    private List<String> imageUrls;
    private Double rating;
    private Integer reviewCount;
    private List<LocalDate> startDates;
    private List<String> durations;
    private Map<String, String> pricePerDuration;
    private List<String> mealPlans;
    private Map<String, String> mealSupplementsPerDay;
    private String tourType;
    private String hotelName;
    private String hotelDescription;
    private String accommodation;
    private Map<String, String> customDetails;
    private GuestQuantity guestQuantity;
    private Integer totalCapacity;
    private Integer bookedCount;
    private Integer freeCancellationDaysBefore;
    private String assignedAgentId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Tour() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDestination() { return destination; }
    public String getSummary() { return summary; }
    public List<String> getImageUrls() { return imageUrls; }
    public Double getRating() { return rating; }
    public Integer getReviewCount() { return reviewCount; }
    public List<LocalDate> getStartDates() { return startDates; }
    public List<String> getDurations() { return durations; }
    public Map<String, String> getPricePerDuration() { return pricePerDuration; }
    public List<String> getMealPlans() { return mealPlans; }
    public Map<String, String> getMealSupplementsPerDay() { return mealSupplementsPerDay; }
    public String getTourType() { return tourType; }
    public String getHotelName() { return hotelName; }
    public String getHotelDescription() { return hotelDescription; }
    public String getAccommodation() { return accommodation; }
    public Map<String, String> getCustomDetails() { return customDetails; }
    public GuestQuantity getGuestQuantity() { return guestQuantity; }
    public Integer getTotalCapacity() { return totalCapacity; }
    public Integer getBookedCount() { return bookedCount; }
    public Integer getFreeCancellationDaysBefore() { return freeCancellationDaysBefore; }
    public String getAssignedAgentId() { return assignedAgentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public void setStartDates(List<LocalDate> startDates) { this.startDates = startDates; }
    public void setDurations(List<String> durations) { this.durations = durations; }
    public void setPricePerDuration(Map<String, String> pricePerDuration) { this.pricePerDuration = pricePerDuration; }
    public void setMealPlans(List<String> mealPlans) { this.mealPlans = mealPlans; }
    public void setMealSupplementsPerDay(Map<String, String> mealSupplementsPerDay) { this.mealSupplementsPerDay = mealSupplementsPerDay; }
    public void setTourType(String tourType) { this.tourType = tourType; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public void setHotelDescription(String hotelDescription) { this.hotelDescription = hotelDescription; }
    public void setAccommodation(String accommodation) { this.accommodation = accommodation; }
    public void setCustomDetails(Map<String, String> customDetails) { this.customDetails = customDetails; }
    public void setGuestQuantity(GuestQuantity guestQuantity) { this.guestQuantity = guestQuantity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }
    public void setBookedCount(Integer bookedCount) { this.bookedCount = bookedCount; }
    public void setFreeCancellationDaysBefore(Integer freeCancellationDaysBefore) { this.freeCancellationDaysBefore = freeCancellationDaysBefore; }
    public void setAssignedAgentId(String assignedAgentId) { this.assignedAgentId = assignedAgentId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class GuestQuantity {
        private Integer adultsMaxValue;
        private Integer childrenMaxValue;
        private Integer totalMaxValue;

        public GuestQuantity() {}

        public Integer getAdultsMaxValue() { return adultsMaxValue; }
        public Integer getChildrenMaxValue() { return childrenMaxValue; }
        public Integer getTotalMaxValue() { return totalMaxValue; }

        public void setAdultsMaxValue(Integer adultsMaxValue) { this.adultsMaxValue = adultsMaxValue; }
        public void setChildrenMaxValue(Integer childrenMaxValue) { this.childrenMaxValue = childrenMaxValue; }
        public void setTotalMaxValue(Integer totalMaxValue) { this.totalMaxValue = totalMaxValue; }
    }
}