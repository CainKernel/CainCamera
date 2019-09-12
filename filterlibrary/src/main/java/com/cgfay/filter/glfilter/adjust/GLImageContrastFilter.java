package com.cgfay.filter.glfilter.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

/**
 * 对比度
 * Created by cain.huang on 2017/8/8.
 */

public class GLImageContrastFilter extends GLImageFilter {

    private int mContrastHandle;
    private float mContrast;

    public GLImageContrastFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/adjust/fragment_contrast.glsl"));
    }

    public GLImageContrastFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mContrastHandle = GLES30.glGetUniformLocation(mProgramHandle, "contrast");
        setContrast(1.0f);
    }

    /**
     * 设置对比度
     * @param contrast 0.0 ~ 4.0, 默认1.0f
     */
    public void setContrast(float contrast) {
        if (contrast < 0.0f) {
            contrast = 0.0f;
        } else if (contrast > 4.0f) {
            contrast = 4.0f;
        }
        mContrast = contrast;
        setFloat(mContrastHandle, mContrast);
    }
}
