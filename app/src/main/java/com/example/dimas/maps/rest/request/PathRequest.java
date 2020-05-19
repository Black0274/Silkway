package com.example.dimas.maps.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Dimas on 11.05.2020.
 */

public class PathRequest {

    @JsonProperty("path")
    private List<LatLng> path;

    public List<LatLng> getPath() {
        return path;
    }

    public void setPath(List<LatLng> path) {
        this.path = path;
    }
}
