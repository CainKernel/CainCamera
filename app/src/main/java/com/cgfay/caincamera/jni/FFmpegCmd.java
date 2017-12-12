package com.cgfay.caincamera.jni;

/**
 * 用于管理FFmpeg命令工具
 * Created by cain.huang on 2017/12/12.
 */

public class FFmpegCmd {

    private native static int run(String[] cmd);
}
