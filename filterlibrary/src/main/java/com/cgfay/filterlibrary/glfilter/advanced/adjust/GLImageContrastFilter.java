package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 对比度
 * Created by cain.huang on 2017/8/8.
 */

public class GLImageContrastFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;                                                              \n" +
            "                                                                                                   \n" +
            "uniform sampler2D inputTexture;                                                                    \n" +
            "uniform lowp float contrast;                                                                       \n" +
            "                                                                                                   \n" +
            "void main()                                                                                        \n" +
            "{                                                                                                  \n" +
            "     lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);                          \n" +
            "                                                                                                   \n" +
            "     gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w); \n" +
            "}                                                                                                  ";


    private int mContrastHandle;
    private float mContrast;

    public GLImageContrastFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageContrastFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mContrastHandle = GLES30.glGetUniformLocation(mProgramHandle, "contrast");
        setContrast(1.0f);
    }

    /**
     * 设置对比度
     * @param contrast 0.0 ~ 4.0, 默认1.0f
     */
    public void setContrast(float contrast) {
        if (contrast < 0.0f) {
            contrast = 0.0f;
        } else if (contrast > 4.0f) {
            contrast = 4.0f;
        }
        mContrast = contrast;
        setFloat(mContrastHandle, mContrast);
    }
}
