/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cgfay.caincamera.gles;

import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.filter.base.BaseImageFilterGroup;
import com.cgfay.caincamera.filter.base.CameraFilter;
import com.cgfay.caincamera.filter.base.IFilter;
import com.cgfay.caincamera.filter.base.SurfaceFilter;
import com.cgfay.caincamera.utils.GlUtil;

import java.util.ArrayDeque;
import java.util.Queue;

public class FullFrameRect {

    // 相机输入
    private CameraFilter mCameraFilter;

    // 显示输出
    private SurfaceFilter mSurfaceFilter;

    // 滤镜组
    private Queue<BaseImageFilter> mFilterLists;

    public FullFrameRect() {
        mCameraFilter = new CameraFilter();
        mSurfaceFilter = new SurfaceFilter();
        mFilterLists = new ArrayDeque<BaseImageFilter>();
    }

    /**
     * 创建OES类型的Texture，用于绑定SurfaceTexture
     * @return
     */
    public int createTextureOES() {
        return GlUtil.createTextureObject(mCameraFilter.getTextureType());
    }

    /**
     * 添加滤镜
     * @param filter
     */
    public void addFilter(BaseImageFilter filter) {
        if (mFilterLists != null) {
            mFilterLists.add(filter);
        }
    }

    /**
     * 绘制到FBO
     * @param framebuffer
     * @param textureId
     * @param texMatrix
     */
    public void drawCameraTexture(int framebuffer, int textureId, float[] texMatrix) {
        if (mCameraFilter != null) {
            mCameraFilter.drawToTexture(framebuffer, textureId, texMatrix);
        }
    }

    /**
     * 将特效绘制到FBO中
     * @param framebuffer
     * @param textureid
     * @param texMatrix
     */
    public void drawFramebuffer(int framebuffer, int textureid, float[] texMatrix) {
        // 存在滤镜时，进入绘制滤镜流程
        if (mFilterLists != null && mFilterLists.size() > 0) {
            drawEffectFilters(framebuffer, textureid, texMatrix);
        } else { // 没滤镜时，绘制无滤镜效果到FBO
            drawOriginFramebuffer(framebuffer, textureid, texMatrix);
        }
    }

    /**
     * 绘制到屏幕显示
     * @param textureId
     * @param texMatrix
     */
    public void drawScreen(int textureId, float[] texMatrix) {
        if (mSurfaceFilter != null) {
            mSurfaceFilter.drawToScreen(textureId, texMatrix);
        }
    }

    /**
     * 绘制无滤镜效果到Framebuffer
     */
    public void drawOriginFramebuffer(int framebuffer, int textureId, float[] texMatrix) {
        if (mSurfaceFilter != null) {
            mSurfaceFilter.drawFramebuffer(framebuffer, textureId, texMatrix);
        }
    }

    /**
     * 绘制特效
     * @param framebuffer
     * @param textureId
     * @param texMatrix
     */
    public void drawEffectFilters(int framebuffer, int textureId, float[] texMatrix) {
        for (IFilter filter : mFilterLists) {
            // 滤镜组绘制完成后需要返回TextureId，可能创建了新的Texture
            if (filter instanceof BaseImageFilterGroup) {

            } else if (filter instanceof BaseImageFilter) {
                filter.drawFramebuffer(framebuffer, textureId, texMatrix);
            }
        }
    }

    /**
     * 释放资源
     * @param doEglCleanup
     */
    public void release(boolean doEglCleanup) {
        if (mCameraFilter != null && doEglCleanup) {
            mCameraFilter.release();
            mCameraFilter = null;
        }
        if (mSurfaceFilter != null && doEglCleanup) {
            mSurfaceFilter.release();
            mSurfaceFilter = null;
        }
        if (mFilterLists != null) {
            for (BaseImageFilter filter : mFilterLists) {
                filter.release();
            }
            mFilterLists.clear();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        release(true);
    }
}
