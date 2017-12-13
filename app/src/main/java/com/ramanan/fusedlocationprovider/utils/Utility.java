package com.ramanan.fusedlocationprovider.utils;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

/**
 * Created by ramanan.g on 13-12-2017.
 */

public class Utility {

    /**
     * ENABLE GPS
     */
    public static boolean isGPSEnabled(Context _context) {
        boolean isGPSAvail = false;
        // LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        if (locationManager != null) {
            isGPSAvail = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPSAvail) {
                // network provider is not enabled
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                _context.startActivity(intent);
            } else {
                isGPSAvail = true;
            }
        }

        return isGPSAvail;
    }
}
