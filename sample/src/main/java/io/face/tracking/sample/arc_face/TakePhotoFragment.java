package io.face.tracking.sample.arc_face;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import io.face.tracking.sample.R;
import io.face.tracking.sample.orientation.Orientation;
import io.face.tracking.sample.orientation.OrientationProvider;
import io.face.tracking.sample.utils.FileUtils;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.FotoapparatBuilder;
import io.fotoapparat.FotoapparatSwitcher;
import io.fotoapparat.hardware.provider.CameraProvider;
import io.fotoapparat.hardware.provider.CameraProviders;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.Size;
import io.fotoapparat.parameter.selector.Selectors;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.parameter.selector.FlashSelectors.autoFlash;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.off;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;

/**
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2016-12-19 15:09
 */
public class TakePhotoFragment extends Fragment {

    private String mSavePath;
    private TakePhotoFragmentCallback mTakePhotoFragmentCallback;

    public static TakePhotoFragment newInstance() {
        return new TakePhotoFragment();
    }

    private static final String TAG = TakePhotoFragment.class.getSimpleName();

    //Views
    private ImageView mNoFaceIv;
    private FaceAreaView mFaceAreaView;
    private ShutterView mShutterView;
    private CameraView mCameraView;
    private FotoapparatSwitcher fotoapparatSwitcher;
    private TextView mAlignmentTipsIv;
    private boolean mIsTipsShow = true;

    //手机方向
    private OrientationProvider mOrientationProvider;

    //脸部追踪
    private FaceTrackerManager mFaceTrackerManager;
    private FacePosition mFacePosition;
    private boolean mIsStartedTakePhoto = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTakePhotoFragmentCallback = (TakePhotoFragmentCallback) getContext();
        mOrientationProvider = Orientation.newInstance(getContext());
        mOrientationProvider.invertAxle(true);
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findView(int id) {
        View layout = getView();
        assert layout != null;
        View view = layout.findViewById(id);
        return (T) view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //脸部追踪
        mFaceTrackerManager = new FaceTrackerManager();
        mFaceTrackerManager.setListener(facePosition -> {
            if (mIsStartedTakePhoto) {
                return;
            }
            mFacePosition = facePosition;
            processOnTrackFace(facePosition);
        });

        //保存路径
        File file = new File(getContext().getFilesDir(), System.currentTimeMillis() + ".jpg");
        FileUtils.makeFilePath(file);
        mSavePath = file.getAbsolutePath();
    }

    private void processOnTrackFace(FacePosition facePosition) {
        mFaceAreaView.showFace(facePosition);
        if (facePosition.hasFace()) {
            mNoFaceIv.setVisibility(View.GONE);
        } else {
            if (mShutterView.isAlignment()) {
                mNoFaceIv.setVisibility(View.VISIBLE);
            } else {
                mNoFaceIv.setVisibility(View.GONE);
            }
        }
        showHideTipsView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containerLayout, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_controller, containerLayout, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
        fotoapparatSwitcher = FotoapparatSwitcher.withDefault(createFotoapparat());
    }

    @Override
    public void onResume() {
        super.onResume();
        //手机方向
        mOrientationProvider.startListening((azimuth, pitch, roll) -> {
            mShutterView.onOrientationChanged(pitch, roll);
            showHideTipsView();
        });
        //相机
        fotoapparatSwitcher.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        fotoapparatSwitcher.stop();
        mOrientationProvider.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFaceTrackerManager != null) {
            mFaceTrackerManager.destroy();
        }
    }

    private void showHideTipsView() {
        if (!mShutterView.isAlignment()) {
            mAlignmentTipsIv.setText(R.string.take_photo_alignment_tips);
            showTextTips();
        } else if (mFacePosition != null && !mFacePosition.hasFace()) {
            mAlignmentTipsIv.setText(R.string.take_photo_no_face);
            showTextTips();
        } else {
            mAlignmentTipsIv.setText(R.string.take_photo_alignment_tips);
            hideTextTips();
        }
    }

    private void hideTextTips() {
        if (mIsTipsShow) {
            mIsTipsShow = false;
            mAlignmentTipsIv.animate().translationY(-mAlignmentTipsIv.getMeasuredHeight());
        }
    }

    private void showTextTips() {
        if (!mIsTipsShow) {
            mIsTipsShow = true;
            mAlignmentTipsIv.animate().translationY(0);
        }
    }

    private Fotoapparat createFotoapparat() {
        CameraProvider cameraProvider;

        if (Build.VERSION.SDK_INT >= 21) {
            cameraProvider = CameraProviders.v1();
        } else {
            cameraProvider = CameraProviders.v2(getContext());
        }

        FotoapparatBuilder builder = Fotoapparat
                .with(getActivity())
                .cameraProvider(cameraProvider)
                .into(mCameraView)
                .photoSize(CameraUtils::findBestPictureSize)
                .previewSize(CameraUtils::findBestPreviewSize)
                .lensPosition(lensPosition(LensPosition.BACK))
                .focusMode(Selectors.firstAvailable(continuousFocus(), autoFocus(), fixed()))
                .flash(Selectors.firstAvailable(autoRedEye(), autoFlash(), torch(), off()))
                .frameProcessor(mFrameProcessor)
                .logger(s -> Log.d(TAG, s));

        return builder.build();
    }

    private FrameProcessor mFrameProcessor = new FrameProcessor() {
        @Override
        public void processFrame(Frame frame) {
            Size size = frame.size;
            mFaceTrackerManager.addFaceTrack(frame.image, size.width, size.height);
        }
    };

    protected void onPictureTaken(byte[] data, int rotationDegrees) {
        PhotoStoreUtils.savePhoto(data, rotationDegrees, mSavePath, () -> {
            mShutterView.stopLoading();
            mTakePhotoFragmentCallback.showPhoto(mSavePath);
        });
    }

    private void setupView() {
        //无脸提示图
        mNoFaceIv = findView(R.id.iv_no_face);
        //脸部遮挡
        mFaceAreaView = findView(R.id.face_area_view);
        //Camera
        mCameraView = findView(R.id.camera_view);
        mAlignmentTipsIv = findView(R.id.tv_tips);
        //设置快门
        mShutterView = findView(R.id.view_shutter);
        mShutterView.setOnClickListener(v -> {
            if (mShutterView.isAlignment()) {
                tackPictureChecked();
            }
        });
    }

    private void tackPictureChecked() {
        if (mFacePosition != null && mFacePosition.hasFace()) {
            tackPicture();
            mShutterView.loading();
        }
    }

    private void tackPicture() {
        mIsStartedTakePhoto = true;
        mTakePhotoFragmentCallback.saveFacePosition(mFacePosition);
        fotoapparatSwitcher.getCurrentFotoapparat()
                .takePicture()
                .toPendingResult()
                .whenDone(photo -> onPictureTaken(photo.encodedImage, photo.rotationDegrees));
    }

    public interface TakePhotoFragmentCallback {
        void showPhoto(String path);
        void saveFacePosition(FacePosition facePosition);
    }

}
