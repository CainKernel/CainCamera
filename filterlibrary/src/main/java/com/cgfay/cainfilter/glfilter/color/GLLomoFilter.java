package com.cgfay.cainfilter.glfilter.color;

import android.opengl.GLES30;

import com.cgfay.cainfilter.camerarender.ParamsManager;
import com.cgfay.cainfilter.glfilter.base.GLImageFilter;
import com.cgfay.cainfilter.utils.GlUtil;

/**
 * LOMO
 * Created by cain.huang on 2017/11/16.
 */

public class GLLomoFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            " \n" +
            " varying mediump vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D mapTexture;\n" +
            " uniform sampler2D vignetteTexture;\n" +
            " \n" +
            " uniform float strength;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "     vec4 originColor = texture2D(inputTexture, textureCoordinate);\n" +
            "     vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "\n" +
            "     vec2 red = vec2(texel.r, 0.16666);\n" +
            "     vec2 green = vec2(texel.g, 0.5);\n" +
            "     vec2 blue = vec2(texel.b, 0.83333);\n" +
            "\n" +
            "     texel.rgb = vec3(\n" +
            "                      texture2D(mapTexture, red).r,\n" +
            "                      texture2D(mapTexture, green).g,\n" +
            "                      texture2D(mapTexture, blue).b);\n" +
            "\n" +
            "     vec2 tc = (2.0 * textureCoordinate) - 1.0;\n" +
            "     float d = dot(tc, tc);\n" +
            "     vec2 lookup = vec2(d, texel.r);\n" +
            "     texel.r = texture2D(vignetteTexture, lookup).r;\n" +
            "     lookup.y = texel.g;\n" +
            "     texel.g = texture2D(vignetteTexture, lookup).g;\n" +
            "     lookup.y = texel.b;\n" +
            "     texel.b\t= texture2D(vignetteTexture, lookup).b;\n" +
            "\n" +
            "     texel.rgb = mix(originColor.rgb, texel.rgb, strength);\n" +
            "\n" +
            "     gl_FragColor = vec4(texel,1.0);\n" +
            " }";


    private int mMapTexture;
    private int mMapTextureLoc;

    private int mVignetteTexture;
    private int mVignetteTextureLoc;

    private int mStrengthLoc;

    public GLLomoFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLLomoFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);

        mMapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");
        mVignetteTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "vignetteTexture");
        mStrengthLoc = GLES30.glGetUniformLocation(mProgramHandle, "strength");
        createTexture();
        setFloat(mStrengthLoc, 1.0f);
    }

    private void createTexture() {
        mMapTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/lomo_map.png");
        mVignetteTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/lomo_vignette.png");
    }

    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureLoc, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mVignetteTexture);
        GLES30.glUniform1i(mVignetteTextureLoc, 2);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(2, new int[]{mMapTexture, mVignetteTexture}, 0);
    }
}
