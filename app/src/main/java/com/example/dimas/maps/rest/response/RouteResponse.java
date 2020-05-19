package com.example.dimas.maps.rest.response;

/**
 * Created by Dimas on 10.03.2020.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.maps.model.LatLng;


import java.util.List;

public class RouteResponse {

    @JsonProperty("path")
    private List<LatLng> path;

    public List<LatLng> getPath() {
        return path;
    }

    public void setPath(List<LatLng> path) {
        this.path = path;
    }
}
