package com.cgfay.media;

import android.util.Log;

/**
 * @author CainHuang
 * @date 2019/6/7
 */
public final class CAVCommand {

    private static final String TAG = "CAVCommand";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("cav_command");
    }

    private static OnExecutorListener sOnCmdExecListener;

    private CAVCommand() {}

    // 同步执行
    private static native int _execute(String[] command);

    /**
     * 执行命令行，执行成功返回0，失败返回错误码。
     * @param commands  命令行数组
     * @return  执行结果
     */
    public synchronized static int execute(String[] commands) {
        sOnCmdExecListener = null;
        return _execute(commands);
    }

    public synchronized static int execute(String[] commands, OnExecutorListener listener) {
        sOnCmdExecListener = listener;
        int ret = _execute(commands);
        onExecuted(ret);
        return ret;
    }

    /**
     * 执行结果回调，native层调用
     * @param ret 为0则执行成功，否则执行失败
     */
    private static void onExecuted(int ret) {
        Log.d(TAG, "onExecuted: " + ret);
        if (sOnCmdExecListener != null) {
            if (ret == 0) {
                sOnCmdExecListener.onSuccess();
            } else {
                sOnCmdExecListener.onFailure();
            }
        }
    }

    /**
     * Native层回调当前处理时长
     * @param progress 处理的时间进度(秒)
     */
    private static void onProgress(int progress) {
        Log.d(TAG, "onProgress: " + progress);
        if (sOnCmdExecListener != null) {
            sOnCmdExecListener.onProgress(progress);
        }
    }

    /**
     * 执行回调处理
     */
    public interface OnExecutorListener {

        // 成功回调
        void onSuccess();

        // 失败回调
        void onFailure();

        /**
         * 正在执行过程中
         * @param process 当前处理时长
         */
        void onProgress(int process);
    }
}
