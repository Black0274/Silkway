package com.example.dimas.maps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private String TAG = "MapsProj";
    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private int PERMISSION_CODE = 1;
    private static final int SETTINGS_ID = 1;
    private static final int ADD_PLACE_ID = 2;

    private int tolerance = 300;
    private String apiKey = "AIzaSyBNDPZA0lCPtjD-C_BYit46hoIVqrEulV0";
    private android.support.v7.widget.Toolbar toolbar;
    private TextView countKm;
    private SeekBar seekBar;
    private int count = 0;
    private LatLng source;
    private LatLng destination;
    private MenuItem settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settings = findViewById(R.id.item_settings);


        if (!isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION))
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean isPermissionGranted(String permission){
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapLongClickListener(this);

        LatLng startPos = new LatLng(49.685985,33.907485);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 3));
    }

    @Override
    public void onMapLongClick(LatLng latLng){
            count += 1;
            if (count == 1) {
                destination = latLng;
                mMap.addMarker(new MarkerOptions().position(destination).title(destination.toString()));
            } else {
                source = destination;
                destination = latLng;
                mMap.addMarker(new MarkerOptions().position(destination).title(destination.toString()));

                List<LatLng> path = new ArrayList();

                GeoApiContext context = new GeoApiContext.Builder()
                        .apiKey(apiKey)
                        .build();
                DirectionsApiRequest req = DirectionsApi.getDirections(context, source.latitude + "," + source.longitude,
                        destination.latitude + "," + destination.longitude);

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
                                                    //Decode polyline and add points to list of route coordinates
                                                    List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                    for (com.google.maps.model.LatLng coord1 : coords1) {
                                                        path.add(new LatLng(coord1.lat, coord1.lng));
                                                    }
                                                }
                                            }
                                        } else {
                                            EncodedPolyline points = step.polyline;
                                            if (points != null) {
                                                //Decode polyline and add points to list of route coordinates
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
                } catch (Exception ex) {
                    Log.e(TAG, ex.getLocalizedMessage());
                }

                if (path.size() > 0) {
                    PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(6);
                    mMap.addPolyline(opts);
                }
            }



        mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.toString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }


    public void settings_click(MenuItem item) {
        //Toast.makeText(this,"SETTINGS CHOSEN", Toast.LENGTH_LONG).show();
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        Settings settings = new Settings();
        String s = "settings";
        settings.show(manager, "settings");

        //showDialog(SETTINGS_ID);
    }

   /* @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SETTINGS_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                LayoutInflater inflater = MapsActivity.this.getLayoutInflater();
                View view = inflater.inflate(R.layout.popup, null);
                builder.setView(view);
                countKm = findViewById(R.id.countKm);
                seekBar = findViewById(R.id.seekBar);
                return builder.create();
            default:
                return null;
        }
    } */


}
