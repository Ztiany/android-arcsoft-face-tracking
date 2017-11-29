package io.face.tracking.sample.arc_face;

import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;

import io.face.tracking.sample.AppExecutor;
import io.face.tracking.sample.utils.ImageCompressor;
import io.face.tracking.sample.utils.ImageUtils;

/**
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-08-28 10:33
 */
class PhotoStoreUtils {


    static void savePhoto(byte[] data, int rotationDegrees, String savePath, Runnable runnable) {
        ImageCompressor.FileOptions fileOptions = new ImageCompressor.FileOptions();
        fileOptions.outFile = savePath;
        ImageCompressor.compressToFile(ImageCompressor.Source.ofBytes(data), fileOptions, (success, path) -> {
            //压缩有可能失败
            if (success) {
                setExifAttribute(path, rotationDegrees);
                runnable.run();
            } else {
                AppExecutor.EXECUTOR.execute(() -> saveRawPhoto(data, rotationDegrees, savePath, runnable));
            }
        });
    }

    @WorkerThread
    private static void saveRawPhoto(byte[] data, int rotationDegrees, String savePath, Runnable runnable) {
        Bitmap bitmap = ImageUtils.getBitmap(data, 0, CameraUtils.BEST_PIC_WIDTH, CameraUtils.BEST_PIC_HEIGHT);
        bitmap = ImageUtils.compressByQuality(bitmap, 50, true);
        ImageUtils.save(bitmap, new File(savePath), Bitmap.CompressFormat.JPEG);
        setExifAttribute(savePath, rotationDegrees);
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private static void setExifAttribute(String path, int rotationDegrees) {
        ExifInterface exifInterface;
        try {
            exifInterface = new ExifInterface(path);
            rotationDegrees = (360 - rotationDegrees);
            if (rotationDegrees == 90) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                exifInterface.saveAttributes();
            } else if (rotationDegrees == 270) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                exifInterface.saveAttributes();
            } else if (rotationDegrees == 180) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                exifInterface.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
