package io.vural.vural;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.Fragments.Chats.ChatFragment;
import io.vural.vural.Fragments.Map.MapsActivity;

public class MainActivity extends AppCompatActivity {

    private MapsActivity mainMap;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            // check for messages
            Client.sendGet(getApplicationContext());

            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_chats:
                    selectedFragment = new ChatFragment();
                    break;
                case R.id.navigation_map:
                    selectedFragment = mainMap;
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, selectedFragment);
            transaction.commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init database
        DatabaseHandler.init(this);

        try {
            Client.userId = DatabaseHandler.getUserId();
        } catch (Exception e) {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
            return;
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // open chat fragment on startup
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ChatFragment());
        transaction.commit();

        // create main map object
        mainMap = new MapsActivity();


        // start background service
        startService(new Intent(this, BackgroundService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // init database
        DatabaseHandler.init(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menuSearch:
                Log.i("debug", "search");
                return true;
            default:
                return false;
        }

    }



}


