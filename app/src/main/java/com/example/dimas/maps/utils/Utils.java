package com.example.dimas.maps.utils;

import com.example.dimas.maps.rest.response.RatingResponse;
import com.example.dimas.maps.rest.type.Place;
import com.example.dimas.maps.rest.type.PlaceType;
import com.example.dimas.maps.rest.response.AuthResponse;
import com.example.dimas.maps.rest.response.PlaceResponse;
import com.example.dimas.maps.rest.response.RouteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dimas on 09.03.2020.
 */

public class Utils {

    public static List<LatLng> convert(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RouteResponse response = mapper.readValue(json, RouteResponse.class);
        List<LatLng> result = new ArrayList<>();

        for (com.google.maps.model.LatLng latLng : response.getPath()){
            result.add(new LatLng(latLng.lat, latLng.lng));
        }

        return result;
    }

    public static List<com.google.maps.model.LatLng> convert(List<LatLng> path){
        List<com.google.maps.model.LatLng> result = new ArrayList<>();
        for (LatLng latLng : path) {
            result.add(new com.google.maps.model.LatLng(latLng.latitude, latLng.longitude));
        }
        return result;
    }

    public static PlaceType convert(Integer number) {
        switch (number) {
            case 0: return PlaceType.MONUMENT;
            case 1: return PlaceType.NATURE;
            case 2: return PlaceType.BEAUTIFUL_VIEW;
            default: return PlaceType.OTHER;
        }
    }


    public static List<Place> convertPlace(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PlaceResponse response = mapper.readValue(json, PlaceResponse.class);
        return response.getPlaces();
    }

    public static RatingResponse convertRating(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RatingResponse.class);
    }

    public static String getToken(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AuthResponse response = mapper.readValue(json, AuthResponse.class);

        return response.getType() + ' ' + response.getToken();
    }

    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder().append('?');

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
