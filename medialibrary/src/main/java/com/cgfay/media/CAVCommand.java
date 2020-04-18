package com.cgfay.media;

/**
 * @author CainHuang
 * @date 2019/6/7
 */
public final class CAVCommand {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("cav_command");
    }

    private CAVCommand() {}

    private static native int _execute(String[] command);

    /**
     * 执行命令行，执行成功返回0，失败返回错误码。
     * @param commands  命令行数组
     * @return  执行结果
     */
    public static int execute(String[] commands) {
        return _execute(commands);
    }
}
