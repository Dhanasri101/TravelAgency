package com.epam.edp.demo.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TourDetailResponseDTO {

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
    private String hotelName;
    private String hotelDescription;
    private String accommodation;
    private String tourType;
    private Map<String, String> customDetails;
    private GuestQuantityDTO guestQuantity;
    private Integer freeCancellationDaysBefore;
    private LocalDate freeCancellationDeadline;

    public TourDetailResponseDTO() {}

    // ── Inner DTO ─────────────────────────────────
    public static class GuestQuantityDTO {
        private Integer adultsMaxValue;
        private Integer childrenMaxValue;
        private Integer totalMaxValue;

        public GuestQuantityDTO() {}

        public GuestQuantityDTO(Integer adultsMaxValue, Integer childrenMaxValue, Integer totalMaxValue) {
            this.adultsMaxValue = adultsMaxValue;
            this.childrenMaxValue = childrenMaxValue;
            this.totalMaxValue = totalMaxValue;
        }

        public Integer getAdultsMaxValue()   { return adultsMaxValue; }
        public Integer getChildrenMaxValue() { return childrenMaxValue; }
        public Integer getTotalMaxValue()    { return totalMaxValue; }

        public void setAdultsMaxValue(Integer v)   { this.adultsMaxValue = v; }
        public void setChildrenMaxValue(Integer v) { this.childrenMaxValue = v; }
        public void setTotalMaxValue(Integer v)    { this.totalMaxValue = v; }
    }

    // ── Getters ───────────────────────────────────
    public String getId()                             { return id; }
    public String getName()                           { return name; }
    public String getDestination()                    { return destination; }
    public String getSummary()                        { return summary; }
    public List<String> getImageUrls()                { return imageUrls; }
    public Double getRating()                         { return rating; }
    public Integer getReviewCount()                   { return reviewCount; }
    public List<LocalDate> getStartDates()            { return startDates; }
    public List<String> getDurations()                { return durations; }
    public Map<String, String> getPricePerDuration()  { return pricePerDuration; }
    public List<String> getMealPlans()                { return mealPlans; }
    public Map<String, String> getMealSupplementsPerDay() { return mealSupplementsPerDay; }
    public String getHotelName()                      { return hotelName; }
    public String getHotelDescription()               { return hotelDescription; }
    public String getAccommodation()                  { return accommodation; }
    public String getTourType()                       { return tourType; }
    public Map<String, String> getCustomDetails()     { return customDetails; }
    public GuestQuantityDTO getGuestQuantity()        { return guestQuantity; }
    public Integer getFreeCancellationDaysBefore()    { return freeCancellationDaysBefore; }
    public LocalDate getFreeCancellationDeadline()    { return freeCancellationDeadline; }

    // ── Setters ───────────────────────────────────
    public void setId(String id)                                           { this.id = id; }
    public void setName(String name)                                       { this.name = name; }
    public void setDestination(String destination)                         { this.destination = destination; }
    public void setSummary(String summary)                                 { this.summary = summary; }
    public void setImageUrls(List<String> imageUrls)                       { this.imageUrls = imageUrls; }
    public void setRating(Double rating)                                   { this.rating = rating; }
    public void setReviewCount(Integer reviewCount)                        { this.reviewCount = reviewCount; }
    public void setStartDates(List<LocalDate> startDates)                  { this.startDates = startDates; }
    public void setDurations(List<String> durations)                       { this.durations = durations; }
    public void setPricePerDuration(Map<String, String> pricePerDuration)  { this.pricePerDuration = pricePerDuration; }
    public void setMealPlans(List<String> mealPlans)                       { this.mealPlans = mealPlans; }
    public void setMealSupplementsPerDay(Map<String, String> m)            { this.mealSupplementsPerDay = m; }
    public void setHotelName(String hotelName)                             { this.hotelName = hotelName; }
    public void setHotelDescription(String hotelDescription)               { this.hotelDescription = hotelDescription; }
    public void setAccommodation(String accommodation)                     { this.accommodation = accommodation; }
    public void setTourType(String tourType)                               { this.tourType = tourType; }
    public void setCustomDetails(Map<String, String> customDetails)        { this.customDetails = customDetails; }
    public void setGuestQuantity(GuestQuantityDTO guestQuantity)           { this.guestQuantity = guestQuantity; }
    public void setFreeCancellationDaysBefore(Integer v)                   { this.freeCancellationDaysBefore = v; }
    public void setFreeCancellationDeadline(LocalDate v)                   { this.freeCancellationDeadline = v; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TourDetailResponseDTO dto = new TourDetailResponseDTO();

        public Builder id(String v)                                          { dto.id = v; return this; }
        public Builder name(String v)                                        { dto.name = v; return this; }
        public Builder destination(String v)                                 { dto.destination = v; return this; }
        public Builder summary(String v)                                     { dto.summary = v; return this; }
        public Builder imageUrls(List<String> v)                             { dto.imageUrls = v; return this; }
        public Builder rating(Double v)                                      { dto.rating = v; return this; }
        public Builder reviewCount(Integer v)                                { dto.reviewCount = v; return this; }
        public Builder startDates(List<LocalDate> v)                         { dto.startDates = v; return this; }
        public Builder durations(List<String> v)                             { dto.durations = v; return this; }
        public Builder pricePerDuration(Map<String, String> v)               { dto.pricePerDuration = v; return this; }
        public Builder mealPlans(List<String> v)                             { dto.mealPlans = v; return this; }
        public Builder mealSupplementsPerDay(Map<String, String> v)          { dto.mealSupplementsPerDay = v; return this; }
        public Builder hotelName(String v)                                   { dto.hotelName = v; return this; }
        public Builder hotelDescription(String v)                            { dto.hotelDescription = v; return this; }
        public Builder accommodation(String v)                               { dto.accommodation = v; return this; }
        public Builder tourType(String v)                                    { dto.tourType = v; return this; }
        public Builder customDetails(Map<String, String> v)                  { dto.customDetails = v; return this; }
        public Builder guestQuantity(GuestQuantityDTO v)                     { dto.guestQuantity = v; return this; }
        public Builder freeCancellationDaysBefore(Integer v)                 { dto.freeCancellationDaysBefore = v; return this; }
        public Builder freeCancellationDeadline(LocalDate v)                 { dto.freeCancellationDeadline = v; return this; }
        public TourDetailResponseDTO build()                                 { return dto; }
    }
}
