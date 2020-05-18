package com.parse.starter;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;

public class DriverRequestActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng driverLocation;
    private LatLng riderLocation;
    private Button acceptRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request);

        acceptRequestButton = findViewById(R.id.accept_request_button);
        acceptRequestButton.setVisibility(View.GONE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;

        getDriverAndRiderLocation();
        setButtonOnClickListener();
        mapFragment.getMapAsync(this);
    }

    private void setButtonOnClickListener() {
        acceptRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void getDriverAndRiderLocation() {
        Intent intent = getIntent();
        if (intent != null) {
            double driverLatitude = intent.getDoubleExtra(Constants.DRIVER_GEOPOINT_LATITUDE_KEY, 0);
            double driverLongitude = intent.getDoubleExtra(Constants.DRIVER_GEOPOINT_LONGITUDE_KEY, 0);

            double riderLatitude = intent.getDoubleExtra(Constants.RIDER_GEOPOINT_LATITUDE_KEY, 0);
            double riderLongitude = intent.getDoubleExtra(Constants.RIDER_GEOPOINT_LONGITUDE_KEY, 0);
            driverLocation = new LatLng(driverLatitude, driverLongitude);
            riderLocation = new LatLng(riderLatitude, riderLongitude);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(driverLocation).title(getString(R.string.your_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15));

            googleMap.addMarker(new MarkerOptions().position(riderLocation).title(getString(R.string.rider_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            acceptRequestButton.setVisibility(View.VISIBLE);
        }
    }
}