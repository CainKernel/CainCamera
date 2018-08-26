package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;

/**
 * 加载一张图片，需要倒过来
 */
public class GLImageInputFilter extends GLImageFilter {

    private static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;                                              \n" +
            "attribute vec4 aPosition;                                             \n" +
            "attribute vec4 aTextureCoord;                                         \n" +
            "varying vec2 textureCoordinate;                                       \n" +
            "void main() {                                                         \n" +
            "    gl_Position = uMVPMatrix * aPosition;                             \n" +
            "    textureCoordinate = vec2(aTextureCoord.x, 1.0 - aTextureCoord.y); \n" +
            "}                                                                     \n";

    public GLImageInputFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }

    public GLImageInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
