package com.cgfay.cainfilter.glfilter.image;

import android.opengl.GLES30;

import com.cgfay.cainfilter.glfilter.base.BaseImageFilter;

/**
 * 饱和度滤镜
 * 饱和度可以解决为彩色光所呈现的彩色的深浅程度，取决于彩色光中混入的白光的数量，
 * 饱和度是某种色光纯度的反映，饱和度越高，则深色越深
 * Created by cain.huang on 2017/7/21.
 */
public class SaturationFilter extends BaseImageFilter {

    // 详细解释请参照assets/shaders/fragment_saturation.glsl文件
//    private static final String FRAGMENT_SATURATION =
//            "precision mediump float;                                               \n" +
//            "varying vec2 textureCoordinate;                                        \n" +
//            "uniform vec4 rangeMin;                                                 \n" +
//            "uniform vec4 rangeMax;                                                 \n" +
//            "uniform sampler2D inputTexture;                                            \n" +
//            "uniform lowp float inputLevel;                                         \n" +
//            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);  \n" +
//            "void main() {                                                          \n" +
//            "  vec4 source = texture2D(inputTexture, textureCoordinate);                \n" +
//            "  vec3 dest = source.rgb;                                              \n" +
//            "  float fMin = dot(rangeMin.rgb, luminanceWeighting);                  \n" +
//            "  float fMax = dot(rangeMax.rgb, luminanceWeighting);                  \n" +
//            "  fMax = 1.0 - (1.0 - fMax) / 1.5;                                     \n" +
//            "  fMax = max(0.5, fMax);                                               \n" +
//            "  float diff = fMax - fMin;                                            \n" +
//            "  dest.r = (dest.r - fMin) / diff;                                     \n" +
//            "  dest.g = (dest.g - fMin) / diff;                                     \n" +
//            "  dest.b = (dest.b - fMin) / diff;                                     \n" +
//            "  float level = clamp(inputLevel, 0.0, 1.0);                           \n" +
//            "  dest = mix(source.rgb, dest, level);                                 \n" +
//            "  gl_FragColor = vec4(dest, source.a);                                 \n" +
//            "}                                                                      \n";

    private static final String FRAGMENT_SATURATION =
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

    private int mRangeMinLoc;
    private int mRangeMaxLoc;
    private int mInputLevelLoc;

    public SaturationFilter() {
        this(VERTEX_SHADER, FRAGMENT_SATURATION);
    }

    public SaturationFilter(String vertexShader, String fragementShader) {
        super(vertexShader, fragementShader);
        mRangeMinLoc = GLES30.glGetUniformLocation(mProgramHandle, "rangeMin");
        mRangeMaxLoc = GLES30.glGetUniformLocation(mProgramHandle, "rangeMax");
        mInputLevelLoc = GLES30.glGetUniformLocation(mProgramHandle, "inputLevel");
        setSaturationMin(new float[]{0.0f, 0.0f, 0.0f});
        setSaturationMax(new float[]{1.0f, 1.0f, 1.0f});
        setSaturationLevel(1.0f);
    }

    /**
     * 设置饱和度值
     * @param value 0.0 ~ 2.0之间
     */
    public void setSaturationLevel(float value) {
        setFloat(mInputLevelLoc, value);
    }

    /**
     * 设置饱和度最小值
     * @param matrix
     */
    public void setSaturationMin(float[] matrix) {
        setFloatVec3(mRangeMinLoc, matrix);
    }

    /**
     * 设置饱和度最大值
     * @param matrix
     */
    public void setSaturationMax(float[] matrix) {
        setFloatVec3(mRangeMaxLoc, matrix);
    }
}
