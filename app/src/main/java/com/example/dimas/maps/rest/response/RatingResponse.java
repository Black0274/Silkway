package com.example.dimas.maps.rest.response;

/**
 * Created by Dimas on 18.05.2020.
 */

public class RatingResponse {

    private Double rating;
    private Integer ratingCount;

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }
}
