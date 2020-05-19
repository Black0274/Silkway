package com.example.dimas.maps.view.windows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.dimas.maps.R;
import com.example.dimas.maps.rest.type.Place;
import com.example.dimas.maps.rest.type.PlaceType;
import com.example.dimas.maps.service.PlaceService;
import com.example.dimas.maps.view.activities.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

public class AddPlaceWindow extends AppCompatDialogFragment /*implements TextView.OnEditorActionListener*/ {

    private TextView errorTextView;
    private EditText editTextName;
    private EditText editTextDescription;
    private RadioButton rbMonument;
    private RadioButton rbNature;
    private RadioButton rbView;
    private RadioButton rbOther;
    private Button okButton;

    private LatLng latLng;

    private PlaceType placeType = PlaceType.OTHER;

    public AddPlaceWindow() {}

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.popup_add_place, container);

        errorTextView = view.findViewById(R.id.addPlaceTextViewError);

        editTextName = view.findViewById(R.id.addPlaceEditTextName);
        editTextName.requestFocus();
        editTextDescription = view.findViewById(R.id.addPlaceEditTextDescription);

        rbMonument = view.findViewById(R.id.addPlaceRbMonument);
        rbMonument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeType = PlaceType.MONUMENT;
            }
        });

        rbNature = view.findViewById(R.id.addPlaceRbNature);
        rbNature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeType = PlaceType.NATURE;
            }
        });

        rbView = view.findViewById(R.id.addPlaceRbBeautifulView);
        rbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeType = PlaceType.BEAUTIFUL_VIEW;
            }
        });

        rbOther = view.findViewById(R.id.addPlaceRbOther);
        rbOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeType = PlaceType.OTHER;
            }
        });

        okButton = view.findViewById(R.id.addPlaceOkButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextName.getText().toString().length() == 0) {
                    errorTextView.setText(" Введите название");
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    errorTextView.setVisibility(View.GONE);

                    Place place = new Place(
                            latLng.latitude,
                            latLng.longitude,
                            editTextName.getText().toString(),
                            editTextDescription.getText().toString(),
                            placeType.ordinal());

                    String response = PlaceService.save(place);

                    switch (response) {
                        case "NOT_AUTHORIZED": {
                            errorTextView.setText(" Пользователь не авторизован");
                            errorTextView.setVisibility(View.VISIBLE);
                            break;
                        }
                        case "UNEXPECTED_ERROR": {
                            errorTextView.setText(" Неизвестная ошибка");
                            errorTextView.setVisibility(View.VISIBLE);
                            break;
                        }
                        default: {
                            AddPlace activity = (AddPlace) getActivity();
                            place.setAuthor(MapsActivity.getUsername());
                            activity.addPlaceText(place);
                            dismiss();
                            break;
                        }
                    }
                }
            }
        });

        return view;
    }
}
