package com.cgfay.picker.selector;


import android.content.Context;
import androidx.annotation.NonNull;

import com.cgfay.picker.model.MediaData;

import java.util.List;

/**
 * 媒体选择器
 */
public interface OnMediaSelector {

    void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList);
}
