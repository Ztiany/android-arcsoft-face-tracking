package io.face.tracking.sample.arc_face;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import io.face.tracking.sample.R;


public class ShutterView extends View {

    private float mPitch;////pitch  z轴与水平面的夹角 范围在-90 到 90° ，手机的任何一面垂直于水平面即为0°，z轴指向下方是为角度为正直
    private float mRoll;//手机方向  -180-180

    private static final float ROLL_OFFSET = 3;//允许的偏差
    private static final float PITCH_OFFSET = 6;//允许的偏差
    private static final int PITCH_RANGE = 90;//Pitch的变化范围

    private static final int SHUTTER_BITMAP_RESOURCE_ID = R.drawable.size_icon_camera_pictures;

    private Bitmap mShutterBitmap;//手机摆正拍照时显示的图片

    private static final int ANIM_TIME = 500;
    private static final int LOADING_ANIM_TIME = 1000;

    private int mWidth;
    private int mCenter;
    private int mInnerSize;//内部宽-减去线宽
    private int mBitmapSize;

    private boolean mHasSensor;//是否有可用的传感器

    private Bitmap mDrawingBitmap;
    private Bitmap mMaskBitmap;//用于实现图片混合
    private Canvas mCanvas;
    private Path mCoordinatePath;
    private PorterDuffXfermode mClearXFerMode;
    private PorterDuffXfermode mMaskXFerMode;

    private static final int COORDINATE_PERCENT = 6;//坐标线长度比
    private RectF mQuasiArcRect;//用于绘制中间坐标小圈圈
    private static final int OUT_CIRCLE_ANGLE = 15;//外面两个半圆的切割角度
    private float mLineWidth = 0.03F;//线条的宽度 整体百分比

    private static final int MAX_SHADOW_RADIUS = 10;


    private RectF mViewRangeRectF;//代表整个View区域的矩形

    private Paint mPaint;//画笔
    private Paint mAnimPaint;
    private Path mCirclePath;
    private float[] mLoadingPoint;
    private float mPointWidth;

    private boolean mIsAlignment = true;//标识手机是否已经对其


    public static final int STATUS_NONE = 0;
    public static final int STATUS_ALIGNMENT = 1;
    public static final int STATUS_NOT_ALIGNMENT = 2;
    public static final int STATUS_LOADING = 3;
    public static final int STATUS_ANIM_TO_ALIGN = 4;
    public static final int STATUS_ANIM_TO_NOT_ALIGN = 5;

    private int mCurrentStatus = STATUS_NONE;


    private
    @ColorInt
    int mCircleColor;//整体背景色
    private
    @ColorInt
    int mCoordinateColor;//对准线颜色
    private
    @ColorInt
    int mMoveLayerColor;//移动图层颜色

    private ValueAnimator mAnimator;
    private float mAnimValue;

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mAnimValue = (float) animation.getAnimatedValue();
            invalidate();
        }
    };

    private AnimatorListenerAdapter mToAlignmentListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mCurrentStatus = STATUS_ALIGNMENT;
            invalidate();
        }
    };
    private AnimatorListenerAdapter mToNotAlignmentListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mCurrentStatus = STATUS_NOT_ALIGNMENT;
            invalidate();
        }
    };
    private PathMeasure mPathMeasure;
    private ValueAnimator mLoadingAnimator;

    public ShutterView(Context context) {
        this(context, null);
    }

    public ShutterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mShutterBitmap = BitmapFactory.decodeResource(getResources(), SHUTTER_BITMAP_RESOURCE_ID);
        mBitmapSize = Math.max(mShutterBitmap.getWidth(), mShutterBitmap.getHeight());

        mViewRangeRectF = new RectF();
        mQuasiArcRect = new RectF();
        mCoordinatePath = new Path();


        mClearXFerMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mMaskXFerMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);


        /*
         Color ...
         */
        mMoveLayerColor = ContextCompat.getColor(getContext(), R.color.transparent_dark_white);
        mCoordinateColor = mCircleColor = Color.WHITE;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mAnimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimPaint.setColor(mCircleColor);
        mAnimPaint.setStyle(Paint.Style.STROKE);

        mCirclePath = new Path();
        mLoadingPoint = new float[2];
        mPointWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
    }


    @Override
    @SuppressWarnings("all")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int width;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {

            width = mBitmapSize;

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }
        /*要求View为正方形*/
        setMeasuredDimension(width, width);
    }


    @Override
    @SuppressWarnings("all")
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mCenter = (int) (((mWidth * 1.0F) / 2) + 0.5F);

        mLineWidth = mWidth * mLineWidth;
        mInnerSize = (int) (mWidth - mLineWidth + 0.5F);
        //减去线条的宽度
        mViewRangeRectF.set(mLineWidth, mLineWidth, mInnerSize, mInnerSize);
        int coordinateWidth = (int) (((mInnerSize * 1.0F) / COORDINATE_PERCENT) + 0.5F);
        mQuasiArcRect.set(
                mCenter - coordinateWidth,
                mCenter - coordinateWidth,
                mCenter + coordinateWidth,
                mCenter + coordinateWidth
        );

        //坐标是固定的，所以直接初始化
        mCoordinatePath.reset();
        mCoordinatePath.moveTo(mCenter - (2 * coordinateWidth), mCenter);//移动到坐标的左边
        mCoordinatePath.lineTo(mCenter - coordinateWidth, mCenter);//移动到坐标的左边
        mCoordinatePath.arcTo(mQuasiArcRect, 180, -180);
        mCoordinatePath.lineTo(mCenter + (2 * coordinateWidth), mCenter);
        mCoordinatePath.moveTo(mCenter, mQuasiArcRect.bottom);//移动到坐标的左边
        mCoordinatePath.lineTo(mCenter, mInnerSize);

        //确定一个圆圈，loading是转圈
        mCirclePath.reset();
        mCirclePath.addCircle(mCenter, mCenter, mBitmapSize / 2 - mPointWidth / 2, Path.Direction.CW);
        mPathMeasure = new PathMeasure(mCirclePath, true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // StateChange
    ///////////////////////////////////////////////////////////////////////////
    private void onAwaysNotAlignment() {
        if (mCurrentStatus == STATUS_LOADING
                || mCurrentStatus == STATUS_ANIM_TO_NOT_ALIGN) {
            return;
        }
        if (mCurrentStatus == STATUS_ANIM_TO_ALIGN
                || mCurrentStatus == STATUS_ALIGNMENT) {
            onToNotAlignment();
            return;
        }

        mCurrentStatus = STATUS_NOT_ALIGNMENT;
        postInvalidate();

    }

    private void onToAlignment() {
        if (mCurrentStatus == STATUS_NONE) {
            mCurrentStatus = STATUS_ALIGNMENT;
            postInvalidate();
            return;
        }
        if (mCurrentStatus == STATUS_ALIGNMENT) {
            postInvalidate();
            return;
        }
        if (mCurrentStatus == STATUS_ANIM_TO_ALIGN) {
            return;
        }
        if (mCurrentStatus == STATUS_ANIM_TO_NOT_ALIGN
                || mCurrentStatus == STATUS_NOT_ALIGNMENT) {
            cancelAnim();
            mCurrentStatus = STATUS_ANIM_TO_ALIGN;
            startAnimToAlignmentView();
        }
    }


    private void onToNotAlignment() {
        if (mCurrentStatus == STATUS_NONE) {
            mCurrentStatus = STATUS_NOT_ALIGNMENT;
            postInvalidate();
            return;
        }
        if (mCurrentStatus == STATUS_NOT_ALIGNMENT) {
            postInvalidate();
            return;
        }
        if (mCurrentStatus == STATUS_ANIM_TO_NOT_ALIGN) {

            return;
        }

        if (mCurrentStatus == STATUS_ANIM_TO_ALIGN
                || mCurrentStatus == STATUS_ALIGNMENT) {
            cancelAnim();
            mCurrentStatus = STATUS_ANIM_TO_NOT_ALIGN;
            startAnimToNotAlignmentView();
        }

    }


    private void onAwaysAlignment() {


        if (mCurrentStatus == STATUS_LOADING
                || mCurrentStatus == STATUS_ANIM_TO_ALIGN) {
            return;
        }

        if (mCurrentStatus == STATUS_ANIM_TO_NOT_ALIGN
                || mCurrentStatus == STATUS_NOT_ALIGNMENT) {
            onToAlignment();
            return;
        }

        if (mCurrentStatus != STATUS_ALIGNMENT) {
            mCurrentStatus = STATUS_ALIGNMENT;
            invalidate();

        }
    }


    private void startAnimToNotAlignmentView() {
        cancelAnim();
        mAnimator = ValueAnimator.ofFloat(mAnimValue, 0F);
        mAnimator.setDuration(ANIM_TIME);
        mAnimator.addUpdateListener(mAnimatorUpdateListener);
        mAnimator.addListener(mToNotAlignmentListener);
        mAnimator.start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //清除图层
        createBitmapIfNeed();
        clearLayer();

        if (mCurrentStatus == STATUS_NOT_ALIGNMENT) {
            drawNotAlignmentStatus();
        } else if (mCurrentStatus == STATUS_ALIGNMENT) {
            drawAlignmentStatus();
        } else if (mCurrentStatus == STATUS_LOADING) {
            drawLoading();
        } else if (mCurrentStatus == STATUS_ANIM_TO_ALIGN) {
            drawNotAlignAnim();
        } else if (mCurrentStatus == STATUS_ANIM_TO_NOT_ALIGN) {
            drawAlignAnim();
        } else if (mCurrentStatus == STATUS_NONE) {
            drawAlignmentStatus();
        }
        makeToCircleShape();//变成圆形
        canvas.drawBitmap(mDrawingBitmap, 0, 0, null);

    }

    private void drawAlignAnim() {

        if (mAnimValue < 0.5F) {
            drawNotAlignmentStatus();
        } else {
            drawAlignmentStatus();
        }

        mCanvas.save();
        mCanvas.translate(mCenter, mCenter);
        float radius = mInnerSize * 1.0F / 2;
        float stroke = mInnerSize * mAnimValue;
        mAnimPaint.setStrokeWidth(stroke);
        mCanvas.drawCircle(0, 0, radius * (1 - mAnimValue), mAnimPaint);
        mCanvas.restore();

    }

    private void drawNotAlignAnim() {

        if (mAnimValue < 0.5F) {
            drawNotAlignmentStatus();
        } else {
            drawAlignmentStatus();
        }

        mCanvas.save();
        mCanvas.translate(mCenter, mCenter);
        float radius = mInnerSize * 1.0F / 2;
        float stroke = mInnerSize * mAnimValue;
        mAnimPaint.setStrokeWidth(stroke);

        mCanvas.drawCircle(0, 0, radius * (1 - mAnimValue), mAnimPaint);
        mCanvas.restore();

    }

    private void drawLoading() {

        mPaint.setShadowLayer(MAX_SHADOW_RADIUS, 0, 0, Color.WHITE);
        mPaint.setStrokeWidth(mPointWidth);
        mCanvas.drawPoint(mLoadingPoint[0], mLoadingPoint[1], mPaint);

    }


    private void startAnimToAlignmentView() {
        cancelAnim();
        mAnimator = ValueAnimator.ofFloat(mAnimValue, 1.0F);
        mAnimator.setDuration(ANIM_TIME);
        mAnimator.addUpdateListener(mAnimatorUpdateListener);
        mAnimator.addListener(mToAlignmentListener);
        mAnimator.start();
    }


    private void drawAlignmentStatus() {
        //画动态坐标
        mCanvas.save();
        mCanvas.translate(mCenter, mCenter);
        mPaint.setColor(mCircleColor);
        mPaint.setStyle(Paint.Style.STROKE);

        int halfWidth = mShutterBitmap.getWidth() / 2;
        int halfHeight = mShutterBitmap.getHeight() / 2;
        mCanvas.drawBitmap(mShutterBitmap, -halfWidth, -halfHeight, null);
        mCanvas.drawCircle(0, 0, halfWidth, mPaint);
        mCanvas.restore();
    }

    /**
     * 没有对其的状态
     */
    private void drawNotAlignmentStatus() {
        //   [90-0] / 90 = [1-0]   1-[1-0]= [0-1]
        float fraction = Math.abs(mPitch) / PITCH_RANGE;

        mCanvas.save();
        //画动态坐标
        mCanvas.save();
        mCanvas.rotate(mRoll, mCenter, mCenter);
        //移动canvas坐标
        mCanvas.translate(0, (mPitch / PITCH_RANGE) * mWidth);
        mPaint.setColor(mMoveLayerColor);
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawArc(mViewRangeRectF, -180, 180, false, mPaint);
        mCanvas.translate(0, -0.1F);
        mCanvas.drawArc(mQuasiArcRect, 0, 180, false, mPaint);
        mCanvas.restore();
        //绘制旋转的圆圈
        mCanvas.save();
        mCanvas.rotate(mRoll, mCenter, mCenter);
        mPaint.setColor(mCircleColor);
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setShadowLayer(fraction * MAX_SHADOW_RADIUS, 0, 0, Color.WHITE);
        mCanvas.drawArc(mViewRangeRectF, OUT_CIRCLE_ANGLE, 180 - OUT_CIRCLE_ANGLE, false, mPaint);
        mCanvas.drawArc(mViewRangeRectF, 180 + OUT_CIRCLE_ANGLE, 180 - OUT_CIRCLE_ANGLE, false, mPaint);
        mCanvas.restore();
        mPaint.setShadowLayer(0, 0, 0, Color.WHITE);


        //画固定坐标
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mCoordinateColor);
        mCanvas.drawPath(mCoordinatePath, mPaint);


        mCanvas.restore();


    }

    private void makeToCircleShape() {
        mPaint.setXfermode(mMaskXFerMode);
        mCanvas.drawBitmap(mMaskBitmap, 0, 0, mPaint);
        mPaint.setXfermode(null);
    }

    @SuppressWarnings("all")
    private void createBitmapIfNeed() {
        if (mDrawingBitmap == null) {
            mDrawingBitmap = Bitmap.createBitmap(mWidth, mWidth, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mDrawingBitmap);

            mMaskBitmap = Bitmap.createBitmap(mWidth, mWidth, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mMaskBitmap);
            canvas.drawColor(Color.TRANSPARENT);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            canvas.drawCircle(mCenter, mCenter, mWidth / 2, paint);
        }
    }

    private void clearLayer() {
        mPaint.setXfermode(mClearXFerMode);
        mCanvas.drawPaint(mPaint);
        mPaint.setXfermode(null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 角度变化
    ///////////////////////////////////////////////////////////////////////////
    public void onOrientationChanged(float pitch, float roll) {
        if (!mHasSensor) {
            mHasSensor = true;
        }
        update(pitch, roll);
    }

    private void update(float pitch, float roll) {
        mRoll = roll;
        mPitch = pitch;
        boolean isAlignment = calcAndCheckCoordinate();
        if (mIsAlignment) {
            if (isAlignment) {
                onAwaysAlignment();
            } else {
                onToNotAlignment();
            }
        } else {
            if (isAlignment) {
                onToAlignment();
            } else {
                onAwaysNotAlignment();
            }
        }
        mIsAlignment = isAlignment;
    }

    private boolean calcAndCheckCoordinate() {
        float absRoll = Math.abs(mRoll);
        float absPitch = Math.abs(mPitch);
        //判断是否已经对其
        return absPitch <= PITCH_OFFSET && absRoll <= ROLL_OFFSET;
    }

    /**
     * 判断是否已经对其
     *
     * @return 对其true
     */
    public boolean isAlignment() {
        return (mIsAlignment && mCurrentStatus == STATUS_ALIGNMENT) || mCurrentStatus == STATUS_NONE;
    }

    public int getCurrentStatus() {
        return mCurrentStatus;
    }

    private void cancelAnim() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnim();
        cancelLoadingAnim();
    }

    ///////////////////////////////////////////////////////////////////////////
    // loading
    ///////////////////////////////////////////////////////////////////////////

    public void loading() {
        mCurrentStatus = STATUS_LOADING;
        startLoadingAnim();
        invalidate();
    }

    private void startLoadingAnim() {
        if (mLoadingAnimator == null) {
            mLoadingAnimator = ValueAnimator.ofFloat(0F, mPathMeasure.getLength());
            mLoadingAnimator.setDuration(LOADING_ANIM_TIME);
            mLoadingAnimator.setInterpolator(new LinearInterpolator());
            mLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mLoadingAnimator.setRepeatMode(ValueAnimator.RESTART);
            mLoadingAnimator.addUpdateListener(animation -> {
                float animatedValue = (float) animation.getAnimatedValue();
                mPathMeasure.getPosTan(animatedValue, mLoadingPoint, null);
                invalidate();
            });
        }
        if (mLoadingAnimator.isRunning()) {
            mLoadingAnimator.cancel();
        }
        mLoadingAnimator.start();
    }

    public void stopLoading() {
        mCurrentStatus = STATUS_NONE;
        cancelLoadingAnim();
        invalidate();
    }

    private void cancelLoadingAnim() {
        if (mLoadingAnimator != null) {
            mLoadingAnimator.cancel();
        }
    }
}
