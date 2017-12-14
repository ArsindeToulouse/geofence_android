package com.arsinde.geofence;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arsinde.geofence.interfaces.GeofenceObserver;
import com.arsinde.geofence.models.GeofenceItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class LocationAlertActivity extends AppCompatActivity
        implements OnMapReadyCallback, GeofenceObserver {

    private static final String TAG = LocationAlertActivity.class.getCanonicalName();

    private GoogleMap mMap;
    private GeofenceItem mGeofenceItem;
    private boolean mRequestingLocationUpdates = false;

    private BroadcastReceiver mBroadcastReceiver;
    private TextView mTvTransitionDetails;
    private TextView mTvLatitude;
    private TextView mTvLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_alert_layout);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setSubtitle("ГЕОЗОНЫ");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);

        mTvTransitionDetails = findViewById(R.id.tv_details);
        mTvLatitude = findViewById(R.id.tv_latitude);
        mTvLongitude = findViewById(R.id.tv_longitude);

        getBroadCast();
        getGeofenceItem();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMinZoomPreference(15);

        showCurrentLocationOnMap();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mGeofenceItem.addLocationAlert(latLng.latitude, latLng.longitude);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.LOCATION_PERMANENT_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCurrentLocationOnMap();
                    Toast.makeText(LocationAlertActivity.this,
                            "Доступ к геоданным получен, Вы можете " +
                                    "добавить или удалить геоточку",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_loc_alert:
                mGeofenceItem.removeLocationAlert();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void getNotification(final String lat, final String lng) {
        String message = "Latitude: " + lat + "; Longitude: " + lng;
        Log.e(TAG, message);
        mTvLatitude.setVisibility(View.VISIBLE);
        mTvLatitude.setText(String.valueOf(lat));
        mTvLongitude.setVisibility(View.VISIBLE);
        mTvLongitude.setText(String.valueOf(lng));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates) {
            mGeofenceItem.startLocationUpdate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mGeofenceItem.stopLocationUpdate();
        mRequestingLocationUpdates = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        mGeofenceItem.removeObserver(this);
    }

    @SuppressLint("MissingPermission")
    private void showCurrentLocationOnMap() {
        if (Permissions.isLocationAccessPermitted(this)) {
            Permissions.requestLocationAccessPermission(this);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getGeofenceItem() {
        mGeofenceItem = new GeofenceItem(this, this);
        mGeofenceItem.addObserver(this);
        mGeofenceItem.getLastDeviceLocation();
        mRequestingLocationUpdates = mGeofenceItem.createLocationRequest();
        mGeofenceItem.getLocationCallback();
    }

    private void getBroadCast() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String transitionInfo = intent.getStringExtra(AppConstants.TRANSITION_TYPE) + ": " +
                        intent.getStringExtra(AppConstants.TRANSITION_DETAILS);

                mTvTransitionDetails.setText(transitionInfo);
            }
        };
        IntentFilter intentFilter = new IntentFilter(AppConstants.BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }
}