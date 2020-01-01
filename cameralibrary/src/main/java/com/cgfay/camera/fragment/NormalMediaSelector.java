package com.cgfay.camera.fragment;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.cgfay.image.activity.ImageEditActivity;
import com.cgfay.picker.model.MediaData;
import com.cgfay.picker.selector.OnMediaSelector;
import com.cgfay.video.activity.VideoCutActivity;

import java.util.List;

/**
 * 普通选择器
 */
public class NormalMediaSelector implements OnMediaSelector {

    @Override
    public void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList) {
        MediaData mediaData = mediaDataList.get(0);
        if (mediaData.isVideo()) {
            Intent intent = new Intent(context, VideoCutActivity.class);
            intent.putExtra(VideoCutActivity.PATH, mediaData.getPath());
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, ImageEditActivity.class);
            intent.putExtra(ImageEditActivity.IMAGE_PATH, mediaData.getPath());
            context.startActivity(intent);
        }
    }
}
