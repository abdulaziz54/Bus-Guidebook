package com.scenicbustour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.scenicbustour.Helpers.KdTree;
import com.scenicbustour.Models.BusStop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmList;

public class SearchActivity extends AppCompatActivity {

    AutoCompleteTextView startTextEdit;
    AutoCompleteTextView destinationTextEdit;
    public static final int REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle("Search Bus Stops");
        startTextEdit = (AutoCompleteTextView) findViewById(R.id.content_main_start_text);
        destinationTextEdit = (AutoCompleteTextView) findViewById(R.id.content_main_destination_text);
        //Prepare Listener when a route start or end point is selected
        AdapterView.OnItemClickListener routeItemSelected = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                routeSelectedCallback();
            }
        };

        //Listen to selection from start or destination list
        startTextEdit.setOnItemClickListener(routeItemSelected);
        destinationTextEdit.setOnItemClickListener(routeItemSelected);
        prepareAutoComplete();
    }

    private void prepareAutoComplete() {
        String[] stopsNames = getIntent().getExtras().getStringArray("stops");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, stopsNames);
        startTextEdit.setAdapter(adapter);
        destinationTextEdit.setAdapter(adapter);
    }


    /**
     * Handles when an Item is selected
     */
    private void routeSelectedCallback() {
        if(!startTextEdit.getText().toString().isEmpty() && !destinationTextEdit.getText().toString().isEmpty()) {
            Intent intent = new Intent();
            intent.putExtra("start", startTextEdit.getText().toString());
            intent.putExtra("destination", destinationTextEdit.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
