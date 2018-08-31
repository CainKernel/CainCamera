package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 酵母
 * Created by cain.huang on 2017/11/16.
 */

public class GLImageHefeFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
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
    private int mEdgeBurnTextureHandle;

    private int mMapTexture;
    private int mMapTextureHandle;

    private int mGradientMapTexture;
    private int mGradientMapTextureHandle;

    private int mSoftLightTexture;
    private int mSoftLightTextureHandle;

    private int mMetalTexture;
    private int mMetalTextureHandle;

    private int mStrengthHandle;

    public GLImageHefeFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageHefeFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mEdgeBurnTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "edgeBurnTexture");
        mMapTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");
        mGradientMapTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "gradientMapTexture");
        mSoftLightTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "softLightTexture");
        mMetalTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "metalTexture");

        mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength");

        createTexture();
        setStrength(1.0f);
    }

    /**
     * 创建纹理
     */
    private void createTexture() {
        mEdgeBurnTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/hefe_edgeburn.png");
        mMapTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/hefe_map.png");
        mGradientMapTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/hefe_gradientmap.png");
        mSoftLightTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/hefe_softlight.png");
        mMetalTexture = OpenGLUtils.createTextureFromAssets(mContext,
                "filters/hefe_metal.png");
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mEdgeBurnTexture);
        GLES30.glUniform1i(mEdgeBurnTextureHandle, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureHandle, 2);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mGradientMapTexture);
        GLES30.glUniform1i(mGradientMapTextureHandle, 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
        GLES30.glBindTexture(getTextureType(), mSoftLightTexture);
        GLES30.glUniform1i(mSoftLightTextureHandle, 4);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
        GLES30.glBindTexture(getTextureType(), mMetalTexture);
        GLES30.glUniform1i(mMetalTextureHandle, 5);
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

    /**
     * 设置强度
     * @param strength
     */
    public void setStrength(float strength) {
        setFloat(mStrengthHandle, strength);
    }
}
