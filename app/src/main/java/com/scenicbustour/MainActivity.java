package com.scenicbustour;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scenicbustour.Models.BusStop;
import com.scenicbustour.Models.Route;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import amr.com.google.places.NearbySearchListener;
import amr.com.google.places.Place;
import amr.com.google.places.PlacesWrapper;
import amr.com.routing.RouteException;
import amr.com.routing.RoutingListener;
import dmax.dialog.SpotsDialog;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    Fragment currentFragment;
    private String routeName;
    List<Place> places;
    BottomNavigationView bottomNavigationView;
    private GoogleApiClient mGoogleApiClient;

    private ArrayList<BusStop> selectedRoute;

    private Place selectedPlace;

    public Place getSelectedPlace() {
        return selectedPlace;
    }

    public void setSelectedPlace(Place selectedPlace) {
        this.selectedPlace = selectedPlace;
    }

    public void setSelectedRoute(ArrayList<BusStop> selectedRoute) {
        this.selectedRoute = selectedRoute;
    }

    public ArrayList<BusStop> getSelectedRoute() {
        return selectedRoute;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        places = new ArrayList<>();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color. colorPrimaryDark));
        routeName = getIntent().getExtras().getString("Route");
        Realm.init(this);
        setupGoogleApiClient();
        prepareUIElements(savedInstanceState);

    }

    public void setPlaces(List<Place> places){
        this.places = places;
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



    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
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

    /**
     * Prepares the UI elements and gets its reference from the xml files
     * @param savedInstanceState
     */
    private void prepareUIElements(Bundle savedInstanceState) {
        setContentView(R.layout.app_bar_main);
        setupWindowAnimations();
        MapFragment mapFragment =  new MapFragment().withRouteName(routeName);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame
                ,mapFragment,
                MapFragment.TAG).commit();
        currentFragment = mapFragment;
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getTitle().toString().equalsIgnoreCase("Map")){
                    MapFragment mapFragment =  new MapFragment().withRouteName(routeName).withSelectedPlace(selectedPlace).withSelectedRoute(selectedRoute);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame
                            ,mapFragment,
                            MapFragment.TAG).commit();
                    currentFragment = mapFragment;
                }else if(item.getTitle().toString().equalsIgnoreCase("Places")){
                    PlacesListFragment placesListFragment = new PlacesListFragment().withPlacesList(places);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame
                            ,placesListFragment,
                            PlacesListFragment.TAG).commit();
                    currentFragment = placesListFragment;
                }
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SearchActivity.REQUEST_CODE && resultCode == RESULT_OK){
            if(currentFragment != null){
                currentFragment.onActivityResult(requestCode,resultCode,data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFragmentInteraction(Place place) {
        this.selectedPlace = place;
        MapFragment mapFragment =  new MapFragment().withRouteName(routeName).withSelectedPlace(selectedPlace).withSelectedRoute(selectedRoute);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame
                ,mapFragment,
                MapFragment.TAG).commit();
        currentFragment = mapFragment;
    }





    @Override
    public void onPause() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                }
            });
        }
        super.onPause();
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
}
