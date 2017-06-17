package com.scenicbustour.Models;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by amryousef on 28/05/2017.
 */

public class Route extends RealmObject {

    String name;
    RealmList<BusStop> stops;



    public Route(){
        stops = new RealmList<BusStop>();
    }

    public Route withName(String name){
        this.name = name;
        return this;
    }

    public void addStop(BusStop busStop){
        stops.add(busStop);
    }

    public void removeStop(BusStop busStop){
        stops.remove(busStop);
    }

    public RealmList<BusStop> getStops() {
        return stops;
    }

    public String getName() {
        return name;
    }
}
