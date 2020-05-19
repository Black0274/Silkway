package com.example.dimas.maps.view.windows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.dimas.maps.R;
import com.example.dimas.maps.rest.response.RatingResponse;
import com.example.dimas.maps.service.PlaceService;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Dimas on 18.05.2020.
 */

public class PlaceWindow extends AppCompatDialogFragment implements Place {

    private TextView titleTextView;
    private TextView errorTextView;
    private RatingBar ratingBar;
    private TextView ratingTextView;
    private TextView descriptionStaticTextView;
    private TextView descriptionTextView;
    private TextView authorTextView;
    private Button addAtListButton;

    private com.example.dimas.maps.rest.type.Place place;
    private boolean addRatingFlag = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.popup_place, container);
        float rating = place.getRating() != null ? place.getRating().floatValue() : 0.0f;
        final int ratingCount = place.getRatingCount() != null ? place.getRatingCount() : 0;

        titleTextView = view.findViewById(R.id.placeTextViewTitle);
        titleTextView.setText(place.getTitle());

        errorTextView = view.findViewById(R.id.placeTextViewError);

        ratingBar = view.findViewById(R.id.placeRatingBar);
        ratingBar.setRating(rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar bar, float rating, boolean fromUser) {
                if (!addRatingFlag) {
                    addRatingFlag = true;
                    Integer ratingInt = (int) Math.ceil(rating);

                    RatingResponse response = PlaceService.changeRating(place.getLat(), place.getLng(), ratingInt);

                    place.setRating(response.getRating());
                    place.setRatingCount(response.getRatingCount());

                    float newRating = response.getRating().floatValue();
                    ratingBar.setRating(newRating);
                    ratingTextView.setText("Оценили: " + response.getRatingCount() + "                      "
                            + new BigDecimal(newRating).setScale(1, RoundingMode.UP).doubleValue());
                    addRatingFlag = false;
                }
            }
        });

        ratingTextView = view.findViewById(R.id.placeTextViewRating);
        ratingTextView.setText("Оценили: " + ratingCount + "                      "
                + new BigDecimal(rating).setScale(1, RoundingMode.UP).doubleValue());

        descriptionStaticTextView = view.findViewById(R.id.placeTextViewDescriptionStatic);
        descriptionTextView = view.findViewById(R.id.placeTextViewDescription);

        if (place.getDescription() != null && place.getDescription().length() > 0) {
            descriptionTextView.setText(place.getDescription());
        } else {
            descriptionStaticTextView.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.GONE);
        }


        authorTextView = view.findViewById(R.id.placeTextViewAuthor);
        authorTextView.setText("Добавлено пользователем " + place.getAuthor());

        addAtListButton = view.findViewById(R.id.placeAddButton);

        return view;
    }

    @Override
    public void actPlace(com.example.dimas.maps.rest.type.Place place) {
        this.place = place;
    }
}
