package com.cgfay.picker;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.cgfay.picker.loader.MediaLoader;

/**
 * 使用Glide加载缩略图
 */
class PickerMediaLoader implements MediaLoader {

    @Override
    public void loadThumbnail(@NonNull Context context, @NonNull ImageView imageView,
                              @NonNull Uri uri, int placeholder, int error) {
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(new RequestOptions()
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadThumbnail(Context context, @NonNull ImageView imageView, @NonNull Uri path,
                              int resize, @DrawableRes int placeholder, @DrawableRes int error) {
        Glide.with(context)
                .asBitmap()
                .load(path)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int width, int height, @NonNull ImageView imageView,
                          @NonNull String path) {
        Glide.with(context)
                .load(path)
                .apply(new RequestOptions()
                        .override(width, height)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(@NonNull Context context, @NonNull ImageView imageView,
                                 @NonNull Uri uri, int resize, @DrawableRes int placeholder,
                                 @DrawableRes int error) {
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .error(error)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadGif(@NonNull Context context, int width, int height,
                        @NonNull ImageView imageView, @NonNull Uri uri) {
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(new RequestOptions()
                        .override(width, height)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }
}
