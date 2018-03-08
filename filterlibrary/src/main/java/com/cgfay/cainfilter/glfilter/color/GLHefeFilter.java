package com.cgfay.cainfilter.glfilter.color;

import android.opengl.GLES30;

import com.cgfay.cainfilter.camerarender.ParamsManager;
import com.cgfay.cainfilter.glfilter.base.GLImageFilter;
import com.cgfay.cainfilter.utils.GlUtil;

/**
 * 酵母
 * Created by cain.huang on 2017/11/16.
 */

public class GLHefeFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER =
            " precision mediump float;\n" +
            " \n" +
            " varying mediump vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D edgeBurnTexture;  //edgeBurn\n" +
            " uniform sampler2D mapTexture;  //hefeMap\n" +
            " uniform sampler2D gradientMapTexture;  //hefeGradientMap\n" +
            " uniform sampler2D softLightTexture;  //hefeSoftLight\n" +
            " uniform sampler2D metalTexture;  //hefeMetal\n" +
            " \n" +
            " uniform float strength;\n" +
            "\n" +
            " void main() {\n" +
            "    vec4 originColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "    vec3 edge = texture2D(edgeBurnTexture, textureCoordinate).rgb;\n" +
            "    texel = texel * edge;\n" +
            "    \n" +
            "    texel = vec3(\n" +
            "                 texture2D(mapTexture, vec2(texel.r, .16666)).r,\n" +
            "                 texture2D(mapTexture, vec2(texel.g, .5)).g,\n" +
            "                 texture2D(mapTexture, vec2(texel.b, .83333)).b);\n" +
            "    \n" +
            "    vec3 luma = vec3(.30, .59, .11);\n" +
            "    vec3 gradSample = texture2D(gradientMapTexture, vec2(dot(luma, texel), .5)).rgb;\n" +
            "    vec3 final = vec3(\n" +
            "                      texture2D(softLightTexture, vec2(gradSample.r, texel.r)).r,\n" +
            "                      texture2D(softLightTexture, vec2(gradSample.g, texel.g)).g,\n" +
            "                      texture2D(softLightTexture, vec2(gradSample.b, texel.b)).b\n" +
            "                      );\n" +
            "    \n" +
            "    vec3 metal = texture2D(metalTexture, textureCoordinate).rgb;\n" +
            "    vec3 metaled = vec3(\n" +
            "                        texture2D(softLightTexture, vec2(metal.r, texel.r)).r,\n" +
            "                        texture2D(softLightTexture, vec2(metal.g, texel.g)).g,\n" +
            "                        texture2D(softLightTexture, vec2(metal.b, texel.b)).b\n" +
            "                        );\n" +
            "    \n" +
            "    metaled.rgb = mix(originColor.rgb, metaled.rgb, strength);\n" +
            "\n" +
            "    gl_FragColor = vec4(metaled, 1.0);\n" +
            " }";


    private int mEdgeBurnTexture;
    private int mEdgeBurnTextureLoc;

    private int mMapTexture;
    private int mMapTextureLoc;

    private int mGradientMapTexture;
    private int mGradientMapTextureLoc;

    private int mSoftLightTexture;
    private int mSoftLightTextureLoc;

    private int mMetalTexture;
    private int mMetalTextureLoc;

    private int mStrengthLoc;

    public GLHefeFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLHefeFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);

        mEdgeBurnTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "edgeBurnTexture");
        mMapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");
        mGradientMapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "gradientMapTexture");
        mSoftLightTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "softLightTexture");
        mMetalTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "metalTexture");

        mStrengthLoc = GLES30.glGetUniformLocation(mProgramHandle, "strength");

        createTexture();
        setFloat(mStrengthLoc, 1.0f);
    }

    private void createTexture() {
        mEdgeBurnTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/hefe_edgeburn.png");
        mMapTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/hefe_map.png");
        mGradientMapTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/hefe_gradientmap.png");
        mSoftLightTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/hefe_softlight.png");
        mMetalTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/hefe_metal.png");
    }

    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mEdgeBurnTexture);
        GLES30.glUniform1i(mEdgeBurnTextureLoc, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureLoc, 2);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mGradientMapTexture);
        GLES30.glUniform1i(mGradientMapTextureLoc, 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
        GLES30.glBindTexture(getTextureType(), mSoftLightTexture);
        GLES30.glUniform1i(mSoftLightTextureLoc, 4);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
        GLES30.glBindTexture(getTextureType(), mMetalTexture);
        GLES30.glUniform1i(mMetalTextureLoc, 5);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(5,
                new int[]{
                        mEdgeBurnTexture,
                        mMapTexture,
                        mGradientMapTexture,
                        mSoftLightTexture,
                        mMetalTexture
                }, 0);
    }
}
