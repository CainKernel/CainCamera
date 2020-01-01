package com.cgfay.camera.camera;

import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.lang.ref.WeakReference;

/**
 * 预览帧分析器
 */
public class PreviewCallbackAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "PreviewCallbackAnalyzer";
    private static final boolean VERBOSE = true;

    private PreviewCallback mPreviewCallback;

    private final WeakReference<CameraXController> mWeakController;

    public PreviewCallbackAnalyzer(CameraXController controller, PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
        mWeakController = new WeakReference<>(controller);
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (mWeakController.get() != null) {
            mWeakController.get().setOrientation(rotationDegrees);
        }
        long start = System.currentTimeMillis();
        if (VERBOSE) {
            Log.d(TAG, "analyze: timestamp - " + image.getTimestamp() + ", orientation - " + rotationDegrees + ", imageFormat - " + image.getFormat());
        }
        if (mPreviewCallback != null && image.getImage() != null) {
            byte[] data = ImageConvert.getDataFromImage(image.getImage(), ImageConvert.COLOR_FORMAT_NV21);
            if (data != null) {
                mPreviewCallback.onPreviewFrame(data);
            }
        }
        if (VERBOSE) {
            Log.d(TAG, "convert cost time - " + (System.currentTimeMillis() - start));
        }
    }
}
