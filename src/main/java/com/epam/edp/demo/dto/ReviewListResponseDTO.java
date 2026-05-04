package com.epam.edp.demo.dto;

import java.time.LocalDate;
import java.util.List;

public class ReviewListResponseDTO {

    private List<ReviewItem> reviews;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Integer totalItems;
    private Double averageRating;

    public ReviewListResponseDTO() {}

    // ── Getters ───────────────────────────────────
    public List<ReviewItem> getReviews() { return reviews; }
    public Integer getPage()             { return page; }
    public Integer getPageSize()         { return pageSize; }
    public Integer getTotalPages()       { return totalPages; }
    public Integer getTotalItems()       { return totalItems; }
    public Double getAverageRating()     { return averageRating; }

    // ── Setters ───────────────────────────────────
    public void setReviews(List<ReviewItem> reviews)   { this.reviews = reviews; }
    public void setPage(Integer page)                  { this.page = page; }
    public void setPageSize(Integer pageSize)          { this.pageSize = pageSize; }
    public void setTotalPages(Integer totalPages)      { this.totalPages = totalPages; }
    public void setTotalItems(Integer totalItems)      { this.totalItems = totalItems; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ReviewListResponseDTO dto = new ReviewListResponseDTO();

        public Builder reviews(List<ReviewItem> v)   { dto.reviews = v; return this; }
        public Builder page(Integer v)               { dto.page = v; return this; }
        public Builder pageSize(Integer v)           { dto.pageSize = v; return this; }
        public Builder totalPages(Integer v)         { dto.totalPages = v; return this; }
        public Builder totalItems(Integer v)         { dto.totalItems = v; return this; }
        public Builder averageRating(Double v)       { dto.averageRating = v; return this; }
        public ReviewListResponseDTO build()         { return dto; }
    }

    // ── Nested item ───────────────────────────────
    public static class ReviewItem {
        private String id;
        private String userName;
        private String userAvatarUrl;
        private Double rate;
        private String comment;
        private LocalDate reviewDate;

        public ReviewItem() {}

        public String getId()            { return id; }
        public String getUserName()      { return userName; }
        public String getUserAvatarUrl() { return userAvatarUrl; }
        public Double getRate()          { return rate; }
        public String getComment()       { return comment; }
        public LocalDate getReviewDate() { return reviewDate; }

        public void setId(String id)                       { this.id = id; }
        public void setUserName(String userName)           { this.userName = userName; }
        public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
        public void setRate(Double rate)                   { this.rate = rate; }
        public void setComment(String comment)             { this.comment = comment; }
        public void setReviewDate(LocalDate reviewDate)    { this.reviewDate = reviewDate; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final ReviewItem item = new ReviewItem();

            public Builder id(String v)            { item.id = v; return this; }
            public Builder userName(String v)      { item.userName = v; return this; }
            public Builder userAvatarUrl(String v) { item.userAvatarUrl = v; return this; }
            public Builder rate(Double v)          { item.rate = v; return this; }
            public Builder comment(String v)       { item.comment = v; return this; }
            public Builder reviewDate(LocalDate v) { item.reviewDate = v; return this; }
            public ReviewItem build()              { return item; }
        }
    }
}
