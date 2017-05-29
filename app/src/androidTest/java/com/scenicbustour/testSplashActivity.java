package com.scenicbustour;

import android.os.AsyncTask;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.scenicbustour.Models.Route;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by amryousef on 28/05/2017.
 */



@RunWith(AndroidJUnit4.class)
public class testSplashActivity {

    @Rule
    public ActivityTestRule<SplashActivity> splashActivityActivityTestRule = new ActivityTestRule<SplashActivity>(SplashActivity.class);





    @Test
    public void testDataIsInDB(){
        Assert.assertTrue(splashActivityActivityTestRule.getActivity().readDataTask.getStatus().equals(AsyncTask.Status.FINISHED));
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Route> realmResults = realm.where(Route.class).findAll();
        Assert.assertNotNull(realmResults);
        Assert.assertTrue(realmResults.size() != 0);

    }


}
