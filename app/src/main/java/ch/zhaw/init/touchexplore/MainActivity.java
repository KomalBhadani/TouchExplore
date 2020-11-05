package ch.zhaw.init.touchexplore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ch.zhaw.init.touchexplore.databinding.ActivityMainBinding;
import ch.zhaw.init.touchexplore.utils.AppLog;
import ch.zhaw.init.touchexplore.utils.AppPref;
import ch.zhaw.init.touchexplore.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.api.geocoding.v5.GeocodingCriteria.TYPE_ADDRESS;
import static com.mapbox.api.geocoding.v5.GeocodingCriteria.TYPE_PLACE;
import static com.mapbox.api.geocoding.v5.GeocodingCriteria.TYPE_POI;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MainActivity extends Base implements OnMapReadyCallback, PermissionsListener, View.OnTouchListener, View.OnClickListener, TextToSpeech.OnInitListener {


    ActivityMainBinding binding;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private static final String TAG = "MainActivity";
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    TextToSpeech textToSpeech;
    Style loadedMapStyle;
    boolean isGpsMode=false;
    MediaPlayer player = new MediaPlayer();
    Vibrator vibrator ;
    UiSettings uiSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        binding=DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.mapView.getMapAsync(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        listeners();
    }

    private void listeners() {
        binding.ivGpsmode.setOnClickListener(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        MainActivity.this.mapboxMap = mapboxMap;
        binding.transparentview.setOnTouchListener(this);

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(8.5391825, 47.3686498)));//Zurich

        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(8.574133368597018, 47.373309648014988)));//Dolder
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(8.540881128030662, 47.366245334324674)));//Buerkliplatz
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(8.552054773306253, 47.35509424431447)));//Chinagarten

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/derz/ckb51bve32el11imvqlp1tb3n")//mapbox://styles/komalbhadani/ckgjed8o70u1719mpdoe7kqv7 //mapbox://styles/mapbox/streets-v8
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                        )
                ), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style mapstyle) {


                loadedMapStyle = mapstyle;

                uiSettings = mapboxMap.getUiSettings();


                List<Layer> layers = loadedMapStyle.getLayers();
                AppLog.e(TAG,"alllayers: "+layers);
                for (int i=0;i<layers.size();i++){
                    AppLog.e(TAG,"alllayers "+layers.get(i).getId()+": "+layers.get(i).getVisibility()+": "+layers.get(i).getMaxZoom()+": "+layers.get(i).getMinZoom() +": "+layers.get(i));
                }

// Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.
                enableLocationComponent(loadedMapStyle);


            }
        });
    }


    private void getfeature(PointF screenPoint, double longitude, double latitude) {


        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint);
        AppLog.e(TAG,"daatatadtadat; "+features);


// Get the first feature within the list if one exist
        if (features.size() > 0) {
            Feature feature = features.get(0);

// Ensure the feature has properties defined
            if (feature.properties() != null) {
                for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
// Log all the properties
                    AppLog.e(TAG, "daatatadtadat: "+String.format("%s = %s", entry.getKey(), entry.getValue()));

                }
                AppLog.e(TAG, "dtypeeeee: " + feature.getProperty("type"));

                if(feature.getProperty("type")!=null) {

                    String type = feature.getProperty("type").getAsString();

                    if(feature.getProperty("name")!=null){
                        String name = feature.getProperty("name").getAsString();

                        playsoundaccordingtype(typeIdentifier(type),type,name);//typeIdentifier(type)
                    }else {
                        playsoundaccordingtype(typeIdentifier(type),type,null);//typeIdentifier(type)

                    }

                }
            }

        }

    }

    private void playsoundaccordingtype(String typeIdentifier, String type, String name) {

        switch (typeIdentifier){
            case Constants.TYPE_BUILDING:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_BUILDING)){
                    appPref.set(AppPref.FEATURE_TYPE,Constants.TYPE_BUILDING);
                    player("door_close.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("door_close.mp3");
                    }
                }
                AppLog.e(TAG,"trackinfo::: "+ player.getTrackInfo());
                break;
            }
            case Constants.TYPE_STREET:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_STREET)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_STREET);
                    player("motorway.mp3");
                    if(!TextUtils.isEmpty(name)){
                        tospeak(name);
                    }
                    //streetVibrationPlayer
                   // customvibration();
                }else {
                    if(!player.isPlaying()){
                        player("motorway.mp3");
                        //streetVibrationPlayer
                       // customvibration();
                        /*if(!TextUtils.isEmpty(name)){
                            tospeak(name);
                        }*/
                    }
                }
                break;
            }
            case Constants.TYPE_FOREST:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_FOREST)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_FOREST);
                    player("forest.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("forest.mp3");
                    }
                }
                break;
            }
            case Constants.TYPE_PLAYGROUND:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_PLAYGROUND)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_PLAYGROUND);
                    player("playground.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("playground.mp3");
                    }
                }
                break;
            }
            case Constants.TYPE_COUNTRY:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_COUNTRY)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_COUNTRY);
                    player("zoom_in.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("zoom_in.mp3");
                    }
                }
                break;
            }
            case Constants.TYPE_RIVER:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_RIVER)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_RIVER);
                    player("river.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("river.mp3");
                    }
                }
                break;
            }

            case Constants.TYPE_GRASS:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_GRASS)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_GRASS);
                    player("park.wav");
                }else {
                    if(!player.isPlaying()){
                        player("park.wav");
                    }
                }
                break;
            }
            case Constants.TYPE_TRAM:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_TRAM)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_TRAM);
                    player("tram.mp3");
                    //trainVibrationPlayer
                    //customvibration();
                }else {
                    if(!player.isPlaying()){
                        player("tram.mp3");
                        //trainVibrationPlayer
                       // customvibration();
                    }
                }
                break;
            }
            case Constants.TYPE_RAIL:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_RAIL)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_RAIL);
                    player("train.mp3");
                    //trainVibrationPlayer
                   // customvibration();
                }else {
                    if(!player.isPlaying()){
                        player("train.mp3");
                        //trainVibrationPlayer
                       // customvibration();
                    }
                }
                break;
            }
            case Constants.TYPE_GOLF:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_GOLF)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_GOLF);
                    player("golf.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("golf.mp3");
                    }
                }

                break;
            }
            case Constants.TYPE_TENNIS:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_TENNIS)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_TENNIS);
                    player("tennis.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("tennis.mp3");
                    }
                }
                break;
            }

            case Constants.TYPE_STADIUM:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_STADIUM)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_STADIUM);
                    player("stadium.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("stadium.mp3");
                    }
                }
                break;
            }
            case Constants.TYPE_FOOTWAY:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_FOOTWAY)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_FOOTWAY);
                    player("gravel.mp3");
                    //streetVibrationPlayer
                   // customvibration();
                    if(type.equalsIgnoreCase("steps")){
                        tospeak("Stairs");
                    }
                }else {
                    if(!player.isPlaying()){
                        player("gravel.mp3");
                        //streetVibrationPlayer
                       // customvibration();
                        if(type.equalsIgnoreCase("steps")){
                            tospeak("Stairs");
                        }
                    }
                }
                break;
            }
            case Constants.TYPE_LAKE:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_LAKE)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_LAKE);
                    player("lake.wav");
                }else {
                    if(!player.isPlaying()){
                        player("lake.wav");
                    }
                }
                break;
            }
            case Constants.TYPE_WATER:{
                if(!appPref.getString(AppPref.FEATURE_TYPE).equalsIgnoreCase(Constants.TYPE_WATER)) {
                    appPref.set(AppPref.FEATURE_TYPE, Constants.TYPE_WATER);
                    player("water.mp3");
                }else {
                    if(!player.isPlaying()){
                        player("water.mp3");
                    }
                }
                break;
            }
            default:
            {
                player.stop();
                vibrator.cancel();
                if(textToSpeech!=null)
                    textToSpeech.stop();
                //stop vibration
                //stop speech
            }
        }
    }

    public void player(String file) {
        AppLog.e(TAG,"filename: "+file);
        try {
            AssetFileDescriptor afd = getApplicationContext().getAssets().openFd(file);
            player.reset();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            } else {
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }


            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String typeIdentifier(String type) {

        for (String item : Constants.LIST_BUILDING) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_BUILDING;
            }
        }

        for (String item : Constants.LIST_STREET) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_STREET;
            }
        }

        for (String item : Constants.LIST_FOREST) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_FOREST;
            }
        }

        for (String item : Constants.LIST_PLAYGROUND) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_PLAYGROUND;
            }
        }

        for (String item : Constants.LIST_COUNTRY) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_COUNTRY;
            }
        }

        for (String item : Constants.LIST_RIVER) {
                if (type.equalsIgnoreCase(item)) {
                    return Constants.TYPE_RIVER;
                }
        }

        for (String item : Constants.LIST_GRASS) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_GRASS;
            }
        }

        for (String item : Constants.LIST_TRAM) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_TRAM;
            }
        }

        for (String item : Constants.LIST_RAIL) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_RAIL;
            }
        }

        for (String item : Constants.LIST_GOLF) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_GOLF;
            }
        }

        for (String item : Constants.LIST_TENNIS) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_TENNIS;
            }
        }

        for (String item : Constants.LIST_STADIUM) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_STADIUM;
            }
        }

        for (String item : Constants.LIST_FOOTWAY) {
            if (type.equalsIgnoreCase(item)) {
                return Constants.TYPE_FOOTWAY;
            }
        }

        return type;
    }



        public  void getAddressFromLocation(final double longitude,final double latitude,  boolean iscurrentlocation) {

                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());


                    String result = null;
                    List<Address> addressList = null;
                    try {

                        addressList = geocoder.getFromLocation(
                                latitude, longitude, 1);
                        if (addressList != null && addressList.size() > 0) {
                            Log.e(TAG,"address:: "+addressList);
                            Address address = addressList.get(0);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                sb.append(address.getAddressLine(i)).append("\n");
                            }

                            sb.append(address.getAddressLine(0)).append("\n");
                            sb.append(address.getLocality()).append("\n");
                            sb.append(address.getPostalCode()).append("\n");
                            sb.append(address.getCountryName());
                            result = sb.toString();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Unable connect to Geocoder", e);
                    } finally {

                        if (result != null) {


                            result = "Latitude: " + latitude + " Longitude: " + longitude +
                                    "\nAddress:\n" + result;
                          //  showToast("address: "+result);
                            Log.e(TAG,"address: "+result);

                            if(!iscurrentlocation){
                                mapInteractionHandler(addressList,result);
                            }
                        } else {

                            result = "Latitude: " + latitude + " Longitude: " + longitude +
                                    "\n Unable to get address for this lat-long.";
                         //   showToast("address: " +result);
                            Log.e(TAG,"address: "+result);

                        }


                    }


        }

    private void mapInteractionHandler( List<Address> addressList, String result) {

    }


    private void tospeak(final String speech) {
        AppLog.e(TAG,"texttospeech");

        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.speak(speech,TextToSpeech.QUEUE_FLUSH, null);

                }
            }
        });
    }

    private void speechzoomlevel(Double zoomLevel) {

        textToSpeech =  new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                  //  textToSpeech.speak(speech,TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }






    private void enableLocationComponent(Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
           locationComponent.setCameraMode(CameraMode.TRACKING);


            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

//get addressfrom location

            final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;

            // Use the map camera target's coordinates to make a reverse geocoding search
            getAddressFromLocation(mapTargetLatLng.getLongitude(),mapTargetLatLng.getLatitude(),true);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onPause() {
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }



/*
    class GestureListener extends GestureDetector.SimpleOnGestureListener implements MapboxMap.OnMapClickListener {
        @Override
        public boolean onDown(MotionEvent e) {
            //showToast("Gesture: Action down");
            Log.e(TAG, "Gesture: Action down");
            mapboxMap.addOnMapClickListener(this);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.e(TAG, "Gesture: Action long press");
           // showToast("Gesture: Action long press");

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.e(TAG, "Gesture: onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            Log.e(TAG, "Gesture: onScroll: ");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            Log.e(TAG, "Gesture: onScroll: ");
            return false;
        }

        @Override
        public boolean onMapClick(@NonNull LatLng point) {
            GeoJsonSource source = loadedMapStyle.getSourceAs(SOURCE_ID);

       */
/* if (source != null) {
            source.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        }*//*


            // Use the map camera target's coordinates to make a reverse geocoding search
            getAddressFromLocation(point.getLatitude(),point.getLongitude());
            return false;
        }
    }
*/

    /**
     * Ontouch event will draw poly line along the touch points
     *
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                Log.e(TAG,"Action down");
                PointF pointf = new PointF();
                pointf.set(event.getX(),event.getY());

                binding.mapView.performClick();

                // Make a geocoding search with the values inputted into the EditTexts
              /*  reverseGeocode(new LatLng(mapboxMap.getProjection().fromScreenLocation(pointf).getLatitude(),
                        mapboxMap.getProjection().fromScreenLocation(pointf).getLongitude()));*/

                getfeature(pointf,mapboxMap.getProjection().fromScreenLocation(pointf).getLongitude(),
                        mapboxMap.getProjection().fromScreenLocation(pointf).getLatitude());
              /*  getAddressFromLocation(mapboxMap.getProjection().fromScreenLocation(pointf).getLongitude(),
                        mapboxMap.getProjection().fromScreenLocation(pointf).getLatitude(),
                        false);*/

                break;

            case MotionEvent.ACTION_MOVE:
                PointF pointfmove = new PointF();
                pointfmove.set(event.getX(),event.getY());

                getfeature(pointfmove,mapboxMap.getProjection().fromScreenLocation(pointfmove).getLongitude(),
                        mapboxMap.getProjection().fromScreenLocation(pointfmove).getLatitude());
                getAddressFromLocation(mapboxMap.getProjection().fromScreenLocation(pointfmove).getLongitude(),
                        mapboxMap.getProjection().fromScreenLocation(pointfmove).getLatitude(),
                         false);

                reverseGeocode(new LatLng(mapboxMap.getProjection().fromScreenLocation(pointfmove).getLatitude(),
                        mapboxMap.getProjection().fromScreenLocation(pointfmove).getLongitude()));

                Log.e(TAG,"Action move");

                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG,"Action up");
                player.stop();

                break;
        }
        return true;
    }

    private void reverseGeocode(final LatLng latLng) {

        try {
// Build a Mapbox geocoding request
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.access_token))
                    .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
                    .geocodingTypes(TYPE_ADDRESS,TYPE_PLACE,TYPE_POI)
                    .mode(GeocodingCriteria.TYPE_PLACE)
                    .build();
            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call,
                                       Response<GeocodingResponse> response) {
                    if (response.body() != null) {
                        AppLog.e(TAG,"response: "+response.body());
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {

// Get the first Feature from the successful geocoding response
                            CarmenFeature feature = results.get(0);
                            Log.e(TAG,"dataaaaa: "+feature.properties().get("category"));
                            Log.e(TAG,"Features: "+feature.type()+" : "+feature);


                        } else {
                            Log.e(TAG,"No result");
                        }
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: " + throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: " + servicesException.toString());
            servicesException.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_gpsmode:{
                if(!isGpsMode){
                    binding.transparentview.setVisibility(View.VISIBLE);
                    isGpsMode=true;
                    uiSettings.setRotateGesturesEnabled(false);
                    binding.ivGpsmode.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.gps_disable));

                }else {
                    binding.transparentview.setVisibility(View.INVISIBLE);
                    isGpsMode=false;
                    uiSettings.setRotateGesturesEnabled(true);
                    binding.ivGpsmode.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.gps));
                }

                break;
            }
        }
    }

    public void customvibration(){
        //long[] pattern = {100,200,100,100,75,25,100,200,100,500,100,200,100,500};

// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrator.vibrate(300);
        }



    }

    @Override
    public void onInit(int status) {

    }

    /*guard CHHapticEngine.capabilitiesForHardware().supportsHaptics else { return }

		do {
        hapticEngine = try CHHapticEngine()
        try self.hapticEngine.start()
    } catch {
        print("There was an error creating the engine: \(error.localizedDescription)")
    }

    let highIntensity = CHHapticEventParameter(parameterID: .hapticIntensity, value: 1)
    let lowIntensity = CHHapticEventParameter(parameterID: .hapticIntensity, value: 0.3)

    let signalVibrationEvent = CHHapticEvent(eventType: .hapticTransient, parameters: [highIntensity], relativeTime: 0)
    let longHighIntensityVibrationEvent = CHHapticEvent(eventType: .hapticContinuous, parameters: [highIntensity], relativeTime: 0, duration: 100)

    let longLowIntensityVibrationEvent = CHHapticEvent(eventType: .hapticContinuous, parameters: [lowIntensity], relativeTime: 0, duration: 100)

    let trainVibrationEvent1 = CHHapticEvent(eventType: .hapticContinuous, parameters: [highIntensity], relativeTime: 0.1, duration: 0.24)


            do {
        let streetPattern = try CHHapticPattern(events: [longHighIntensityVibrationEvent], parameters: [])
        streetVibrationPlayer = try hapticEngine.makeAdvancedPlayer(with: streetPattern)
        streetVibrationPlayer.loopEnabled = true

        let walkPattern = try CHHapticPattern(events: [longLowIntensityVibrationEvent], parameters: [])
        walkVibrationPlayer = try hapticEngine.makeAdvancedPlayer(with: walkPattern)
        walkVibrationPlayer.loopEnabled = true


        let trainPattern = try CHHapticPattern(events: [trainVibrationEvent1], parameters: [])
        trainVibrationPlayer = try hapticEngine.makeAdvancedPlayer(with: trainPattern)
        trainVibrationPlayer.loopEnabled = true

        let signalPattern = try CHHapticPattern(events: [signalVibrationEvent], parameters: [])

        signalVibrationPlayer = try hapticEngine.makeAdvancedPlayer(with: signalPattern)
        signalVibrationPlayer.loopEnabled = false
    } catch {
        print("Failed to run taptic engine")
    }*/
}