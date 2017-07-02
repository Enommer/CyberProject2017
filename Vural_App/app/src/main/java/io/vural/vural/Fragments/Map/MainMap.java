package io.vural.vural.Fragments.Map;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.vural.vural.NewMessage.CreateMessageLocation;
import io.vural.vural.R;


public class MainMap extends MapsActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback{

    FloatingActionButton newMessageBtn;
    LatLng selectedLocation;


    public MainMap() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // set on map long click to this class
//        super.googleMap.setOnMapLongClickListener(this);
//        super.googleMap.setOnMapClickListener(this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newMessageBtn = (FloatingActionButton) getActivity().findViewById(R.id.newMessageBtn);
        newMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createMessage = new Intent(getContext(), CreateMessageLocation.class);
                startActivity(createMessage);
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        super.googleMap.clear();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        Log.d("debug", "onMapReady");

//        super.googleMap.setOnMapClickListener(this);

//        setMapClickListener(this);
//        super.googleMap.setOnMapClickListener();
    }
}
