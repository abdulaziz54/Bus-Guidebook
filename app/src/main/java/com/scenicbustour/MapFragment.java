package com.scenicbustour;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scenicbustour.Models.BusStop;
import com.scenicbustour.Models.RealmString;
import com.scenicbustour.Models.Route;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MapFragment extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {

    public static final String TAG = "MAP_FRAGMENT";
    private OnFragmentInteractionListener mListener;
    private View rootView;

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
    HashMap<String, BusStop> stopsHashMap;
    Route selectedRoute;

    String routeName;


    public MapFragment() {
        // Required empty public constructor
    }

    public MapFragment withRouteName(String routeName) {
        this.routeName = routeName;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        routes = new ArrayList<>();
        stopsHashMap = new HashMap<>();
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        startTextEdit = (AutoCompleteTextView) rootView.findViewById(R.id.content_main_start_text);
        destinationTextEdit = (AutoCompleteTextView) rootView.findViewById(R.id.content_main_destination_text);

        //Prepare Listener when a route start or end point is selected
        AdapterView.OnItemClickListener routeItemSelected = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                routeSelectedCallback();
            }
        };

        //Listen to selection from start or destination list
        startTextEdit.setOnItemClickListener(routeItemSelected);
        destinationTextEdit.setOnItemClickListener(routeItemSelected);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Realm.init(getContext());
        setupGoogleApiClient();
        getBusStops();

        super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Gets all the bus stops from the database
     */
    private void getBusStops() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Route> realmResults = realm.where(Route.class).findAll();
        for (Route route : realmResults) {
            if (route.getName().equalsIgnoreCase(routeName)) {
                this.routes.add(route);
                selectedRoute = route;
                prepareAutoComplete(route.getStops());
                break;
            }
        }
    }

    /**
     * Prepares the autocomplete suggestions by get an array of all the names of the bus stops
     * @param stops
     */
    private void prepareAutoComplete(RealmList<BusStop> stops) {
        String[] stopsNames = new String[stops.size()];

        for (int x = 0; x < stops.size(); x++) {
            stopsHashMap.put(stops.get(x).getName(), stops.get(x));
            stopsNames[x] = stops.get(x).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getContext(), android.R.layout.simple_list_item_1, stopsNames);
        startTextEdit.setAdapter(adapter);
        destinationTextEdit.setAdapter(adapter);
    }


    /**
     * Handles when an Item is selected
     */
    private void routeSelectedCallback() {
        if (stopsHashMap.get(startTextEdit.getText().toString()) != null && stopsHashMap.get(destinationTextEdit.getText().toString()) != null) {
            //Get Start bus stop and destination bus stop
            BusStop start = stopsHashMap.get(startTextEdit.getText().toString());
            BusStop destination = stopsHashMap.get(destinationTextEdit.getText().toString());

            //Get the index of those bus stops from the list of the stops
            int startIndex = selectedRoute.getStops().indexOf(start);
            int destinationIndex = selectedRoute.getStops().indexOf(destination);

            //Get the full route of the user
            ArrayList<BusStop> fullRoute;
            if (startIndex < destinationIndex) {
                fullRoute = getForwardDirectionStops(startIndex, destinationIndex);
            } else {
                fullRoute = getReturnDirectionStops(startIndex, destinationIndex);
            }

            //Add all the bus stops the user will pass by on the mao
            addMarkersToMap(fullRoute);

        }
    }

    /**
     * Takes the full route of the user and adds markers on the map
     * @param fullRoute
     */
    private void addMarkersToMap(ArrayList<BusStop> fullRoute) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int index = 0;
        BitmapDescriptor icon;
        for (BusStop stop : fullRoute) {
            if (index == 0)
                icon = BitmapDescriptorFactory.fromResource(R.drawable.start_icon);
            else if (index == fullRoute.size() - 1)
                icon = BitmapDescriptorFactory.fromResource(R.drawable.finish_icon);
            else
                icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop);
            StringBuilder times = new StringBuilder();
            for(RealmString time : stop.getTimes()){
                times.append(time.getValue());
                times.append("     ");
            }
            Marker marker = this.mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(stop.getLatitude(), stop.getLongitude())).snippet(Integer.toString(1))
                            .icon(icon)
                            .title(stop.getName())
                            .snippet(times.toString()));
            builder.include(marker.getPosition());
            index++;

        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        this.mMap.animateCamera(cu);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onStart() {
        mapView.onStart();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        mapView.onStart();
        super.onStop();
    }

    /**
     * Get the list of bus stops when the user is moving in the forward direction
     * @param startIndex
     * @param destinationIndex
     * @return
     */
    private ArrayList<BusStop> getForwardDirectionStops(int startIndex, int destinationIndex) {
        ArrayList<BusStop> stops = new ArrayList<>();
        for (int x = startIndex; x <= destinationIndex; x++) {
            stops.add(selectedRoute.getStops().get(x));
        }
        return stops;
    }

    /**
     * Gets the list of bus stops when the user is moving in the reverse direction
     * @param startIndex
     * @param destinationIndex
     * @return
     */
    private ArrayList<BusStop> getReturnDirectionStops(int startIndex, int destinationIndex) {
        ArrayList<BusStop> stops = new ArrayList<>();
        for (int x = startIndex; x >= destinationIndex; x--) {
            stops.add(selectedRoute.getStops().get(x));
        }
        return stops;
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity() /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
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
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            if (mDefaultLocation == null) {
                mDefaultLocation = new LatLng(54.42362664, -2.400541699);
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
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               requestLocationPermission();
            }
            mMap.setMyLocationEnabled(true);
        }

    }

    private void requestLocationPermission(){
         /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
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
