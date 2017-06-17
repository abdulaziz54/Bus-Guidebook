package com.scenicbustour;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.scenicbustour.Models.BusStop;
import com.scenicbustour.Models.Route;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private boolean mLocationPermissionGranted;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private MapView mapView;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private LatLng mDefaultLocation;

    AutoCompleteTextView startTextEdit;
    AutoCompleteTextView destinationTextEdit;

    ArrayList<Route> routes;
    HashMap<String,Route> routeHashMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color. colorPrimaryDark));
        routes = new ArrayList<>();
        routeHashMap = new HashMap<>();
        Realm.init(this);
        prepareUIElements(savedInstanceState);
        setupGoogleApiClient();
        getBusStops();

    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

    /**
     * Gets all the bus stops from the database
     */
    private void getBusStops() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Route> realmResults = realm.where(Route.class).findAll();
        for(Route route : realmResults){
            this.routes.add(route);
            prepareAutoComplete(route.getStops());
        }
    }

    /**
     * Prepares the autocomplete suggestions by get an array of all the names of the bus stops
     * @param stops
     */
    private void prepareAutoComplete(RealmList<BusStop> stops) {
        String[] stopsNames = new String[stops.size()];

        for(int x = 0;x<stops.size();x++){
            stopsNames[x] = stops.get(x).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_list_item_1,stopsNames);
        startTextEdit.setAdapter(adapter);
        destinationTextEdit.setAdapter(adapter);
    }

    /**
     * Prepares the UI elements and gets its reference from the xml files
     * @param savedInstanceState
     */
    private void prepareUIElements(Bundle savedInstanceState) {
        setContentView(R.layout.app_bar_main);
        setupWindowAnimations();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        startTextEdit = (AutoCompleteTextView) findViewById(R.id.content_main_start_text);
        destinationTextEdit = (AutoCompleteTextView) findViewById(R.id.content_main_destination_text);

        //Prepare Listener when a route start or end point is selected
        AdapterView.OnItemSelectedListener routeItemSelected =  new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                routeSelectedCallback();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        //Listen to selection from start or destination list
        startTextEdit.setOnItemSelectedListener(routeItemSelected);
        destinationTextEdit.setOnItemSelectedListener(routeItemSelected);


    }

    /**
     * Handles when an Item is selected
     */
    private void routeSelectedCallback() {
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void getDeviceLocation() {
    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                updateLocationUI();
                return;
            }
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), 14.0f));
        } else {
            Log.d("Main", "Current location is null. Using defaults.");
            if(mDefaultLocation == null){
                mDefaultLocation = new LatLng(54.42362664,-2.400541699);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 14.0f));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }
}
