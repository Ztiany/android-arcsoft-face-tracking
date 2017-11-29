package io.face.tracking.sample.arc_face;

import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-11-13 15:17
 */
public class FaceTrackerManager {

    private static final String TAG = FaceTrackerManager.class.getSimpleName();

    private static final String APP_ID = ArcFaceId.APP_ID;
    private static final String FT_KEY = ArcFaceId.FT_KEY;

    private Listener mListener;

    private volatile boolean mIsDestroy;
    private boolean mIsSDKInitializeSuccess;

    //版本信息
    @SuppressWarnings("all")
    private AFT_FSDKVersion version = new AFT_FSDKVersion();

    //这个类具体实现了人脸跟踪的功能
    private AFT_FSDKEngine engine = new AFT_FSDKEngine();

    private HandlerThread mHandlerThread;
    private Handler mTrackerHandler;

    private Handler mUIHandler;

    private static final int MAX_FACE = 25;

    public FaceTrackerManager() {
        //初始化引擎，设置检测角度、范围，数量。创建对象后，必须先于其他成员函数调用，否则其他成员函数会返回MERR_BAD_STATE
        AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(APP_ID, FT_KEY, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, MAX_FACE);
        Log.d(TAG, "AFT_FSDK_InitialFaceEngine = " + err.getCode());
        mIsSDKInitializeSuccess = err.getCode() == AFT_FSDKError.MOK;
        if (mIsSDKInitializeSuccess) {
            err = engine.AFT_FSDK_GetVersion(version);
            Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());
            initUIHandler();
            initTrackerThread();
        }
    }

    private void initUIHandler() {
        mUIHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (mIsDestroy) {
                    return;
                }

                FacePosition trackRest = (FacePosition) msg.obj;
                if (mListener != null) {
                    mListener.onResult(trackRest);
                }
            }
        };
    }

    private void initTrackerThread() {
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mTrackerHandler = new Handler(mHandlerThread.getLooper()) {

            private List<AFT_FSDKFace> mFaceList = new ArrayList<>();

            @Override
            public void handleMessage(Message msg) {
                if (mIsDestroy) {
                    return;
                }

                //get info
                TrackInfo trackInfo = (TrackInfo) msg.obj;
                int width = trackInfo.width;
                int height = trackInfo.height;
                byte[] data = trackInfo.data;

                //face track
                mFaceList.clear();
                AFT_FSDKError err = null;
                try {
                    err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, mFaceList);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (err != null) {
                    Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
                    Log.d(TAG, "AFT_FSDK_FaceFeatureDetect list =" + mFaceList);
                }

                //notify result
                FacePosition facePosition = new FacePosition(width, height);
                for (AFT_FSDKFace aft_fsdkFace : mFaceList) {
                    facePosition.addFace(new Rect(aft_fsdkFace.getRect()));
                }
                Message message = mUIHandler.obtainMessage();
                message.obj = facePosition;
                message.sendToTarget();

                //clear result.
                mFaceList.clear();
            }
        };
    }

    /**
     * 销毁引擎，释放占用的内存资源。
     */
    public void destroy() {
        Log.d(TAG, "destroy() called");
        mIsDestroy = true;
        if (mHandlerThread != null) {
            mTrackerHandler.removeCallbacksAndMessages(null);
            mUIHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
        }
        mIsSDKInitializeSuccess = false;
        try {
            mUIHandler.postDelayed(() -> engine.AFT_FSDK_UninitialFaceEngine(), 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测输入的图像中存在的人脸，只支持NV21的编码格式
     *
     * @param data   数据
     * @param width  宽
     * @param height 高
     */
    public void addFaceTrack(byte[] data, int width, int height) {
        if (!mIsSDKInitializeSuccess) {
            return;
        }
        if (mIsDestroy) {
            return;
        }
        final TrackInfo mTrackInfo = new TrackInfo(data, width, height);
        Message message = mTrackerHandler.obtainMessage();
        message.obj = mTrackInfo;
        message.sendToTarget();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private class TrackInfo {
        private final byte[] data;
        private final int width;
        private final int height;

        private TrackInfo(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }
    }

    public interface Listener {
        void onResult(FacePosition facePosition);
    }

    public boolean isSDKEnable() {
        return mIsSDKInitializeSuccess;
    }

}
