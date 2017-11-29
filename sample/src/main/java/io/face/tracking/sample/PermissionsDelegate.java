package io.face.tracking.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

class PermissionsDelegate {

    private static final int REQUEST_CODE = 10;
    private final Activity activity;

    PermissionsDelegate(Activity activity) {
        this.activity = activity;
    }

    boolean hasCameraPermission() {
        int permissionCheckResult = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED;
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
    }

    boolean resultGranted(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode != REQUEST_CODE) {
            return false;
        }

        if (grantResults.length < 1) {
            return false;
        }

        return permissions[0].equals(Manifest.permission.CAMERA);
    }
}
