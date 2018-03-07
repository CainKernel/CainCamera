package com.cgfay.cainfilter.glfilter.color;

import android.opengl.GLES30;

import com.cgfay.cainfilter.core.ParamsManager;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilter;
import com.cgfay.cainfilter.utils.GlUtil;

/**
 * 晨鸟滤镜
 * Created by cain.huang on 2017/11/16.
 */

public class GLEarlyBirdFilter extends GLBaseImageFilter {
    private static final String FRAGMENT_SHADER =
            " precision mediump float;\n" +
            "\n" +
            " varying mediump vec2 textureCoordinate;\n" +
            "\n" +
            " uniform sampler2D inputTexture;\n" +
            " uniform sampler2D curveTexture; //earlyBirdCurves\n" +
            " uniform sampler2D overlayTexture; //earlyBirdOverlay\n" +
            " uniform sampler2D vignetteTexture; //vignette\n" +
            " uniform sampler2D blowoutTexture; //earlyBirdBlowout\n" +
            " uniform sampler2D mapTexture; //earlyBirdMap\n" +
            "\n" +
            " const mat3 saturate = mat3(1.210300,  -0.089700, -0.091000,\n" +
            "                            -0.176100,  1.123900, -0.177400,\n" +
            "                            -0.034200, -0.034200, 1.265800);\n" +
            " \n" +
            " const vec3 rgbPrime = vec3(0.25098, 0.14640522, 0.0);\n" +
            " const vec3 desaturate = vec3(.3, .59, .11);\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "\n" +
            "     vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "\n" +
            "\n" +
            "     vec2 lookup;\n" +
            "     lookup.y = 0.5;\n" +
            "\n" +
            "     lookup.x = texel.r;\n" +
            "     texel.r = texture2D(curveTexture, lookup).r;\n" +
            "\n" +
            "     lookup.x = texel.g;\n" +
            "     texel.g = texture2D(curveTexture, lookup).g;\n" +
            "\n" +
            "     lookup.x = texel.b;\n" +
            "     texel.b = texture2D(curveTexture, lookup).b;\n" +
            "\n" +
            "     float desaturatedColor;\n" +
            "     vec3 result;\n" +
            "     desaturatedColor = dot(desaturate, texel);\n" +
            "\n" +
            "\n" +
            "     lookup.x = desaturatedColor;\n" +
            "     result.r = texture2D(overlayTexture, lookup).r;\n" +
            "     lookup.x = desaturatedColor;\n" +
            "     result.g = texture2D(overlayTexture, lookup).g;\n" +
            "     lookup.x = desaturatedColor;\n" +
            "     result.b = texture2D(overlayTexture, lookup).b;\n" +
            "\n" +
            "     texel = saturate * mix(texel, result, .5);\n" +
            "\n" +
            "     vec2 tc = (2.0 * textureCoordinate) - 1.0;\n" +
            "     float d = dot(tc, tc);\n" +
            "\n" +
            "     vec3 sampled;\n" +
            "     lookup.y = .5;\n" +
            "\n" +
            "     lookup = vec2(d, texel.r);\n" +
            "     texel.r = texture2D(vignetteTexture, lookup).r;\n" +
            "     lookup.y = texel.g;\n" +
            "     texel.g = texture2D(vignetteTexture, lookup).g;\n" +
            "     lookup.y = texel.b;\n" +
            "     texel.b = texture2D(vignetteTexture, lookup).b;\n" +
            "     float value = smoothstep(0.0, 1.25, pow(d, 1.35)/1.65);\n" +
            "\n" +
            "     lookup.x = texel.r;\n" +
            "     sampled.r = texture2D(blowoutTexture, lookup).r;\n" +
            "     lookup.x = texel.g;\n" +
            "     sampled.g = texture2D(blowoutTexture, lookup).g;\n" +
            "     lookup.x = texel.b;\n" +
            "     sampled.b = texture2D(blowoutTexture, lookup).b;\n" +
            "     texel = mix(sampled, texel, value);\n" +
            "\n" +
            "\n" +
            "     lookup.x = texel.r;\n" +
            "     texel.r = texture2D(mapTexture, lookup).r;\n" +
            "     lookup.x = texel.g;\n" +
            "     texel.g = texture2D(mapTexture, lookup).g;\n" +
            "     lookup.x = texel.b;\n" +
            "     texel.b = texture2D(mapTexture, lookup).b;\n" +
            "\n" +
            "     gl_FragColor = vec4(texel, 1.0);\n" +
            " }\n";

    private int mCurveTexture;
    private int mCurveTextureLoc;

    private int mOverlayTexture;
    private int mOverlayTextureLoc;

    private int mVignetteTexture;
    private int mVignetteTextureLoc;

    private int mBlowoutTexture;
    private int mBlowoutTextureLoc;

    private int mMapTexture;
    private int mMapTextureLoc;

    public GLEarlyBirdFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLEarlyBirdFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mCurveTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "curveTexture");
        mOverlayTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "overlayTexture");
        mVignetteTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "vignetteTexture");
        mBlowoutTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "blowoutTexture");
        mMapTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "mapTexture");

        createTexture();
    }

    private void createTexture() {
        mCurveTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/earlybird_curves.png");
        mOverlayTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/earlybird_overlay.png");
        mVignetteTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/earlybird_vignette.png");
        mBlowoutTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/earlybird_blowout.png");
        mMapTexture = GlUtil.createTextureFromAssets(ParamsManager.context,
                "filters/earlybird_map.png");
    }


    @Override
    public void onDrawArraysBegin() {
        super.onDrawArraysBegin();
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(getTextureType(), mCurveTexture);
        GLES30.glUniform1i(mCurveTextureLoc, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(getTextureType(), mOverlayTexture);
        GLES30.glUniform1i(mOverlayTextureLoc, 2);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
        GLES30.glBindTexture(getTextureType(), mVignetteTexture);
        GLES30.glUniform1i(mVignetteTextureLoc, 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
        GLES30.glBindTexture(getTextureType(), mBlowoutTexture);
        GLES30.glUniform1i(mBlowoutTextureLoc, 4);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
        GLES30.glBindTexture(getTextureType(), mMapTexture);
        GLES30.glUniform1i(mMapTextureLoc, 5);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(5,
                new int[] {
                        mCurveTexture,
                        mOverlayTexture,
                        mVignetteTexture,
                        mBlowoutTexture,
                        mMapTexture
                }, 0);
    }
}
