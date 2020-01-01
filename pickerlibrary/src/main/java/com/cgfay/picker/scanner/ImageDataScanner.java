package com.cgfay.picker.scanner;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;

import com.cgfay.picker.loader.MediaDataLoader;

public class ImageDataScanner extends MediaDataScanner {

    public ImageDataScanner(@NonNull Context context, @NonNull LoaderManager manager, IMediaDataReceiver dataReceiver) {
        super(context, manager, dataReceiver);
    }

    @Override
    protected int getLoaderId() {
        return IMAGE_LOADER_ID;
    }

    @Override
    protected int getMediaType() {
        return MediaDataLoader.LOAD_IMAGE;
    }
}
