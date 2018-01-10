package com.cgfay.cainffmpeg.nativehelper;

import android.util.Log;


import com.cgfay.utilslibrary.FileUtils;

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
     * 裁剪视频大小 (x, y - (x + cropWidth), (y + cropHeight))
     * @param srcVideo 原路径
     * @param desVideo 输出路径
     * @param bitRate  视频码率（2M = 2048, 1.5M = 1536）
     * @param cropWidth 裁剪的宽度
     * @param cropHeight 裁剪的高度
     * @param x         裁剪的起始位置x
     * @param y         裁剪的起始位置y
     * @return
     */
    public static boolean cutVideoSize(String srcVideo, String desVideo, int bitRate,
                                       int cropWidth, int cropHeight, int x, int y) {
        // 裁剪视频的命令如下：
        // ./ffmpeg -i 2x.mp4 -filter_complex "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]" -map "[v]" -map "[a]" output3.mp4
        String filter = String.format(Locale.getDefault(), "crop=%d:%d:%d:%d", cropWidth, cropHeight, x, y);
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-vf");
        cmds.add("" + filter);
        cmds.add("-acodec");
        cmds.add("copy");
        cmds.add("-b:v");
        int rate = (int)(bitRate * 1.5f);
        cmds.add(rate + "k");
        cmds.add("-y");
        cmds.add(desVideo);

        String[] commands = cmds.toArray(new String[cmds.size()]);

        int result = runSafely(commands);

        return (result == 1);
    }

    /**
     * 剪切视频时长(startTime, endTime)
     * @param srcVideo
     * @param desVideo
     * @param startTime
     * @param endTime
     * @return
     */
    public static boolean cutVideoTime(String srcVideo, String desVideo,
                                       int startTime, int endTime) {

        String startStr = "00:00:00";
        String endStr = "00:00:00";
        if (startTime < 0 || endTime < 0 || startTime >= endTime) {
            return false;
        }
        // 计算开始时间
        if (startTime < 10) {// 小于10秒
            startStr = "00:00:0" + startTime;
        } else if (startTime < 60) {//小于60秒
            startStr = "00:00:" + startTime;
        } else if (startTime < 300) { // 大于60秒时，小于5分钟
            int endwidth = startTime % 60;
            if (endwidth < 10) { // 尾数小于10
                startStr = "00:0" + (startTime / 60) + ":0" + endwidth;
            } else {
                startStr = "00:0" + (startTime / 60) + ":" + endwidth;
            }
        }

        // 计算结束时间
        if (endTime < 10) { // 小于10秒
            endStr = "00:00:0" + endTime;
        } else if (endTime < 60) { // 小于60秒
            endStr = "00:00:" + endTime;
        } else if (endTime < 300) { // 小于5分钟
            int endWidth = endTime % 60;
            if (endWidth < 10) {
                endStr = "00:0" + (endTime / 60) + ":0" + endWidth;
            } else {
                endStr = "00:0" + (endTime / 60) + ":" + endWidth;
            }
        }
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-vcodec");
        cmds.add("copy");
        cmds.add("-acodec");
        cmds.add("copy");
        cmds.add("-ss");
        cmds.add(startStr);
        cmds.add("-t");
        cmds.add(endStr);
        cmds.add(desVideo);

        String[] commands = cmds.toArray(new String[cmds.size()]);

        int result = runSafely(commands);

        return (result == 1);
    }


    /**
     * 调整播放速度
     * @param srcVideo
     * @param speed
     * @param desVideo
     * @param callback
     */
    public static void adjustVideoSpeed(String srcVideo, float speed,
                                        String desVideo, OnCompletionListener callback) {
        // ffmpeg命令
        //./ffmpeg -i 2x.mp4 -filter_complex "[0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a]" -map "[v]" -map "[a]" output3.mp4
        String filter = String.format(Locale.getDefault(), "[0:v]setpts=%f*PTS[v];[0:a]atempo=%f[a]", 1/speed, speed);

        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-filter_complex");
        cmds.add(filter);
        cmds.add("-map");
        cmds.add("[v]");
        cmds.add("-map");
        cmds.add("[a]");
        cmds.add("-y");
        cmds.add(desVideo);

        runSync(cmds, callback);
    }

    /**
     * 合成图片到视频(图片的尺寸需要提前调整)
     * @param imagePath 需要合成的图片路径
     * @param srcVideo 需要合成的视频路径
     * @param desVideo 合成的视频路径
     * @return
     */
    public static boolean mergeImageToVideo(String imagePath,
                                            String srcVideo, String desVideo) {
        // ffmpeg命令：
        //ffmpeg -i mediaPath -i imagePath -filter_complex overlay=0:0 -vcodec libx264 -profile:v baseline -preset ultrafast -b:v 3000k -g 30 -f mp4 outPath
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-i");
        cmds.add("copy");
        cmds.add("-i");
        cmds.add(imagePath);
        cmds.add("-filter_complex");
        cmds.add("overlay=0:0");
        cmds.add("-vcodec libx264 -profile:v baseline -preset ultrafast -b:v 3000k -g 25");
        cmds.add("-f mp4");
        cmds.add(desVideo);

        String[] commands = cmds.toArray(new String[cmds.size()]);
        int result = runSafely(commands);

        return (result == 1);
    }


    /**
     * MP4转TS
     * @param srcVideo MP4视频路径
     * @param desVideo TS输出路径
     * @return
     */
    public static boolean mp4ToTs(String srcVideo, String desVideo) {
        // FFmpeg命令
        //./ffmpeg -i 0.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts ts0.ts
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        cmds.add(srcVideo);
        cmds.add("-c");
        cmds.add("copy");
        cmds.add("-bsf:v");
        cmds.add("h264_mp4toannexb");
        cmds.add("-f");
        cmds.add("mpegts");
        cmds.add(desVideo);

        String[] commands = cmds.toArray(new String[cmds.size()]);
        int result = runSafely(commands);

        return (result == 1);
    }

    /**
     * TS合成MP4
     * @param tsPaths  TS流列表
     * @param desVideo 输出MP4视频路径
     * @return
     */
    public static boolean tsToMp4(List<String> tsPaths, String desVideo) {
        // FFmpeg命令
        //ffmpeg -i "concat:ts0.ts|ts1.ts|ts2.ts|ts3.ts" -c copy -bsf:a aac_adtstoasc out2.mp4
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("ffmpeg");
        cmds.add("-i");
        String concat="concat:";
        for (String part : tsPaths) {
            concat += part;
            concat += "|";
        }
        concat = concat.substring(0, concat.length()-1);
        cmds.add(concat);
        cmds.add("-c");
        cmds.add("copy");
        cmds.add("-bsf:a");
        cmds.add("aac_adtstoasc");
        cmds.add("-y");
        cmds.add(desVideo);
        String[] commands = cmds.toArray(new String[cmds.size()]);
        int result = runSafely(commands);

        return (result == 1);
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
     * 合并多个视频（Bug：不同滤镜的视频合成失败）
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


}
