package io.vural.vural;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.vural.viewer.UnityPlayerActivity;
import io.vural.vural.Database.DatabaseHandler;

public class ViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        Intent unity = new Intent(getApplicationContext(), io.vural.viewer.UnityPlayerNativeActivity.class);
        startActivity(unity);
    }
}
