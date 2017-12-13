# getFusedLocationProviderClient

Retrieving Location with LocationServices API

Android Service that run in background and receive location updates on broadcast receiver.

Get current location every few minutes interval

    private final static long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    
The Fused Location Provider Client API is a Google Play Services API that wraps the GPS location. 

we can use for : 

    Register for location  
    
    Connect to the location sensor
    
    Register for updates / accuracy changes
    
    Get last known location
    
Installation

Fused Location Provider Client requires the use of the Google Play SDK. You must include the library in your app/build.gradle file:

dependencies {

    implementation 'com.google.android.gms:play-services-location:11.6.2'
    
}

Add the following permissions to the AndroidManifest.xml

uses-permission android:name="android.permission.INTERNET"

uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"

uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"

uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"

If you want to access the GPS sensor, you need the ACCESS_FINE_LOCATION permission. Otherwise you need the ACCESS_COARSE_LOCATION permission
