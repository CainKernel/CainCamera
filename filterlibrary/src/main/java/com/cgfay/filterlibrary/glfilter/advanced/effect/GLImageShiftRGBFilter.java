package com.cgfay.filterlibrary.glfilter.advanced.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * RGB通道偏移滤镜
 */
public class GLImageShiftRGBFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "\n" +
            "uniform float scale;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    vec2 uv = textureCoordinate.xy;\n" +
            "    vec2 scaleCoordinate = vec2((scale - 1.0) * 0.5 + uv.x / scale ,\n" +
            "                                (scale - 1.0) * 0.5 + uv.y / scale);\n" +
            "    vec4 smoothColor = texture2D(inputTexture, scaleCoordinate);\n" +
            "\n" +
            "    // 计算红色通道偏移值\n" +
            "    vec4 shiftRedColor = texture2D(inputTexture,\n" +
            "         scaleCoordinate + vec2(-0.1 * (scale - 1.0), - 0.1 *(scale - 1.0)));\n" +
            "\n" +
            "    // 计算绿色通道偏移值\n" +
            "    vec4 shiftGreenColor = texture2D(inputTexture,\n" +
            "         scaleCoordinate + vec2(-0.075 * (scale - 1.0), - 0.075 *(scale - 1.0)));\n" +
            "\n" +
            "    // 计算蓝色偏移值\n" +
            "    vec4 shiftBlueColor = texture2D(inputTexture,\n" +
            "         scaleCoordinate + vec2(-0.05 * (scale - 1.0), - 0.05 *(scale - 1.0)));\n" +
            "\n" +
            "    vec3 resultColor = vec3(shiftRedColor.r, shiftGreenColor.g, shiftBlueColor.b);\n" +
            "\n" +
            "    gl_FragColor = vec4(resultColor, smoothColor.a);\n" +
            "}";

    private int mScaleHandle;

    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageShiftRGBFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageShiftRGBFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mScaleHandle = GLES20.glGetUniformLocation(mProgramHandle, "scale");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        mScale = 1.0f + 0.3f * getInterpolation(mOffset);
        mOffset += 0.06f;
        if (mOffset > 1.0f) {
            mOffset = 0.0f;
        }
        GLES20.glUniform1f(mScaleHandle, mScale);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }
}
