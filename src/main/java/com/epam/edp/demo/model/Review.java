package com.epam.edp.demo.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String tourId;

    private String userId;
    private String userName;
    private String userAvatarUrl;
    private Double rate;
    private String comment;
    private LocalDate reviewDate;

    @CreatedDate
    private LocalDateTime createdAt;

    public Review() {}

    public String getId()            { return id; }
    public String getTourId()        { return tourId; }
    public String getUserId()        { return userId; }
    public String getUserName()      { return userName; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public Double getRate()          { return rate; }
    public String getComment()       { return comment; }
    public LocalDate getReviewDate() { return reviewDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(String id)                       { this.id = id; }
    public void setTourId(String tourId)               { this.tourId = tourId; }
    public void setUserId(String userId)               { this.userId = userId; }
    public void setUserName(String userName)           { this.userName = userName; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
    public void setRate(Double rate)                   { this.rate = rate; }
    public void setComment(String comment)             { this.comment = comment; }
    public void setReviewDate(LocalDate reviewDate)    { this.reviewDate = reviewDate; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }
}
