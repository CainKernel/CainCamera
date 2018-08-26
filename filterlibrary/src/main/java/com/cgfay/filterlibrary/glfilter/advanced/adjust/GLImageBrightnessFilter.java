package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 光照亮度
 * 亮度是人眼的所感受到的光的明暗程度
 * 亮度是彩色光在量上的特征，如果没有色彩，则只有亮度的一维变量。
 * Created by cain on 2017/7/30.
 */

public class GLImageBrightnessFilter extends GLImageFilter {
    private static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;                                              \n" +
            "uniform sampler2D inputTexture;                                                    \n" +
            "uniform lowp float brightness;                                                     \n" +
            "                                                                                   \n" +
            "void main()                                                                        \n" +
            "{                                                                                  \n" +
            "    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);           \n" +
            "    gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);    \n" +
            "}                                                                                  ";

    private int mBrightnessHandle;
    private float mBrightness;

    public GLImageBrightnessFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBrightnessFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mBrightnessHandle = GLES30.glGetUniformLocation(mProgramHandle, "brightness");
        setBrightness(0);
    }

    /**
     * 设置亮度
     * @param brightness -1.0 to 1.0, 默认为0.0f
     */
    public void setBrightness(float brightness) {
        if (brightness < -1.0f) {
            brightness = -1.0f;
        } else if (brightness > 1.0f) {
            brightness = 1.0f;
        }
        mBrightness = brightness;
        setFloat(mBrightnessHandle, mBrightness);
    }
}
