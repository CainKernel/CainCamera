package com.cgfay.video.engine;

import android.view.Surface;

/**
 * 视频编辑渲染引擎
 */
public class VideoEditRenderEngine {

    private static class EditEngineHolder {
        public static VideoEditRenderEngine instance = new VideoEditRenderEngine();
    }

    public VideoEditRenderEngine() {
    }

    public static VideoEditRenderEngine getInstance() {
        return EditEngineHolder.instance;
    }

    /**
     * 设置绑定的Surface
     * @param surface
     */
    public void setSurface(Surface surface) {

    }

}
