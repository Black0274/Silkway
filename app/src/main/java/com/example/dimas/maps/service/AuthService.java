package com.example.dimas.maps.service;

import com.example.dimas.maps.rest.Rest;
import com.example.dimas.maps.rest.response.Message;
import com.example.dimas.maps.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.*;
import okhttp3.Response;

/**
 * Created by Dimas on 10.05.2020.
 */

public class AuthService {

    private static CountDownLatch latch;
    private static String result = null;

    public static String signIn(String username, String password) {
        try {
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("password", password);

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonObject.toString());

            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(Rest.SIGN_IN)
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
                    if (response.code() == 200) {
                        result = Utils.getToken(response.body().string());
                        latch.countDown();
                        return;
                    }
                    result = Message.UNEXPECTED_ERROR.name();
                    latch.countDown();
                }
            });

            latch.await();
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }

        String response = result;
        result = null;
        return response;
    }

    public static String signUp(String username, String password) {
        try {
            OkHttpClient client = new OkHttpClient();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("password", password);

            RequestBody requestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), jsonObject.toString());

            Request request = new Request.Builder()
                    .post(requestBody)
                    .url(Rest.SIGN_UP)
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
                    if (response.code() == 400 && Objects.equals(response.body().string(), "Fail -> Username is already taken!")) {
                        result = Message.USERNAME_IS_ALREADY_TAKEN.name();
                        latch.countDown();
                        return;
                    }
                    if (response.code() == 200) {
                        result = Utils.getToken(response.body().string());
                        latch.countDown();
                        return;
                    }
                    result = Message.UNEXPECTED_ERROR.name();
                    latch.countDown();
                }
            });

            latch.await();
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }

        String response = result;
        result = null;
        return response;
    }
}
