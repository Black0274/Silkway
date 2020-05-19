package com.example.dimas.maps.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Dimas on 10.05.2020.
 */

public class AuthResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("type")
    private String type;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
