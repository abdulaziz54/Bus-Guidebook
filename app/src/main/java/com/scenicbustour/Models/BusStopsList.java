package com.scenicbustour.Models;

import com.google.android.gms.maps.model.LatLng;
import com.scenicbustour.Helpers.KdTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by AmrYousef on 11/09/2017.
 */

public class BusStopsList  {

    private ArrayList<BusStop> busStops;
    private KdTree busStopsTree;
    private HashMap<String,BusStop> busStopsMap;
    private HashMap<KdTree.XYZPoint,BusStop> nodeToStopMap;


    public BusStopsList(){
        busStops = new ArrayList<>();
        busStopsTree = new KdTree();
        busStopsMap = new HashMap<>();
        nodeToStopMap = new HashMap<>();
    }

    public void add(BusStop stop){
        busStops.add(stop);
        busStopsMap.put(stop.getName(),stop);
        busStopsTree.add(new KdTree.XYZPoint(stop.getLatitude(),stop.getLongitude()));
        nodeToStopMap.put(new KdTree.XYZPoint(stop.getLatitude(),stop.getLongitude()),stop);
    }

    public void addAll(List<BusStop> stops){
        for(BusStop stop:stops){
            add(stop);
        }
    }

    public BusStop getStopByName(String name){
        return busStopsMap.get(name);
    }

    public String[] getBusStopsNames(){
        String[] names = new String[busStopsMap.size()];
        return busStopsMap.keySet().toArray(names);
    }

    public BusStop getNearestBusStop(LatLng latLng){
        List<KdTree.XYZPoint> nearestNodes =new ArrayList<>(busStopsTree.nearestNeighbourSearch(1,new KdTree.XYZPoint(latLng.latitude,latLng.longitude)));
        return nodeToStopMap.get(nearestNodes.get(0));
    }
}
