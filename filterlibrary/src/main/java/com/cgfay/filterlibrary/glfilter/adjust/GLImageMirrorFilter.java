package com.cgfay.filterlibrary.glfilter.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 镜像翻转
 * Created by cain.huang on 2017/7/21.
 */
public class GLImageMirrorFilter extends GLImageFilter {

    private int mAngleHandle;
    private int mMirrorXHandle;
    private int mMirrorYHandle;

    private float mAngle;
    private float mMirrorX;
    private float mMirrorY;

    public GLImageMirrorFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/adjust/fragment_mirror.glsl"));
    }

    public GLImageMirrorFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mAngleHandle = GLES30.glGetUniformLocation(mProgramHandle, "Angle");
        mMirrorXHandle = GLES30.glGetUniformLocation(mProgramHandle, "MirrorX");
        mMirrorYHandle = GLES30.glGetUniformLocation(mProgramHandle, "MirrorY");
        setAngle(0.0f);
        setMirrorX(0.0f);
        setMirrorY(0.0f);
    }

    /**
     * 设置旋转角度
     * @param angle
     */
    public void setAngle(float angle) {
        mAngle = angle;
        setFloat(mAngleHandle, mAngle);
    }

    /**
     * x坐标
     * @param mirrorX
     */
    public void setMirrorX(float mirrorX) {
        mMirrorX = mirrorX;
        setFloat(mMirrorXHandle, mMirrorX);
    }

    /**
     * y坐标
     * @param mirrorY
     */
    public void setMirrorY(float mirrorY) {
        mMirrorY = mirrorY;
        setFloat(mMirrorYHandle, mMirrorY);
    }
}
