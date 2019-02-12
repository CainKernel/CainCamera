package com.cgfay.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class VideoTextureView extends TextureView {

    private int mVideoWidth;
    private int mVideoHeight;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        if (mVideoWidth != videoWidth || mVideoHeight != videoHeight) {
            mVideoWidth = videoWidth;
            mVideoHeight = videoHeight;
            requestLayout();
        }
    }

    @Override
    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            super.setRotation(rotation);
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float viewRotation = getRotation();

        if (viewRotation == 90f || viewRotation == 270f) {
            int temp = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = temp;
        }

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                width = widthSize;
                height = heightSize;
                if (mVideoWidth * height < width * mVideoHeight) {
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthMode == MeasureSpec.EXACTLY) {
                width = widthSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                    height = heightSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
            } else if (heightMode == MeasureSpec.EXACTLY) {
                height = heightSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                    width = widthSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else {
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                    height = heightSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                    width = widthSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // do nothing
        }
        setMeasuredDimension(width, height);
    }
}
