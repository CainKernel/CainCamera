package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 日落
 * Created by cain.huang on 2017/11/16.
 */

public class GLImageSunsetFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform sampler2D curveTexture; // curve\n" +
            "\n" +
            "uniform sampler2D maskTexture; // grey1Frame\n" +
            "uniform sampler2D maskTexture1; // grey2Frame\n" +
            "\n" +
            "void main() {\n" +
            "    float GreyVal; \n" +
            "    lowp vec4 textureColor; \n" +
            "    lowp vec4 textureColorOri; \n" +
            "    float xCoordinate = textureCoordinate.x;\n" +
            "    float yCoordinate = textureCoordinate.y;\n" +
            "\n" +
            "    highp float redCurveValue; \n" +
            "    highp float greenCurveValue; \n" +
            "    highp float blueCurveValue; \n" +
            "\n" +
            "    vec4 grey1Color; \n" +
            "    vec4 grey2Color; \n" +
            "\n" +
            "    textureColor = texture2D( inputTexture, vec2(xCoordinate, yCoordinate));\n" +
            "    grey1Color = texture2D(maskTexture1, vec2(xCoordinate, yCoordinate));\n" +
            "    grey2Color = texture2D(maskTexture, vec2(xCoordinate, yCoordinate));\n" +
            "\n" +
            "    redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).r;\n" +
            "    greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).g;\n" +
            "    blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).b;\n" +
            "\n" +
            "    textureColorOri = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); \n" +
            "    textureColor = (textureColorOri - textureColor) * grey1Color.r + textureColor; \n" +
            "\n" +
            "    redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).a;\n" +
            "    greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).a;\n" +
            "    blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).a;\n" +
            "\n" +
            "    textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); \n" +
            "    mediump vec4 textureColor2 = vec4(0.08627, 0.03529, 0.15294, 1.0); \n" +
            "    textureColor2 = textureColor + textureColor2 - (2.0 * textureColor2 * textureColor); \n" +
            "\n" +
            "    textureColor = (textureColor2 - textureColor) * 0.6784 + textureColor; \n" +
            "\n" +
            "\n" +
            "    mediump vec4 overlay = vec4(0.6431, 0.5882, 0.5803, 1.0); \n" +
            "    mediump vec4 base = textureColor;\n" +
            "\n" +
            "    mediump float ra; \n" +
            "    if (base.r < 0.5) { \n" +
            "        ra = overlay.r * base.r * 2.0; \n" +
            "    } else {\n" +
            "        ra = 1.0 - ((1.0 - base.r) * (1.0 - overlay.r) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "    mediump float ga; \n" +
            "    if (base.g < 0.5) { \n" +
            "        ga = overlay.g * base.g * 2.0; \n" +
            "    } else { \n" +
            "        ga = 1.0 - ((1.0 - base.g) * (1.0 - overlay.g) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "    mediump float ba; \n" +
            "    if (base.b < 0.5) {\n" +
            "        ba = overlay.b * base.b * 2.0; \n" +
            "    } else { \n" +
            "        ba = 1.0 - ((1.0 - base.b) * (1.0 - overlay.b) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "    textureColor = vec4(ra, ga, ba, 1.0); \n" +
            "    base = (textureColor - base) + base;\n" +
            "\n" +
            "    overlay = vec4(0.0, 0.0, 0.0, 1.0);\n" +
            "\n" +
            "    if (base.r < 0.5) {\n" +
            "        ra = overlay.r * base.r * 2.0; \n" +
            "    } else { \n" +
            "        ra = 1.0 - ((1.0 - base.r) * (1.0 - overlay.r) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "    if (base.g < 0.5) { \n" +
            "        ga = overlay.g * base.g * 2.0;\n" +
            "    } else { \n" +
            "        ga = 1.0 - ((1.0 - base.g) * (1.0 - overlay.g) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "    if (base.b < 0.5) { \n" +
            "        ba = overlay.b * base.b * 2.0; \n" +
            "    } else { \n" +
            "        ba = 1.0 - ((1.0 - base.b) * (1.0 - overlay.b) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "    textureColor = vec4(ra, ga, ba, 1.0); \n" +
            "    textureColor = (textureColor - base) * (grey2Color * 0.549) + base; \n" +
            "\n" +
            "    gl_FragColor = vec4(textureColor.r, textureColor.g, textureColor.b, 1.0); \n" +
            "} ";

    private int mCurveTexture;
    private int mCurveTextureHandle;

    private int mMaskTexture;
    private int mMaskTextureHandle;

    private int mMaskTexture1;
    private int mMaskTexture1Handle;

    public GLImageSunsetFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageSunsetFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mCurveTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        mMaskTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "maskTexture");
        mMaskTexture1Handle = GLES30.glGetUniformLocation(mProgramHandle, "maskTexture1");

        createTexture();
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        byte[] arrayOfByte = new byte[2048];
        int[] arrayOfInt1 = { 0, 1, 2, 3, 5, 5, 7, 8, 9, 10, 11, 12, 13, 15, 16, 16, 18, 19, 20, 21, 23, 24, 25, 26, 27, 29, 30, 31, 32, 33, 35, 36, 38, 39, 40, 41, 42, 44, 45, 47, 48, 49, 51, 52, 54, 55, 56, 59, 60, 62, 63, 64, 66, 67, 70, 71, 72, 74, 76, 78, 79, 80, 83, 84, 85, 88, 89, 90, 93, 94, 95, 98, 99, 100, 102, 104, 106, 107, 108, 109, 112, 113, 114, 116, 117, 118, 119, 120, 122, 124, 125, 126, 128, 129, 130, 131, 132, 132, 133, 135, 136, 137, 138, 139, 140, 141, 142, 142, 143, 145, 146, 147, 148, 148, 149, 150, 151, 151, 152, 153, 154, 155, 155, 156, 157, 157, 158, 159, 160, 160, 161, 162, 162, 163, 164, 165, 165, 166, 167, 167, 168, 169, 169, 170, 171, 171, 172, 173, 173, 174, 175, 175, 176, 177, 177, 178, 178, 179, 179, 180, 181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 188, 188, 189, 190, 190, 191, 192, 193, 193, 194, 194, 194, 195, 196, 197, 197, 198, 199, 200, 201, 201, 202, 203, 204, 204, 205, 206, 207, 208, 208, 208, 209, 210, 211, 212, 212, 213, 214, 215, 216, 217, 218, 218, 219, 220, 221, 222, 222, 223, 224, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 234, 235, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 247, 248, 248, 249, 250, 251, 252, 253, 254, 255 };
        int[] arrayOfInt2 = { 0, 1, 2, 3, 4, 5, 6, 7, 9, 9, 10, 12, 12, 13, 14, 16, 16, 17, 19, 20, 20, 22, 23, 24, 25, 26, 27, 29, 30, 31, 32, 33, 35, 36, 37, 39, 40, 41, 42, 43, 44, 46, 47, 49, 50, 51, 53, 54, 56, 57, 59, 61, 62, 64, 65, 66, 69, 70, 72, 73, 76, 77, 78, 80, 82, 84, 85, 87, 89, 90, 93, 94, 95, 98, 99, 100, 103, 104, 106, 108, 109, 111, 112, 114, 116, 117, 118, 120, 122, 123, 124, 125, 126, 129, 130, 131, 132, 133, 135, 136, 137, 138, 139, 140, 141, 142, 143, 145, 146, 147, 148, 149, 150, 151, 152, 152, 153, 154, 155, 156, 157, 158, 158, 159, 160, 161, 162, 162, 163, 164, 165, 165, 166, 167, 167, 168, 169, 170, 170, 171, 172, 172, 173, 173, 174, 175, 175, 176, 177, 177, 178, 178, 178, 179, 179, 180, 181, 181, 182, 182, 183, 184, 184, 185, 185, 186, 187, 187, 188, 188, 189, 190, 190, 191, 191, 192, 193, 193, 194, 194, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 201, 202, 202, 203, 204, 204, 205, 206, 207, 208, 208, 208, 209, 210, 210, 211, 212, 213, 214, 215, 215, 216, 217, 218, 219, 220, 221, 222, 222, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 234, 235, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 248, 249, 250, 251, 252, 253, 254, 255 };
        int[] arrayOfInt3 = { 0, 1, 2, 3, 4, 5, 5, 7, 8, 9, 9, 11, 12, 12, 13, 14, 16, 16, 17, 18, 20, 20, 21, 22, 23, 25, 25, 26, 27, 29, 30, 31, 31, 32, 34, 35, 36, 37, 39, 40, 41, 41, 42, 44, 45, 46, 47, 48, 50, 51, 52, 53, 54, 56, 57, 59, 60, 61, 63, 64, 65, 66, 67, 69, 71, 72, 73, 74, 76, 78, 79, 80, 82, 83, 84, 85, 88, 89, 90, 92, 93, 94, 95, 98, 99, 100, 102, 103, 104, 106, 107, 108, 111, 112, 113, 114, 116, 117, 118, 119, 120, 122, 123, 124, 125, 126, 128, 129, 130, 131, 132, 133, 135, 136, 137, 138, 139, 140, 141, 142, 143, 145, 146, 147, 147, 148, 149, 150, 151, 152, 153, 154, 154, 155, 156, 157, 158, 159, 159, 160, 161, 162, 162, 163, 164, 165, 166, 166, 167, 168, 169, 169, 170, 171, 172, 172, 173, 174, 175, 175, 176, 177, 178, 178, 178, 179, 179, 180, 181, 182, 182, 183, 184, 185, 185, 186, 187, 188, 188, 189, 190, 191, 191, 192, 193, 194, 194, 194, 195, 196, 197, 198, 198, 199, 200, 201, 202, 203, 203, 204, 205, 206, 207, 208, 208, 209, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 222, 223, 224, 225, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 235, 236, 237, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 248, 249, 250, 251, 252, 253, 254, 255 };
        int[] arrayOfInt4 = { 0, 1, 3, 4, 6, 7, 9, 10, 12, 13, 14, 16, 17, 19, 20, 21, 23, 24, 26, 27, 28, 30, 31, 32, 34, 35, 36, 38, 39, 40, 42, 43, 44, 45, 47, 48, 49, 51, 52, 53, 54, 55, 57, 58, 59, 60, 62, 63, 64, 65, 66, 67, 69, 70, 71, 72, 73, 74, 75, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 115, 116, 117, 118, 119, 120, 121, 121, 122, 123, 124, 125, 126, 126, 127, 128, 129, 130, 130, 131, 132, 133, 134, 135, 135, 136, 137, 138, 139, 140, 141, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 230, 231, 232, 233, 234, 235, 235, 236, 237, 238, 239, 239, 240, 241, 242, 243, 243, 244, 245, 245, 246, 247, 247, 248, 249, 249, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255 };
        for (int i = 0; i < 256; i++) {
            arrayOfByte[(i * 4)] = ((byte)arrayOfInt1[i]);
            arrayOfByte[(1 + i * 4)] = ((byte)arrayOfInt2[i]);
            arrayOfByte[(2 + i * 4)] = ((byte)arrayOfInt3[i]);
            arrayOfByte[(3 + i * 4)] = ((byte)arrayOfInt4[i]);
        }
        int[] arrayOfInt5 = { 0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 8, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19, 19, 20, 20, 20, 21, 21, 21, 22, 22, 23, 23, 23, 24, 24, 24, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28, 28, 28, 29, 29, 30, 29, 31, 31, 31, 31, 32, 32, 33, 33, 34, 34, 34, 34, 35, 35, 36, 36, 37, 37, 37, 38, 38, 39, 39, 39, 40, 40, 40, 41, 42, 42, 43, 43, 44, 44, 45, 45, 45, 46, 47, 47, 48, 48, 49, 50, 51, 51, 52, 52, 53, 53, 54, 55, 55, 56, 57, 57, 58, 59, 60, 60, 61, 62, 63, 63, 64, 65, 66, 67, 68, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 88, 89, 90, 91, 93, 94, 95, 96, 97, 98, 100, 101, 103, 104, 105, 107, 108, 110, 111, 113, 115, 116, 118, 119, 120, 122, 123, 125, 127, 128, 130, 132, 134, 135, 137, 139, 141, 143, 144, 146, 148, 150, 152, 154, 156, 158, 160, 163, 165, 167, 169, 171, 173, 175, 178, 180, 182, 185, 187, 189, 192, 194, 197, 199, 201, 204, 206, 209, 211, 214, 216, 219, 221, 224, 226, 229, 232, 234, 236, 239, 241, 245, 247, 250, 252, 255 };
        for (int j = 0; j < 256; j++) {
            arrayOfByte[(1024 + j * 4)] = ((byte)arrayOfInt5[j]);
            arrayOfByte[(1 + (1024 + j * 4))] = ((byte)arrayOfInt5[j]);
            arrayOfByte[(2 + (1024 + j * 4))] = ((byte)arrayOfInt5[j]);
            arrayOfByte[(3 + (1024 + j * 4))] = -1;
        }
        mCurveTexture = OpenGLUtils.createTexture(arrayOfByte, 256, 2);

        mMaskTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/sunset_mask.png");

        mMaskTexture1 = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/sunset_mask1.png");

    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mCurveTexture);
        GLES30.glUniform1i(mCurveTextureHandle, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mMaskTexture);
        GLES30.glUniform1i(mMaskTextureHandle, 2);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mMaskTexture1);
        GLES30.glUniform1i(mMaskTexture1Handle, 3);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(3, new int[]{mCurveTexture, mMaskTexture, mMaskTexture1}, 0);
    }
}
