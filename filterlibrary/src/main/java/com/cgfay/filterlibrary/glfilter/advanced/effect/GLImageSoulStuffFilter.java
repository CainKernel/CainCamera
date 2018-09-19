package com.cgfay.filterlibrary.glfilter.advanced.effect;

import android.content.Context;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 灵魂出窍滤镜
 */
public class GLImageSoulStuffFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "\n" +
            "uniform float scale;\n" +
            "\n" +
            "void main() {\n" +
            "     vec2 uv = textureCoordinate.xy;\n" +
            "     // 输入纹理\n" +
            "     vec4 sourceColor = texture2D(inputTexture, fract(uv));\n" +
            "     // 将纹理坐标中心转成(0.0, 0.0)再做缩放\n" +
            "     vec2 center = vec2(0.5, 0.5);\n" +
            "     uv -= center;\n" +
            "     uv = uv / scale;\n" +
            "     uv += center;\n" +
            "     // 缩放纹理\n" +
            "     vec4 scaleColor = texture2D(inputTexture, fract(uv));\n" +
            "     // 线性混合\n" +
            "     gl_FragColor = mix(sourceColor, scaleColor, 0.5 * (0.6 - fract(scale)));\n" +
            "}";

    private int mScaleHandle;

    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    public GLImageSoulStuffFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageSoulStuffFilter(Context context, String vertexShader, String fragmentShader) {
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
        mScale = 1.0f + 0.5f * getInterpolation(mOffset);
        mOffset += 0.04f;
        if (mOffset > 1.0f) {
            mOffset = 0.0f;
        }
        GLES20.glUniform1f(mScaleHandle, mScale);
    }

    private float getInterpolation(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}
