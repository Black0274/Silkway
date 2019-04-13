package com.example.dimas.maps;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.Polyline;
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
    public static final String APP_PREFERENCES = "SilkwaySettings";
    public static final String APP_PREFERENCES_COUNT_CLICK = "countClick";
    public static final String APP_PREFERENCES_COUNT_DISTANCE = "countDistance";
    public static final String APP_PREFERENCES_MODE = "mode";
    public static final String APP_PREFERENCES_OPT_CHECKED = "optChecked";
    //public static final String APP_PREFERENCES_FIRST_CHANGE_SEEK_BAR = "firstChangeSeekBar";
    public static final String APP_PREFERENCES_NEW_DESTINATION = "newDestination";
    public static final String APP_PREFERENCES_CAMERA_LAT = "cameraLat";
    public static final String APP_PREFERENCES_CAMERA_LON = "cameraLon";
    public static final String APP_PREFERENCES_ZOOM = "zoom";
    public static final String APP_PREFERENCES_PATH = "path";
    public static final String APP_PREFERENCES_SIMPLIFIED_PATH = "simplifiedPath";
    public static final String APP_PREFERENCES_PATH_MARKERS = "pathMarkers";
    public static final String APP_PREFERENCES_PLACES_STR = "placesStr";
    private SharedPreferences appSettings;

    private String apiKey;
    private android.support.v7.widget.Toolbar toolbar;
    //private LatLng source;
    //private LatLng destination;
    //private Menu menu;

    private int tolerance = 5000;
    private int countClick;
    private int countDistance;
    private boolean mode;   // true - все места
    private boolean addPlaceFlag;
    private boolean optChecked;
    //private boolean firstChangeSeekBar;
    private boolean newDestination;
    private boolean deletePlaceChosen;
    private double cameraPositionLat;
    private double cameraPositionLon;
    private float zoom;
    private Polyline polylinePath;
    private Polyline polylineSimplifiedPath;
    private List<LatLng> path;
    private LinkedList<Marker> places;
    private LinkedList<MarkerOptions> placesOptions;
    private List<LatLng> simplifiedPath;
    private List<LatLng> pathMarkers;
    private LinkedList<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("Silkway");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initVariables();

        if (!isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION))
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initVariables(){
        mode = true;
        addPlaceFlag = false;
        optChecked = false;
        //firstChangeSeekBar = false;
        newDestination = false;
        deletePlaceChosen = false;
        countClick = 0;
        countDistance = 100;
        cameraPositionLat = 49.685985;
        cameraPositionLon = 33.907485;
        zoom = 3;

        path = new ArrayList<>();
        places = new LinkedList<>();
        simplifiedPath = new ArrayList<>();
        pathMarkers = new ArrayList<>();
        polylines = new LinkedList<>();
        placesOptions = new LinkedList<>();

        apiKey = getString(R.string.google_maps_key);
        appSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        LatLng startPos = new LatLng(cameraPositionLat,cameraPositionLon);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, zoom));

        if (appSettings.contains(APP_PREFERENCES_PATH_MARKERS)){
            for (LatLng latLng: pathMarkers){
                mMap.addMarker(new MarkerOptions().position(latLng));
            }
        }

        if (appSettings.contains(APP_PREFERENCES_PATH)){
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(6);
            polylines.add(mMap.addPolyline(opts));
        }

        if (appSettings.contains(APP_PREFERENCES_SIMPLIFIED_PATH)){
            PolylineOptions optsSimplifiedPathPart = new PolylineOptions()
                    .addAll(simplifiedPath)
                    .color(Color.MAGENTA);
            polylines.add(mMap.addPolyline(optsSimplifiedPathPart));
        }

        if (appSettings.contains(APP_PREFERENCES_PLACES_STR)){
            for (MarkerOptions markerOptions: placesOptions){
                Marker marker = mMap.addMarker(markerOptions);
                places.add(marker);

            }
        }
        
        //Toast.makeText(this, "START", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
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
        pathMarkers.add(latLng);

        ArrayList<LatLng> pathPart = new ArrayList<>();

        if (countClick == 1) {
            mMap.addMarker(new MarkerOptions().position(pathMarkers.get(pathMarkers.size() - 1)));
            path.add(latLng);
            //simplifiedPath.add(latLng);
        } else {
            LatLng source = pathMarkers.get(pathMarkers.size() - 2);
            LatLng destination = pathMarkers.get(pathMarkers.size() - 1);

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

            polylinePath = mMap.addPolyline(new PolylineOptions().addAll(pathPart).color(Color.BLUE).width(6));
            polylines.add(polylinePath);

            if (optChecked){
                List<LatLng> simplifiedPathPart = PolyUtil.simplify(pathPart, tolerance);
                polylineSimplifiedPath = mMap.addPolyline(new PolylineOptions()
                        .addAll(simplifiedPathPart)
                        .color(Color.MAGENTA));
                simplifiedPath.addAll(simplifiedPathPart);
                polylines.add(polylineSimplifiedPath);
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
        mode = rb_changer;
        optChecked = checked;
        countDistance = count;
        addPlaceFlag = false;
        deletePlaceChosen = false;

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

            showPlacesAtDistance(count);
            newDestination = false;
        }
    }

    public void addPlaceClick(MenuItem item) {
        deletePlaceChosen = false;
        if (!addPlaceFlag) {
            Toast.makeText(this, "Удерживайте палец на карте чтобы добавить новое место",
                    Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, "Добавление объектов завершено",
                    Toast.LENGTH_LONG).show();
        addPlaceFlag = !addPlaceFlag;
    }

    private void showAllPlaces(){
        for (Marker marker: places){
            marker.setVisible(true);
        }

    }

    private void showPlacesAtDistance(int maxDistance){
        List<LatLng> list;
        boolean flag = false;
        if (optChecked) {
            list = simplifiedPath;
            list.add(path.get(0));
            flag = true;
        }
        else
            list = path;

        for (Marker marker: places){
            marker.setVisible(false);
            for (LatLng latLng: list) {
                double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), latLng) / 1000;
                if (distance <= maxDistance)
                    marker.setVisible(true);
            }
        }

        if (flag)
            list.remove(path.get(0));
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
            places.remove(marker);
            pathMarkers.remove(marker.getPosition());
            marker.remove();

            if (pathMarkers.isEmpty() && !path.isEmpty()){
                removePolyline();
            }
        }
        else{
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            marker.showInfoWindow();
        }
        return true;
    }

    private void removePolyline(){
        for (Polyline polyline: polylines)
            polyline.remove();

        polylines.clear();
        path.clear();
        simplifiedPath.clear();
        countClick = 0;
        newDestination = false;
        mode = true;
        if (places.size() != 0) {
            deletePlaceChosen = false;
            Toast.makeText(this, "Удаление завершено", Toast.LENGTH_LONG).show();
        }
        showAllPlaces();
    }

    public void deletePlaceClick(MenuItem item) {
        if (!deletePlaceChosen)
            Toast.makeText(this, "Выберите места которые нужно удалить", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Удаление завершено", Toast.LENGTH_LONG).show();

        deletePlaceChosen = !deletePlaceChosen;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isPermissionGranted(ACCESS_FINE_LOCATION_PERMISSION))
            putValues();
    }

    private void putValues(){
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putInt(APP_PREFERENCES_COUNT_CLICK, countClick);
        editor.putInt(APP_PREFERENCES_COUNT_DISTANCE, countDistance);
        editor.putBoolean(APP_PREFERENCES_MODE, mode);
        editor.putBoolean(APP_PREFERENCES_OPT_CHECKED, optChecked);
        //editor.putBoolean(APP_PREFERENCES_FIRST_CHANGE_SEEK_BAR, firstChangeSeekBar);
        editor.putBoolean(APP_PREFERENCES_NEW_DESTINATION, newDestination);
        editor.putFloat(APP_PREFERENCES_CAMERA_LAT, (float) mMap.getCameraPosition().target.latitude);
        editor.putFloat(APP_PREFERENCES_CAMERA_LON, (float) mMap.getCameraPosition().target.longitude);
        editor.putFloat(APP_PREFERENCES_ZOOM, mMap.getCameraPosition().zoom);

        String pathString = PolyUtil.encode(path);
        editor.putString(APP_PREFERENCES_PATH, pathString);

        String simplifiedPathString = PolyUtil.encode(simplifiedPath);
        editor.putString(APP_PREFERENCES_SIMPLIFIED_PATH, simplifiedPathString);

        String pathMarkersString = PolyUtil.encode(pathMarkers);
        editor.putString(APP_PREFERENCES_PATH_MARKERS, pathMarkersString);

        String placesStr = stringParser();
        if (places != null && places.size() > 0) {
            //Toast.makeText(this, placesStr, Toast.LENGTH_LONG).show();
            editor.putString(APP_PREFERENCES_PLACES_STR, placesStr);
        } else
            editor.putString(APP_PREFERENCES_PLACES_STR, null);

        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        countClick = appSettings.getInt(APP_PREFERENCES_COUNT_CLICK, 0);
        countDistance = appSettings.getInt(APP_PREFERENCES_COUNT_DISTANCE, 100);
        mode = appSettings.getBoolean(APP_PREFERENCES_MODE, true);
        optChecked = appSettings.getBoolean(APP_PREFERENCES_OPT_CHECKED, false);
        //firstChangeSeekBar = appSettings.getBoolean(APP_PREFERENCES_FIRST_CHANGE_SEEK_BAR, false);
        newDestination = appSettings.getBoolean(APP_PREFERENCES_NEW_DESTINATION, false);
        deletePlaceChosen = false;
        cameraPositionLat = appSettings.getFloat(APP_PREFERENCES_CAMERA_LAT, (float) 49.685985);
        cameraPositionLon = appSettings.getFloat(APP_PREFERENCES_CAMERA_LON, (float) 33.907485);
        zoom = appSettings.getFloat(APP_PREFERENCES_ZOOM, 3);

        path = PolyUtil.decode(appSettings.getString(APP_PREFERENCES_PATH, ""));
        simplifiedPath = PolyUtil.decode(appSettings.getString(APP_PREFERENCES_SIMPLIFIED_PATH, ""));
        pathMarkers = PolyUtil.decode(appSettings.getString(APP_PREFERENCES_PATH_MARKERS, ""));
        String strPlaces = appSettings.getString(APP_PREFERENCES_PLACES_STR, null);
        placesOptions = new LinkedList<>();
        if (strPlaces != null)
            markerParser(strPlaces);
    }

    private String stringParser(){
        StringBuilder strBuilder = new StringBuilder();
        for (Marker marker: places){
            strBuilder.append(String.valueOf(marker.getPosition().latitude)).append('□');
            strBuilder.append(String.valueOf(marker.getPosition().longitude)).append('□');
            if (marker.isVisible())
                strBuilder.append('▲').append('□');
            else
                strBuilder.append('▼').append('□');
            strBuilder.append(marker.getTitle()).append('□');
            strBuilder.append(marker.getSnippet()).append('■');
        }
        return strBuilder.toString();
    }

    private void markerParser(String strPlaces){
        String[] arrStr = strPlaces.split("■");
        for (String str: arrStr){
            String[] values = str.split("□");

            double lat = Double.valueOf(values[0]);
            double lon = Double.valueOf(values[1]);
            LatLng latLng = new LatLng(lat, lon);

            boolean isVisible = values[2].equals("▲");
            String title = values[3];
            if (title == null || title.equals("") || title.equals("null"))
                title = "";
            String snippet = values[4];

            if (snippet == null || snippet.equals("") || snippet.equals("null")){
                placesOptions.add(new MarkerOptions().position(latLng)
                        .title(title).visible(isVisible)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            } else {
                placesOptions.add(new MarkerOptions().position(latLng)
                        .title(title).snippet(snippet).visible(isVisible)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            }
        }
    }
}
