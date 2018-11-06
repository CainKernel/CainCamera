package com.cgfay.cameralibrary.loader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

/**
 * 加载图片
 */
public interface MediaLoader {

    /**
     * 加载缩略图
     * @param context
     * @param placeholder   占位图
     * @param imageView     显示的widget
     * @param uri           路径
     */
    void loadThumbnail(Context context, Drawable placeholder, ImageView imageView, Uri uri);

    /**
     * 加载图片
     * @param context
     * @param width         期望缩放的宽度
     * @param height        期望缩放的高度
     * @param imageView     显示的widget
     * @param uri           路径
     */
    void loadImage(Context context, int width, int height, ImageView imageView, Uri uri);

    /**
     * 加载GIF缩略图
     * @param context
     * @param placeholder
     * @param imageView
     * @param uri
     */
    void loadGifThumbnail(Context context, Drawable placeholder, ImageView imageView, Uri uri);

    /**
     * 加载GIF缩略图
     * @param context
     * @param width         期望缩放的宽度
     * @param height        期望缩放的高度
     * @param imageView     显示的widget
     * @param uri           路径
     */
    void loadGif(Context context, int width, int height, ImageView imageView, Uri uri);

}
