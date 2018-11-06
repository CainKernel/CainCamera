package com.cgfay.filterlibrary.glfilter.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 色调
 * 色调是色彩的类别，比如红绿蓝，取决于彩色光的光谱成分。
 * Created by cain on 2017/7/30.
 */

public class GLImageHueFilter extends GLImageFilter {

    private int mHueAdjustHandle;
    private float mHue;

    public GLImageHueFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_hue.glsl"));
    }

    public GLImageHueFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mHueAdjustHandle = GLES30.glGetUniformLocation(mProgramHandle, "hueAdjust");
        setHue(0);
    }

    /**
     * 设置色调 0 ~ 360
     * @param hue
     */
    public void setHue(float hue) {
        mHue = hue;
        float hueAdjust = (mHue % 360.0f) * (float) Math.PI / 180.0f;
        setFloat(mHueAdjustHandle, hueAdjust);
    }
}
