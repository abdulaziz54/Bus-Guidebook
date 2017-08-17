package com.scenicbustour;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Space;

import com.scenicbustour.Models.BusStop;
import com.scenicbustour.Models.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class SplashActivity extends AppCompatActivity {

    ImageView iconImage ;

    ConstraintLayout constraintLayout;
    Route route;
    ReadDataTask readDataTask;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        iconImage = (ImageView) findViewById(R.id.activity_splash_icon);
        constraintLayout = (ConstraintLayout) findViewById(R.id.activity_splash_parent);
        final Realm realm = Realm.getDefaultInstance();

        ScaleAnimation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(2000); // scale to 3 times as big in 3 seconds
        scaleAnimation.setInterpolator(this, android.R.interpolator.accelerate_decelerate);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(realm.where(Route.class).findAll().size() == 0) {
                    readDataTask = new ReadDataTask();
                    readDataTask.execute(SplashActivity.this);
                }else{
                    Intent intent = new Intent(SplashActivity.this,RouteSelectorActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iconImage.startAnimation(scaleAnimation);

    }

    public class ReadDataTask extends AsyncTask<Activity, Void,Activity>{


        @Override
        protected Activity doInBackground(Activity... activities) {
            Realm realm = Realm.getDefaultInstance();
            int fileID = activities[0].getResources().getIdentifier("locations","raw",activities[0].getPackageName());
            InputStream inputStream =activities[0].getResources().openRawResource(fileID);
            byte[] fileBytes = new byte[0];
            try {
                fileBytes = new byte[inputStream.available()];
                inputStream.read(fileBytes);
                JSONArray appJson = new JSONArray(new String(fileBytes));
                for(int x = 0 ; x<appJson.length();x++){
                    JSONObject object = appJson.getJSONObject(x);
                    final Route route = new Route().withName(object.getString("name"));
                    JSONArray stopsJSON = object.getJSONArray("stops");
                    for(int y = 0 ; y< stopsJSON.length() ; y++){
                        JSONObject stop = stopsJSON.getJSONObject(y);
                        BusStop busStop = new BusStop();
                        busStop.withName(stop.getString("name"));
                        JSONArray latitudesJSON =stop.getJSONArray("latitude");
                        for(int z = 0;  z< latitudesJSON.length() ; z++){
                            busStop.withLatitude(Float.parseFloat(String.valueOf(latitudesJSON.getDouble(z))));
                        }
                        JSONArray longitudesJSON =stop.getJSONArray("longitude");
                        for(int z = 0;  z< longitudesJSON.length() ; z++){
                            busStop.withLongitude(Float.parseFloat(String.valueOf(longitudesJSON.getDouble(z))));
                        }
                        JSONArray timesJSON = stop.getJSONArray("time");
                        for(int z=0;z<timesJSON.length();z++){
                            busStop.addTime(timesJSON.getString(z));
                        }

                        route.addStop(busStop);
                    }
                    realm.beginTransaction();
                    Route realmRoute = realm.copyToRealm(route);
                    realm.commitTransaction();


                }

            } catch (Exception e) {
                Log.e("SplashActivity", "Failed to get Bus Stops: "+e.getMessage());
            }

            return activities[0];
        }

        @Override
        protected void onPostExecute(Activity activity) {
            Intent intent = new Intent(activity,RouteSelectorActivity.class);
            startActivity(intent);
            finish();
            super.onPostExecute(activity);
        }
    }
}
