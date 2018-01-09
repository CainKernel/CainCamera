package com.cgfay.cainfilter.filter.image;

import android.opengl.GLES30;

import com.cgfay.cainfilter.filter.base.BaseImageFilter;

/**
 * 高斯模糊滤镜
 * Created by cain.huang on 2017/7/21.
 */
public class GuassFilter extends BaseImageFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;                                                         \n" +
            "varying mediump vec2 textureCoordinate;                                              \n" +
            "uniform sampler2D inputTexture;                                                      \n" +
            "uniform mediump float radius;                                                    \n" +
            "uniform mediump vec2 offset;                                                     \n" +
            "uniform mediump vec2 imageSize;                                                  \n" +
            "uniform mediump float blurSize;                                                  \n" +
            "void main(void) {                                                                \n" +
            "    mediump int guassRadius = int(radius);                                       \n" +
            "    mediump float fTemplateLen = 2.0 * radius + 1.0;                             \n" +
            "    mediump vec2 myOffset = offset * vec2(blurSize) / imageSize;                 \n" +
            "    mediump vec4 sum = vec4(0.0);                                                \n" +
            "                                                                                 \n" +
            "    mediump float Guass[6];                                                      \n" +
            "    Guass[0] = 0.062745;                                                         \n" +
            "    Guass[1] = 0.078431;                                                         \n" +
            "    Guass[2] = 0.090196;                                                         \n" +
            "    Guass[3] = 0.098039;                                                         \n" +
            "    Guass[4] = 0.105882;                                                         \n" +
            "    Guass[5] = 0.105882;                                                         \n" +
            "                                                                                 \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (-5.0 * myOffset)) * Guass[0];    \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (-4.0 * myOffset)) * Guass[1];    \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (-3.0 * myOffset)) * Guass[2];    \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (-2.0 * myOffset)) * Guass[3];    \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (-1.0 * myOffset)) * Guass[4];    \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (0.0 * myOffset)) * Guass[5];     \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (1.0 * myOffset)) * Guass[4];     \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (2.0 * myOffset)) * Guass[3];     \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (3.0 * myOffset)) * Guass[2];     \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (4.0 * myOffset)) * Guass[1];     \n" +
            "    sum += texture2D(inputTexture, textureCoordinate + (5.0 * myOffset)) * Guass[0];     \n" +
            "                                                                                 \n" +
            "    gl_FragColor = sum;                                                          \n" +
            "}                                                                                \n";

    private int mRadiusLoc;
    private int mOffsetLoc;
    private int mImageSizeLoc;
    private int mBlurSizeLoc;

    public GuassFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GuassFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mRadiusLoc = GLES30.glGetUniformLocation(mProgramHandle, "radius");
        mOffsetLoc = GLES30.glGetUniformLocation(mProgramHandle, "offset");
        mImageSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageSize");
        mBlurSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "blurSize");
        setGuassRadius(20.0f);
        setOffset(new float[]{540f, 960f});
        setImageSize(new float[]{1080, 1920});
        setBlurSize(new float[]{100.0f, 100.0f});
    }

    /**
     * 设置高斯模糊半径
     * @param value
     */
    public void setGuassRadius(float value) {
        setFloat(mRadiusLoc, value);
    }

    /**
     * 设置偏移量
     * @param values
     */
    public void setOffset(float[] values) {
        setFloatVec2(mOffsetLoc, values);
    }

    /**
     * 设置图片大小
     * @param values
     */
    public void setImageSize(float[] values) {
        setFloatVec2(mImageSizeLoc, values);
    }

    /**
     * 设置高斯模糊的大小
     * @param values
     */
    public void setBlurSize(float[] values) {
        setFloatVec2(mBlurSizeLoc, values);
    }
}
