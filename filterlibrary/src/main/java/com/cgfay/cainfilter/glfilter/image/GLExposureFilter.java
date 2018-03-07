package com.cgfay.cainfilter.glfilter.image;

import android.opengl.GLES30;

import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilter;

/**
 * 曝光
 * Created by cain.huang on 2017/8/8.
 */

public class GLExposureFilter extends GLBaseImageFilter {

    private static final String FRAGMENT_SHADER =
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


    private int mExposureLoc;
    private float mExposure;

    public GLExposureFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLExposureFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mExposureLoc = GLES30.glGetUniformLocation(mProgramHandle, "exposure");
        setExposure(0);
    }

    /**
     * 设置曝光度
     * @param exposure
     */
    public void setExposure(float exposure) {
        mExposure = exposure;
        setFloat(mExposureLoc, mExposure);
    }
}
