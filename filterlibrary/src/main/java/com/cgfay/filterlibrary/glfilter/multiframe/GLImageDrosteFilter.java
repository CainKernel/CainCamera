package com.cgfay.filterlibrary.glfilter.multiframe;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 德罗斯特效应滤镜
 */
public class GLImageDrosteFilter extends GLImageFilter {

    private int mRepeatHandle;
    private float repeat;

    public GLImageDrosteFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/multiframe/fragment_droste.glsl"));
    }

    public GLImageDrosteFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mRepeatHandle = GLES30.glGetUniformLocation(mProgramHandle, "repeat");
            setRepeat(4);
        }
    }

    /**
     * 设置重复次数
     * @param repeat
     */
    public void setRepeat(int repeat) {
        this.repeat = repeat;
        setFloat(mRepeatHandle, repeat);
    }
}
