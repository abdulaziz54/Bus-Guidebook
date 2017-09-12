package com.scenicbustour;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import amr.com.google.places.Place;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlacesListFragment extends Fragment {


    public static final String TAG = "PlacesListFragment";
    List<Place> placesList;

    public PlacesListFragment() {
        // Required empty public constructor
    }

    public PlacesListFragment withPlacesList(List<Place> placesList){
        this.placesList = placesList;
        return this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_places_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.places_list);
        if(placesList == null || placesList.size() == 0){

            view.findViewById(R.id.hint_text).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);

        }else {
            view.findViewById(R.id.hint_text).setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            PlacesListAdapter adapter = new PlacesListAdapter(placesList, (MainActivity) getActivity(),(MapFragment.OnFragmentInteractionListener) getActivity());
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setNestedScrollingEnabled(false);
            View childAt = recyclerView.getChildAt(0);
            if (childAt != null && recyclerView.getChildAdapterPosition(childAt) == 0) {
                childAt.setTranslationY(-childAt.getTop() / 2);// or use view.animate().translateY();
            }
            recyclerView.setAdapter(adapter);
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
