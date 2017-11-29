package io.face.tracking.sample.arc_face;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.face.tracking.sample.R;


/**
 * 考虑使用SurfaceView，绘制性能
 *
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-11-13 15:58
 */
public class FaceAreaView extends View {

    private Matrix mMatrix = new Matrix();

    private Bitmap mMaskBitmap;

    private FacePosition mFacePosition;
    private Canvas mCanvas;

    public FaceAreaView(Context context) {
        this(context, null);
    }

    public FaceAreaView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceAreaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMaskBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_avatar_portrait);
    }

    private Action1<Rect> faceAction = new Action1<Rect>() {
        @Override
        public void accept(Rect rect) {
            if (mMaskBitmap != null && mCanvas != null) {
                mCanvas.drawBitmap(mMaskBitmap, null, rect, null);
            }
        }
    };

    public void showFace(FacePosition facePosition) {
        mFacePosition = facePosition;
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        if (mFacePosition == null || !mFacePosition.hasFace()) {
            return;
        }

        float scaleX;
        float scaleY;

        int width = getWidth();
        int height = getHeight();

        int faceParentWidth = mFacePosition.getWidth();
        int faceParentHeight = mFacePosition.getHeight();

        if (faceParentWidth > faceParentHeight) {
            scaleX = width * 1.0F / faceParentHeight;
            scaleY = height * 1.0F / faceParentWidth;
        } else {
            scaleX = width * 1.0F / faceParentWidth;
            scaleY = height * 1.0F / faceParentHeight;
        }
        mMatrix.setScale(scaleX, scaleY);
        canvas.setMatrix(mMatrix);
        mFacePosition.forEachFace(faceAction);
    }

}
