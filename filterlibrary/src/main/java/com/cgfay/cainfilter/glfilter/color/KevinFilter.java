package com.cgfay.cainfilter.glfilter.color;

import android.opengl.GLES30;

import com.cgfay.cainfilter.core.ParamsManager;
import com.cgfay.cainfilter.glfilter.base.BaseImageFilter;
import com.cgfay.cainfilter.utils.GlUtil;

/**
 * 凯文
 * Created by cain.huang on 2017/11/16.
 */

public class KevinFilter extends BaseImageFilter {

    private static final String FRAGMENT_SHADER =
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
    private int mMapTextureLoc;

    public KevinFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public KevinFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mMapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");
        createTexture();
    }

    private void createTexture() {
        mMapTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/kevin_map.png");
    }

    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureLoc, 1);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(1, new int[]{mMapTexture}, 0);
    }
}
