package com.arsinde.geofence;

public class AppConstants {

    private AppConstants() {}

    public static final int LOCATION_PERMANENT_REQUEST_CODE = 1;
    public static final int GEOFENCE_RADIUS_IN_METERS = 50;

    public final static String BROADCAST_ACTION = "com.arsinde.geofence";
    public final static String BROADCAST_ACTION_ALERT = "alert";
    public final static int BROADCAST_ACTION_ALERT_ID = 201;
    public final static String BROADCAST_ACTION_ITEM = "item";
    public final static String TRANSITION_TYPE = "transition_type";
    public final static String TRANSITION_DETAILS = "transition_details";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static int LOCATION_REQUEST_INTERVAL = 1000;
    public final static int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;
    public final static int LOITERING_DELAY_IN_MILLISEC = 1000;
}
