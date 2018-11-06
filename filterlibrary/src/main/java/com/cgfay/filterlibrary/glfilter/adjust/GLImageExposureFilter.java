package com.cgfay.filterlibrary.glfilter.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 曝光
 * Created by cain.huang on 2017/8/8.
 */

public class GLImageExposureFilter extends GLImageFilter {

    private int mExposureHandle;
    private float mExposure;

    public GLImageExposureFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/adjust/fragment_exposure.glsl"));
    }

    public GLImageExposureFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mExposureHandle = GLES30.glGetUniformLocation(mProgramHandle, "exposure");
        setExposure(0);
    }

    /**
     * 设置曝光度
     * @param exposure -10.0 - 10.0, 默认为0.0f
     */
    public void setExposure(float exposure) {
        if (exposure < -10.0f) {
            exposure = -10.0f;
        } else if (exposure > 10.0f) {
            exposure = 10.0f;
        }
        mExposure = exposure;
        setFloat(mExposureHandle, mExposure);
    }
}
