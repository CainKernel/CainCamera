package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 凯文
 * Created by cain.huang on 2017/11/16.
 */

public class GLImageKevinFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            " precision mediump float;\n" +
            "\n" +
            " varying mediump vec2 textureCoordinate;\n" +
            "\n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D mapTexture;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "     vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "\n" +
            "     vec2 lookup;\n" +
            "     lookup.y = .5;\n" +
            "\n" +
            "     lookup.x = texel.r;\n" +
            "     texel.r = texture2D(mapTexture, lookup).r;\n" +
            "\n" +
            "     lookup.x = texel.g;\n" +
            "     texel.g = texture2D(mapTexture, lookup).g;\n" +
            "\n" +
            "     lookup.x = texel.b;\n" +
            "     texel.b = texture2D(mapTexture, lookup).b;\n" +
            "\n" +
            "     gl_FragColor = vec4(texel, 1.0);\n" +
            " }\n";

    private int mMapTexture;
    private int mMapTextureHandle;

    public GLImageKevinFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageKevinFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mMapTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");
        createTexture();
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mMapTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/kevin_map.png");
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureHandle, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, new int[]{mMapTexture}, 0);
    }
}
