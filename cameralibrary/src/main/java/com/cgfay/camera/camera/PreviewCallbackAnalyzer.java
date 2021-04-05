package com.cgfay.camera.camera;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

/**
 * 预览帧分析器
 */
public class PreviewCallbackAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "PreviewCallbackAnalyzer";
    private static final boolean VERBOSE = false;

    private PreviewCallback mPreviewCallback;


    public PreviewCallbackAnalyzer(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(@NonNull ImageProxy image) {
        long start = System.currentTimeMillis();
        if (VERBOSE) {
            Log.d(TAG, "analyze: timestamp - " + image.getImageInfo().getTimestamp() + ", " +
                    "orientation - " + image.getImageInfo().getRotationDegrees() + ", imageFormat" +
                    " - " + image.getFormat());
        }
        if (mPreviewCallback != null && image.getImage() != null) {
            byte[] data = ImageConvert.getDataFromImage(image.getImage(),
                    ImageConvert.COLOR_FORMAT_NV21);
            if (data != null) {
                mPreviewCallback.onPreviewFrame(data);
            }
        }
        // 使用完需要释放，否则下一次不会回调了
        image.close();
        if (VERBOSE) {
            Log.d(TAG, "convert cost time - " + (System.currentTimeMillis() - start));
        }
    }
}
