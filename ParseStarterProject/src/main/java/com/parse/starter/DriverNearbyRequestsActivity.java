package com.parse.starter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.parse.starter.PermissionUtils.FINE_LOCATION_PERMISSION_REQUEST_CODE;
import static com.parse.starter.PermissionUtils.MINIMUM_LOCATION_UPDATE_TIME;

public class DriverNearbyRequestsActivity extends AppCompatActivity {

    private ArrayAdapter arrayAdapter;
    private List<String> nearbyRequestsStrings;
    private List<ParseGeoPoint> nearbyRequestsGeopoints;
    private ParseGeoPoint currentGeoPoint;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_nearby_requests);

        setTitle(getString(R.string.nearby_requests));
        setupListView();
        checkForPermission();
    }

    private void getNearbyRequestsFromDatabase() {
        ParseQuery.getQuery(Constants.REQUEST_TABLE_KEY)
                .whereNear(Constants.LOCATION_KEY, currentGeoPoint)
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null && objects != null && objects.size() > 0) {
                            for (ParseObject object : objects) {
                                ParseGeoPoint geoPoint = object.getParseGeoPoint(Constants.LOCATION_KEY);
                                if (geoPoint != null) {
                                    double distance = geoPoint.distanceInKilometersTo(currentGeoPoint);
                                    DecimalFormat format = new DecimalFormat("#.##");
                                    nearbyRequestsStrings.add(format.format(distance) + " " + getString(R.string.kilometer));
                                    nearbyRequestsGeopoints.add(geoPoint);
                                    arrayAdapter.notifyDataSetChanged();
                                }
                            }
                        } else if (e != null) {
                            Toast.makeText(DriverNearbyRequestsActivity.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupListView() {
        nearbyRequestsStrings = new ArrayList<>();
        nearbyRequestsGeopoints = new ArrayList<>();

        ListView listView = findViewById(R.id.nearby_requests_list_view);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearbyRequestsStrings);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent goToDriverRequestActivityIntent = new Intent(DriverNearbyRequestsActivity.this,
                        DriverRequestActivity.class);

                double riderLatitude = nearbyRequestsGeopoints.get(position).getLatitude();
                double riderLongitude = nearbyRequestsGeopoints.get(position).getLongitude();
                double driverLongitude = currentGeoPoint.getLongitude();
                double driverLatitude = currentGeoPoint.getLatitude();

                goToDriverRequestActivityIntent.putExtra(Constants.RIDER_GEOPOINT_LATITUDE_KEY, riderLatitude);
                goToDriverRequestActivityIntent.putExtra(Constants.RIDER_GEOPOINT_LONGITUDE_KEY, riderLongitude);
                goToDriverRequestActivityIntent.putExtra(Constants.DRIVER_GEOPOINT_LATITUDE_KEY, driverLatitude);
                goToDriverRequestActivityIntent.putExtra(Constants.RIDER_GEOPOINT_LONGITUDE_KEY, driverLongitude);
                startActivity(goToDriverRequestActivityIntent);
            }
        });
    }

    private void enableMyLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                getNearbyRequestsFromDatabase();
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
        }
    }

    private void setupLocationManagerAndListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_LOCATION_UPDATE_TIME, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                currentGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                getNearbyRequestsFromDatabase();
            }
        }
    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            setupLocationManagerAndListener();
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(DriverNearbyRequestsActivity.this, FINE_LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }
}