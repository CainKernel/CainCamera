package com.cgfay.caincamera.photo_edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cgfay.caincamera.bean.MediaMeta;

import java.lang.ref.WeakReference;

/**
 * 图片编辑管理器
 * Created by cain on 2017/11/15.
 */

public final class PhotoEditManager {

    private static final String TAG = "PhotoEditManager";

    private static PhotoEditManager mInstance;

    // 图像元数据
    private MediaMeta mMediaMeta;

    private WeakReference<Context> mWeakContext;
    private WeakReference<ImageView> mWeakImageView;

    // 原图像
    private Bitmap mSource;

    public static PhotoEditManager getInstance() {
        if (mInstance == null) {
            mInstance = new PhotoEditManager();
        }
        return mInstance;
    }

    private PhotoEditManager() {}

    /**
     * 设置共享上下文
     * @param context
     */
    public void setContext(Context context) {
        if (mWeakContext != null) {
            mWeakContext.clear();
            mWeakContext = null;
        }
        mWeakContext = new WeakReference<Context>(context);
    }

    /**
     * 设置需要返回的视图
     * @param imageView
     */
    public void setImageView(ImageView imageView) {
        if (mWeakImageView != null) {
            mWeakImageView.clear();
            mWeakImageView = null;
        }
        mWeakImageView = new WeakReference<ImageView>(imageView);
    }

    /**
     * 设置图片元数据
     * @param mediaMeta
     */
    public void setImageMeta(MediaMeta mediaMeta) {
        mMediaMeta = mediaMeta;
    }

    /**
     * 释放持有的资源
     */
    public void release() {
        if (mMediaMeta != null) {
            mMediaMeta = null;
        }
        if (mWeakImageView != null) {
            mWeakImageView.clear();
            mWeakImageView = null;
        }
        if (mWeakContext != null) {
            mWeakContext.clear();
            mWeakContext = null;
        }
    }


    /**
     * 显示图片
     */
    public void showImage() {
        if (mMediaMeta == null) {
            return;
        }
        Glide.with(mWeakContext.get()).asBitmap().load(mMediaMeta.getPath()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                mSource = resource;
                if (mWeakImageView != null && mWeakImageView.get() != null) {
                    mWeakImageView.get().setImageBitmap(mSource);
                }
            }
        });
    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        Log.d(TAG, "brightness = " + brightness);
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        Log.d(TAG, "contrast = " + contrast);
    }

    /**
     * 设置曝光
     * @param exposure
     */
    public void setExposure(float exposure) {
        Log.d(TAG, "exposure = " + exposure);
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        Log.d(TAG, "hue = " + hue);
    }

    /**
     * 设置饱和度 0.0 ~ 2.0之间
     * @param saturation
     */
    public void setSaturation(float saturation) {
        Log.d(TAG, "saturation = " + saturation);
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        Log.d(TAG, "sharpness = " + sharpness);
    }

}
