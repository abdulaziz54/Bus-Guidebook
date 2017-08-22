package com.scenicbustour;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import amr.com.google.places.Place;


public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.ViewHolder>{


    private List<Place> placesList;
    private MainActivity context;

    PlacesListAdapter(List<Place> placeList, MainActivity context){
        this.placesList = placeList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.place_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Place place = placesList.get(position);
        Glide.with(context).load(place.getIconLink()).into(holder.logo);
        holder.title.setText(place.getName());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.showPlaceInMap(placesList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private FrameLayout parent;
        private ImageView logo;
        private TextView title;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.place_name);
            logo = itemView.findViewById(R.id.place_icon);
            parent = itemView.findViewById(R.id.place_item_parent);
            logo.setVisibility(View.INVISIBLE);
        }
    }

}
