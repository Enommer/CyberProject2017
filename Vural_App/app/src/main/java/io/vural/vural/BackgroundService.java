package io.vural.vural;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;

import io.vural.vural.Database.DatabaseHandler;
import io.vural.vural.Fragments.Map.Message;

public class BackgroundService extends Service {

    private LocationManager locationManager = null;

    private class LocationListener implements android.location.LocationListener
    {
        Location lastKnownLocation;

        public LocationListener(String provider)
        {
            Log.e("Location_Services", "Location Listener: " + provider);
            lastKnownLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e("Location_Services", "Location Changed: " + location);
            lastKnownLocation.set(location);
            checkIfCloseToMessage(lastKnownLocation);
            Client.sendGet(getApplicationContext());
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e("Location_Services", "Provider Disabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e("Location_Services", "Provider Enabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e("Location_Services", "Status Changed: " + provider);
        }
    }

    private void checkIfCloseToMessage(Location location) {
        ArrayList<Message> messages = DatabaseHandler.getAllMessages();
        Message closestMessage = null;

        if(messages.size() > 0){
            closestMessage = messages.get(0);
        }


        for(int i = 0; i < messages.size(); i++){
            Message message = messages.get(i);

            double latDelta = location.getLatitude() - message.getLat();
            double lngDelta = location.getLongitude() - message.getLng();

            if(latDelta < 0.0003 && lngDelta < 0.0003){
                sendNotification("You are close to a message from " + message.getSrcPhoneNumber());
                // TODO launch message from click
            }

            double closestMessageCoordsDelta = closestMessage.getLat() - location.getLatitude() + closestMessage.getLng() - location.getLongitude();

            // update closest message
            if(closestMessageCoordsDelta < (latDelta + lngDelta)){
                closestMessage = message;
                writeToSharedPrefrences(closestMessage);
            }

        }
    }

    public void writeToSharedPrefrences(Message message) {
        String sharedPreferenceName = this.getPackageName() + ".v2.playerprefs";
        SharedPreferences sharedPreferences = this.getSharedPreferences(sharedPreferenceName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("message_text", message.getMessageData());
        editor.putString("model_id", message.getModelId());
        editor.putString("message_lat", String.valueOf(message.getLat()));
        editor.putString("message_lng", String.valueOf(message.getLng()));
        editor.apply();
    }

    LocationListener[] locationListeners = new LocationListener[] {new LocationListener(LocationManager.GPS_PROVIDER), new LocationListener(LocationManager.NETWORK_PROVIDER)};

    private void initLocationManager() {
        Log.e("Location_Services", "init location manager");
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e("Location_Services", "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // time change that triggers location changed
        int LOCATION_INTERVAL = 1000;
        // distance change that triggers location changed
        float LOCATION_DISTANCE = 10f;

        initLocationManager();

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListeners[1]);
        } catch (java.lang.SecurityException e) {
            Log.i("Location_Services", "location request failed", e);
        } catch (IllegalArgumentException e) {
            Log.d("Location_Services", "network provider does not exist, " + e.getMessage());
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i("Location_Services", "location request failed", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("Location_Services", "gps provider does not exist " + ex.getMessage());
        }

    }

    public void sendNotification(String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle("Vural");
        builder.setContentText(message);
        builder.setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManagerCompat.from(this).notify(3626, notification);

    }

}
