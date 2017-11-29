package io.face.tracking.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import io.face.tracking.sample.arc_face.FacePosition;
import io.face.tracking.sample.arc_face.ShowImageFragment;
import io.face.tracking.sample.arc_face.ShowImageFragment.ShowImageFragmentCallback;
import io.face.tracking.sample.arc_face.TakePhotoFragment;
import io.face.tracking.sample.arc_face.TakePhotoFragment.TakePhotoFragmentCallback;


public class MainActivity extends AppCompatActivity implements TakePhotoFragmentCallback, ShowImageFragmentCallback {

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private FacePosition mFacePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        boolean hasCameraPermission = permissionsDelegate.hasCameraPermission();

        if (hasCameraPermission) {
            showTakePhotoFragment();
        } else {
            permissionsDelegate.requestCameraPermission();
        }
    }

    private void showTakePhotoFragment() {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag(TakePhotoFragment.class.getName());
        if (fragment == null) {
            TakePhotoFragment takePhotoFragment = TakePhotoFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, takePhotoFragment, takePhotoFragment.getClass().getName())
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            getWindow().getDecorView().post(() -> showTakePhotoFragment());
        }
    }

    @Override
    public void showPhoto(String path) {
        Fragment fragment = ShowImageFragment.newInstance(path);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fl_container, fragment, fragment.getClass().getName())
                .commit();
    }

    @Override
    public void saveFacePosition(FacePosition facePosition) {
        mFacePosition = facePosition;
    }

    @Override
    public void showTakePhotoPage() {
        TakePhotoFragment fragment = TakePhotoFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, android.R.anim.fade_out)
                .replace(R.id.fl_container, fragment, fragment.getClass().getName())
                .commit();
    }

    @Override
    public FacePosition getFacePosition() {
        return mFacePosition;
    }
}
