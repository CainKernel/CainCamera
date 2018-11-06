package com.cgfay.filterlibrary.glfilter.beauty;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 美肤滤镜
 */
public class GLImageBeautyComplexionFilter extends GLImageFilter {

    private int grayTextureLoc;
    private int lookupTextureLoc;

    private int levelRangeInvLoc;
    private int levelBlackLoc;
    private int alphaLoc;

    private int mGrayTexture;
    private int mLookupTexture;

    private float levelRangeInv;
    private float levelBlack;
    private float alpha;

    public GLImageBeautyComplexionFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/beauty/fragment_beauty_complexion.glsl"));
    }

    public GLImageBeautyComplexionFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        grayTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "grayTexture");
        lookupTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "lookupTexture");
        levelRangeInvLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelRangeInv");
        levelBlackLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelBlack");
        alphaLoc = GLES30.glGetUniformLocation(mProgramHandle, "alpha");
        createTexture();
        levelRangeInv = 1.040816f;
        levelBlack = 0.01960784f;
        alpha = 1.0f;
    }

    private void createTexture() {
        mGrayTexture = OpenGLUtils.createTextureFromAssets(mContext, "texture/skin_gray.png");
        mLookupTexture = OpenGLUtils.createTextureFromAssets(mContext, "texture/skin_lookup.png");
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        OpenGLUtils.bindTexture(grayTextureLoc, mGrayTexture, 1);
        OpenGLUtils.bindTexture(lookupTextureLoc, mLookupTexture, 2);
        GLES30.glUniform1f(levelRangeInvLoc, levelRangeInv);
        GLES30.glUniform1f(levelBlackLoc, levelBlack);
        GLES30.glUniform1f(alphaLoc, alpha);
    }

    @Override
    public void release() {
        super.release();
        GLES30.glDeleteTextures(2, new int[]{ mGrayTexture, mLookupTexture }, 0);
    }

    /**
     * 美肤程度
     * @param level 0 ~ 1.0f
     */
    public void setComplexionLevel(float level) {
        alpha = level;
    }

}
