package com.scenicbustour;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.View;

import com.scenicbustour.Models.Route;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class RouteSelectorActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Route> routes;
    RouteSelectorArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_selector);
        setupWindowAnimations();
        routes = new ArrayList<>();
        Realm.init(this);
        adapter = new RouteSelectorArrayAdapter(this,routes);
        recyclerView = (RecyclerView) findViewById(R.id.activity_route_selector_recyclerview);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        View childAt = recyclerView.getChildAt(0);
        if (childAt != null && recyclerView.getChildAdapterPosition(childAt) == 0) {
            childAt.setTranslationY(-childAt.getTop() / 2);// or use view.animate().translateY();
        }
        getRoutes();
    }

    /**
     * Get All the routes stored
     */
    private void getRoutes(){
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Route> realmResults = realm.where(Route.class).findAll();
            for(Route route : realmResults) {
                this.routes.add(route);
                adapter.notifyDataSetChanged();
            }
    }
    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }
}
