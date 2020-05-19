package com.example.dimas.maps.rest.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Dimas on 17.05.2020.
 */

public class Place {

    private Double lat;
    private Double lng;

    private String title;
    private String description;
    private Integer type;
    private Double rating;
    private Integer ratingCount;
    private String author;

    @JsonIgnore
    private Marker marker;

    @JsonIgnore
    private MarkerOptions markerOptions;

    public Place() {}

    public Place(Double lat, Double lng, String title, String description, Integer type) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public void initMarker(GoogleMap map) {
        float color;

        switch (type) {
            case 0:
                color = BitmapDescriptorFactory.HUE_AZURE;
                break;
            case 1:
                color = 100.0f;
                break;
            case 2:
                color = 50.0f;
                break;
            default:
                color = BitmapDescriptorFactory.HUE_ORANGE;
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(title)
                .snippet(description)
                .icon(BitmapDescriptorFactory.defaultMarker(color));

        this.markerOptions = markerOptions;
        this.marker = map.addMarker(markerOptions);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }
}
