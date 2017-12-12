package com.cgfay.caincamera.jni;

import android.util.Log;

import com.cgfay.caincamera.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 用于管理FFmpeg命令工具
 * Created by cain.huang on 2017/12/12.
 */

public class FFmpegCmd {
    private static final String TAG = "FFmpegCmd";
    private static boolean VERBOSE = false;

    private static final int RUN_SUCCESS = 0;
    private static final int RUN_FAILED = 1;

    // 是否正在运行命令
    private static boolean mIsRunning = false;

    private static final String STR_DEBUG_PARAM = "-d";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ffmpeg_cmd");
    }


    private native static int run(String[] cmd);

    public interface OnCompletionListener {
        void onCompletion(boolean result);
    }

    private static int runSafely(String[] cmd) {
        int result = -1;

        long time = System.currentTimeMillis();
        try {
            result = run(cmd);
            if (VERBOSE) {
                Log.d(TAG, "time = " + (System.currentTimeMillis() - time));
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }

    private static void runSync(ArrayList<String> cmds, final OnCompletionListener listener) {
        if (VERBOSE) {
            cmds.add(STR_DEBUG_PARAM);
        }

        final String[] commands = cmds.toArray(new String[cmds.size()]);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int result = runSafely(commands);
                callbackResult(result, listener);
            }
        };
        mIsRunning = true;
        new Thread(runnable).start();
    }

    private static void callbackResult(int result, OnCompletionListener listener) {
        if (VERBOSE) {
            Log.d(TAG, "result = " + result);
        }

        if (listener != null) {
            listener.onCompletion(result == 1);
        }

        mIsRunning = false;
    }

    /**
     * 音视频混合
     * @param srcVideo      视频路径
     * @param videoVolume   视频声音
     * @param srcAudio      音频路径
     * @param audioVolume   音频声音
     * @param desVideo      目标视频
     * @param callback      状态回调
     * @return
     */
    public static boolean AVMuxer(String srcVideo, float videoVolume,
                                  String srcAudio, float audioVolume, String desVideo,
                                  OnCompletionListener callback) {
        if (srcAudio == null || srcAudio.length() <= 0
                || desVideo == null || desVideo.length() <= 0) {
            return false;
        }

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-i");
        cmds.add(srcAudio);

        cmds.add("-c:v");
        cmds.add("copy");
        cmds.add("-map");
        cmds.add("0:v:0");

        cmds.add("-strict");
        cmds.add("-2");

        if (videoVolume <= 0.001f) { // 使用audio声音
            cmds.add("-c:a");
            cmds.add("aac");

            cmds.add("-map");
            cmds.add("1:a:0");

            cmds.add("-shortest");

            if (audioVolume < 0.99 || audioVolume > 1.01) {
                cmds.add("-vol");
                cmds.add(String.valueOf((int)(audioVolume * 100)));
            }

        } else if (videoVolume > 0.001f && audioVolume > 0.001f) { // 混合音视频声音

            cmds.add("-filter_complex");
            cmds.add(String.format(
                    "[0:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a0]; " +
                    "[1:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a1];" +
                    "[a0][a1]amix=inputs=2:duration=first[aout]", videoVolume, audioVolume));

            cmds.add("-map");
            cmds.add("[aout]");

        } else {
            Log.w(TAG, String.format(Locale.getDefault(),
                    "Illigal volume : SrcVideo = %.2f, SrcAudio = %.2f",
                    videoVolume, audioVolume));
            if (callback != null) {
                callback.onCompletion(RUN_FAILED == 1);
            }
        }

        cmds.add("-f");
        cmds.add("mp4");
        cmds.add("-y");
        cmds.add("-movflags");
        cmds.add("faststart");
        cmds.add(desVideo);

        runSync(cmds, callback);

        return true;
    }

    /**
     * 设置播放速度
     * @param srcVideo
     * @param speed
     * @param desVideo
     * @param callback
     */
    public static void setPlaybackSpeed(String srcVideo, float speed,
                                        String desVideo, OnCompletionListener callback) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);

        cmds.add("-y");
        cmds.add("-filter_complex");
        cmds.add("[0:v]setpts=" + speed + "*PTS[v];[0:a]atempo=" + 1 / speed + "[a]");
        cmds.add("-map");
        cmds.add("[v]");
        cmds.add("-map");
        cmds.add("[a]");
        cmds.add(desVideo);

        runSync(cmds, callback);
    }


    /**
     * 剪切视频
     * @param srcVideo
     * @param desVideo
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean cutVideo(String srcVideo, String desVideo,
                                   float startTime, float endTime) {

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);

        cmds.add("-y");
        cmds.add("-ss");
        cmds.add("" + startTime);
        cmds.add("-t");
        cmds.add("" + endTime);
        cmds.add("-c");
        cmds.add("copy");
        cmds.add(desVideo);

        String[] commands = cmds.toArray(new String[cmds.size()]);

        int result = runSafely(commands);

        return (result == 1);
    }

    /**
     * 将图片转成视频
     * @param picPath   图片路径
     * @param duration  时间
     * @param desVideo  输出视频路径
     * @param callback  回调
     */
    public static void convertPictureToVideo(String picPath, float duration,
                                             String desVideo, OnCompletionListener callback) {

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-y");
        cmds.add("-loop");
        cmds.add("1");
        cmds.add("-f");
        cmds.add("image2");
        cmds.add("-i");
        cmds.add(picPath);

        cmds.add("-t");
        cmds.add(""+duration);
        cmds.add("-r");
        cmds.add("15");

        cmds.add(desVideo);

        runSync(cmds, callback);
    }

    /**
     * 添加Gif到视频
     * @param videoPath
     * @param gifPath
     * @param x
     * @param y
     * @param startTime
     * @param desVideo
     * @param callback
     */
    public static void addGifToVideo(String videoPath, String gifPath,
                                     float x, float y,
                                     float startTime, String desVideo,
                                     OnCompletionListener callback) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-y");
        cmds.add("-i");
        cmds.add(videoPath);
//        cmds.add("-ignore_loop");
//        cmds.add("0");
        cmds.add("-i");
        cmds.add(gifPath);

        cmds.add("-ss");
        cmds.add("" + startTime);

        cmds.add("-filter_complex");
        cmds.add("overlay=" + x + ":" + y);

        cmds.add(desVideo);

        runSync(cmds, callback);
    }

    /**
     * 旋转视频
     * @param srcVideo
     * @param desVideo
     * @param callback
     */
    public static void rotateVideo(String srcVideo, String desVideo,
                                   OnCompletionListener callback) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);

        cmds.add("-vf");
//        cmds.add("transpose=1:portrait");
        cmds.add("rotate=PI/2");
        cmds.add(desVideo);

        runSync(cmds, callback);
    }

    /**
     * 添加水印
     * @param srcVideo
     * @param waterMark
     * @param desVideo
     * @param callback
     */
    public static void addWaterMark(String srcVideo, String waterMark,
                                    String desVideo, OnCompletionListener callback) {

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-i");
        cmds.add(waterMark);

        cmds.add("-y");
        cmds.add("-filter_complex");
        cmds.add("[0:v][1:v]overlay=main_w-overlay_w-10:main_h-overlay_h-10[out]"); // 位置
        cmds.add("-map");
        cmds.add("[out]");
        cmds.add("-map");
        cmds.add("0:a");
        cmds.add("-codec:a"); // keep audio
        cmds.add("copy");
        cmds.add(desVideo);

        runSync(cmds, callback);
    }

    /**
     * 将视频转成Gif
     * @param videoPath 视频路径
     * @param gifPath   gif路径
     * @param callback  回调
     */
    public static void convertVideoToGif(String videoPath, String gifPath,
                                         OnCompletionListener callback) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(videoPath);

        cmds.add("-f");
        cmds.add("gif");
        cmds.add(gifPath);

        runSync(cmds, callback);
    }

    /**
     * 合并多个视频
     * @param videoPathList 视频列表
     * @param desVideo      输出视频
     * @return
     */
    public static boolean combineVideo(List<String> videoPathList, String desVideo) {
        String tmpFile = "/sdcard/videolist.txt";
        String content = "ffconcat version 1.0\n";

        for (String path : videoPathList) {
            content += "\nfile " + path;
        }

        FileUtils.writeFile(tmpFile, content, false);

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-y");

        cmds.add("-safe");
        cmds.add("0");

        cmds.add("-f");
        cmds.add("concat");

        cmds.add("-i");
        cmds.add(tmpFile);


        cmds.add("-c");
        cmds.add("copy");

        cmds.add(desVideo);

        if (VERBOSE) {
            cmds.add(STR_DEBUG_PARAM);
        }

        String[] commands = cmds.toArray(new String[cmds.size()]);
        int result = runSafely(commands);
        FileUtils.deleteFile(tmpFile);

        return result == 1;
    }

    /**
     * 检测视频文件是否正确
     * @param videoPath 视频路径
     * @param time      时间
     * @param picPath
     * @param callback
     */
    public static void getVideoShoot(String videoPath, float time, String picPath,
                                     OnCompletionListener callback) {

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-y");
        cmds.add("-ss");
        cmds.add("" + time);
        cmds.add("-i");
        cmds.add(videoPath);
        cmds.add("-r");
        cmds.add("1");
        cmds.add("-vframes");
        cmds.add("1");
//        cmds.add("-vf");
//        cmds.add("select=eq(pict_type\\,I)");
        cmds.add("-an");
        cmds.add("-f");
        cmds.add("mjpeg");
        cmds.add(picPath);

        runSync(cmds, callback);
    }

    /**
     * 裁剪视频
     * @param srcPath   视频路径
     * @param x         x起始坐标
     * @param y         y起始坐标
     * @param width     宽度
     * @param height    高度
     * @param destPath  目标路径
     * @param callback  回调
     */
    public static void cropVideo(String srcPath, int x, int y, int width, int height,
                                 String destPath, OnCompletionListener callback) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-y");
        cmds.add("-i");
        cmds.add(srcPath);
        cmds.add("-filter:v");
        cmds.add("crop=" + width + ":" + height + ":" + x + ":" + y);
        cmds.add(destPath);

        runSync(cmds, callback);
    }


}
