package com.cgfay.medialibrary.listener;

import android.net.Uri;

import java.util.List;

/**
 * 媒体选择监听器
 */
public interface OnMediaSelectedListener {

    void onSelected(List<Uri> uriList, List<String> pathList, boolean isVideo);
}
