package com.ramanan.fusedlocationprovider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.ramanan.fusedlocationprovider.services.LocationService;
import com.ramanan.fusedlocationprovider.utils.Utility;

public class MainActivity extends AppCompatActivity {
    private LocationReceiver mLocationReceiver;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Activity A_;
    private TextView currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        A_ = MainActivity.this;
        currentLocation = findViewById(R.id.currentLocation);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //GPS
        if (Utility.isGPSEnabled(A_)) {
            if (Utility.checkLocationPermission(A_)) {
                Intent intent = new Intent(A_, LocationService.class);
                startService(intent);
                //Register BroadcastReceiver to receive event from our location service
                mLocationReceiver = new LocationReceiver();
                registerReceiver(mLocationReceiver, new IntentFilter(LocationService.MY_LOCATION));
                Log.i(TAG, " GPS and Location Services are started");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationReceiver != null) {
            unregisterReceiver(mLocationReceiver);
        }
        stopService(new Intent(A_, LocationService.class));
        Log.i("onPause", " GPS and Location Services are un-registered");
    }

    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Location location = intent.getParcelableExtra(LocationService.INTENT_LOCATION_VALUE);
                handleLocationUpdates(location);
            }
        }

    }

    private void handleLocationUpdates(Location location) {
        String msg = "Current Location Update : " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("LocationUpdates:", msg);
        currentLocation.setText(msg);
    }
}
