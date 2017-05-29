package com.scenicbustour.Models;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by amryousef on 28/05/2017.
 */

public class BusStop extends RealmObject {

    private RealmList<RealmFloat> latitude;
    private RealmList<RealmFloat> longitude;
    private String name;

    public BusStop(){
        latitude = new RealmList<>();
        longitude = new RealmList<>();
    }

    /**
     * Sets the name of the bus stop
     * @param name
     * @return
     */
    public BusStop withName(String name){
        this.name = name;
        return this;
    }

    /**
     * Sets the latitude of the Bus Stop
     * @param latitude
     * @return
     */
    public void withLatitude(float latitude){
        this.latitude.add(new RealmFloat().setValue(latitude));
    }

    /**
     * Sets the longitude of the Bus Stop
     * @param longitude
     * @return
     */
    public void withLongitude(float longitude){
        this.longitude.add(new RealmFloat().setValue(longitude));
    }

    /**
     * Returns the latitude of the Bus stop
     * @return
     */
    public float getLatitude() {

        return this.latitude.get(0).getValue();
    }

    /**
     * Returns the longitudes defined for the bus stop
     * @return
     */
    public ArrayList<Float> getLongitudes() {
        ArrayList<Float> result = new ArrayList<>();
        for(RealmFloat longitude : this.longitude){
            result.add(longitude.getValue());
        }
        return result;
    }

    /**
     * Returns the latitudes defined for the Bus stop
     * @return
     */
    public ArrayList<Float>  getLatitudes() {

        ArrayList<Float> result = new ArrayList<>();
        for(RealmFloat lat : this.latitude){
            result.add(lat.getValue());
        }
        return result;
    }

    /**
     * Returns the longitude of the bus stop
     * @return
     */
    public float getLongitude() {
        return this.longitude.get(0).getValue();

    }
    /**
     * Returns the name of the bus stop
     * @return
     */
    public String getName() {
        return name;
    }
}
