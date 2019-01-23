package com.gerdaszabo.szage.bookpilot.model;

/**
 * Store object for the Store finder
 */

public class Store {

    private double mLongitude;
    private double mLatitude;
    private String mPlaceName;
    private String mVicinity;

    // Constructor
    public Store(double latitude, double longitude, String placeName, String vicinity) {
        mLatitude = latitude;
        mLongitude = longitude;
        mPlaceName = placeName;
        mVicinity = vicinity;
    }

    /* Getter methods */

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public String getVicinity() {
        return mVicinity;
    }
}
