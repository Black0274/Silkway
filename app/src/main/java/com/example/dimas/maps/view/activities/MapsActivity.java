package com.example.dimas.maps.view.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dimas.maps.R;
import com.example.dimas.maps.rest.type.Place;
import com.example.dimas.maps.rest.type.PlaceType;
import com.example.dimas.maps.service.PlaceService;
import com.example.dimas.maps.service.RouteService;
import com.example.dimas.maps.utils.Utils;
import com.example.dimas.maps.view.windows.AddPlace;
import com.example.dimas.maps.view.windows.AddPlaceWindow;
import com.example.dimas.maps.view.windows.Login;
import com.example.dimas.maps.view.windows.LoginWindow;
import com.example.dimas.maps.view.windows.PlaceWindow;
import com.example.dimas.maps.view.windows.Settings;
import com.example.dimas.maps.view.windows.SettingsWindow;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, Settings, AddPlace, Login {

    private GoogleMap map;
    private String TAG = "MapsProj";
    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private int PERMISSION_CODE = 1;
    private static final String APP_PREFERENCES = "SilkwaySettings";
    private static final String APP_PREFERENCES_COUNT_CLICK = "countClick";
    private static final String APP_PREFERENCES_COUNT_DISTANCE = "countDistance";
    private static final String APP_PREFERENCES_MODE = "mode";
    private static final String APP_PREFERENCES_TYPES = "type";
    private static final String APP_PREFERENCES_INCLUDED_TYPES = "includedTypes";
    private static final String APP_PREFERENCES_NEW_DESTINATION = "newDestination";
    private static final String APP_PREFERENCES_CAMERA_LAT = "cameraLat";
    private static final String APP_PREFERENCES_CAMERA_LON = "cameraLon";
    private static final String APP_PREFERENCES_ZOOM = "zoom";
    private static final String APP_PREFERENCES_PATH = "path";
    private static final String APP_PREFERENCES_SIMPLIFIED_PATH = "simplifiedPath";
    private static final String APP_PREFERENCES_PATH_MARKERS = "pathMarkersCoords";
    private static final String APP_PREFERENCES_TOKEN = "token";
    private static final String APP_PREFERENCES_USERNAME = "username";

    private SharedPreferences appSettings;
    private String apiKey;

    private static String token;
    private static String username;

    public static String getToken() {
        return token;
    }

    public static String getUsername() {
        return username;
    }

    private android.support.v7.widget.Toolbar toolbar;
    private android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    private MenuItem exitItem;

    private static final int tolerance = 5000;
    private int countClick;
    private int countDistance;
    private boolean types;  // false - все типы
    private boolean mode;   // true - все места
    private Set<PlaceType> includedTypes;
    private boolean addPlaceFlag;
    private boolean newDestination;
    private double cameraPositionLat;
    private double cameraPositionLng;
    private float zoom;
    private List<LatLng> path;
    private List<Place> places;
    private List<LatLng> simplifiedPath;
    private List<Marker> pathMarkers;
    // TODO: remove
    private List<LatLng> pathMarkersCoords;
    private List<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("Silkway");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initVariables();

        if (!isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initVariables() {
        mode = true;
        types = false;
        addPlaceFlag = false;
        newDestination = false;
        countClick = 0;
        countDistance = 100;
        includedTypes = new HashSet<>();
        cameraPositionLat = 49.685985;
        cameraPositionLng = 33.907485;
        zoom = 3;

        path = new ArrayList<>();
        places = new ArrayList<>();
        simplifiedPath = new ArrayList<>();
        pathMarkers = new ArrayList<>();
        pathMarkersCoords = new ArrayList<>();
        polylines = new ArrayList<>();

        apiKey = getString(R.string.google_maps_key);
        appSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);

        LatLng startPos = new LatLng(cameraPositionLat, cameraPositionLng);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, zoom));

        if (appSettings.contains(APP_PREFERENCES_PATH_MARKERS)) {
            for (LatLng latLng : pathMarkersCoords) {
                pathMarkers.add(map.addMarker(new MarkerOptions().position(latLng)));
            }
        }

        if (appSettings.contains(APP_PREFERENCES_PATH)) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(6);
            polylines.add(map.addPolyline(opts));
        }

//        if (appSettings.contains(APP_PREFERENCES_SIMPLIFIED_PATH)) {
//            PolylineOptions optsSimplifiedPathPart = new PolylineOptions()
//                    .addAll(simplifiedPath)
//                    .color(Color.MAGENTA);
//            polylines.add(map.addPolyline(optsSimplifiedPathPart));
//        }

        if (isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION)) {
            getPlaces();
        }

        if (!mode && simplifiedPath != null && simplifiedPath.size() > 0) {
            showPlacesAtDistance(countDistance);
        }
        if (types) {
            showPlacesByTypes();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        exitItem = menu.findItem(R.id.item_exit);
        exitItem.setTitle("Выход (" + username + ")");
        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (addPlaceFlag) {
            FragmentManager manager = getSupportFragmentManager();
            AddPlaceWindow addPlace = new AddPlaceWindow();
            addPlace.setLatLng(latLng);
            addPlace.show(manager, "add_place");

            addPlaceFlag = false;
        } else {
            newDestination = true;
            buildRoute(latLng);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!pathMarkers.contains(marker)) {
            for (Place place : places) {
                if (place.getMarker().equals(marker)) {
                    PlaceWindow placeWindow = new PlaceWindow();
                    placeWindow.show(fragmentManager, "place");
                    placeWindow.actPlace(place);
                    return true;
                }
            }
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
//        marker.showInfoWindow();
        return true;
    }

    @Override
    public void addPlaceText(Place place) {
        place.initMarker(map);
        places.add(place);
    }

    public void settingsClick(MenuItem item) {
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.show(fragmentManager, "settings");

        try {
            settingsWindow.actSettings(mode, countDistance, types, includedTypes);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void actSettings(boolean rbChanger, int count, boolean switchChanger, Set<PlaceType> checkedBoxes) {
        mode = rbChanger;
        countDistance = count;
        types = switchChanger;
        includedTypes = checkedBoxes;
        addPlaceFlag = false;

        if (mode) {
            showAllPlaces();
        } else {
            if (path.size() == 0) {
                Toast.makeText(this, "Ошибка: маршрут не построен", Toast.LENGTH_LONG).show();
                return;
            }
            if (places.size() == 0) {
                Toast.makeText(this, "Ошибка: мест не найдено", Toast.LENGTH_LONG).show();
                return;
            }

            showPlacesAtDistance(count);
            newDestination = false;
        }

        if (types) {
            showPlacesByTypes();
        }
    }

    public void loginClick(MenuItem item) {
        token = null;
        deletePath();
        mode = true;
        types = false;
        countDistance = 100;
        includedTypes.clear();
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setCancelable(false);
        loginWindow.show(fragmentManager, "login");
    }

    @Override
    public void addLogin(String token, String username) {
        MapsActivity.token = token;
        MapsActivity.username = username;

        getPlaces();

        showAllPlaces();
        exitItem.setTitle("Выход (" + username + ")");
    }

    public void saveClick(MenuItem item) {
        String response = RouteService.save(path);
        switch (response) {
            case "NOT_AUTHORIZED": {
                Toast.makeText(this, "Вы не авторизованы", Toast.LENGTH_LONG).show();
                return;
            }
            case "SUCCESS": {
                Toast.makeText(this, "Маршрут успешно сохранен", Toast.LENGTH_LONG).show();
                return;
            }
            default: {
                Toast.makeText(this, "Неизвестная ошибка", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void loadClick(MenuItem item) {
        List<LatLng> loaded = RouteService.load();
        if (loaded != null && loaded.size() > 0) {
            countClick = 2;
            deletePath();
            path = loaded;
            simplifiedPath = PolyUtil.simplify(path, tolerance);
            drawPath();

            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            bounds.include(path.get(0)).include(path.get(path.size() - 1));
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 300));
        } else {
            Toast.makeText(this, "Отсутствуют загруженные маршруты", Toast.LENGTH_LONG).show();
        }
    }

    public void deletePlaceClick(MenuItem item) {

        deletePath();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION))
            putValues();
    }

    private void putValues() {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putInt(APP_PREFERENCES_COUNT_CLICK, countClick);
        editor.putInt(APP_PREFERENCES_COUNT_DISTANCE, countDistance);
        editor.putBoolean(APP_PREFERENCES_MODE, mode);
        editor.putBoolean(APP_PREFERENCES_TYPES, types);
        editor.putBoolean(APP_PREFERENCES_NEW_DESTINATION, newDestination);
        editor.putFloat(APP_PREFERENCES_CAMERA_LAT, (float) map.getCameraPosition().target.latitude);
        editor.putFloat(APP_PREFERENCES_CAMERA_LON, (float) map.getCameraPosition().target.longitude);
        editor.putFloat(APP_PREFERENCES_ZOOM, map.getCameraPosition().zoom);

        Set<String> includedTypesString = new HashSet<>();
        for (PlaceType placeType : includedTypes) {
            includedTypesString.add(String.valueOf(placeType.name()));
        }
        editor.putStringSet(APP_PREFERENCES_INCLUDED_TYPES, includedTypesString);

        String pathString = PolyUtil.encode(path);
        editor.putString(APP_PREFERENCES_PATH, pathString);

        String simplifiedPathString = PolyUtil.encode(simplifiedPath);
        editor.putString(APP_PREFERENCES_SIMPLIFIED_PATH, simplifiedPathString);

        String pathMarkersString = PolyUtil.encode(pathMarkersCoords);
        editor.putString(APP_PREFERENCES_PATH_MARKERS, pathMarkersString);

        editor.putString(APP_PREFERENCES_TOKEN, token);
        editor.putString(APP_PREFERENCES_USERNAME, username);

        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        countClick = appSettings.getInt(APP_PREFERENCES_COUNT_CLICK, 0);
        countDistance = appSettings.getInt(APP_PREFERENCES_COUNT_DISTANCE, 100);
        mode = appSettings.getBoolean(APP_PREFERENCES_MODE, true);
        types = appSettings.getBoolean(APP_PREFERENCES_TYPES, false);
        newDestination = appSettings.getBoolean(APP_PREFERENCES_NEW_DESTINATION, false);
        cameraPositionLat = appSettings.getFloat(APP_PREFERENCES_CAMERA_LAT, (float) 49.685985);
        cameraPositionLng = appSettings.getFloat(APP_PREFERENCES_CAMERA_LON, (float) 33.907485);
        zoom = appSettings.getFloat(APP_PREFERENCES_ZOOM, 3);

        path = PolyUtil.decode(appSettings.getString(APP_PREFERENCES_PATH, ""));
        simplifiedPath = PolyUtil.decode(appSettings.getString(APP_PREFERENCES_SIMPLIFIED_PATH, ""));
        pathMarkersCoords = PolyUtil.decode(appSettings.getString(APP_PREFERENCES_PATH_MARKERS, ""));

        includedTypes = new HashSet<>();
        Set<String> includedTypesString = appSettings.getStringSet(APP_PREFERENCES_INCLUDED_TYPES, null);
        if (includedTypesString != null) {
            for (String placeType : includedTypesString) {
                includedTypes.add(PlaceType.valueOf(placeType));
            }
        }


        token = appSettings.getString(APP_PREFERENCES_TOKEN, null);
        username = appSettings.getString(APP_PREFERENCES_USERNAME, null);

        if (isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION)) {
            checkAuthorized();
        }
    }

    private void buildRoute(LatLng latLng) {
        countClick += 1;
        pathMarkersCoords.add(latLng);

        if (countClick == 1) {
            pathMarkers.add(map.addMarker(new MarkerOptions().position(pathMarkersCoords.get(pathMarkersCoords.size() - 1))));
            path.add(latLng);
            //simplifiedPath.add(latLng);
        } else {
            LatLng source = pathMarkersCoords.get(pathMarkersCoords.size() - 2);
            LatLng destination = pathMarkersCoords.get(pathMarkersCoords.size() - 1);

            pathMarkers.add((map.addMarker(new MarkerOptions().position(destination))));

            List<LatLng> pathPart = RouteService.build(
                    source.latitude,
                    source.longitude,
                    destination.latitude,
                    destination.longitude);

            path.addAll(pathPart);
            String log = "Route: " + String.valueOf(path.size());

            Polyline polylinePath = map.addPolyline(new PolylineOptions().addAll(pathPart).color(Color.BLUE).width(6));
            polylines.add(polylinePath);

            List<LatLng> simplifiedPathPart = PolyUtil.simplify(pathPart, tolerance);

//            Polyline polylineSimplifiedPath = map.addPolyline(new PolylineOptions()
//                    .addAll(simplifiedPathPart)
//                    .color(Color.MAGENTA));
//            polylines.add(polylineSimplifiedPath);

            simplifiedPath.addAll(simplifiedPathPart);
            log += "\nSimplified route: " + String.valueOf(simplifiedPath.size());

            Toast.makeText(this, log, Toast.LENGTH_LONG).show();

            if (!mode) {
                showPlacesAtDistance(countDistance);
                newDestination = false;
            }
            if (types) {
                showPlacesByTypes();
            }
        }
    }

    public void addPlaceClick(MenuItem item) {
        if (!addPlaceFlag) {
            Toast.makeText(this, "Удерживайте палец на карте чтобы добавить новое место", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, "Добавление объектов завершено", Toast.LENGTH_LONG).show();
        addPlaceFlag = !addPlaceFlag;
    }

    private void drawPath() {
        LatLng start = path.get(0);
        LatLng finish = path.get(path.size() - 1);

        pathMarkers.add(map.addMarker(new MarkerOptions().position(start)));
        pathMarkers.add(map.addMarker(new MarkerOptions().position(finish)));

        polylines.add(map.addPolyline(new PolylineOptions().addAll(path).color(Color.BLUE).width(6)));
//        polylines.add(map.addPolyline(new PolylineOptions().addAll(simplifiedPath).color(Color.MAGENTA).width(6)));

        pathMarkersCoords.add(start);
        pathMarkersCoords.add(finish);
    }

    private void deletePath() {
        if (!path.isEmpty()) {
            pathMarkersCoords.clear();
            deletePathMarkers();

            for (Polyline polyline : polylines) {
                polyline.remove();
            }

            polylines.clear();
            path.clear();
            simplifiedPath.clear();
            countClick = 0;
            newDestination = false;
            mode = true;

            showAllPlaces();

            if (types) {
                showPlacesByTypes();
            }
        }
    }

    private void deletePathMarkers() {
        for (Marker marker : pathMarkers) {
            marker.remove();
        }
        pathMarkers.clear();
    }

    private void showAllPlaces() {
        for (Place place : places) {
            place.getMarker().setVisible(true);
        }
    }

    private void showPlacesAtDistance(int maxDistance) {
        simplifiedPath.add(path.get(0));

        for (Place place : places) {
            Marker marker = place.getMarker();
            marker.setVisible(false);
            for (LatLng latLng : simplifiedPath) {
                double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), latLng) / 1000;
                if (distance <= maxDistance)
                    marker.setVisible(true);
            }
        }

        simplifiedPath.remove(path.get(0));
    }

    private void showPlacesByTypes() {
        if (includedTypes == null) {
            return;
        }

        if (includedTypes.size() == 0) {
            for (Place place : places) {
                place.getMarker().setVisible(false);
            }
            return;
        }

        for (Place place : places) {
            Marker marker = place.getMarker();
            if (!includedTypes.contains(Utils.convert(place.getType()))) {
                marker.setVisible(false);
            }
        }
    }

    private void checkAuthorized() {
        if (token == null || token.length() == 0) {
            loginClick(null);
        }
    }

    private void getPlaces() {
        places = PlaceService.getAll();

        if (places != null && places.size() > 0) {
            for (Place place : places) {
                place.initMarker(map);
            }
        }
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
}