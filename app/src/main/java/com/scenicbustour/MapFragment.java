package com.scenicbustour;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.scenicbustour.Helpers.KdTree;
import com.scenicbustour.Models.BusStop;
import com.scenicbustour.Models.RealmString;
import com.scenicbustour.Models.Route;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import amr.com.google.places.NearbySearchListener;
import amr.com.google.places.Place;
import amr.com.google.places.PlacesWrapper;
import amr.com.routing.RouteException;
import amr.com.routing.Routing;
import amr.com.routing.RoutingListener;
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
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback,RoutingListener {

    public static final String TAG = "MAP_FRAGMENT";
    private OnFragmentInteractionListener mListener;
    private View rootView;
    private KdTree kdTree;
    private HashMap<KdTree.XYZPoint, BusStop> pointBusStopHashMap;
//    private GoogleApiClient mGoogleApiClient;
    private boolean mLocationPermissionGranted;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private MapView mapView;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private LatLng mDefaultLocation;

    private Marker currentLocationMarker;

    private FusedLocationProviderClient mFusedLocationClient;

    private TextView nearestBusStopTextView;
    private TextView busTimeTextView;


    ArrayList<Route> routes;
    HashMap<String, BusStop> stopsHashMap;
    Route selectedRoute;

    String routeName;
    String[] stopsNames;
    Snackbar snackbar;


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
        pointBusStopHashMap = new HashMap<>();
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SearchActivity.class);
                Bundle bundle = new Bundle();
                bundle.putStringArray("stops",stopsNames);
                intent.putExtras(bundle);
                getActivity().startActivityForResult(intent,SearchActivity.REQUEST_CODE);
            }
        });
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        nearestBusStopTextView = (TextView) rootView.findViewById(R.id.nearest_bus_stop);
        busTimeTextView = (TextView) rootView.findViewById(R.id.time_to_bus);



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            mLocationPermissionGranted = true;
            getDeviceLocation();
        }
        subscribeToLocationUpdate();




        // Inflate the layout for this fragment
        return rootView;
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation(){
        if(mLocationPermissionGranted) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateDeviceLocation(location);


                }
            });
        }
    }

    private void updateDeviceLocation(Location location) {
        List<KdTree.XYZPoint> nearestPoints = new ArrayList<>(kdTree.nearestNeighbourSearch(1,new KdTree.XYZPoint(location.getLatitude(),location.getLongitude())));
        BusStop stop =  null;
        for(BusStop current : selectedRoute.getStops()){
            if(current.getLatitude() == nearestPoints.get(0).getX() && current.getLongitude() == nearestPoints.get(0).getY()) {
                stop = current;
            }


        }
        if(currentLocationMarker == null){
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation);
            currentLocationMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude())).snippet(Integer.toString(1))
                            .icon(icon)
                            .title("Your Location")
                            .snippet("You are here"));
            CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
            mMap.animateCamera(cu);

        }else{
            currentLocationMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
        }


        if(stop != null) {
            nearestBusStopTextView.setText(stop.getName());
            Date now = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
            try {
                busTimeTextView.setText(stop.getNearestTime(simpleDateFormat.format(now)));
            } catch (Exception e) {
                Log.e("time", "can't get time");
            }
        }


    }

    protected void subscribeToLocationUpdate() {
        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        updateDeviceLocation(locationResult.getLastLocation());
                        super.onLocationResult(locationResult);
                    }
                },null);
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Realm.init(getContext());
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

    @Override
    public void onRoutingFailure(RouteException e) {

        if(snackbar == null || !snackbar.isShownOrQueued()) {
            snackbar = Snackbar.make(rootView, "Check Internet Conntection", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<amr.com.routing.Route> route, int shortestRouteIndex) {

        mMap.addPolyline(route.get(shortestRouteIndex).getPolyOptions());
    }

    @Override
    public void onRoutingCancelled() {

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
        List<KdTree.XYZPoint> points = new ArrayList<>();
        stopsNames = new String[stops.size()];

        for (int x = 0; x < stops.size(); x++) {

            KdTree.XYZPoint point  =  new KdTree.XYZPoint(stops.get(x).getLatitude(),stops.get(x).getLongitude());
            points.add(point);
            pointBusStopHashMap.put(point,stops.get(x));
            stopsHashMap.put(stops.get(x).getName(), stops.get(x));
            stopsNames[x] = stops.get(x).getName();
        }

        kdTree = new KdTree(points);

    }


    /**
     * Takes the full route of the user and adds markers on the map
     * @param fullRoute
     */
    private void addMarkersToMap(ArrayList<BusStop> fullRoute) {
        mMap.clear();
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int index = 0;
        Bitmap icon;
        final List<Place> placesList = new ArrayList<>();
        for (final BusStop stop : fullRoute) {
            if (index == 0)
                icon = getBitmap(R.drawable.ic_pin_green);
            else if (index == fullRoute.size() - 1)
                icon = getBitmap(R.drawable.ic_place_pin);
            else
                icon = getBitmap(R.drawable.ic_bus_stop);

            if(index != fullRoute.size()-1){
                Routing mRoute=new Routing.Builder().travelMode().withListener(this).waypoints(
                        (new LatLng(stop.getLatitude(),stop.getLongitude())), (new LatLng(fullRoute.get(index+1).getLatitude(),fullRoute.get(index+1).getLongitude()))).build();
                mRoute.execute();
            }
            final StringBuilder times = new StringBuilder();
            for(RealmString time : stop.getTimes()){
                times.append(time.getValue());
                times.append("     ");
            }
            Marker marker = this.mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(stop.getLatitude(), stop.getLongitude())).snippet(Integer.toString(1))
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .title(stop.getName())
                            .snippet(times.toString()));
            builder.include(marker.getPosition());
            PlacesWrapper placesWrapper = new PlacesWrapper("AIzaSyDO4pqQ98AfnrVclR3j27bQPFo_EelalHk");
            placesWrapper.buildNearbySearch(stop.getLatitude(), stop.getLongitude(), 3000, new NearbySearchListener() {
                @Override
                public void onResultsReady(@NotNull List<Place> places) {

                    for(Place place : places){
                        if(place.getType().contains("locality") || place.getType().contains("political")
                                || place.getType().contains("natural_feature") ) {
                            placesList.add(place);
                            Marker marker = mMap.addMarker(
                                    new MarkerOptions()
                                            .position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()))
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_pin_blue)))
                                            .title(place.getName()));
                            builder.include(marker.getPosition());
                        }
                    }
                }

                @Override
                public void onError(@NotNull Throwable throwable) {

                    Log.e("NearbySearchListener", String.format("Error Getting Place: %s",throwable.getMessage() ));

                }
            }).enqueueSearch();
            index++;

        }
        ((MainActivity) getActivity()).setPlaces(placesList);
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        this.mMap.animateCamera(cu);
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
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



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

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
                    getDeviceLocation();
                }
            }
        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        routeSelectedCallback(data.getStringExtra("start"),data.getStringExtra("destination"));
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Handles when an Item is selected
     */
    private void routeSelectedCallback(String startStop, String destinationStop) {
        if(startStop != null && destinationStop != null){
            //Get Start bus stop and destination bus stop
            BusStop start = stopsHashMap.get(startStop);
            BusStop destination = stopsHashMap.get(destinationStop);

            //Get the index of those bus stops from the list of the stops
            int startIndex = selectedRoute.getStops().indexOf(start);
            int destinationIndex = selectedRoute.getStops().indexOf(destination);

            //Get the full route of the user
            ArrayList<BusStop> fullRoute;
            if(startIndex < destinationIndex){
                fullRoute = getForwardDirectionStops(startIndex,destinationIndex);
            }else{
                fullRoute = getReturnDirectionStops(startIndex,destinationIndex);
            }

            //Add all the bus stops the user will pass by on the mao
            addMarkersToMap(fullRoute);

        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
    }
}
