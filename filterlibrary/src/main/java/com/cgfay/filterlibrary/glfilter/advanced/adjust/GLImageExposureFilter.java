package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 曝光
 * Created by cain.huang on 2017/8/8.
 */

public class GLImageExposureFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;                                          \n" +
            "                                                                               \n" +
            "uniform sampler2D inputTexture;                                                \n" +
            "uniform highp float exposure;                                                  \n" +
            "                                                                               \n" +
            "void main()                                                                    \n" +
            "{                                                                              \n" +
            "   highp vec4 textureColor = texture2D(inputTexture, textureCoordinate);       \n" +
            "                                                                               \n" +
            "   gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w); \n" +
            "}                                                                              ";


    private int mExposureHandle;
    private float mExposure;

    public GLImageExposureFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageExposureFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mExposureHandle = GLES30.glGetUniformLocation(mProgramHandle, "exposure");
        setExposure(0);
    }

    /**
     * 设置曝光度
     * @param exposure -10.0 - 10.0, 默认为0.0f
     */
    public void setExposure(float exposure) {
        if (exposure < -10.0f) {
            exposure = -10.0f;
        } else if (exposure > 10.0f) {
            exposure = 10.0f;
        }
        mExposure = exposure;
        setFloat(mExposureHandle, mExposure);
    }
}
