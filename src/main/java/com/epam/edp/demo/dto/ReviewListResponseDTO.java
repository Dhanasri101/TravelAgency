package com.epam.edp.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewListResponseDTO {

    private List<ReviewItem> reviews;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
    private Integer totalItems;
    private Double averageRating;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewItem {
        private String id;
        private String userName;
        private String userAvatarUrl;
        private Double rate;
        private String comment;
        private LocalDate reviewDate;
    }
}
