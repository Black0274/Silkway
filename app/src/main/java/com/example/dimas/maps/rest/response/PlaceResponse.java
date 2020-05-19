package com.example.dimas.maps.rest.response;

import com.example.dimas.maps.rest.type.Place;

import java.util.List;

/**
 * Created by Dimas on 17.05.2020.
 */

public class PlaceResponse {

    private List<Place> places;

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }
}
