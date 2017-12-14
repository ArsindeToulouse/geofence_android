package com.arsinde.geofence.interfaces;

public interface GeofenceObserver {
    void getNotification(final String latitude, final String longitude);
}
