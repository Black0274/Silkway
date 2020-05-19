package com.example.dimas.maps.service;

import com.example.dimas.maps.rest.Rest;
import com.example.dimas.maps.rest.request.RatingRequest;
import com.example.dimas.maps.rest.type.Place;
import com.example.dimas.maps.rest.response.Message;
import com.example.dimas.maps.rest.response.RatingResponse;
import com.example.dimas.maps.utils.Utils;
import com.example.dimas.maps.view.activities.MapsActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
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
 * Created by Dimas on 17.05.2020.
 */

public class PlaceService {

    private static String json = "DEFAULT VALUE";
    private static String result = null;
    private static CountDownLatch latch;

    public static String save(Place place) {
        try {
            OkHttpClient client = new OkHttpClient();

            ObjectMapper mapper = new ObjectMapper();
            String jsonRequest = mapper.writeValueAsString(place);

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonRequest);

            Request request = new Request.Builder()
                    .post(requestBody)
                    .addHeader("Authorization", MapsActivity.getToken())
                    .url(Rest.PLACE_ADD)
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

    public static List<Place> getAll() {
        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .get()
                    .addHeader("Authorization", MapsActivity.getToken())
                    .url(Rest.PLACE_GET_ALL)
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
            return Utils.convertPlace(json);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RatingResponse changeRating(Double lat, Double lng, Integer rating) {
        try {
            OkHttpClient client = new OkHttpClient();

            ObjectMapper mapper = new ObjectMapper();
            RatingRequest ratingRequest = new RatingRequest(lat, lng, rating);
            String jsonRequest = mapper.writeValueAsString(ratingRequest);

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonRequest);

            Request request = new Request.Builder()
                    .post(requestBody)
                    .addHeader("Authorization", MapsActivity.getToken())
                    .url(Rest.PLACE_ADD_RATING)
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
                    if (response.code() == 200) {
                        json = Objects.requireNonNull(response.body()).string();
                        latch.countDown();
                        return;
                    }
                    latch.countDown();
                }
            });

            latch.await();
            return Utils.convertRating(json);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
