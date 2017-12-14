package com.arsinde.geofence.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.arsinde.geofence.AppConstants;
import com.arsinde.geofence.interfaces.GeofenceObserver;
import com.arsinde.geofence.interfaces.GeofencePublisher;
import com.arsinde.geofence.LocationAlertIntentService;
import com.arsinde.geofence.Permissions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class GeofenceItem implements GeofencePublisher {

    private static final String TAG = GeofenceItem.class.getCanonicalName();

    private final Context mContext;
    private final Activity mActivity;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private GeofencingClient geofencingClient;
    private final List<GeofenceObserver> mList = new ArrayList<>();
    private String mLatitude;
    private String mLongitude;

    public GeofenceItem(final Context context, final Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        geofencingClient = LocationServices.getGeofencingClient(activity);
    }

    @SuppressLint("MissingPermission")
    public void getLastDeviceLocation() {
        if (Permissions.isLocationAccessPermitted(mContext)) {
            Permissions.requestLocationAccessPermission(mActivity);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.e(TAG, "getLastDeviceLocation is success.");
                            if (location != null) {
                                mLatitude = String.valueOf(location.getLatitude());
                                mLongitude = String.valueOf(location.getLongitude());
                                notifyObserver();
                            }
                        }
                    });
        }
    }

    public void stopLocationUpdate() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdate() {
        Log.e(TAG, "startLocationUpdate is started.");
        if (Permissions.isLocationAccessPermitted(mContext)) {
            Permissions.requestLocationAccessPermission(mActivity);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null);
        }
    }

    public boolean createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(AppConstants.LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(AppConstants.LOCATION_REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return true;
    }

    public void getLocationCallback() {
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e(TAG, "getLocationCallback is onLocationResult.");
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLatitude = String.valueOf(location.getLatitude());
                        mLongitude = String.valueOf(location.getLongitude());
                        notifyObserver();
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
    }

    @SuppressLint("MissingPermission")
    public void addLocationAlert(double lat, double lng){
        if (Permissions.isLocationAccessPermitted(mContext)) {
            Permissions.requestLocationAccessPermission(mActivity);
        } else  {
            String key = ""+lat+"-"+lng;
            Geofence geofence = getGeofence(lat, lng, key);

            geofencingClient.addGeofences(getGeofencingRequest(geofence),
                    getGeofencePendingIntent())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext,
                                        "Геозона была добавлена успешно",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(mContext,
                                        "Невозможно добавить геозону",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void removeLocationAlert(){
        if (Permissions.isLocationAccessPermitted(mContext)) {
            Permissions.requestLocationAccessPermission(mActivity);
        } else {
            geofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(mContext,
                                        "Геозона была успешно удалена",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(mContext,
                                        "Невозможно удалить геозону",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(mActivity, LocationAlertIntentService.class);
        return PendingIntent.getService(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_DWELL |
                        GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .addGeofence(geofence)
                .build();
    }

    private Geofence getGeofence(double lat, double lang, String key) {
        return new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(lat, lang, AppConstants.GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(AppConstants.LOITERING_DELAY_IN_MILLISEC)
                .build();
    }

    @Override
    public void addObserver(GeofenceObserver activity) {
        mList.add(activity);
    }

    @Override
    public void removeObserver(GeofenceObserver activity) {
        mList.remove(activity);
    }

    @Override
    public void notifyObserver() {
        Log.e(TAG, "");
        for (GeofenceObserver observer : mList) {
            observer.getNotification(mLatitude, mLongitude);
        }
    }
}
