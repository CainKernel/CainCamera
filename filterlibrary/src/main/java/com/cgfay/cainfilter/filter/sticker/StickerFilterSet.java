package com.cgfay.cainfilter.filter.sticker;

import android.opengl.GLES30;

import com.cgfay.cainfilter.type.StickerType;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态贴纸集合
 * Created by cain on 2018/1/13.
 */

public class StickerFilterSet {

    private int mProgramHandle;
    private int muMVPMatrixLoc;
    private int maPositionLoc;
    private int maTextureCoordLoc;
    private int mInputTextureLoc;

    // 贴纸集合
    private ArrayList<StickerItemFilter> mStickerItems;

    /**
     * 构造时使用已存在的program，这里需要传句柄进来，方便某个部位的贴纸使用
     * @param programHandle program句柄
     * @param mvpMatrixLoc  总变换句柄
     * @param positionLoc   位置句柄
     * @param textureCoordLoc 纹理坐标句柄
     * @param inputTextureLoc 纹理句柄
     */
    public StickerFilterSet(int programHandle, int mvpMatrixLoc, int positionLoc,
                            int textureCoordLoc, int inputTextureLoc) {
        mProgramHandle = programHandle;
        muMVPMatrixLoc = mvpMatrixLoc;
        maPositionLoc = positionLoc;
        maTextureCoordLoc = textureCoordLoc;
        mInputTextureLoc = inputTextureLoc;
        mStickerItems = new ArrayList<StickerItemFilter>();

//        mStickerItems.add(new StickerItemFilter(StickerType.NOSE));
//        mStickerItems.add(new StickerItemFilter(StickerType.EAR));
//        mStickerItems.add(new StickerItemFilter(StickerType.BEARD));
//        mStickerItems.add(new StickerItemFilter(StickerType.FACE));
//        mStickerItems.add(new StickerItemFilter(StickerType.HEAD));
    }

    /**
     * 输入图像发生变化
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        if (mStickerItems != null && mStickerItems.size() > 0) {
            for (int i = 0; i < mStickerItems.size(); i++) {
                mStickerItems.get(i).onInputSizeChanged(width, height);
            }
        }
    }

    /**
     * 绘制贴纸
     */
    public void drawSubSticker() {
        if (mStickerItems != null && mStickerItems.size() > 0) {
            // 开启混合
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA);
            // 使用当前的program
            GLES30.glUseProgram(mProgramHandle);
            // 逐个绘制贴纸集合中的的所有贴纸
            for (int i = 0; i < mStickerItems.size(); i++) {
                drawSubItem(mStickerItems.get(i));
            }
            // 关闭混合
            GLES30.glDisable(GLES30.GL_BLEND);
        }
    }

    /**
     * 绘制某个部位的贴纸
     * @param itemFilter
     */
    private void drawSubItem(StickerItemFilter itemFilter) {
        // 绑定顶点坐标缓冲
        GLES30.glVertexAttribPointer(maPositionLoc, itemFilter.getCoordsPerVertex(),
                GLES30.GL_FLOAT, false, 0, itemFilter.getVertexBuffer());
        GLES30.glEnableVertexAttribArray(maPositionLoc);

        // 绑定纹理坐标缓冲
        GLES30.glVertexAttribPointer(maTextureCoordLoc, itemFilter.getCoordsPerTexture(),
                GLES30.GL_FLOAT, false, 0, itemFilter.getTextureBuffer());
        GLES30.glEnableVertexAttribArray(maTextureCoordLoc);

        // 计算并绑定总变换矩阵
        itemFilter.calculateMVPMatrix();
        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false,
                itemFilter.getMVPMatrix(), 0);
        // 绑定纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, itemFilter.getTexture());
        GLES30.glUniform1i(mInputTextureLoc, 0);
        // 绘制纹理
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        // 解绑数据
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        // 更新贴纸
        itemFilter.updateTexture();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mStickerItems.size() > 0) {
            for (int i = 0; i < mStickerItems.size(); i++) {
                mStickerItems.get(i).release();
            }
            mStickerItems.clear();
            mStickerItems = null;
        }
    }

    /**
     * 添加贴纸
     * @param itemFilter
     */
    public void addSubSticker(StickerItemFilter itemFilter) {
        mStickerItems.add(itemFilter);
    }

    /**
     * 添加贴纸
     * @param itemFilters
     */
    public void addSubSticker(List<StickerItemFilter> itemFilters) {
        mStickerItems.addAll(itemFilters);
    }

    /**
     * 清空贴纸集合
     */
    public void clearSubStickers() {
        if (mStickerItems.size() > 0) {
            for (int i = 0; i < mStickerItems.size(); i++) {
                mStickerItems.get(i).release();
            }
            mStickerItems.clear();
        }
    }
}
