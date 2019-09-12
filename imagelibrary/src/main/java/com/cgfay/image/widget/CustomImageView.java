package com.cgfay.image.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint("AppCompatCustomView")
public class CustomImageView extends View {

    // 变换矩阵
    private Matrix mMatrix = new Matrix();
    // 透视变换矩阵
    private Matrix mPerspectMatirx = new Matrix();

    private Bitmap mBitmap;
    private int mImageWidth;
    private int mImageHeight;

    private int perspectWidthOffset;
    private int perspectHeightOffset;

    public CustomImageView(Context context) {
        this(context, null);
        init();
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     *
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    /**
     * 设置透视距离
     * @param x x坐标
     * @param y y坐标
     * @param horizontal 是横向透视还是纵向透视
     */
    public void setPerspective(int x, int y, boolean horizontal) {

    }
}
