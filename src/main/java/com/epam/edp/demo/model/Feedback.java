package com.epam.edp.demo.model;

import com.epam.edp.demo.enums.FeedbackStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
public class Feedback {

    @Id
    private String id;

    @Indexed(unique = true)
    private String bookingId;

    @Indexed
    private String tourId;

    @Indexed
    private String customerId;

    private int rating;

    private String comment;

    private FeedbackStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
