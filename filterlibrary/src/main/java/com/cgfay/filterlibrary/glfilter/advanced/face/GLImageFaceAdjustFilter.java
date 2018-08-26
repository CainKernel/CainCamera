package com.cgfay.filterlibrary.glfilter.advanced.face;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.model.Beauty;
import com.cgfay.filterlibrary.glfilter.model.FacePoints;
import com.cgfay.filterlibrary.glfilter.model.IBeautify;
import com.cgfay.filterlibrary.glfilter.model.IFacePoints;

/**
 * 美型滤镜基类
 */
public class GLImageFaceAdjustFilter extends GLImageFilter implements IBeautify, IFacePoints {

    // 美型参数
    protected Beauty mBeauty;
    // 关键点参数
    protected FacePoints mFacePoints;

    public GLImageFaceAdjustFilter(Context context) {
        super(context);
    }

    public GLImageFaceAdjustFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void onBeauty(Beauty beauty) {
        mBeauty = beauty;
    }

    @Override
    public void onFacePoints(FacePoints facePoints) {
        mFacePoints = facePoints;
    }
}
