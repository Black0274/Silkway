package com.example.dimas.maps.service;

import com.example.dimas.maps.rest.Rest;
import com.example.dimas.maps.rest.response.Message;
import com.example.dimas.maps.rest.response.RouteResponse;
import com.example.dimas.maps.utils.Utils;
import com.example.dimas.maps.view.activities.MapsActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Dimas on 09.03.2020.
 */

public class RouteService {

    private static String json = "DEFAULT VALUE";
    private static String result = null;
    private static CountDownLatch latch;

    // from server
    public static List<LatLng> build(double sourceLat, double sourceLng, double destLat, double destLng) {
        try {
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sourceLat", sourceLat);
            jsonObject.put("sourceLng", sourceLng);
            jsonObject.put("destLat", destLat);
            jsonObject.put("destLng", destLng);

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonObject.toString());

            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(Rest.ROUTE_SHOW)
                    .addHeader("Authorization", MapsActivity.getToken())
                    .build();

            Call call = client.newCall(request);
            latch = new CountDownLatch(1);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    json = Objects.requireNonNull(response.body()).string();
                    latch.countDown();
                }
            });

            latch.await();
            return Utils.convert(json);
        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // from Google
    public static List<LatLng> build(double sourceLat, double sourceLng, double destLat, double destLng, String apiKey) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, sourceLat + "," + sourceLng,
                destLat + "," + destLng);

        List<LatLng> path = new ArrayList<>();

        try {
            DirectionsResult res = req.await();

            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of path coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of path coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return path;
    }

    public static String save(List<LatLng> route) {
        try {
            OkHttpClient client = new OkHttpClient();

            ObjectMapper mapper = new ObjectMapper();
            RouteResponse path = new RouteResponse();
            path.setPath(Utils.convert(route));
            String jsonRequest = mapper.writeValueAsString(path);

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonRequest);

            Request request = new Request.Builder()
                    .post(requestBody)
                    .addHeader("Authorization", MapsActivity.getToken())
                    .url(Rest.ROUTE_SAVE)
                    .build();

            Call call = client.newCall(request);
            latch = new CountDownLatch(1);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 401) {
                        result = Message.NOT_AUTHORIZED.name();
                        latch.countDown();
                        return;
                    }
                    if (response.code() == 200 && response.body().string().equals("success")) {
                        result = Message.SUCCESS.name();
                        latch.countDown();
                        return;
                    }
                    result = Message.UNEXPECTED_ERROR.name();
                    latch.countDown();
                }
            });

            latch.await();
        } catch (JsonProcessingException | InterruptedException e) {
            e.printStackTrace();
        }

        String response = result;
        result = null;
        return response;
    }

    public static List<LatLng> load() {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .get()
                    .addHeader("Authorization", MapsActivity.getToken())
                    .url(Rest.ROUTE)
                    .build();

            Call call = client.newCall(request);
            latch = new CountDownLatch(1);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    json = null;
                    latch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        json = Objects.requireNonNull(response.body()).string();
                        latch.countDown();
                        return;
                    }
                    latch.countDown();
                }
            });

            latch.await();
            return Utils.convert(json);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
