package io.vural.vural.Fragments.Map;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.Fragments.Chats.AddUserToChat;
import io.vural.vural.NewMessage.CreateMessageChat;
import io.vural.vural.NewMessage.CreateMessageData;
import io.vural.vural.NewMessage.CreateMessageLocation;
import io.vural.vural.R;
import io.vural.vural.ViewerActivity;

import static android.content.Context.MODE_PRIVATE;

public class MapsActivity extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraMoveListener {

    MapView mMapView;
    private GoogleMap googleMap;
    private EditText locationIn;
    private Button searchBtn;
    private FloatingActionButton newMessageBtn;
    LatLng selectedLocation;
    ArrayList<Message> messageList;
    private Marker newMessageMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;

        // set listeners for map clicks
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getTitle().equals("Send Message To This Location")){
                   return false;
                }

                // write to shared prefrences the nearest message
                for(int i = 0; i < messageList.size(); i++){
                    if(marker.getPosition().latitude == messageList.get(i).getLat()){
                        writeToSharedPrefrences(messageList.get(i));
                    }
                }
                Intent unity = new Intent(getContext(), ViewerActivity.class);
                startActivity(unity);
                return false;
            }
        });

        // set my location enabled to on if user gave permissions
        turnOnMyLocation();

        locationIn = (EditText) getView().findViewById(R.id.locationIn);
        searchBtn = (Button) getView().findViewById(R.id.searchBtn);
        newMessageBtn = (FloatingActionButton) getView().findViewById(R.id.newMessageBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = locationIn.getText().toString();
                locationIn.setText("");

                List<Address> addressList = null;

                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(getContext());
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(addressList.size() > 0) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        locationSelected(latLng);
                    }
                }
            }
        });
        newMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent nextStep = null;
                if(selectedLocation == null) {
                    // check if chat selected
                    if(getActivity().getIntent().hasExtra("chat_id")) {
                        // no location selected so show toast with error
                        Toast.makeText(getActivity(), "Select Location", Toast.LENGTH_LONG).show();
                    }else{
                        // Go to choose chat
                        nextStep = new Intent(getContext(), CreateMessageChat.class);
                    }
                }else{
                    // check if chat was chosen
                    if(getActivity().getIntent().hasExtra("chat_id")) {
                        // Go to create message data
                        nextStep = new Intent(getContext(), CreateMessageData.class);
                        // Attach chat id
                        nextStep.putExtra("chat_id", getActivity().getIntent().getExtras().getString("chat_id"));
                    }else{
                        // Go to choose chat
                        nextStep = new Intent(getContext(), CreateMessageChat.class);
                    }
                    // Attach location
                    nextStep.putExtra("location", selectedLocation);
                }
                
                if(nextStep != null) {
                    startActivity(nextStep);
                }
            }
        });

        // get message list
        String chatId = getActivity().getIntent().getStringExtra("chat_id");

        // re-init database when returning from ar viewer
        if(DatabaseHandler.database == null){
            DatabaseHandler.init(getContext());
        }

        if(chatId != null) {
            Log.d("chat_id", chatId);
            messageList = DatabaseHandler.getMessagesForChatId(chatId);
        }else{
            // if it is the main map show all messages
            messageList = DatabaseHandler.getAllMessages();
        }
        addMessagesToMap();
    }

    public void writeToSharedPrefrences(Message message) {
        String sharedPreferenceName = getActivity().getPackageName() + ".v2.playerprefs";
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(sharedPreferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("message_text", message.getMessageData());
        editor.putString("model_id", message.getModelId());
        editor.putString("message_lat", String.valueOf(message.getLat()));
        editor.putString("message_lng", String.valueOf(message.getLng()));
        editor.apply();
    }

    public void addMessagesToMap(){
        for(int i = 0; i < messageList.size(); i++){
            Message message = messageList.get(i);
            googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).position(new LatLng(message.getLat(), message.getLng())).title(message.getMessageData()).snippet("From: " + message.getSrcPhoneNumber() + " Time:" + message.getTimeStamp()));
        }
    }



    public void turnOnMyLocation(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission denied, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Permission Granted
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            turnOnMyLocation();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        locationDeselected();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        locationSelected(latLng);
    }

    @Override
    public void onCameraMove() {
        locationDeselected();
    }

    public void locationSelected(LatLng latLng){
        selectedLocation = latLng;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
        newMessageBtn.setImageResource(android.R.drawable.ic_menu_send);
        newMessageMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Send Message To This Location"));

    }

    public void locationDeselected(){
        selectedLocation = null;
        if(newMessageMarker != null){
            newMessageMarker.remove();
        }

        newMessageBtn.setImageResource(android.R.drawable.sym_action_email);
    }
}
