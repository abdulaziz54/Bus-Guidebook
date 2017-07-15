package com.scenicbustour.Models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by amryousef on 28/05/2017.
 */

public class BusStop extends RealmObject {

    private RealmList<RealmFloat> latitude;
    private RealmList<RealmFloat> longitude;
    private String name;
    private RealmList<RealmString> times;
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

    public RealmList<RealmString> getTimes() {
        return times;
    }

    /**
     * Returns the nearest time of the stop from timeNow
     * @param timeNow
     * @return
     */
    public String getNearestTime(String timeNow) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        Date now = simpleDateFormat.parse(timeNow);
        int index = 0;
        long difference = 100;
        for(int x = 0; x<times.size();x++){
            RealmString time = times.get(0);
            Date current = simpleDateFormat.parse(time.getValue());
            long diffMinutes = (now.getTime() - current.getTime()) / (60 * 1000) % 60;
            if(diffMinutes < difference ){
                index = x;
            }
            difference = diffMinutes;
        }
        return this.times.get(index).getValue();

    }

    public String getTime(int index){
        return times.get(index).getValue();
    }

    /**
     * add time to list with format hh:ss
     * @param time
     */
    public void addTime(String time){
        if(times == null){
            times = new RealmList<>();
        }
        this.times.add(new RealmString().setValue(time));
    }
}
