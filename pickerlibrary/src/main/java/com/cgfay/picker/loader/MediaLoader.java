package com.cgfay.picker.loader;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import android.net.Uri;
import android.widget.ImageView;

/**
 * 图片加载器
 */
public interface MediaLoader {

    /**
     * 加载缩略图
     * @param context
     * @param imageView
     * @param uri
     * @param placeholder
     * @param error
     */
    void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull Uri uri, @DrawableRes int placeholder, @DrawableRes int error);

    /**
     * 加载缩略图
     * @param context
     * @param resize        期望缩放大小
     * @param placeholder   占位图
     * @param imageView     显示的widget
     * @param uri           uri路径
     */
    void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull Uri uri, int resize, @DrawableRes int placeholder, @DrawableRes int error);

    /**
     * 加载图片
     * @param context
     * @param width         期望缩放的宽度
     * @param height        期望缩放的高度
     * @param imageView     显示的widget
     * @param uri          uri路径
     */
    void loadImage(@NonNull Context context, int width, int height, @NonNull ImageView imageView, @NonNull String uri);

    /**
     * 加载GIF缩略图
     * @param context       上下文
     * @param resize        尺寸
     * @param placeholder   占位图
     * @param imageView     显示的widget
     * @param uri           uri路径
     */
    void loadGifThumbnail(@NonNull Context context, @NonNull ImageView imageView, @NonNull Uri uri, int resize, @DrawableRes int placeholder, @DrawableRes int error);

    /**
     * 加载GIF缩略图
     * @param context       上下文
     * @param width         期望缩放的宽度
     * @param height        期望缩放的高度
     * @param imageView     显示的widget
     * @param uri           uri路径
     */
    void loadGif(@NonNull Context context, int width, int height, @NonNull ImageView imageView, @NonNull Uri uri);
}
