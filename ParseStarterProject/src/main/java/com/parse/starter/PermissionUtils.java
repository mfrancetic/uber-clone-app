package com.parse.starter;

import android.Manifest;
import android.content.pm.PackageManager;

class PermissionUtils {

    public static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 200;
    public static final int MINIMUM_LOCATION_UPDATE_TIME = 30000;

    public static boolean isPermissionGranted(int requestCode, int[] grantResults, String accessFineLocation) {
        if (accessFineLocation.equals(Manifest.permission.ACCESS_FINE_LOCATION) && requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public static void requestPermission(RiderActivity riderActivity, int locationPermissionRequestCode, String accessCourseLocation, boolean permissionMissing) {
        if (permissionMissing) {
            riderActivity.requestPermissions(new String[]{accessCourseLocation}, locationPermissionRequestCode);
        }
    }
}