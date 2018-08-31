package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 拿铁滤镜
 * Created by cain on 2017/11/15.
 */

public class GLImageLatteFilter extends GLImageFilter {
    private static final String FRAGMENT_SHADER = "" +
            "precision highp float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform sampler2D curveTexture;\n" +
            "\n" +
            "vec3 rgb2hsv(vec3 c) \n" +
            "{ \n" +
            "vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
            "vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n" +
            "vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n" +
            "\n" +
            "float d = q.x - min(q.w, q.y);\n" +
            "float e = 1.0e-10;\n" +
            "return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
            "} \n" +
            "\n" +
            "vec3 hsv2rgb(vec3 c) \n" +
            "{ \n" +
            "vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
            "vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
            "return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y); \n" +
            "}\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "float GreyVal;\n" +
            "lowp vec4 textureColor;\n" +
            "lowp vec4 textureColorOri;\n" +
            "float xCoordinate = textureCoordinate.x;\n" +
            "float yCoordinate = textureCoordinate.y;\n" +
            "\n" +
            "highp float redCurveValue; \n" +
            "highp float greenCurveValue; \n" +
            "highp float blueCurveValue;\n" +
            "\n" +
            "textureColor = texture2D( inputTexture, vec2(xCoordinate, yCoordinate));\n" +
            "mediump vec4 base = textureColor; \n" +
            "mediump vec4 overlay = vec4(0.792, 0.58, 0.372, 1.0); \n" +
            "\n" +
            "// step1 overlay blending \n" +
            "mediump float ra; \n" +
            "if (base.r < 0.5) \n" +
            "{ \n" +
            "ra = overlay.r * base.r * 2.0; \n" +
            "} \n" +
            "else \n" +
            "{ \n" +
            "ra = 1.0 - ((1.0 - base.r) * (1.0 - overlay.r) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "mediump float ga; \n" +
            "if (base.g < 0.5) \n" +
            "{ \n" +
            "ga = overlay.g * base.g * 2.0;\n" +
            "    } \n" +
            "else \n" +
            "{ \n" +
            "ga = 1.0 - ((1.0 - base.g) * (1.0 - overlay.g) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "mediump float ba; \n" +
            "if (base.b < 0.5) \n" +
            "{ \n" +
            "ba = overlay.b * base.b * 2.0; \n" +
            "    } \n" +
            "else \n" +
            "{ \n" +
            "ba = 1.0 - ((1.0 - base.b) * (1.0 - overlay.b) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "textureColor = vec4(ra, ga, ba, 1.0); \n" +
            "textureColor = (textureColor - base) * 0.3 + base; \n" +
            "\n" +
            "redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).r;\n" +
            "greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).g;\n" +
            "blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).b;\n" +
            "\n" +
            "redCurveValue = texture2D(curveTexture, vec2(redCurveValue, 1.0)).g;\n" +
            "greenCurveValue = texture2D(curveTexture, vec2(greenCurveValue, 1.0)).g;\n" +
            "blueCurveValue = texture2D(curveTexture, vec2(blueCurveValue, 1.0)).g;\n" +
            "\n" +
            "\n" +
            "vec3 tColor = vec3(redCurveValue, greenCurveValue, blueCurveValue); \n" +
            "tColor = rgb2hsv(tColor); \n" +
            "\n" +
            "tColor.g = tColor.g * 0.6; \n" +
            "\n" +
            "float dStrength = 1.0; \n" +
            "float dSatStrength = 0.2; \n" +
            "\n" +
            "float dGap = 0.0; \n" +
            "\n" +
            "if( tColor.r >= 0.0 && tColor.r < 0.417) \n" +
            "{ \n" +
            "tColor.g = tColor.g + (tColor.g * dSatStrength); \n" +
            "    } \n" +
            "else if( tColor.r > 0.958 && tColor.r <= 1.0) \n" +
            "{ \n" +
            "tColor.g = tColor.g + (tColor.g * dSatStrength); \n" +
            "    } \n" +
            "else if( tColor.r >= 0.875 && tColor.r <= 0.958) \n" +
            "{ \n" +
            "dGap = abs(tColor.r - 0.875); \n" +
            "dStrength = (dGap / 0.0833); \n" +
            "\n" +
            "tColor.g = tColor.g + (tColor.g * dSatStrength * dStrength); \n" +
            "    } \n" +
            "else if( tColor.r >= 0.0417 && tColor.r <= 0.125) \n" +
            "{ \n" +
            "dGap = abs(tColor.r - 0.125);\n" +
            "dStrength = (dGap / 0.0833); \n" +
            "\n" +
            "tColor.g = tColor.g + (tColor.g * dSatStrength * dStrength); \n" +
            "} \n" +
            "\n" +
            "\n" +
            "tColor = hsv2rgb(tColor); \n" +
            "tColor = clamp(tColor, 0.0, 1.0); \n" +
            "\n" +
            "redCurveValue = texture2D(curveTexture, vec2(tColor.r, 1.0)).r;\n" +
            "greenCurveValue = texture2D(curveTexture, vec2(tColor.g, 1.0)).r;\n" +
            "blueCurveValue = texture2D(curveTexture, vec2(tColor.b, 1.0)).r;\n" +
            "\n" +
            "base = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); \n" +
            "overlay = vec4(0.792, 0.494, 0.372, 1.0); \n" +
            "\n" +
            "// step5 overlay blending \n" +
            "if (base.r < 0.5) \n" +
            "{\n" +
            "ra = overlay.r * base.r * 2.0; \n" +
            "    } \n" +
            "else \n" +
            "{ \n" +
            "ra = 1.0 - ((1.0 - base.r) * (1.0 - overlay.r) * 2.0); \n" +
            "    } \n" +
            "\n" +
            "if (base.g < 0.5) \n" +
            "{ \n" +
            "ga = overlay.g * base.g * 2.0; \n" +
            "} \n" +
            "else \n" +
            "{ \n" +
            "ga = 1.0 - ((1.0 - base.g) * (1.0 - overlay.g) * 2.0); \n" +
            "} \n" +
            "\n" +
            "if (base.b < 0.5) \n" +
            "{ \n" +
            "ba = overlay.b * base.b * 2.0; \n" +
            "}\n" +
            "else \n" +
            "{ \n" +
            "ba = 1.0 - ((1.0 - base.b) * (1.0 - overlay.b) * 2.0);\n" +
            "    } \n" +
            "\n" +
            "textureColor = vec4(ra, ga, ba, 1.0); \n" +
            "textureColor = (textureColor - base) * 0.15 + base; \n" +
            "\n" +
            "gl_FragColor = vec4(textureColor.r, textureColor.g, textureColor.b, 1.0); \n" +
            "}";

    private int mCurveTexture;
    private int mCurveTextureHandle;

    public GLImageLatteFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageLatteFilter(Context context, String vertexShader, String fragmentShader) {
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
        byte[] arrayOfByte = new byte[2048];
        int[] arrayOfInt1 = { 5, 6, 8, 9, 11, 12, 14, 15, 16, 18, 19, 21, 22, 23, 25, 26, 28, 29, 30, 32, 33, 34, 36, 37, 39, 40, 41, 43, 44, 45, 46, 48, 49, 50, 52, 53, 54, 55, 56, 58, 59, 60, 61, 62, 64, 65, 66, 67, 68, 69, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 108, 109, 110, 111, 112, 113, 114, 115, 115, 116, 117, 118, 119, 120, 120, 121, 122, 123, 124, 125, 125, 126, 127, 128, 129, 130, 130, 131, 132, 133, 134, 134, 135, 136, 137, 137, 138, 139, 140, 141, 141, 142, 143, 144, 145, 145, 146, 147, 148, 148, 149, 150, 151, 151, 152, 153, 154, 155, 155, 156, 157, 158, 158, 159, 160, 161, 162, 162, 163, 164, 165, 166, 166, 167, 168, 169, 170, 170, 171, 172, 173, 174, 174, 175, 176, 177, 178, 178, 179, 180, 181, 182, 183, 183, 184, 185, 186, 187, 188, 189, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255 };
        int[] arrayOfInt2 = { 5, 6, 8, 11, 12, 14, 15, 18, 19, 21, 22, 25, 26, 28, 29, 32, 33, 34, 36, 39, 40, 41, 43, 44, 46, 48, 49, 50, 52, 54, 55, 56, 58, 59, 61, 62, 64, 65, 66, 67, 69, 71, 72, 73, 74, 75, 76, 78, 79, 80, 81, 82, 83, 85, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 108, 108, 109, 110, 111, 112, 113, 114, 115, 115, 116, 117, 118, 119, 120, 120, 121, 122, 123, 125, 125, 126, 127, 128, 129, 130, 130, 131, 132, 133, 134, 134, 135, 136, 137, 137, 138, 139, 140, 141, 141, 142, 143, 144, 145, 145, 146, 147, 148, 148, 149, 149, 150, 151, 151, 152, 153, 154, 155, 155, 156, 157, 158, 158, 159, 160, 161, 162, 162, 163, 164, 165, 165, 166, 166, 167, 168, 169, 170, 170, 171, 172, 173, 174, 174, 175, 175, 176, 177, 178, 178, 179, 180, 181, 182, 183, 183, 184, 184, 185, 186, 187, 188, 189, 189, 190, 191, 192, 192, 193, 194, 195, 196, 197, 198, 198, 199, 199, 200, 201, 202, 203, 204, 205, 206, 206, 207, 208, 209, 210, 211, 212, 213, 213, 214, 215, 215, 216, 217, 218, 219, 219, 220, 221, 222, 223, 224, 225, 226, 226, 227, 228, 229, 230, 231, 232, 232, 233, 234, 235, 237, 238, 239, 240, 240, 241, 242, 243, 244, 245, 246, 246, 247, 248, 249, 250, 251, 252, 252, 253, 254, 255 };
        int[] arrayOfInt3 = { 5, 6, 8, 11, 12, 14, 15, 16, 18, 21, 22, 23, 25, 26, 28, 30, 32, 33, 34, 36, 37, 40, 41, 43, 44, 45, 46, 49, 50, 52, 53, 54, 55, 58, 59, 60, 61, 62, 64, 66, 67, 68, 69, 71, 72, 73, 75, 76, 77, 78, 79, 80, 81, 83, 85, 86, 87, 88, 89, 90, 92, 93, 94, 95, 95, 96, 97, 98, 99, 101, 102, 103, 104, 105, 106, 107, 108, 108, 109, 111, 112, 113, 114, 115, 115, 116, 117, 118, 119, 120, 120, 121, 123, 124, 125, 125, 126, 127, 128, 129, 130, 130, 131, 132, 133, 134, 134, 135, 136, 137, 137, 138, 139, 140, 141, 141, 142, 143, 144, 145, 145, 146, 147, 148, 148, 149, 150, 151, 151, 152, 153, 154, 155, 155, 156, 156, 157, 158, 158, 159, 160, 161, 162, 162, 163, 164, 165, 165, 166, 166, 167, 168, 169, 170, 170, 170, 171, 172, 173, 174, 174, 175, 176, 176, 177, 178, 178, 179, 180, 180, 181, 182, 183, 183, 184, 184, 185, 186, 187, 188, 189, 189, 189, 190, 191, 192, 192, 193, 194, 195, 196, 196, 197, 198, 198, 199, 199, 200, 201, 202, 202, 203, 204, 205, 206, 206, 207, 208, 209, 209, 210, 211, 212, 213, 213, 214, 215, 215, 215, 216, 217, 218, 218, 219, 220, 221, 221, 222, 223, 224, 224, 225, 226, 227, 227, 228, 229, 230, 230, 231, 232, 233, 233, 234, 235, 237, 237, 238, 239, 240, 240, 241, 242, 243, 243, 244 };
        for (int i = 0; i < 256; i++){
            arrayOfByte[(i * 4)] = ((byte)arrayOfInt1[i]);
            arrayOfByte[(1 + i * 4)] = ((byte)arrayOfInt2[i]);
            arrayOfByte[(2 + i * 4)] = ((byte)arrayOfInt3[i]);
            arrayOfByte[(3 + i * 4)] = -1;
        }
        int[] arrayOfInt4 = { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 10, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 30, 31, 32, 33, 34, 36, 37, 38, 39, 40, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 54, 55, 56, 57, 58, 60, 61, 62, 63, 64, 66, 67, 68, 69, 71, 72, 73, 74, 75, 77, 78, 79, 80, 81, 83, 84, 85, 86, 87, 89, 90, 91, 92, 93, 95, 96, 97, 98, 99, 101, 102, 103, 104, 105, 107, 108, 109, 110, 111, 113, 114, 115, 116, 117, 119, 120, 121, 122, 123, 125, 126, 127, 128, 129, 131, 132, 133, 134, 135, 137, 138, 139, 140, 141, 143, 144, 145, 146, 147, 149, 150, 151, 152, 153, 155, 156, 157, 158, 159, 161, 162, 163, 164, 165, 167, 168, 169, 170, 171, 173, 174, 175, 176, 177, 179, 180, 181, 182, 184, 185, 186, 187, 188, 190, 191, 192, 193, 194, 196, 197, 198, 199, 200, 202, 203, 204, 205, 206, 208, 209, 210, 211, 212, 214, 215, 216, 217, 218, 220, 221, 222, 223, 224, 226, 227, 228, 229, 230, 232, 233, 234, 235, 236, 238, 239, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240, 240 };
        int[] arrayOfInt5 = { 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 29, 30, 31, 32, 33, 35, 36, 37, 38, 39, 41, 42, 43, 44, 46, 47, 48, 49, 51, 52, 53, 54, 56, 57, 58, 59, 61, 62, 63, 65, 66, 67, 69, 70, 71, 73, 74, 75, 77, 78, 79, 81, 82, 83, 85, 86, 87, 89, 90, 92, 93, 94, 96, 97, 99, 100, 101, 103, 104, 106, 107, 108, 110, 111, 113, 114, 116, 117, 119, 120, 121, 123, 124, 126, 127, 129, 130, 132, 133, 135, 136, 138, 139, 140, 142, 143, 145, 146, 147, 149, 150, 151, 153, 154, 155, 157, 158, 159, 160, 162, 163, 164, 165, 167, 168, 169, 170, 171, 173, 174, 175, 176, 177, 178, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 210, 211, 212, 213, 214, 215, 215, 216, 217, 218, 219, 219, 220, 221, 222, 222, 223, 224, 225, 225, 226, 227, 227, 228, 229, 229, 230, 231, 231, 232, 233, 233, 234, 234, 235, 236, 236, 237, 237, 238, 238, 239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 244, 245, 245, 246, 246, 247, 247, 247, 248, 248, 249, 249, 249, 250, 250, 250, 251, 251, 251, 252, 252, 252, 252, 253, 253, 253, 253, 254, 254, 254, 254, 255, 255, 255 };
        int[] arrayOfInt6 = { 0, 0, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 8, 8, 8, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19, 19, 20, 20, 20, 21, 21, 21, 22, 22, 23, 23, 23, 24, 24, 24, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28, 28, 28, 29, 29, 30, 29, 31, 31, 31, 31, 32, 32, 33, 33, 34, 34, 34, 34, 35, 35, 36, 36, 37, 37, 37, 38, 38, 39, 39, 39, 40, 40, 40, 41, 42, 42, 43, 43, 44, 44, 45, 45, 45, 46, 47, 47, 48, 48, 49, 50, 51, 51, 52, 52, 53, 53, 54, 55, 55, 56, 57, 57, 58, 59, 60, 60, 61, 62, 63, 63, 64, 65, 66, 67, 68, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 88, 89, 90, 91, 93, 94, 95, 96, 97, 98, 100, 101, 103, 104, 105, 107, 108, 110, 111, 113, 115, 116, 118, 119, 120, 122, 123, 125, 127, 128, 130, 132, 134, 135, 137, 139, 141, 143, 144, 146, 148, 150, 152, 154, 156, 158, 160, 163, 165, 167, 169, 171, 173, 175, 178, 180, 182, 185, 187, 189, 192, 194, 197, 199, 201, 204, 206, 209, 211, 214, 216, 219, 221, 224, 226, 229, 232, 234, 236, 239, 241, 245, 247, 250, 252, 255 };
        for (int j = 0; j < 256; j++){
            arrayOfByte[(1024 + j * 4)] = ((byte)arrayOfInt5[j]);
            arrayOfByte[(1 + (1024 + j * 4))] = ((byte)arrayOfInt4[j]);
            arrayOfByte[(2 + (1024 + j * 4))] = ((byte)arrayOfInt6[j]);
            arrayOfByte[(3 + (1024 + j * 4))] = -1;
        }
        mCurveTexture = OpenGLUtils.createTexture(arrayOfByte, 256, 2);
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
