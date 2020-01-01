package com.cgfay.picker.scanner;

import androidx.annotation.NonNull;

import com.cgfay.picker.model.MediaData;

import java.util.List;

public interface IMediaDataReceiver {

    void onMediaDataObserve(@NonNull List<MediaData> mediaDataList);
}
