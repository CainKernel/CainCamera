package com.cgfay.media;

import androidx.annotation.NonNull;

import com.cgfay.uitls.utils.FileUtils;

/**
 * 媒体编辑器
 */
public class CainMediaEditor {

    private static final String TAG = "CainMediaEditor";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("soundtouch");
        System.loadLibrary("yuv");
        System.loadLibrary("media_editor");
    }

    // 初始化
    private native long nativeInit();
    // 释放资源
    private native void nativeRelease(long handle);
    // 视频裁剪
    private native void videoCut(long handle, String srcPath, String dstPath, float start, float duration, float speed, OnEditProcessListener listener);
    // 音频裁剪
    private native void audioCut(long handle, String srcPath, String dstPath, float start, float duration, float speed, OnEditProcessListener listener);
    // 视频逆序
    private native void videoReverse(long handle, String srcPath, String dstPath, OnEditProcessListener listener);

    private long handle;

    public CainMediaEditor() {
        handle = nativeInit();
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (handle != 0) {
            nativeRelease(handle);
            handle = 0;
        }
    }

    /**
     * 视频裁剪
     * @param srcPath
     * @param dstPath
     * @param start
     * @param duration
     * @param listener
     */
    public void videoCut(@NonNull String srcPath, @NonNull String dstPath,
                         float start, float duration, OnEditProcessListener listener) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, 1.0f, listener);
        } else {
            if (listener != null) {
                listener.onError("source path is not exists.");
            }
        }
    }

    /**
     * 视频裁剪，带倍速调整
     * @param srcPath
     * @param start
     * @param duration
     * @param speed
     * @return
     */
    public void videoSpeedCut(@NonNull String srcPath, @NonNull String dstPath,
                              float start, float duration, float speed,
                              OnEditProcessListener listener) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, speed, listener);
        } else {
            if (listener != null) {
                listener.onError("source path is not exists.");
            }
        }
    }

    /**
     * 音频裁剪
     * 音频文件结尾处是空的时候，似乎会失败，提前终止了，后续这里可以做优化
     * @param srcPath
     * @param dstPath
     * @param start
     * @param duration
     * @return
     */
    public void audioCut(@NonNull String srcPath, @NonNull String dstPath,
                         float start, float duration, OnEditProcessListener listener) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, 1.0f, listener);
        } else {
            if (listener != null) {
                listener.onError("source path is not exists.");
            }
        }
    }

    /**
     * 音频裁剪，带倍速处理
     * @param srcPath
     * @param dstPath
     * @param start
     * @param duration
     * @param speed
     * @param listener
     */
    public void audioSpeedCut(@NonNull String srcPath, @NonNull String dstPath,
                              float start, float duration, float speed,
                              OnEditProcessListener listener) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, speed, listener);
        } else {
            if (listener != null) {
                listener.onError("source path is not exists.");
            }
        }
    }

    /**
     * TODO 视频逆序处理，不支持存在B帧的视频进行逆序
     * @param srcPath
     * @param dstPath
     * @param listener
     */
    public void videoReverse(@NonNull String srcPath, @NonNull String dstPath,
                             OnEditProcessListener listener) {
        if (FileUtils.fileExists(srcPath)) {
            videoReverse(handle, srcPath, dstPath, listener);
        } else {
            if (listener != null) {
                listener.onError("source path is not exists.");
            }
        }
    }

    /**
     * 编辑处理监听器
     */
    public interface OnEditProcessListener {

        // 正在处理
        void onProcessing(int percent);

        // 处理成功
        void onSuccess();

        // 处理出错，返回出错消息
        void onError(String msg);
    }
}
