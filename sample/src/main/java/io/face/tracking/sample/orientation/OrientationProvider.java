package io.face.tracking.sample.orientation;

public interface OrientationProvider {

    void startListening(Listener listener);

    void invertAxle(boolean isInvert);

    void stopListening();

    interface Listener {
        void onOrientationChanged(float azimuth, float pitch, float roll);
    }


}