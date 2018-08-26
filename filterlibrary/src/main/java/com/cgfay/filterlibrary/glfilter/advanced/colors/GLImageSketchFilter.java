package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 素描滤镜效果
 * Created by cain.huang on 2017/8/8.
 */

public class GLImageSketchFilter extends GLImageFilter {
    private static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;                                  \n" +
            "precision mediump float;                                               \n" +
            "                                                                       \n" +
            "uniform sampler2D inputTexture;                                        \n" +
            "uniform vec2 singleStepOffset;                                         \n" +
            "uniform float strength;                                                \n" +
            "                                                                       \n" +
            "const highp vec3 W = vec3(0.299,0.587,0.114);                          \n" +
            "                                                                       \n" +
            "void main()                                                            \n" +
            "{                                                                      \n" +
            "    float threshold = 0.0;                                             \n" +
            "    //pic1                                                             \n" +
            "    vec4 oralColor = texture2D(inputTexture, textureCoordinate);       \n" +
            "                                                                       \n" +
            "    //pic2                                                             \n" +
            "    vec3 maxValue = vec3(0.0, 0.0, 0.0);                               \n" +
            "                                                                       \n" +
            "    for(int i = -2; i<=2; i++)                                         \n" +
            "    {                                                                  \n" +
            "        for(int j = -2; j<=2; j++)                                     \n" +
            "        {                                                              \n" +
            "            vec4 tempColor = texture2D(inputTexture,                   \n" +
            "                   textureCoordinate + singleStepOffset * vec2(i, j)); \n" +
            "            maxValue.r = max(maxValue.r, tempColor.r);                 \n" +
            "            maxValue.g = max(maxValue.g, tempColor.g);                 \n" +
            "            maxValue.b = max(maxValue.b, tempColor.b);                 \n" +
            "            threshold += dot(tempColor.rgb, W);                        \n" +
            "        }                                                              \n" +
            "    }                                                                  \n" +
            "    //pic3                                                             \n" +
            "    float gray1 = dot(oralColor.rgb, W);                               \n" +
            "                                                                       \n" +
            "    //pic4                                                             \n" +
            "    float gray2 = dot(maxValue, W);                                    \n" +
            "                                                                       \n" +
            "    //pic5                                                             \n" +
            "    float contour = gray1 / gray2;                                     \n" +
            "                                                                       \n" +
            "    threshold = threshold / 25.;                                       \n" +
            "    float alpha = max(1.0,gray1>threshold?1.0:(gray1/threshold));      \n" +
            "                                                                       \n" +
            "    float result = contour * alpha + (1.0-alpha)*gray1;                \n" +
            "                                                                       \n" +
            "    gl_FragColor = vec4(vec3(result,result,result), oralColor.w);      \n" +
            "}                                                                      ";

    private int mSingleStepOffsetLoc;
    private int mStrengthLoc;
    private float mStrength; // 0.0 ~ 1.0f之间

    public GLImageSketchFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageSketchFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mSingleStepOffsetLoc = GLES30.glGetUniformLocation(mProgramHandle, "singleStepOffset");
        mStrengthLoc = GLES30.glGetUniformLocation(mProgramHandle, "strength");
        setStrength(0.5f);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    /**
     * 设置偏移值
     * @param width
     * @param height
     */
    private void setTexelSize(float width, float height) {
        setFloatVec2(mSingleStepOffsetLoc, new float[]{1.0f / width, 1.0f / height});
    }

    /**
     * 设置强度
     * @param strength 0.0 ~ 1.0f之间
     */
    public void setStrength(float strength) {
        mStrength = strength;
        setFloat(mStrengthLoc, mStrength);
    }
}
