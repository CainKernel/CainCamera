package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 色调
 * 色调是色彩的类别，比如红绿蓝，取决于彩色光的光谱成分。
 * Created by cain on 2017/7/30.
 */

public class GLImageHueFilter extends GLImageFilter {
    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;                                                 \n" +
            "varying highp vec2 textureCoordinate;                                  \n" +
            "                                                                       \n" +
            "uniform sampler2D inputTexture;                                        \n" +
            "uniform mediump float hueAdjust;                                       \n" +
            "const highp vec4 kRGBToYPrime = vec4 (0.299, 0.587, 0.114, 0.0);       \n" +
            "const highp vec4 kRGBToI = vec4 (0.595716, -0.274453, -0.321263, 0.0); \n" +
            "const highp vec4 kRGBToQ = vec4 (0.211456, -0.522591, 0.31135, 0.0);   \n" +
            "                                                                       \n" +
            "const highp vec4 kYIQToR = vec4 (1.0, 0.9563, 0.6210, 0.0);            \n" +
            "const highp vec4 kYIQToG = vec4 (1.0, -0.2721, -0.6474, 0.0);          \n" +
            "const highp vec4 kYIQToB = vec4 (1.0, -1.1070, 1.7046, 0.0);           \n" +
            "                                                                       \n" +
            "void main ()                                                           \n" +
            "{                                                                      \n" +
            "   highp vec4 color = texture2D(inputTexture, textureCoordinate);      \n" +
            "                                                                       \n" +
            "   // Convert to YIQ                                                   \n" +
            "   highp float YPrime = dot (color, kRGBToYPrime);                     \n" +
            "   highp float I = dot (color, kRGBToI);                               \n" +
            "   highp float Q = dot (color, kRGBToQ);                               \n" +
            "                                                                       \n" +
            "   // Calculate the hue and chroma                                     \n" +
            "   highp float hue = atan (Q, I);                                      \n" +
            "   highp float chroma = sqrt (I * I + Q * Q);                          \n" +
            "                                                                       \n" +
            "   // Make the user's adjustments                                      \n" +
            "   hue += (-hueAdjust); //why negative rotation?                       \n" +
            "                                                                       \n" +
            "   // Convert back to YIQ                                              \n" +
            "   Q = chroma * sin (hue);                                             \n" +
            "   I = chroma * cos (hue);                                             \n" +
            "                                                                       \n" +
            "   // Convert back to RGB                                              \n" +
            "   highp vec4 yIQ = vec4 (YPrime, I, Q, 0.0);                          \n" +
            "   color.r = dot (yIQ, kYIQToR);                                       \n" +
            "   color.g = dot (yIQ, kYIQToG);                                       \n" +
            "   color.b = dot (yIQ, kYIQToB);                                       \n" +
            "                                                                       \n" +
            "   // Save the result                                                  \n" +
            "   gl_FragColor = color;                                               \n" +
            "}                                                                      ";

    private int mHueAdjustHandle;
    private float mHue;

    public GLImageHueFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
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
