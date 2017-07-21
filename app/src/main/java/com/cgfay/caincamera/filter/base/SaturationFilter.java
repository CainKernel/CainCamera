package com.cgfay.caincamera.filter.base;

import android.opengl.GLES20;

/**
 * 饱和度滤镜
 * Created by cain.huang on 2017/7/21.
 */
public class SaturationFilter extends BaseImageFilter {

    // 详细解释请参照assets/shaders/fragment_saturation.glsl文件
//    private static final String FRAGMENT_SATURATION =
//            "precision mediump float;                                               \n" +
//            "varying vec2 textureCoordinate;                                        \n" +
//            "uniform vec4 rangeMin;                                                 \n" +
//            "uniform vec4 rangeMax;                                                 \n" +
//            "uniform sampler2D sTexture;                                            \n" +
//            "uniform lowp float inputLevel;                                         \n" +
//            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);  \n" +
//            "void main() {                                                          \n" +
//            "  vec4 source = texture2D(sTexture, textureCoordinate);                \n" +
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

//    private static final String FRAGMENT_SATURATION = "precision mediump float;\n" +
//            "varying highp vec2 textureCoordinate;\n" +
//            "uniform sampler2D sTexture;\n" +
//            "uniform lowp float inputLevel;\n" +
//            "// Values from \\\"Graphics Shaders: Theory and Practice\\\" by Bailey and Cunningham\n" +
//            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
//            "void main() {\n" +
//            "    lowp vec4 textureColor = texture2D(sTexture, textureCoordinate);\n" +
//            "    lowp float luminance = dot(textureColor.rgb, luminanceWeighting);\n" +
//            "    lowp vec3 greyScaleColor = vec3(luminance);\n" +
//            "    gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, inputLevel), textureColor.w);\n" +
//            "}";

    private static final String FRAGMENT_SATURATION = "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform vec4 rangeMin;  // 最小值，比如 rgb(0.0f, 0.0f, 0.0f)\n" +
            "uniform vec4 rangeMax;  // 最大值，比如 rgb(1.0f, 1.0f, 1.0f)\n" +
            "uniform sampler2D sTexture;\n" +
            "uniform lowp float inputLevel;   // 变化的等级, 0 ~ 1之间\n" +
            "const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
            "void main() {\n" +
            "  vec4 source = texture2D(sTexture, textureCoordinate);\n" +
            "  vec3 dest = source.rgb;\n" +
            "  if (inputLevel >= 0.5) {\n" +
            "    float fMin = dot(rangeMin.rgb, luminanceWeighting);\n" +
            "    float fMax = dot(rangeMax.rgb, luminanceWeighting);\n" +
            "    fMax = 1.0 - (1.0 - fMax) / 1.5;\n" +
            "    fMax = max(0.5, fMax);\n" +
            "    float diff = fMax - fMin;\n" +
            "    // 计算差分\n" +
            "    dest.r = (dest.r - fMin) / diff;\n" +
            "    dest.g = (dest.g - fMin) / diff;\n" +
            "    dest.b = (dest.b - fMin) / diff;\n" +
            "    float level = clamp(inputLevel - 0.5, 0.0, 1.0);\n" +
            "    // dest = source.rgb * (1 - levle) + dest * level;\n" +
            "    dest = mix(source.rgb, dest, level);\n" +
            "  } else {\n" +
            "    lowp float luminance = dot(source.rgb, luminanceWeighting);\n" +
            "    lowp vec3 greyScaleColor = vec3(luminance);\n" +
            "    lowp float level = clamp(inputLevel, 0.0, 1.0);\n" +
            "    level = level + level;\n" +
            "    dest = mix(greyScaleColor, source.rgb, level);\n" +
            "  }\n" +
            "  gl_FragColor = vec4(dest, source.a);\n" +
            "}";

    private int mRangeMinLoc;
    private int mRangeMaxLoc;
    private int mInputLevelLoc;

    public SaturationFilter() {
        this(VERTEX_SHADER, FRAGMENT_SATURATION);
    }

    public SaturationFilter(String vertexShader, String fragementShader) {
        super(vertexShader, fragementShader);
        mRangeMinLoc = GLES20.glGetUniformLocation(mProgramHandle, "rangeMin");
        mRangeMaxLoc = GLES20.glGetUniformLocation(mProgramHandle, "rangeMax");
        mInputLevelLoc = GLES20.glGetUniformLocation(mProgramHandle, "inputLevel");
        setSaturationMin(new float[]{0.0f, 0.0f, 0.0f});
        setSaturationMax(new float[]{1.0f, 1.0f, 1.0f});
        setSaturationLevel(0.5f);
    }

    /**
     * 设置饱和度值
     * @param value 0.0 ~ 1.0之间
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
