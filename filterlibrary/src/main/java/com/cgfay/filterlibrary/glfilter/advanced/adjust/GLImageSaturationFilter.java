package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 饱和度滤镜
 * 饱和度可以解决为彩色光所呈现的彩色的深浅程度，取决于彩色光中混入的白光的数量，
 * 饱和度是某种色光纯度的反映，饱和度越高，则深色越深
 * Created by cain.huang on 2017/7/21.
 */
public class GLImageSaturationFilter extends GLImageFilter {

    private static final String FRAGMENT_SATURATION = "" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform lowp float inputLevel;\n" +
            "// Values from \\\"Graphics Shaders: Theory and Practice\\\" by Bailey and Cunningham\n" +
            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
            "void main() {\n" +
            "    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    lowp float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
            "    lowp vec3 greyScaleColor = vec3(luminance);\n" +
            "    gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, inputLevel), textureColor.w);\n" +
            "}";

    private int mRangeMinHandle;
    private int mRangeMaxHandle;
    private int mInputLevelHandle;

    private float mSaturation;

    public GLImageSaturationFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SATURATION);
    }

    public GLImageSaturationFilter(Context context, String vertexShader, String fragementShader) {
        super(context, vertexShader, fragementShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mRangeMinHandle = GLES30.glGetUniformLocation(mProgramHandle, "rangeMin");
        mRangeMaxHandle = GLES30.glGetUniformLocation(mProgramHandle, "rangeMax");
        mInputLevelHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputLevel");
        setSaturationMin(new float[]{0.0f, 0.0f, 0.0f});
        setSaturationMax(new float[]{1.0f, 1.0f, 1.0f});
        setSaturation(1.0f);
    }

    /**
     * 设置饱和度值
     * @param saturation 0.0 ~ 2.0之间
     */
    public void setSaturation(float saturation) {
        if (saturation < 0.0f) {
            saturation = 0.0f;
        } else if (saturation > 2.0f) {
            saturation = 2.0f;
        }
        mSaturation = saturation;
        setFloat(mInputLevelHandle, mSaturation);
    }

    /**
     * 设置饱和度最小值
     * @param matrix
     */
    public void setSaturationMin(float[] matrix) {
        setFloatVec3(mRangeMinHandle, matrix);
    }

    /**
     * 设置饱和度最大值
     * @param matrix
     */
    public void setSaturationMax(float[] matrix) {
        setFloatVec3(mRangeMaxHandle, matrix);
    }
}
