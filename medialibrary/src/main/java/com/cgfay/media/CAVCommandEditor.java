package com.cgfay.media;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.cgfay.uitls.utils.FileUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 命令行编辑器，使用命令行对视频、音频进行编辑处理
 */
public class CAVCommandEditor {

    private static final String TAG = "CAVCommandEditor";

    private Handler mHandler;

    public CAVCommandEditor() {
        HandlerThread thread = new HandlerThread("cav_command_editor");
        thread.start();
        mHandler = new Handler(thread.getLooper());
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
        if (mHandler == null) {
            return;
        }
        mHandler.getLooper().quitSafely();
        mHandler = null;
    }

    /**
     * 执行命令行
     * @param cmd
     * @param callback
     */
    public void execCommand(String[] cmd, @Nullable CommandProcessCallback callback) {
        if (cmd == null || cmd.length <= 0) {
            if (callback != null) {
                callback.onProcessResult(-1);
            }
            return;
        }
        mHandler.post(() -> {
            int ret = CAVCommand.execute(cmd, new CAVCommand.OnExecutorListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure() {

                }

                @Override
                public void onProgress(int process) {
                    if (callback != null) {
                        callback.onProcessing(process);
                    }
                }
            });
            if (callback != null) {
                callback.onProcessResult(ret);
            }
        });
    }

    /**
     * 命令行执行回调
     */
    public interface CommandProcessCallback {

        void onProcessing(int current);

        void onProcessResult(int result);
    }

    /**
     * 合并音视频命令
     * @param videoPath
     * @param audioPath
     * @param output
     * @return
     */
    public static String[] mergeAudioVideo(@NonNull String videoPath, @NonNull String audioPath, @NonNull String output) {
        List<String> cmdList = new ArrayList<String>();

        float duration = getDuration(audioPath) / 1000000f;
        cmdList.add("ffmpeg");
        cmdList.add("-i");
        cmdList.add(audioPath);
        cmdList.add("-i");
        cmdList.add(videoPath);

        cmdList.add("-ss");
        cmdList.add("0");
        cmdList.add("-t");
        cmdList.add("" + duration);

        cmdList.add("-acodec");
        cmdList.add("copy");
        cmdList.add("-vcodec");
        cmdList.add("copy");

        cmdList.add("-y");
        cmdList.add(output);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 使用concat协议合并多个视频命令，需要分辨率相同，帧率和码率也得相同
     * @param videos
     * @param output
     * @return
     */
    public static String[] concatVideo(@NonNull Context context, @NonNull List<String> videos, @NonNull String output) {
        String concatPath = generateConcatPath(context);
        writeConcatToFile(videos, concatPath);
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");
        cmdList.add("-f");
        cmdList.add("concat");
        cmdList.add("-safe");
        cmdList.add("0");
        cmdList.add("-i");
        cmdList.add(concatPath);

        cmdList.add("-c");
        cmdList.add("copy");
        cmdList.add("-threads");
        cmdList.add("5");

        cmdList.add("-y");
        cmdList.add(output);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将pcm文件转成aac文件命令
     * @param pcmPath
     * @param dstPath
     * @param sampleRate
     * @param channel
     * @return
     */
    public static String[] pcmToAAC(@NonNull String pcmPath, @NonNull String dstPath, int sampleRate, int channel) {
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("ffmpeg");

        cmdList.add("-f");
        cmdList.add("s16le");
        cmdList.add("-ar");
        cmdList.add(String.valueOf(sampleRate));
        cmdList.add("-ac");
        cmdList.add(String.valueOf(channel));

        cmdList.add("-i");
        cmdList.add(pcmPath);

        // 指定libfdk-aac作为aac编码器
        cmdList.add("-acodec");
        cmdList.add("libfdk-aac");
        // 指定采样率44100hz
        cmdList.add("-b:a");
        cmdList.add("44100");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将两个PCM音频混合
     * @param srcPath
     * @param srcPath1
     * @param dstPath
     *
     * @param sampleRate
     * @param channel
     * @param volume
     *
     * @param sampleRate1
     * @param channel1
     * @param volume1
     * @return
     */
    public static String[] pcmMix(@NonNull String srcPath, @NonNull String srcPath1, @NonNull String dstPath,
                                  int sampleRate, int channel, float volume,
                                  int sampleRate1, int channel1, float volume1) {
        List<String> cmdList = new ArrayList<String>();

        String filter = String.format(Locale.getDefault(),
                "[0:a]volume=volume=%f[a1]; [1:a]volume=volume=%f[a2]; " +
                        "[a1][a2]amix=inputs=2:duration=first:dropout_transition=2", volume, volume1);

        cmdList.add("ffmpeg");

        cmdList.add("-f");
        cmdList.add("s16le");
        cmdList.add("-ar");
        cmdList.add(String.valueOf(sampleRate));
        cmdList.add("-ac");
        cmdList.add(String.valueOf(channel));

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-f");
        cmdList.add("s16le");
        cmdList.add("-ar");
        cmdList.add(String.valueOf(sampleRate1));
        cmdList.add("-ac");
        cmdList.add(String.valueOf(channel1));

        cmdList.add("-i");
        cmdList.add(srcPath1);

        cmdList.add("-filter_complex");
        cmdList.add(filter);
        cmdList.add("-f");
        cmdList.add("s16le");
        cmdList.add("-acodec");
        cmdList.add("pcm_s16le");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将两个音频延时混合命令，混合得到AAC格式的文件
     * @param audioPath
     * @param audioPath1
     * @param dstPath
     * @param leftDelayMs   第二个音频左声道相对于第一个音频的延时
     * @param rightDelayMs  第二个音频右声道相对于第一个音频的延时
     * @return
     */
    public static String[] audioDelayMix(@NonNull String audioPath, @NonNull String audioPath1, @NonNull String dstPath, int leftDelayMs, int rightDelayMs) {
        List<String> cmdList = new ArrayList<String>();
        String overlayXY = String.format(Locale.getDefault(),
                "[1:a]adelay=%d|%d[delaya1]; " +
                        "[0:a][delaya1]amix=inputs=2:duration=first:dropout_transition=2",
                leftDelayMs, rightDelayMs);

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(audioPath);

        cmdList.add("-i");
        cmdList.add(audioPath1);

        cmdList.add("-filter_complex");
        cmdList.add(overlayXY);

        cmdList.add("-acodec");
        cmdList.add("libfdk-aac");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 两个音频文件按照不同音量进行合成命令
     * @param audioPath1
     * @param audioPath2
     * @param volume1
     * @param volume2
     * @return
     */
    public static String[] audioVolumeMix(@NonNull String audioPath1, @NonNull String audioPath2, @NonNull String dstPath, float volume1, float volume2) {
        List<String> cmdList = new ArrayList<String>();

        String filter = String.format(Locale.getDefault(),
                "[0:a]volume=volume=%f[a1]; [1:a]volume=volume=%f[a2]; " +
                        "[a1][a2]amix=inputs=2:duration=first:dropout_transition=2", volume1, volume2);

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(audioPath1);

        cmdList.add("-i");
        cmdList.add(audioPath2);

        cmdList.add("-filter_complex");
        cmdList.add(filter);

        cmdList.add("-acodec");
        cmdList.add("libfdk-aac");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将pcm和video文件合并命令，视频文件不能有音频流
     * @param pcmPath       PCM文件路径
     * @param sampleRate    采样率
     * @param channel       通道
     * @param videoPath     视频文件路径
     * @return              返回合成的mp4文件，失败返回null
     */
    public static String[] pcmVideoMerge(@NonNull String pcmPath, @NonNull String videoPath,
                                         @NonNull String dstPath, int sampleRate, int channel) {
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("ffmpeg");

        cmdList.add("-f");
        cmdList.add("s16le");
        cmdList.add("-ar");
        cmdList.add(String.valueOf(sampleRate));
        cmdList.add("-ac");
        cmdList.add(String.valueOf(channel));

        cmdList.add("-i");
        cmdList.add(pcmPath);

        cmdList.add("-i");
        cmdList.add(videoPath);

        cmdList.add("-b:a");
        cmdList.add("48000");

        cmdList.add("-acodec");
        cmdList.add("libfdk-aac");

        cmdList.add("-vcodec");
        cmdList.add("copy");

        cmdList.add("-y");
        cmdList.add(dstPath);


        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 音頻和視頻按不同音量进行混合命令
     * @param videoPath     源视频路径
     * @param audioPath     源音频路径
     * @param videoVolume   视频音量 0.0 ~ 1.0
     * @param audioVolume   音频音量 0.0 ~ 1.0
     * @return  返回混合后的视频文件，null表示失败
     */
    public static String[] audioVideoMix(@NonNull String videoPath, @NonNull String audioPath,
                                         @NonNull String dstPath, float videoVolume, float audioVolume) {
        ArrayList<String> cmdList = new ArrayList<>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(videoPath);

        cmdList.add("-i");
        cmdList.add(audioPath);

        cmdList.add("-c:v");
        cmdList.add("copy");
        cmdList.add("-map");
        cmdList.add("0:v:0");

        cmdList.add("-strict");
        cmdList.add("-2");

        if (videoVolume == 0.0f) { // 使用audio声音
            cmdList.add("-c:a");
            cmdList.add("aac");

            cmdList.add("-map");
            cmdList.add("1:a:0");

            cmdList.add("-shortest");

            if (audioVolume < 0.99 || audioVolume > 1.01) {
                cmdList.add("-vol");
                cmdList.add(String.valueOf((int)(audioVolume * 100)));
            }

        } else if (videoVolume > 0.001f && audioVolume > 0.001f) { // 混合音视频声音

            cmdList.add("-filter_complex");
            cmdList.add(String.format(
                    "[0:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a0];" +
                            "[1:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a1];" +
                            "[a0][a1]amix=inputs=2:duration=first[aout]", videoVolume, audioVolume));

            cmdList.add("-map");
            cmdList.add("[aout]");
        } else {
            Log.w(TAG, String.format(Locale.getDefault(),
                    "Illigal volume : SrcVideo = %.2f, SrcAudio = %.2f",
                    videoVolume, audioVolume));
        }

        cmdList.add("-f");
        cmdList.add("mp4");
        cmdList.add("-y");
        cmdList.add("-movflags");
        cmdList.add("faststart");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 分离音频文件
     * @param srcPath
     * @return  返回音频文件
     */
    public static String[] splitAudioTrack(@NonNull String srcPath, @NonNull String audioPath) {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-acodec");
        cmdList.add("copy");
        cmdList.add("-vn");

        cmdList.add("-y");
        cmdList.add(audioPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 分离视频文件
     * @param srcPath
     * @return
     */
    public static String[] splitVideoTrack(@NonNull String srcPath, @NonNull String videoPath) {

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-vcodec");
        cmdList.add("copy");
        cmdList.add("-an");
        cmdList.add("-y");
        cmdList.add(videoPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 视频裁剪
     * @param srcPath    源媒体路径
     * @param start     起始位置，秒
     * @param duration  时长，秒
     */
    public static String[] videoCut(@NonNull String srcPath, @NonNull String dstFile,
                                    float start, float duration) {
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-ss");
        cmdList.add(String.valueOf(start));

        cmdList.add("-t");
        cmdList.add(String.valueOf(duration));

        cmdList.add("-vcodec");
        cmdList.add("copy");

        cmdList.add("-acodec");
        cmdList.add("copy");

        cmdList.add("-y");
        cmdList.add(dstFile);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 音频裁剪命令，如果源文件是视频文件，则去掉视频流
     * @param srcPath
     * @param dstPath
     * @param start         // 开始时间(毫秒)
     * @param duration           // 结束时间(毫秒)
     * @return
     */
    public static String[] audioCut(@NonNull String srcPath, @NonNull String dstPath,
                                    int start, int duration) {
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-ss");
        cmdList.add(String.valueOf(start/1000));

        cmdList.add("-t");
        cmdList.add(String.valueOf(duration/1000));

        // 去掉视频流
        cmdList.add("-vn");

        cmdList.add("-acodec");
        cmdList.add("copy");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 调整视频的播放速度，备注：1080P 20秒视频 4倍速需要将近30秒的时间，而2倍速则要将近40秒，实在太慢了
     * 建议使用videoCut方法调整速度
     * @param srcPath   源视频路径
     * @param speed 0.5 ~ 4.0
     * @return
     */
    public static String[] adjustVideoSpeed(@NonNull String srcPath, @NonNull String dstPath, float speed) {

        if (speed < 0.5f) {
            speed = 0.5f;
        } else if (speed > 4.0f) {
            speed = 4.0f;
        }

        String filter = String.format(Locale.getDefault(), "[0:v]setpts=%f*PTS[v];[0:a]atempo=%f[a]", 1 / speed,
                speed);

        if (speed > 2.0) {
            filter = String.format(Locale.getDefault(), "[0:v]setpts=%f*PTS[v];[0:a]atempo=2.0,atempo=%f[a]", 1 / speed,
                    (speed / 2.0f));
        }

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        // 指定视频编码器
        cmdList.add("-vcodec");
        cmdList.add("libx264");

        cmdList.add("-filter_complex");
        cmdList.add(filter);

        cmdList.add("-map");
        cmdList.add("[v]");

        cmdList.add("-map");
        cmdList.add("[a]");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 调整音频速度，这里比较快，20秒1080P只需要800毫秒不到的时间
     * @param srcPath
     * @param speed
     * @return
     */
    public static String[] adjustAudioSpeed(@NonNull String srcPath, @NonNull String dstPath, float speed) {
        if (speed < 0.25f) {
            speed = 0.25f;
        } else if (speed > 4.0f) {
            speed = 4.0f;
        }

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-vn");

        cmdList.add("-filter:a");
        String filter = String.format(Locale.getDefault(), "atempo=%f", speed);
        // 备注：这里当速度大于2.0/小于0.5时，atempo都需要拆分成两个。此时拼接时呈倍数关系的。
        // 比如，speed = 3.0 时，相当于 atempo=2.0,atempo=1.5，先调整为2.0倍后再调整1.5倍
        if (speed > 2.0) {
            filter = String.format(Locale.getDefault(), "atempo=2.0,atempo=%f", (speed / 2.0f));
        } else if (speed < 0.5) {
            filter = String.format(Locale.getDefault(), "atempo=0.5,atempo=%f", (speed / 0.5f));
        }
        cmdList.add(filter);

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将图片转成视频
     * @param picPath
     * @param duration
     * @return
     */
    public static String[] pictureTransVideo(@NonNull String picPath, @NonNull String dstFile, float duration) {
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(picPath);

        cmdList.add("-loop");
        cmdList.add("1");

        cmdList.add("-t");
        cmdList.add(String.valueOf(duration));

        // 指定编码格式
        cmdList.add("-vcodec");
        cmdList.add("libx264");

        cmdList.add("-y");
        cmdList.add(dstFile);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 精确提取一帧图片命令
     * @param srcPath
     * @param position  指定的时间，单位：秒
     * @return
     */
    public static String[] getOneFrameExactly(@NonNull String srcPath, @NonNull String pngPath, float position) {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-ss");
        cmdList.add(String.valueOf(position));

        cmdList.add("-vframes");
        cmdList.add("1");

        cmdList.add("-y");
        cmdList.add(pngPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 精确提取一帧图片命令，带缩放宽高
     * @param srcPath
     * @param position
     * @param scaleWidth
     * @param scaleHeight
     * @return
     */
    public static String[] getOneScaleFrameExactly(@NonNull String srcPath, @NonNull String pngPath, float position, int scaleWidth, int scaleHeight) {
        List<String> cmdList = new ArrayList<String>();

        String resolution = String.valueOf(scaleWidth);
        resolution += "x";
        resolution += String.valueOf(scaleHeight);

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-ss");
        cmdList.add(String.valueOf(position));

        cmdList.add("-s");
        cmdList.add(resolution);

        cmdList.add("-vframes");
        cmdList.add("1");

        cmdList.add("-y");
        cmdList.add(pngPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 將mp3转码成aac命令，存在耗时
     * @param srcPath
     * @return
     */
    public static String[] mp3TransAAC(@NonNull String srcPath, @NonNull String dstFile) {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-acodec");
        cmdList.add("libfdk-aac"); // 这里使用libfdk-aac进行转码

        cmdList.add("-y");
        cmdList.add(dstFile);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将mp3转码成aac格式，带时长
     * @param srcPath
     * @param start
     * @param duration
     * @return
     */
    public static String[] mp3TransAAC(@NonNull String srcPath, @NonNull String dstPath, float start, float duration) {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-ss");
        cmdList.add(String.valueOf(start));

        cmdList.add("-t");
        cmdList.add(String.valueOf(duration));

        cmdList.add("-acodec");
        cmdList.add("libfdk-aac"); // 这里使用libfdk-aac进行转码

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * mp4转码成ts流命令
     * @param srcPath
     * @return
     */
    public static String[] mp4TransTs(@NonNull String srcPath, @NonNull String dstTs) {
        List<String> cmdList = new ArrayList<String>();

        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-c");
        cmdList.add("copy");

        cmdList.add("-bsf:v");
        cmdList.add("h264_mp4toannexb");

        cmdList.add("-f");
        cmdList.add("mpegts");

        cmdList.add("-y");
        cmdList.add(dstTs);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 将ts流转码成MP4命令
     * @param tsArray
     * @return
     */
    public static String[] tsTransMP4(String[] tsArray, @NonNull String dstPath) {
        String concat = "concat:";
        for (int i = 0; i < tsArray.length - 1; i++) {
            concat += tsArray[i];
            concat += "|";
        }
        concat += tsArray[tsArray.length - 1];

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(concat);

        cmdList.add("-c");
        cmdList.add("copy");

        cmdList.add("-bsf:a");
        cmdList.add("aac_adtstoasc");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] cmds = new String[cmdList.size()];
        cmdList.toArray(cmds);
        return cmds;
    }

    /**
     * 写入concat内容
     * @param content
     * @param fileName
     */
    private static void writeConcatToFile(List<String> content, String fileName) {
        String strContent = "";
        for (int i = 0; i < content.size(); i++) {
            strContent += "file " + content.get(i) + "\r\n";
        }
        try {
            File file = new File(fileName);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();
            file.createNewFile();
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
            Log.e(TAG, "concat path:" + fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
    }

    /**
     * 生成concat文件路径
     * @return
     */
    private static String generateConcatPath(Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "ff_concat.txt";
        // 删除旧的concat文件，防止出错
        FileUtils.deleteFile(path);

        // 重新创建新的concat.txt文件
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

    /**
     * 获取时长(微妙，us)
     * @param url
     * @return
     */
    public static long getDuration(String url) {
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(url);
            int videoExt = selectVideoTrack(mediaExtractor);
            if (videoExt == -1) {
                videoExt = selectAudioTrack(mediaExtractor);
                if (videoExt == -1) {
                    return 0;
                }
            }
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(videoExt);
            long res = mediaFormat.containsKey(MediaFormat.KEY_DURATION) ? mediaFormat.getLong(MediaFormat.KEY_DURATION) : 0;//时长
            mediaExtractor.release();
            return res;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 选择视频轨道
     * @param extractor
     * @return
     */
    private static int selectVideoTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 选择音频轨道
     * @param extractor
     * @return
     */
    private static int selectAudioTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

}
