package io.face.tracking.sample.arc_face;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class FacePosition {

    private final int width;
    private final int height;
    private final List<Rect> faceList = new ArrayList<>();

    FacePosition(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    void addFace(Rect rect) {
        if (rect != null) {
            rotateDeg90(rect);
            faceList.add(rect);
        }
    }

    public List<Rect> getFaceList() {
        return faceList;
    }

    public boolean hasFace() {
        return !faceList.isEmpty();
    }

    public void forEachFace(Action1<Rect> faceAction) {
        for (Rect rect : faceList) {
            faceAction.accept(rect);
        }
    }

    /**
     * 将矩形随原图顺时针旋转90度，这个只针对正面拍摄
     *
     * @param r      待旋转的矩形
     * @param width  输入矩形对应的原图宽
     * @param height 输入矩形对应的原图高
     * @return 旋转后的矩形
     */
    private void rotateDeg90(Rect r) {
        int left = r.left;
        r.left = height - r.bottom;
        r.bottom = r.right;
        r.right = height - r.top;
        r.top = left;
    }

}