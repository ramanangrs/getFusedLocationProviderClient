package com.ramanan.fusedlocationprovider.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private static final long FASTEST_INTERVAL = 2000; /* 2 sec */
    private static final float mGPSAccuracyLevel = 5f;
    public static final String MY_LOCATION = "MY_CURRENT_LOCATION";
    public static final String INTENT_LOCATION_VALUE = "currentLocation";
    private Intent broadcastIntent;
    private int recordCountStatus = 0;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    public static final String GPS_NOT_ENABLED = "GPS_NOT_ENABLED";

    public LocationService() {
    }

    @Override
    public void onCreate() {    // initialize only once
        super.onCreate();
        broadcastIntent = new Intent(MY_LOCATION);
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        mSettingsClient = LocationServices.getSettingsClient(this);
        startLocationUpdates();

        // getLastLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Context.bindService() to obtain a persistent connection to a service.  does not call onStartCommand(). The client will receive the IBinder object that the service returns from its onBind(Intent) method,
        //The service will remain running as long as the connection is established
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //onStartCommand() can get called multiple times.
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
        //Service is not restarted , START_NOT_STICKY : if this service's process is killed while it is started (after returning from onStartCommand(Intent, int, int)), and there are no new start intents to deliver to it, then take the service out of the started state and don't recreate until a future explicit call to Context.startService(Intent).
        // Service is restarted if it gets terminated , START_STICKY or START_REDELIVER_INTENT are used for services that should only remain running while processing any commands sent to them.
        // if this service's process is killed while it is started (after returning from onStartCommand(Intent, int, int)), then leave it in the started state but don't retain this delivered intent.
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        createLocationRequest();
        buildLocationSettingsRequest();

        //settingsClient.checkLocationSettings(builder.build());
        //or
        // the Task object that validates the location settings
        //Check whether current location settings are satisfied:
        Task<LocationSettingsResponse> task = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                initializeLocationSettings(mLocationRequest);
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        broadcastIntent.putExtra(GPS_NOT_ENABLED, mLocationRequest);
                        sendBroadcast(broadcastIntent);
                        Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        break;

                }
                 /*if (e instanceof ResolvableApiException) {
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(this, 1);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }*/
            }
        });
    }

    private void createLocationRequest() {
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        /*
        * PRIORITIES
        * PRIORITY_BALANCED_POWER_ACCURACY -
        * PRIORITY_HIGH_ACCURACY -
        * PRIORITY_LOW_POWER -
        * PRIORITY_NO_POWER -
        * */
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private void initializeLocationSettings(LocationRequest mLocationRequest) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
       // getLastLocation();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    //Last known location
    /*
    The getLastLocation() method returns a Task that you can use to get a Location object with the latitude and longitude coordinates of a geographic location.

    The location object may be null in the following situations:
    Location is turned off in the device settings. The result could be null even if the last location was previously retrieved because disabling location also clears the cache.

    The device never recorded its location, which could be the case of a new device or a device that has been restored to factory settings.
    Google Play services on the device has restarted, and there is no active Fused Location Provider client that has requested location after the services restarted. To avoid this situation you can create a new client and request location updates yourself.
    */

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        //FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get Last Known Location");
                        e.printStackTrace();
                    }
                });
    }

    //register for location updates with onLocationChanged:
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("handleNewLocation", msg);

        double mAccuracy = location.getAccuracy(); // Get Accuracy
        if (mAccuracy < mGPSAccuracyLevel) {    // Accuracy reached  < 5f. stop the location updates
            if (recordCountStatus == 0) {    // prevent multiple calls
                recordCountStatus += 1;
                stopLocationUpdates();
            }
        }
        broadcastIntent.putExtra(INTENT_LOCATION_VALUE, location);
        sendBroadcast(broadcastIntent);
    }

    protected void stopLocationUpdates() {
        //stop location updates when  is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
