package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 布鲁克林滤镜
 * Created by cain.huang on 2017/11/16.
 */

public class GLImageBrooklynFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            " precision mediump float;\n" +
            " varying mediump vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D curveTexture;\n" +
            " uniform sampler2D mapTexture;\n" +
            " uniform sampler2D curveTexture1;\n" +
            " \n" +
            " uniform float strength;\n" +
            "\n" +
            " /**\n" +
            "  * 灰度\n" +
            "  */\n" +
            " float colorGray(vec4 color) {\n" +
            "    float gray = 0.2125 * color.r + 0.7154 * color.g + 0.0721 * color.b;\n" +
            "    \n" +
            "    return gray;\n" +
            " }\n" +
            " \n" +
            " /**\n" +
            "  * 色调映射\n" +
            "  */\n" +
            " vec4 toneMapping(vec4 color) {\n" +
            "    vec4 mapped;\n" +
            "    mapped.r = texture2D(curveTexture, vec2(color.r, 0.0)).r;\n" +
            "    mapped.g = texture2D(curveTexture, vec2(color.g, 0.0)).g;\n" +
            "    mapped.b = texture2D(curveTexture, vec2(color.b, 0.0)).b;\n" +
            "    mapped.a = color.a;\n" +
            "    \n" +
            "    return mapped;\n" +
            " }\n" +
            " \n" +
            " /**\n" +
            "  * 颜色控制\n" +
            "  */\n" +
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
            " \n" +
            " /**\n" +
            "  * 色调调整\n" +
            "  */\n" +
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
            " /**\n" +
            "  * 颜色矩阵\n" +
            "  */\n" +
            " vec4 colorMatrix(vec4 color, float red, float green, float blue, float alpha, vec4 bias) {\n" +
            "    color = color * vec4(red, green, blue, alpha) + bias;\n" +
            "    \n" +
            "    return color;\n" +
            " }\n" +
            " \n" +
            " /**\n" +
            "  * 混合\n" +
            "  */\n" +
            " vec4 multiplyBlend(vec4 overlay, vec4 base){\n" +
            "    vec4 outputColor;\n" +
            "    \n" +
            "    float a = overlay.a + base.a * (1.0 - overlay.a);\n" +
            "\n" +
            "    outputColor.rgb = ((1.0-base.a) * overlay.rgb * overlay.a +\n" +
            "                      (1.0-overlay.a) * base.rgb * base.a +\n" +
            "                      overlay.a * base.a * overlay.rgb * base.rgb) / a;\n" +
            "\n" +
            "    outputColor.a = a;\n" +
            "    \n" +
            "    return outputColor;\n" +
            " }\n" +
            " \n" +
            " void main() {\n" +
            "    vec4 originColor = texture2D(inputTexture, textureCoordinate);\n" +
            "\n" +
            "    vec4 color = texture2D(inputTexture, textureCoordinate);\n" +
            "    color.a = 1.0;\n" +
            "    // 色调映射\n" +
            "    color = toneMapping(color);\n" +
            "    // 颜色控制\n" +
            "    color = colorControl(color, 0.88, 0.03, 0.85);\n" +
            "    // 色调调整\n" +
            "    color = hueAdjust(color, -0.0444);\n" +
            "    // 混合\n" +
            "    vec4 bg = vec4(0.5647, 0.1961, 0.0157, 0.14);\n" +
            "    color = multiplyBlend(bg, color);\n" +
            "    vec4 bg2 = texture2D(mapTexture, textureCoordinate);\n" +
            "    bg2.a *= 0.9;\n" +
            "    color = multiplyBlend(bg2, color);\n" +
            "    \n" +
            "    // 色调映射\n" +
            "    color = toneMapping(color);\n" +
            "    \n" +
            "    color.rgb = mix(originColor.rgb, color.rgb, strength);\n" +
            "\n" +
            "    gl_FragColor = color;\n" +
            " }";

    private float mStrength;
    private int mStrengthHandle;

    private int mCurveTexture;
    private int mCurveTextureHandle;

    private int mMapTexture;
    private int mMapTextureHandle;

    private int mCurveTexture1;
    private int mCurveTexture1Handle;

    public GLImageBrooklynFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBrooklynFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength");
        mCurveTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        mMapTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");
        mCurveTexture1Handle = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture1");
        createTexture();
        setStrength(1.0f);
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mCurveTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/brooklyn_curves.png");
        mMapTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/brooklyn_map.png");
        mCurveTexture1 = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/brooklyn_curves1.png");

    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mCurveTexture);
        GLES30.glUniform1i(mCurveTextureHandle, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureHandle, 2);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mCurveTexture1);
        GLES30.glUniform1i(mCurveTexture1Handle, 3);

    }

    @Override
    public void release() {
        GLES30.glDeleteTextures(3, new int[]{mCurveTexture, mMapTexture, mCurveTexture1}, 0);
        super.release();
    }

    /**
     * 设置强度
     * @param strength
     */
    public void setStrength(float strength) {
        setFloat(mStrengthHandle, strength);
    }
}
