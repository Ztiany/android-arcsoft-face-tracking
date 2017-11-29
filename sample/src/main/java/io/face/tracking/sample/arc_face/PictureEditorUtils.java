package io.face.tracking.sample.arc_face;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;


class PictureEditorUtils {

    static void addFaceMask(FacePosition savedFacePosition, Bitmap source, final Bitmap faceMask, Action1<Bitmap> action1) {
        Bitmap bitmap = Bitmap.createBitmap(source.copy(Bitmap.Config.RGB_565, true));
        final Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);

        float scaleX;
        float scaleY;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d("PictureEditorUtils", "width:" + width);
        Log.d("PictureEditorUtils", "height:" + height);

        int faceParentWidth = savedFacePosition.getWidth();//640
        int faceParentHeight = savedFacePosition.getHeight();//360
        Log.d("PictureEditorUtils", "faceParentWidth:" + faceParentWidth);
        Log.d("PictureEditorUtils", "faceParentHeight:" + faceParentHeight);

        if (faceParentWidth > faceParentHeight) {
            scaleX = width * 1.0F / faceParentHeight;
            scaleY = height * 1.0F / faceParentWidth;
        } else {
            scaleX = width * 1.0F / faceParentWidth;
            scaleY = height * 1.0F / faceParentHeight;
        }
        Log.d("FaceAreaView", "scaleX:" + scaleX + " scaleY:" + scaleY);
        matrix.setScale(scaleX, scaleY);
        canvas.setMatrix(matrix);
        savedFacePosition.forEachFace(rect -> canvas.drawBitmap(faceMask, null, rect, paint));
        action1.accept(bitmap);
    }
}
