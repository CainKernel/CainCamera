package com.cgfay.picker.loader;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import android.widget.ImageView;

/**
 * 图片加载器
 */
public interface MediaLoader {

    /**
     * 加载缩略图
     * @param context
     * @param imageView
     * @param path
     * @param placeholder
     * @param error
     */
    void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull String path, @DrawableRes int placeholder, @DrawableRes int error);

    /**
     * 加载缩略图
     * @param context
     * @param resize        期望缩放大小
     * @param placeholder   占位图
     * @param imageView     显示的widget
     * @param path          路径
     */
    void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull String path, int resize, @DrawableRes int placeholder, @DrawableRes int error);

    /**
     * 加载图片
     * @param context
     * @param width         期望缩放的宽度
     * @param height        期望缩放的高度
     * @param imageView     显示的widget
     * @param path           路径
     */
    void loadImage(@NonNull Context context, int width, int height, @NonNull ImageView imageView, @NonNull String path);

    /**
     * 加载GIF缩略图
     * @param context
     * @param resize
     * @param placeholder
     * @param imageView
     * @param path
     */
    void loadGifThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull String path, int resize, @DrawableRes int placeholder, @DrawableRes int error);

    /**
     * 加载GIF缩略图
     * @param context
     * @param width         期望缩放的宽度
     * @param height        期望缩放的高度
     * @param imageView     显示的widget
     * @param path          路径
     */
    void loadGif(@NonNull Context context, int width, int height, @NonNull ImageView imageView, @NonNull String path);
}
