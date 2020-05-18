package com.parse.starter;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng driverLocation;
    private LatLng riderLocation;
    private Button acceptRequestButton;
    private SupportMapFragment mapFragment;
    private String riderUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request);

        acceptRequestButton = findViewById(R.id.accept_request_button);
        acceptRequestButton.setVisibility(View.GONE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_driver);

        getDriverAndRiderLocation();
        setButtonOnClickListener();

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setButtonOnClickListener() {
        acceptRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequestInDatabase();
            }
        });
    }

    private void acceptRequestInDatabase() {
        ParseQuery.getQuery(Constants.REQUEST_TABLE_KEY)
                .whereMatches(Constants.USERNAME_KEY, riderUsername)
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e != null) {
                            Toast.makeText(DriverRequestActivity.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        } else if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.put(Constants.DRIVER_USERNAME_KEY, ParseUser.getCurrentUser().getUsername());
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            navigateToDirections();
                                            Toast.makeText(DriverRequestActivity.this, getString(R.string.request_accepted),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(DriverRequestActivity.this, e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void navigateToDirections() {
        Uri intentUri = Uri.parse("google.navigation:q=" + riderLocation.latitude + " , " + riderLocation.longitude);
        Intent navigateToDirectionsIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        navigateToDirectionsIntent.setPackage("com.google.android.apps.maps");
        startActivity(navigateToDirectionsIntent);
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

            riderUsername = intent.getStringExtra(Constants.USERNAME_KEY);
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

            ArrayList<Marker> markers = new ArrayList<>();
            markers.add(googleMap.addMarker(new MarkerOptions().position(driverLocation).title(getString(R.string.your_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
            markers.add(googleMap.addMarker(new MarkerOptions().position(riderLocation).title(getString(R.string.rider_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            int padding = 100; // offset from edges of the map in pixels
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            googleMap.moveCamera(cameraUpdate);

            acceptRequestButton.setVisibility(View.VISIBLE);
        }
    }
}