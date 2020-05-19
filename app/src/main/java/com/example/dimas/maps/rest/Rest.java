package com.example.dimas.maps.rest;

/**
 * Created by Dimas on 10.05.2020.
 */

public class Rest {

    public static final String SIGN_IN = "http://64.225.79.145:8080/Silkway/auth/signin";                  // POST
    public static final String SIGN_UP = "http://64.225.79.145:8080/Silkway/auth/signup";                  // POST
    public static final String REGISTER = "http://64.225.79.145:8080/Silkway/auth/register";               // POST

    public static final String ROUTE = "http://64.225.79.145:8080/Silkway/route";                          // GET
    public static final String ROUTE_BUILD = "http://64.225.79.145:8080/Silkway/route/build";              // POST
    public static final String ROUTE_SAVE = "http://64.225.79.145:8080/Silkway/route/save";                // POST
    public static final String ROUTE_SHOW = "http://64.225.79.145:8080/Silkway/route/show";                // POST

    public static final String PLACE_ADD = "http://64.225.79.145:8080/Silkway/place";                      // POST
    public static final String PLACE_GET_ALL = "http://64.225.79.145:8080/Silkway/place/all";              // GET
    public static final String PLACE_ADD_RATING = "http://64.225.79.145:8080/Silkway/place/rating";        // POST
    public static final String PLACE_LIST = "http://64.225.79.145:8080/Silkway/place/list";                // GET POST DELETE
}
