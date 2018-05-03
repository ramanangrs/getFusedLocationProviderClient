package com.ramanan.fusedlocationprovider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ramanan.fusedlocationprovider.services.LocationService;
import com.ramanan.fusedlocationprovider.utils.Utility;

public class MainActivity extends AppCompatActivity {
    private LocationReceiver mLocationReceiver;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Activity A_;
    private TextView currentLocation;
    private static final int REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE = 33;

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
        //PERMISSION
        if (Utility.checkLocationPermission(A_)) {
            registerLocationService();
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


    private void registerLocationService() {
        //LOCATION SERVICE
        //Clients can also use Context.bindService() to obtain a persistent connection to a service.
        Intent intent = new Intent(A_, LocationService.class);
        startService(intent);
        //Register BroadcastReceiver to receive event from our location service
        mLocationReceiver = new LocationReceiver();
        registerReceiver(mLocationReceiver, new IntentFilter(LocationService.MY_LOCATION));
        Log.i(TAG, " Registered location broadcast receiver");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted.
                    registerLocationService();
                } else {
                    // permission denied
                    Toast.makeText(this, "You have denied permission to access location", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey(LocationService.GPS_NOT_ENABLED)) {
                    LocationRequest locationRequest = intent.getParcelableExtra(LocationService.GPS_NOT_ENABLED);
                    checkForLocationSettings(locationRequest);
                }
                if (extras.containsKey(LocationService.INTENT_LOCATION_VALUE)) {
                    Location location = intent.getParcelableExtra(LocationService.INTENT_LOCATION_VALUE);
                    handleLocationUpdates(location);
                }
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


    //Check for location settings.
    public void checkForLocationSettings(LocationRequest locationRequest) {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.addLocationRequest(locationRequest);
            SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);

            settingsClient.checkLocationSettings(builder.build())
                    .addOnSuccessListener(A_, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            //Setting is success...
                            Toast.makeText(A_, "Enabled the Location successfully. Now you can start", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Enabled the Location successfully.");
                            registerLocationService();
                        }
                    })
                    .addOnFailureListener(A_, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Show the location prompt");
                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the
                                        // result in onActivityResult().
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(A_, REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE);
                                    } catch (Exception e1) {
                                        Log.i(TAG, "Location not satisfied. Show the location enable dialog Error :", e1);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Log.i(TAG, "Setting change is not available. Try in another device");
                                    Toast.makeText(A_, "Setting change is not available.Try in another device.", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

        } catch (Exception ex) {
            Log.i(TAG, "checkForLocationSettings :", ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes");
                        //PERMISSION
                        if (Utility.checkLocationPermission(A_)) {
                            registerLocationService();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes");
                        break;
                }
                break;
        }
    }
}
