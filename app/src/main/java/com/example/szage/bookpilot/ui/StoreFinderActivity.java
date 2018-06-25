package com.example.szage.bookpilot.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.szage.bookpilot.QueryUtils;
import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.model.Store;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 *  Request device location and based on that displays bookstores nearby
 */

public class StoreFinderActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback {

    private static final String LOG_TAG = StoreFinderActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1000;
    private static final int PERMISSION_REQUEST_CODE = 34;

    private LocationManager mLocationManager;
    private double mLatitude;
    private double mLongitude;
    private GoogleMap mMap;
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_finder);

        /* Get the SupportMapFragment and request notification
         when the map is ready to be used. */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Get the location service
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    /**
     * Setup the map
     *
     * @param googleMap is the map that displays location data
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(StoreFinderActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
            return;
        } else {
            // Enable some properties
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            // Get current device location
            getCurrentGpsLocation();
        }
    }


    /**
     * Request location of the device
     */
    private void getCurrentGpsLocation() {

        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission already checked
            return;
        }
        // Call location listener that will refresh location
        mLocationManager.requestLocationUpdates(bestProvider, 60000, 10, mLocationListener);
    }

    // Updates location
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            // Get the latitude and longitude from location
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            LatLng latLng = new LatLng(mLatitude, mLongitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(String.valueOf(R.string.current_location)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            // Check network info
            NetworkInfo networkInfo = QueryUtils.getNetworkInfo(getBaseContext());
            // If there's no connectivity
            if (networkInfo == null) {
                // Inform user about it with a snackbar
                Snackbar snackbar = Snackbar.make(findViewById(R.id.map),
                        R.string.snackbar_message, Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                // Otherwise fetch Store objects asynchronously
                new NearbyStoresTask().execute();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    /**
     * Handling connectivity failure
     *
     * @param connectionResult is the result message / code of the connection request
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Log the error
        Log.e(LOG_TAG, String.valueOf(R.string.places_api_error_code)
                + connectionResult.getErrorCode());

        // Inform user about the problem
        Toast.makeText(this,
                String.valueOf(R.string.places_api_error_code) +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    /**
     * Async task that fetch store objects and displays them
     */
    class NearbyStoresTask extends AsyncTask<String, Void, List<Store>> {

        // Get the URL for place searching
        String placesUrl = QueryUtils.getURL(mLongitude, mLatitude);

        @Override
        protected List<Store> doInBackground(String... strings) {

            // Fetch Stores' location nearby
            return QueryUtils.fetchStoreLocations(placesUrl);
        }

        @Override
        protected void onPostExecute(List<Store> stores) {
            super.onPostExecute(stores);

            for (int i = 0; i < stores.size(); i++) {

                // Get store details
                double latitude = stores.get(i).getLatitude();
                double longitude = stores.get(i).getLongitude();
                String placeName = stores.get(i).getPlaceName();
                String vicinity = stores.get(i).getVicinity();

                // Mark each on the map
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(latitude, longitude);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity);
                mMap.addMarker(markerOptions);
            }
        }
    }

    /**
     * Launching the place picker
     */
    private void startPlacePicker(double longitude, double latitude) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(StoreFinderActivity.this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the result of request permission
     *
     * @param requestCode is the code of the permission request
     * @param permissions is requested permissions
     * @param grantResults whether it's granted or denied
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If permission granted, detect bookstores
                    detectCurrentPlace();
                }
                break;
        }
    }

    /**
     * Detects bookstores according to device location
     *
     * @throws SecurityException
     */
    private void detectCurrentPlace() throws SecurityException {

        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mClient, null);

        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {

                // get the mLatitude & mLongitude of the first likely place
                double latitude = placeLikelihoods.get(0).getPlace().getLatLng().latitude;
                double longitude = placeLikelihoods.get(0).getPlace().getLatLng().longitude;

                // launch the place picker
                startPlacePicker(longitude, latitude);

                for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                    Log.i(LOG_TAG, String.format("Place '%s' with " + "Longitude & Latitude: '%s'" +
                                    "likelihood: %g",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getPlace().getLatLng(),
                            placeLikelihood.getLikelihood()));
                }
                placeLikelihoods.release();
            }
        });
    }
}
