package com.example.dimas.maps;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
       GoogleMap.OnMarkerClickListener, SettingsInterface, AddPlaceInterface {

    private GoogleMap mMap;
    private String TAG = "MapsProj";
    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private int PERMISSION_CODE = 1;

    private String apiKey;
    private android.support.v7.widget.Toolbar toolbar;
    private LatLng source;
    private LatLng destination;
    private Menu menu;

    private int tolerance = 5000;
    private int countClick = 0;
    private int countDistance = 100;
    private boolean mode = true;   // true - all places
    private boolean addPlaceFlag = false;
    private boolean optChecked = false;
    private boolean firstChangeSeekBar = false;
    private boolean newDestination = false;
    private boolean deletePlaceChosen = false;
    private ArrayList<LatLng> path;
    private LinkedList<Marker> places;
    private LinkedList<LatLng> simplifiedPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("Silkway");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        apiKey = getString(R.string.google_maps_key);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        path = new ArrayList<>();
        places = new LinkedList<>();
        simplifiedPath = new LinkedList<>();

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
        mMap.setOnMarkerClickListener(this);

        LatLng startPos = new LatLng(49.685985,33.907485);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 3));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng){
        if (addPlaceFlag){
            FragmentManager manager = getSupportFragmentManager();
            AddPlace addPlace = new AddPlace();
            addPlace.show(manager, "add_place");

            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            places.add(marker);
            addPlaceFlag = false;
        } else {
            newDestination = true;
            buildRoute(latLng);
        }
    }


    private void buildRoute(LatLng latLng){
        countClick += 1;
        ArrayList<LatLng> pathPart = new ArrayList<>();

        if (countClick == 1) {
            destination = latLng;
            mMap.addMarker(new MarkerOptions().position(destination));
            path.add(latLng);
            simplifiedPath.add(latLng);
        } else {
            source = destination;
            destination = latLng;
            mMap.addMarker(new MarkerOptions().position(destination));

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
                                                //Decode polyline and add points to list of path coordinates
                                                List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                for (com.google.maps.model.LatLng coord1 : coords1) {
                                                    pathPart.add(new LatLng(coord1.lat, coord1.lng));
                                                }
                                            }
                                        }
                                    } else {
                                        EncodedPolyline points = step.polyline;
                                        if (points != null) {
                                            //Decode polyline and add points to list of path coordinates
                                            List<com.google.maps.model.LatLng> coords = points.decodePath();
                                            for (com.google.maps.model.LatLng coord : coords) {
                                                pathPart.add(new LatLng(coord.lat, coord.lng));
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
            path.addAll(pathPart);
            String log = "Route: " + String.valueOf(path.size());

            PolylineOptions opts = new PolylineOptions().addAll(pathPart).color(Color.BLUE).width(6);
            mMap.addPolyline(opts);

            if (optChecked){
                List<LatLng> simplifiedPathPart = PolyUtil.simplify(pathPart, tolerance);
                mMap.addPolyline(new PolylineOptions()
                        .addAll(simplifiedPathPart)
                        .color(Color.MAGENTA));
                simplifiedPath.addAll(simplifiedPathPart);
                log += "\nSimplified route: " + String.valueOf(simplifiedPath.size());
            }

            Toast.makeText(this, log, Toast.LENGTH_LONG).show();

            if (!mode){
                showPlacesAtDistance(countDistance);
                newDestination = false;
            }
        }
    }


    public void settingsClick(MenuItem item) {
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        Settings settings = new Settings();
        settings.show(manager, "settings");

        try {
            settings.actSettings(mode, optChecked, countDistance);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }

        deletePlaceChosen = false;
    }

    @Override
    public void actSettings(boolean rb_changer, boolean checked,  int count) {
        boolean openDistanceFun = false;
        if (countDistance != count){
            countDistance = count;
            firstChangeSeekBar = true;
            openDistanceFun = true;
        } else if ((!rb_changer && !firstChangeSeekBar) || (!rb_changer && mode))
            openDistanceFun = true;

        mode = rb_changer;
        optChecked = checked;

        if (mode)
            showAllPlaces();
        else {
            if (path.size() == 0){
                Toast.makeText(this, "Ошибка: маршрут не построен", Toast.LENGTH_LONG).show();
                return;
            }
            if (places.size() == 0){
                Toast.makeText(this, "Ошибка: мест не найдено", Toast.LENGTH_LONG).show();
                return;
            }

            if (openDistanceFun || newDestination) {
                showPlacesAtDistance(count);
                newDestination = false;
            }
        }
    }

    public void addPlaceClick(MenuItem item) {
        addPlaceFlag = true;
        deletePlaceChosen = false;
        Toast.makeText(this, "Удерживайте палец на карте чтобы добавить новое место",
                Toast.LENGTH_LONG).show();
    }

    private void showAllPlaces(){
        for (Marker marker: places){
            marker.setVisible(true);
        }

    }

    private void showPlacesAtDistance(int maxDistance){
        List<LatLng> list;
        if (optChecked)
            list = simplifiedPath;
        else
            list = path;

        for (Marker marker: places){
            marker.setVisible(false);
            for (LatLng latLng: list) {
                double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), latLng) / 1000;
                if (distance <= maxDistance){
                    marker.setVisible(true);
                }
            }
        }
    }

    @Override
    public void addPlaceText(String name, String description) {
        Marker marker = places.getLast();
        marker.setTitle(name);
        if (description.length() != 0)
            marker.setSnippet(description);
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        if (deletePlaceChosen) {
            marker.remove();
            places.remove(marker);
            //Toast.makeText(this, String.valueOf(places.size()), Toast.LENGTH_LONG).show();
        }
        else{
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            marker.showInfoWindow();
        }
        return true;
    }

    public void deletePlaceClick(MenuItem item) {
        if (!deletePlaceChosen)
            Toast.makeText(this, "Выберите места которые нужно удалить", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Удаление завершено", Toast.LENGTH_LONG).show();

        deletePlaceChosen = !deletePlaceChosen;
    }
}
