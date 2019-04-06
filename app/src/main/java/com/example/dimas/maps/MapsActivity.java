package com.example.dimas.maps;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        SettingsInterface {

    private GoogleMap mMap;
    private String TAG = "MapsProj";
    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private int PERMISSION_CODE = 1;
    private static final int SETTINGS_ID = 1;
    private static final int ADD_PLACE_ID = 2;

    private String apiKey = "AIzaSyBNDPZA0lCPtjD-C_BYit46hoIVqrEulV0";
    private android.support.v7.widget.Toolbar toolbar;
    private TextView countKm;
    private SeekBar seekBar;
    private LatLng source;
    private LatLng destination;
    private MenuItem settings;

    private int tolerance = 300;
    private int countClick = 0;
    private int countDistance = 100;
    private boolean mode = true;   // true - all places
    private boolean add_place_flag = false;
    private ArrayList<LatLng> route;
    private ArrayList<LatLng> places;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settings = findViewById(R.id.item_settings);

        route = new ArrayList();
        places = new ArrayList<>();

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
        if (add_place_flag){
            places.add(latLng);
            mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.toString()));
            add_place_flag = false;
            Toast.makeText(this, String.valueOf(places.size()), Toast.LENGTH_LONG).show();
        } else
            buildRoute(latLng);
    }

    private void buildRoute(LatLng latLng){
        countClick += 1;
        if (countClick == 1) {
            destination = latLng;
            mMap.addMarker(new MarkerOptions().position(destination).title(destination.toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        } else {
            source = destination;
            destination = latLng;
            mMap.addMarker(new MarkerOptions().position(destination).title(destination.toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

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
                                                    this.route.add(new LatLng(coord1.lat, coord1.lng));
                                                }
                                            }
                                        }
                                    } else {
                                        EncodedPolyline points = step.polyline;
                                        if (points != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords = points.decodePath();
                                            for (com.google.maps.model.LatLng coord : coords) {
                                                this.route.add(new LatLng(coord.lat, coord.lng));
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

            if (route.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(route).color(Color.BLUE).width(6);
                mMap.addPolyline(opts);
            }
        }
       // mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.toString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }



    public void settings_click(MenuItem item) {
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        Settings settings = new Settings();
        settings.show(manager, "settings");

        SettingsInterface activity = (SettingsInterface) settings;

        try {
            activity.stopSettings(mode, countDistance);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stopSettings(boolean rb_changer, int count) {
        mode = rb_changer;
        countDistance = count;
        //Toast.makeText(this, String.valueOf(countDistance) + " --M", Toast.LENGTH_LONG).show();
    }

    public void add_place_click(MenuItem item) {
        add_place_flag = true;
        Toast.makeText(this, "Удерживайте палец на карте чтобы добавить новое место", Toast.LENGTH_LONG).show();
    }
}
