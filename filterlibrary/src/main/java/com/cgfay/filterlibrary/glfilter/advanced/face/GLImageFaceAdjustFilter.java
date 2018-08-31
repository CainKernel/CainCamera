package com.cgfay.filterlibrary.glfilter.advanced.face;

import android.content.Context;
import android.util.Log;

import com.cgfay.filterlibrary.glfilter.base.GLImageDrawElementsFilter;
import com.cgfay.filterlibrary.glfilter.model.Beauty;
import com.cgfay.filterlibrary.glfilter.model.FacePoints;
import com.cgfay.filterlibrary.glfilter.model.IBeautify;
import com.cgfay.filterlibrary.glfilter.model.IFacePoints;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 美型滤镜
 */
public class GLImageFaceAdjustFilter extends GLImageDrawElementsFilter implements IBeautify, IFacePoints {

    public GLImageFaceAdjustFilter(Context context) {
        super(context);
    }

    public GLImageFaceAdjustFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    /**
     * 美型参数
     * @param beauty
     */
    @Override
    public void onBeauty(Beauty beauty) {

    }

    @Override
    public void onFacePoints(FacePoints facePoints) {

    }
}
