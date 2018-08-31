package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 佛洛伊特
 * Created by cain.huang on 2017/11/16.
 */

public class GLImageFreudFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            " precision highp float;\n" +
            " varying mediump vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D randTexture;\n" +
            " \n" +
            " uniform float inputTextureHeight;\n" +
            " uniform float inputTextureWidth;\n" +
            " \n" +
            " float texture2Size = 1024.0;\n" +
            " \n" +
            " uniform float strength;\n" +
            "\n" +
            " float colorGray(vec4 color) {\n" +
            "    float gray = 0.2125 * color.r + 0.7154 * color.g + 0.0721 * color.b;\n" +
            "    \n" +
            "    return gray;\n" +
            " }\n" +
            " \n" +
            "\n" +
            " vec4 toneMapping(vec4 color) {\n" +
            "    \n" +
            "    vec4 mapped;\n" +
            "    mapped.r = texture2D(randTexture, vec2(color.r, 0.0)).r;\n" +
            "    mapped.g = texture2D(randTexture, vec2(color.g, 0.0)).g;\n" +
            "    mapped.b = texture2D(randTexture, vec2(color.b, 0.0)).b;\n" +
            "    mapped.a = color.a;\n" +
            "    \n" +
            "    return mapped;\n" +
            " }\n" +
            " \n" +
            " vec4 colorControl(vec4 color, float saturation, float brightness, float contrast) {\n" +
            "    float gray = colorGray(color);\n" +
            "    \n" +
            "    color.rgb = vec3(saturation) * color.rgb + vec3(1.0-saturation) * vec3(gray);\n" +
            "    color.r = clamp(color.r, 0.0, 1.0);\n" +
            "    color.g = clamp(color.g, 0.0, 1.0);\n" +
            "    color.b = clamp(color.b, 0.0, 1.0);\n" +
            "    \n" +
            "    color.rgb = vec3(contrast) * (color.rgb - vec3(0.5)) + vec3(0.5);\n" +
            "    color.r = clamp(color.r, 0.0, 1.0);\n" +
            "    color.g = clamp(color.g, 0.0, 1.0);\n" +
            "    color.b = clamp(color.b, 0.0, 1.0);\n" +
            "    \n" +
            "    color.rgb = color.rgb + vec3(brightness);\n" +
            "    color.r = clamp(color.r, 0.0, 1.0);\n" +
            "    color.g = clamp(color.g, 0.0, 1.0);\n" +
            "    color.b = clamp(color.b, 0.0, 1.0);\n" +
            "    \n" +
            "    return color;\n" +
            " }\n" +
            "\n" +
            " vec4 hueAdjust(vec4 color, float hueAdjust) {\n" +
            "    vec3 kRGBToYPrime = vec3(0.299, 0.587, 0.114);\n" +
            "    vec3 kRGBToI = vec3(0.595716, -0.274453, -0.321263);\n" +
            "    vec3 kRGBToQ = vec3(0.211456, -0.522591, 0.31135);\n" +
            "    \n" +
            "    vec3 kYIQToR   = vec3(1.0, 0.9563, 0.6210);\n" +
            "    vec3 kYIQToG   = vec3(1.0, -0.2721, -0.6474);\n" +
            "    vec3 kYIQToB   = vec3(1.0, -1.1070, 1.7046);\n" +
            "    \n" +
            "    float yPrime = dot(color.rgb, kRGBToYPrime);\n" +
            "    float I = dot(color.rgb, kRGBToI);\n" +
            "    float Q = dot(color.rgb, kRGBToQ);\n" +
            "    \n" +
            "    float hue = atan(Q, I);\n" +
            "    float chroma  = sqrt (I * I + Q * Q);\n" +
            "    \n" +
            "    hue -= hueAdjust;\n" +
            "    \n" +
            "    Q = chroma * sin (hue);\n" +
            "    I = chroma * cos (hue);\n" +
            "    \n" +
            "    color.r = dot(vec3(yPrime, I, Q), kYIQToR);\n" +
            "    color.g = dot(vec3(yPrime, I, Q), kYIQToG);\n" +
            "    color.b = dot(vec3(yPrime, I, Q), kYIQToB);\n" +
            "    \n" +
            "    return color;\n" +
            " }\n" +
            " \n" +
            " vec4 colorMatrix(vec4 color, float red, float green, float blue, float alpha, vec4 bias) {\n" +
            "    color = color * vec4(red, green, blue, alpha) + bias;\n" +
            "    \n" +
            "    return color;\n" +
            "}\n" +
            " \n" +
            "\n" +
            " vec4 multiplyBlend(vec4 overlay, vec4 base) {\n" +
            "    vec4 outputColor;\n" +
            "    \n" +
            "    float a = overlay.a + base.a * (1.0 - overlay.a);\n" +
            "\n" +
            "    outputColor.rgb = ((1.0-base.a) * overlay.rgb * overlay.a + (1.0-overlay.a) * base.rgb * base.a + overlay.a * base.a * overlay.rgb * base.rgb) / a;\n" +
            "    \n" +
            "    outputColor.a = a;\n" +
            "    \n" +
            "    return outputColor;\n" +
            " }\n" +
            " \n" +
            "\n" +
            " float pseudoRandom(vec2 co) {\n" +
            "    mediump float a = 12.9898;\n" +
            "    mediump float b = 78.233;\n" +
            "    mediump float c = 43758.5453;\n" +
            "    mediump float dt= dot(co.xy ,vec2(a,b));\n" +
            "    mediump float sn= mod(dt,3.14);\n" +
            "    return fract(sin(sn) * c);\n" +
            " }\n" +
            " \n" +
            " void main() {\n" +
            "    vec4 originColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    vec4 color = texture2D(inputTexture, textureCoordinate);\n" +
            "    \n" +
            "    color.a = 1.0;\n" +
            "\n" +
            "    color = colorControl(color, 0.5, 0.1, 0.9);\n" +
            "\n" +
            "\tfloat x = textureCoordinate.x * inputTextureWidth / texture2Size;\n" +
            "    float y = textureCoordinate.y * inputTextureHeight / texture2Size;\n" +
            "\n" +
            "    vec4 rd = texture2D(randTexture, textureCoordinate);\n" +
            "    rd = colorControl(rd, 1.0, 0.4, 1.2);\n" +
            "\n" +
            "    color = multiplyBlend(rd, color);\n" +
            "\n" +
            "    color = colorMatrix(color, 1.0, 1.0, 1.0, 1.0, vec4(-0.15, -0.15, -0.15, 0));\n" +
            "    \n" +
            "    color.rgb = mix(originColor.rgb, color.rgb, strength);\n" +
            "    gl_FragColor = color;\n" +
            "}";

    private int mRandTexture;
    private int mRandTextureHandle;

    private int mStrengthHandle;
    private int mInputTextureWidthHandle;
    private int mInputTextureHeightHandle;

    public GLImageFreudFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageFreudFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mRandTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "randTexture");
        mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength");
        mInputTextureWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputTextureWidth");
        mInputTextureHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputTextureHeight");
        createTexture();
        setStrength(1.0f);
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mRandTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/freud_rand.png");
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setFloat(mInputTextureWidthHandle, 1.0f / mImageWidth);
        setFloat(mInputTextureHeightHandle, 1.0f / mImageHeight);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mRandTexture);
        GLES30.glUniform1i(mRandTextureHandle, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, new int[]{mRandTexture}, 0);
    }

    /**
     * 设置强度
     * @param strength
     */
    public void setStrength(float strength) {
        setFloat(mStrengthHandle, strength);
    }
}
