package io.vural.vural.NewMessage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import io.vural.vural.R;

public class CreateMessageLocation extends AppCompatActivity {

    private Bundle bundle;
    private LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_message_location);
    }
}
