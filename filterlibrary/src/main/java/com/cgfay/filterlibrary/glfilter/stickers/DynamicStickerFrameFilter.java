package com.cgfay.filterlibrary.glfilter.stickers;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerFrameData;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 前景贴纸滤镜
 */
public class DynamicStickerFrameFilter extends DynamicStickerBaseFilter {

    private int mStickerCoordHandle;
    private int mStickerTextureHandle;
    private int mEnableStickerHandle;
    // 贴纸缓冲
    private FloatBuffer mStickerBuffer;
    private int mStickerTexture;

    public DynamicStickerFrameFilter(Context context, DynamicSticker sticker) {
        super(context, sticker, OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_frame.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_frame.glsl"));
        mStickerBuffer = OpenGLUtils.createFloatBuffer(new float[] {
                0.0f, 0.0f,     // 0 bottom left
                1.0f, 0.0f,     // 1 bottom right
                0.0f, 1.0f,     // 2 top left
                1.0f, 1.0f      // 3 top right

        });
        mStickerTexture = OpenGLUtils.GL_NOT_TEXTURE;
        // 前景贴纸加载器
        if (mDynamicSticker != null && mDynamicSticker.dataList != null) {
            for (int i = 0; i < mDynamicSticker.dataList.size(); i++) {
                if (mDynamicSticker.dataList.get(i) instanceof DynamicStickerFrameData) {
                    String path = mDynamicSticker.unzipPath + "/" + mDynamicSticker.dataList.get(i).stickerName;
                    mStickerLoaderList.add(new DynamicStickerLoader(this, mDynamicSticker.dataList.get(i), path));
                }
            }
        }
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mStickerCoordHandle = GLES30.glGetAttribLocation(mProgramHandle, "aStickerCoord");
            mStickerTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "stickerTexture");
            mEnableStickerHandle = GLES30.glGetUniformLocation(mProgramHandle, "enableSticker");
        }
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        for (int i = 0; i < mStickerLoaderList.size(); i++) {
            mStickerLoaderList.get(i).updateStickerTexture();
            mStickerTexture = mStickerLoaderList.get(i).getStickerTexture();
        }
        return super.drawFrame(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        for (int i = 0; i < mStickerLoaderList.size(); i++) {
            mStickerLoaderList.get(i).updateStickerTexture();
            mStickerTexture = mStickerLoaderList.get(i).getStickerTexture();
        }
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        mStickerBuffer.position(0);
        GLES30.glVertexAttribPointer(mStickerCoordHandle, 2,
                GLES30.GL_FLOAT, false, 0, mStickerBuffer);
        GLES30.glEnableVertexAttribArray(mStickerCoordHandle);
        if (mStickerTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mStickerTextureHandle, mStickerTexture, 1);
            GLES30.glUniform1i(mEnableStickerHandle, 1);
        } else {
            GLES30.glUniform1i(mEnableStickerHandle, 0);
        }
    }

    @Override
    public void onDrawFrameAfter() {
        super.onDrawFrameAfter();
        GLES30.glDisableVertexAttribArray(mStickerCoordHandle);
    }

    @Override
    public void release() {
        super.release();
        for (int i = 0; i < mStickerLoaderList.size(); i++) {
            if (mStickerLoaderList.get(i) != null) {
                mStickerLoaderList.get(i).release();
            }
        }
        mStickerLoaderList.clear();
    }
}
