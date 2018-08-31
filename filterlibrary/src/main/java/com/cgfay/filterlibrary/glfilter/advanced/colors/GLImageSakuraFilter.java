package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 樱花滤镜
 * Created by cain on 2017/11/15.
 */

public class GLImageSakuraFilter extends GLImageFilter {
    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float; \n" +
            "varying mediump vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform sampler2D curveTexture; //curve\n" +
            "uniform float texelWidthOffset;\n" +
            "uniform float texelHeightOffset;\n" +
            "\n" +
            "\n" +
            "vec4 gaussianBlur(sampler2D sampler) { \n" +
            "lowp float strength = 1.; \n" +
            "vec4 color = vec4(0.); \n" +
            "vec2 step  = vec2(0.); \n" +
            "\n" +
            "color += texture2D(sampler,textureCoordinate)* 0.25449 ;\n" +
            "\n" +
            "step.x = 1.37754 * texelWidthOffset  * strength; \n" +
            "step.y = 1.37754 * texelHeightOffset * strength; \n" +
            "color += texture2D(sampler,textureCoordinate+step) * 0.24797;\n" +
            "color += texture2D(sampler,textureCoordinate-step) * 0.24797;\n" +
            "\n" +
            "step.x = 3.37754 * texelWidthOffset  * strength; \n" +
            "step.y = 3.37754 * texelHeightOffset * strength; \n" +
            "color += texture2D(sampler,textureCoordinate+step) * 0.09122;\n" +
            "color += texture2D(sampler,textureCoordinate-step) * 0.09122;\n" +
            "\n" +
            "step.x = 5.37754 * texelWidthOffset  * strength; \n" +
            "step.y = 5.37754 * texelHeightOffset * strength; \n" +
            "color += texture2D(sampler,textureCoordinate+step) * 0.03356;\n" +
            "color += texture2D(sampler,textureCoordinate-step) * 0.03356;\n" +
            "\n" +
            "return color; \n" +
            "} \n" +
            "\n" +
            "vec4 overlay(vec4 c1, vec4 c2){ \n" +
            "vec4 r1 = vec4(0.,0.,0.,1.); \n" +
            "\n" +
            "r1.r = c1.r < 0.5 ? 2.0*c1.r*c2.r : 1.0 - 2.0*(1.0-c1.r)*(1.0-c2.r); \n" +
            "r1.g = c1.g < 0.5 ? 2.0*c1.g*c2.g : 1.0 - 2.0*(1.0-c1.g)*(1.0-c2.g); \n" +
            "r1.b = c1.b < 0.5 ? 2.0*c1.b*c2.b : 1.0 - 2.0*(1.0-c1.b)*(1.0-c2.b); \n" +
            "\n" +
            "return r1; \n" +
            "} \n" +
            "\n" +
            "vec4 level0c(vec4 color, sampler2D sampler) { \n" +
            "    color.r = texture2D(sampler, vec2(color.r, 0.)).r; \n" +
            "    color.g = texture2D(sampler, vec2(color.g, 0.)).r; \n" +
            "color.b = texture2D(sampler, vec2(color.b, 0.)).r; \n" +
            "return color; \n" +
            "} \n" +
            "\n" +
            "vec4 normal(vec4 c1, vec4 c2, float alpha) { \n" +
            "    return (c2-c1) * alpha + c1; \n" +
            "} \n" +
            "\n" +
            "vec4 screen(vec4 c1, vec4 c2) { \n" +
            "vec4 r1 = vec4(1.) - ((vec4(1.) - c1) * (vec4(1.) - c2)); \n" +
            "return r1; \n" +
            "} \n" +
            "\n" +
            "void main() { \n" +
            "// naver skin \n" +
            "lowp vec4 c0 = texture2D(inputTexture, textureCoordinate);\n" +
            "lowp vec4 c1 = gaussianBlur(inputTexture);\n" +
            "lowp vec4 c2 = overlay(c0, level0c(c1, curveTexture));\n" +
            "lowp vec4 c3 = normal(c0,c2,0.15); \n" +
            "\n" +
            "gl_FragColor = c3; \n" +
            "}";


    private int mCurveTexture;
    private int mCurveTextureHandle;

    public GLImageSakuraFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageSakuraFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mCurveTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        createTexture();
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        byte[] arrayOfByte = new byte[1024];
        int[] arrayOfInt = { 95, 95, 96, 97, 97, 98, 99, 99, 100, 101, 101, 102, 103, 104, 104, 105, 106, 106, 107, 108, 108, 109, 110, 111, 111, 112, 113, 113, 114, 115, 115, 116, 117, 117, 118, 119, 120, 120, 121, 122, 122, 123, 124, 124, 125, 126, 127, 127, 128, 129, 129, 130, 131, 131, 132, 133, 133, 134, 135, 136, 136, 137, 138, 138, 139, 140, 140, 141, 142, 143, 143, 144, 145, 145, 146, 147, 147, 148, 149, 149, 150, 151, 152, 152, 153, 154, 154, 155, 156, 156, 157, 158, 159, 159, 160, 161, 161, 162, 163, 163, 164, 165, 165, 166, 167, 168, 168, 169, 170, 170, 171, 172, 172, 173, 174, 175, 175, 176, 177, 177, 178, 179, 179, 180, 181, 181, 182, 183, 184, 184, 185, 186, 186, 187, 188, 188, 189, 190, 191, 191, 192, 193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 200, 200, 201, 202, 202, 203, 204, 204, 205, 206, 207, 207, 208, 209, 209, 210, 211, 211, 212, 213, 213, 214, 215, 216, 216, 217, 218, 218, 219, 220, 220, 221, 222, 223, 223, 224, 225, 225, 226, 227, 227, 228, 229, 229, 230, 231, 232, 232, 233, 234, 234, 235, 236, 236, 237, 238, 239, 239, 240, 241, 241, 242, 243, 243, 244, 245, 245, 246, 247, 248, 248, 249, 250, 250, 251, 252, 252, 253, 254, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 };
        for (int i = 0; i < 256; i++) {
            arrayOfByte[(i * 4)] = ((byte)arrayOfInt[i]);
            arrayOfByte[(1 + i * 4)] = ((byte)arrayOfInt[i]);
            arrayOfByte[(2 + i * 4)] = ((byte)arrayOfInt[i]);
            arrayOfByte[(3 + i * 4)] = ((byte)arrayOfInt[i]);
        }
        mCurveTexture = OpenGLUtils.createTexture(arrayOfByte, 256, 1);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mCurveTexture);
        GLES30.glUniform1i(mCurveTextureHandle, 1);
    }

    @Override
    public void release() {
        GLES30.glDeleteTextures(1, new int[]{mCurveTexture}, 0);
        super.release();
    }
}
