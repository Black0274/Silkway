package com.example.dimas.maps.rest.request;

/**
 * Created by Dimas on 18.05.2020.
 */

public class RatingRequest {

    private Double lat;
    private Double lng;
    private Integer rating;

    public RatingRequest(Double lat, Double lng, Integer rating) {
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
