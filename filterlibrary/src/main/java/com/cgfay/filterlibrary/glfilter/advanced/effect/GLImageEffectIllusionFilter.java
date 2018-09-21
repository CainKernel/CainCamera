package com.cgfay.filterlibrary.glfilter.advanced.effect;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 幻觉滤镜
 */
public class GLImageEffectIllusionFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;     // 当前输入纹理\n" +
            "uniform sampler2D inputTextureLast; // 上一次的纹理\n" +
            "uniform sampler2D lookupTable;      // 颜色查找表纹理\n" +
            "\n" +
            "// 分RGB通道混合，不同颜色通道混合值不一样\n" +
            "const lowp vec3 blendValue = vec3(0.1, 0.3, 0.6);\n" +
            "\n" +
            "// 计算lut映射之后的颜色值\n" +
            "vec4 getLutColor(vec4 textureColor, sampler2D lookupTexture) {\n" +
            "    mediump float blueColor = textureColor.b * 63.0;\n" +
            "\n" +
            "    mediump vec2 quad1;\n" +
            "    quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "    quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "\n" +
            "    mediump vec2 quad2;\n" +
            "    quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "    quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "\n" +
            "    highp vec2 texPos1;\n" +
            "    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "\n" +
            "    highp vec2 texPos2;\n" +
            "    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "\n" +
            "    lowp vec4 newColor1 = texture2D(lookupTexture, texPos1);\n" +
            "    lowp vec4 newColor2 = texture2D(lookupTexture, texPos2);\n" +
            "\n" +
            "    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "    vec4 color = vec4(newColor.rgb, textureColor.w);\n" +
            "    return color;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    // 当前纹理颜色\n" +
            "    vec4 currentColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    // 上一轮纹理颜色\n" +
            "    vec4 lastColor = texture2D(inputTextureLast, textureCoordinate);\n" +
            "    // lut映射的颜色值\n" +
            "    vec4 lutColor = getLutColor(currentColor, lookupTable);\n" +
            "    // 将lut映射之后的纹理与上一轮的纹理进行线性混合\n" +
            "    gl_FragColor = vec4(mix(lastColor.rgb, lutColor.rgb, blend), currentColor.a);\n" +
            "}";

    private int mLastTextureHandle;
    private int mLookupTableHandle;
    private int mLastTexture;
    private int mLookupTable;

    public GLImageEffectIllusionFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageEffectIllusionFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mLastTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputTextureLast");
            mLookupTableHandle = GLES30.glGetUniformLocation(mProgramHandle, "lookupTable");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();

        // 绑定上一次纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mLastTexture);
        GLES30.glUniform1i(mLastTextureHandle, 1);

        // 绑定lut纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mLookupTable);
        GLES30.glUniform1i(mLastTextureHandle, 2);
    }

    /**
     * 设置上一次纹理id
     * @param lastTexture
     */
    public void setLastTexture(int lastTexture) {
        mLastTexture = lastTexture;
    }

    /**
     * 设置lut纹理id
     * @param lookupTableTexture
     */
    public void setLookupTable(int lookupTableTexture) {
        mLookupTable = lookupTableTexture;
    }
}
