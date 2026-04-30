package com.epam.edp.demo.dto;

import java.time.LocalDate;
import java.util.List;

public class TourListResponseDTO {

    private List<TourItem> tours;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Integer totalItems;

    public TourListResponseDTO() {}

    public TourListResponseDTO(List<TourItem> tours, Integer page, Integer pageSize, Integer totalPages, Integer totalItems) {
        this.tours = tours;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

    public List<TourItem> getTours() { return tours; }
    public Integer getPage() { return page; }
    public Integer getPageSize() { return pageSize; }
    public Integer getTotalPages() { return totalPages; }
    public Integer getTotalItems() { return totalItems; }

    public void setTours(List<TourItem> tours) { this.tours = tours; }
    public void setPage(Integer page) { this.page = page; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<TourItem> tours;
        private Integer page;
        private Integer pageSize;
        private Integer totalPages;
        private Integer totalItems;

        public Builder tours(List<TourItem> tours) { this.tours = tours; return this; }
        public Builder page(Integer page) { this.page = page; return this; }
        public Builder pageSize(Integer pageSize) { this.pageSize = pageSize; return this; }
        public Builder totalPages(Integer totalPages) { this.totalPages = totalPages; return this; }
        public Builder totalItems(Integer totalItems) { this.totalItems = totalItems; return this; }

        public TourListResponseDTO build() {
            return new TourListResponseDTO(tours, page, pageSize, totalPages, totalItems);
        }
    }

    public static class TourItem {
        private String id;
        private String name;
        private String destination;
        private LocalDate startDate;
        private List<String> durations;
        private List<String> mealPlans;
        private String price;
        private Double rating;
        private Integer reviews;
        private LocalDate freeCancellation;

        public TourItem() {}

        public TourItem(String id, String name, String destination, LocalDate startDate, List<String> durations,
                        List<String> mealPlans, String price, Double rating, Integer reviews, LocalDate freeCancellation) {
            this.id = id;
            this.name = name;
            this.destination = destination;
            this.startDate = startDate;
            this.durations = durations;
            this.mealPlans = mealPlans;
            this.price = price;
            this.rating = rating;
            this.reviews = reviews;
            this.freeCancellation = freeCancellation;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDestination() { return destination; }
        public LocalDate getStartDate() { return startDate; }
        public List<String> getDurations() { return durations; }
        public List<String> getMealPlans() { return mealPlans; }
        public String getPrice() { return price; }
        public Double getRating() { return rating; }
        public Integer getReviews() { return reviews; }
        public LocalDate getFreeCancellation() { return freeCancellation; }

        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setDestination(String destination) { this.destination = destination; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public void setDurations(List<String> durations) { this.durations = durations; }
        public void setMealPlans(List<String> mealPlans) { this.mealPlans = mealPlans; }
        public void setPrice(String price) { this.price = price; }
        public void setRating(Double rating) { this.rating = rating; }
        public void setReviews(Integer reviews) { this.reviews = reviews; }
        public void setFreeCancellation(LocalDate freeCancellation) { this.freeCancellation = freeCancellation; }

        public static TourItemBuilder builder() { return new TourItemBuilder(); }

        public static class TourItemBuilder {
            private String id;
            private String name;
            private String destination;
            private LocalDate startDate;
            private List<String> durations;
            private List<String> mealPlans;
            private String price;
            private Double rating;
            private Integer reviews;
            private LocalDate freeCancellation;

            public TourItemBuilder id(String id) { this.id = id; return this; }
            public TourItemBuilder name(String name) { this.name = name; return this; }
            public TourItemBuilder destination(String destination) { this.destination = destination; return this; }
            public TourItemBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
            public TourItemBuilder durations(List<String> durations) { this.durations = durations; return this; }
            public TourItemBuilder mealPlans(List<String> mealPlans) { this.mealPlans = mealPlans; return this; }
            public TourItemBuilder price(String price) { this.price = price; return this; }
            public TourItemBuilder rating(Double rating) { this.rating = rating; return this; }
            public TourItemBuilder reviews(Integer reviews) { this.reviews = reviews; return this; }
            public TourItemBuilder freeCancellation(LocalDate freeCancellation) { this.freeCancellation = freeCancellation; return this; }

            public TourItem build() {
                return new TourItem(id, name, destination, startDate, durations, mealPlans, price, rating, reviews, freeCancellation);
            }
        }
    }
}