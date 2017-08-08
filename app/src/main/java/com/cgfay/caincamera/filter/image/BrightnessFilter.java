package com.cgfay.caincamera.filter.image;

import android.opengl.GLES30;

import com.cgfay.caincamera.filter.base.BaseImageFilter;

/**
 * 光照亮度
 * 亮度是人眼的所感受到的光的明暗程度
 * 亮度是彩色光在量上的特征，如果没有色彩，则只有亮度的一维变量。
 * Created by cain on 2017/7/30.
 */

public class BrightnessFilter extends BaseImageFilter {
    private static final String FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;                                              \n" +
            "uniform sampler2D inputTexture;                                                    \n" +
            "uniform lowp float brightness;                                                     \n" +
            "                                                                                   \n" +
            "void main()                                                                        \n" +
            "{                                                                                  \n" +
            "    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);           \n" +
            "    gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);    \n" +
            "}                                                                                  ";

    private int mBrightnessLoc;
    private float mBrightness;

    public BrightnessFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public BrightnessFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mBrightnessLoc = GLES30.glGetUniformLocation(mProgramHandle, "brightness");
        setBrightness(0);
    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        mBrightness = brightness;
        setFloat(mBrightnessLoc, mBrightness);
    }
}
