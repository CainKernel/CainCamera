package com.cgfay.camera.widget;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.cgfay.cameralibrary.R;

import java.lang.ref.WeakReference;

public class PreviewMeasureListener implements CameraMeasureFrameLayout.OnMeasureListener {

    private final WeakReference<CameraMeasureFrameLayout> mWeakLayout;

    public PreviewMeasureListener(@NonNull CameraMeasureFrameLayout view) {
        mWeakLayout = new WeakReference<>(view);
    }

    @Override
    public void onMeasure(int width, int height) {
        if (mWeakLayout.get() != null) {
            calculatePreviewLayout(mWeakLayout.get(), width, height);
            mWeakLayout.get().setOnMeasureListener(null);
        }
    }

    /**
     * 计算预览布局
     */
    private void calculatePreviewLayout(@NonNull View preview, int widthPixel, int heightPixel) {
        float height = preview.getResources().getDimension(R.dimen.camera_tab_height);
        if (widthPixel * 1.0f / heightPixel > 9f/16f) {
            widthPixel = (int)(heightPixel * (9f / 16f));
            ViewGroup.LayoutParams params = preview.getLayoutParams();
            params.width = widthPixel;
            params.height = heightPixel;
            preview.setLayoutParams(params);
        } else if (widthPixel * 1.0f / (heightPixel - height) < 9f/16f) {
            ViewGroup.LayoutParams params = preview.getLayoutParams();
            params.width = widthPixel;
            params.height = (int)(heightPixel - height);
            preview.setLayoutParams(params);
        }
        preview.requestLayout();
    }

}
