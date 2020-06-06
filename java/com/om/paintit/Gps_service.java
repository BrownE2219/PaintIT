package com.om.paintit;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Gps_service extends Service {

    FirebaseAuth firebaseAuth;
    LocationManager locationManager;
    LocationListener locationListener;
    FirebaseDatabase database;
    DatabaseReference ref;
    String Uid;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //making foreground
    @RequiresApi(Build.VERSION_CODES.O)
    public void startMyownForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.om.paintit";
        String channelName = "Backgrould Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(android.R.color.black);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        Uid = firebaseAuth.getCurrentUser().getUid();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyownForeground();
        else{
            startForeground(1,new Notification());
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("location: ", location.toString());
                data(location.getLatitude(),location.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startService(intent);
            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Only when location changes after 2Km or each 30 min
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2000,locationListener);
        }
    }


    // sending data to firebaseDatabase
    public void data(double lattitue, double longitude) {
        ref.child(Uid).child("location").child("lattitude").setValue(lattitue);
        ref.child(Uid).child("location").child("longitude").setValue(longitude);
    }



    //Destroy command to run again the service

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this,Restart_service.class);
        this.sendBroadcast(broadcastIntent);
    }
}
