package com.arsinde.geofence.interfaces;

public interface GeofencePublisher {

    void addObserver(GeofenceObserver activity);
    void removeObserver(GeofenceObserver activity);
    void notifyObserver();
}
