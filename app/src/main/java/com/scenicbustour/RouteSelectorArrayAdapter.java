package com.scenicbustour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.scenicbustour.Models.Route;

import java.util.ArrayList;

/**
 * Created by amr_f on 17/06/2017.
 */

public class RouteSelectorArrayAdapter extends RecyclerView.Adapter<RouteSelectorArrayAdapter.ViewHolder>{

    AppCompatActivity activity;
    ArrayList<Route> routes;

    public RouteSelectorArrayAdapter(AppCompatActivity activity,ArrayList<Route> routes){
        this.activity = activity;
        this.routes = routes;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.route_list_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final Route route = routes.get(i);
        viewHolder.routeNameTextView.setText(route.getName());
        viewHolder.routeItemParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity,MainActivity.class);
                intent.putExtra("Route",route.getName());
                activity.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView routeNameTextView;
        FrameLayout routeItemParent;

        public ViewHolder(View itemView) {
            super(itemView);
            routeItemParent = (FrameLayout) itemView.findViewById(R.id.route_item_parent);
            routeNameTextView = (TextView) itemView.findViewById(R.id.route_item_name);
        }
    }
}
