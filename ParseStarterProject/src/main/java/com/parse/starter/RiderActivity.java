package com.parse.starter;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import static com.parse.starter.PermissionUtils.FINE_LOCATION_PERMISSION_REQUEST_CODE;
import static com.parse.starter.PermissionUtils.MINIMUM_LOCATION_UPDATE_TIME;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button callCancelUberButton;
    private Location lastKnownLocation;
    private boolean requestIsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        callCancelUberButton = findViewById(R.id.call_cancel_uber_button);

        checkIfRequestIsActive();
        mapFragment.getMapAsync(this);
    }

    private void checkIfRequestIsActive() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.REQUEST_TABLE_KEY);
        query.whereEqualTo(Constants.USERNAME_KEY, ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && objects.size() > 0) {
                    requestIsActive = true;
                    callCancelUberButton.setText(getString(R.string.cancel_uber));
                }
            }
        });
    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
            enableMyLocation();
            setupLocationManagerAndListener();
            displayButton();
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, FINE_LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    private void displayButton() {
        callCancelUberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestIsActive) {
                    cancelUber();
                } else {
                    callUber();
                }
            }
        });
        callCancelUberButton.setVisibility(View.VISIBLE);
    }

    private void callUber() {
        final ParseObject request = new ParseObject(Constants.REQUEST_TABLE_KEY);

        request.put(Constants.USERNAME_KEY, ParseUser.getCurrentUser().getUsername());
        request.put(Constants.LOCATION_KEY, new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
        request.put(Constants.REQUEST_ACTIVE_KEY, true);
        request.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(RiderActivity.this, getString(R.string.uber_called),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RiderActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        callCancelUberButton.setText(getString(R.string.cancel_uber));
    }

    private void cancelUber() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.REQUEST_TABLE_KEY);
        query.whereEqualTo(Constants.USERNAME_KEY, ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && objects.size() > 0) {
                    for (ParseObject object : objects) {
                        object.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(RiderActivity.this, getString(R.string.uber_canceled),
                                            Toast.LENGTH_SHORT).show();
                                    callCancelUberButton.setText(getString(R.string.call_uber));
                                    requestIsActive = false;
                                } else {
                                    Toast.makeText(RiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
                    }
                } else if (e != null) {
                    Toast.makeText(RiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
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
        mMap = googleMap;
        checkForPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(requestCode, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
            setupLocationManagerAndListener();
            Toast.makeText(this, getString(R.string.location_permission_granted),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Permission was denied. Display an error message
            Toast.makeText(this, getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT).show();
            callCancelUberButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setupLocationManagerAndListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_LOCATION_UPDATE_TIME, 0, locationListener);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                updateMap(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
            }
        }
    }

    private void enableMyLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(new LatLng(location.getLatitude(), location.getLongitude()));
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
    }

    private void updateMap(LatLng latLng) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.your_location))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }
}