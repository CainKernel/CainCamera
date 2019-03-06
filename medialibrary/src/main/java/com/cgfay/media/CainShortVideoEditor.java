package com.cgfay.media;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cgfay.media.annotations.AccessedByNative;
import com.cgfay.utilslibrary.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 短视频编辑器，用于裁剪音视频、音频混响、音视频合成等处理
 * 通过命令行来实现
 */
public class CainShortVideoEditor {

    private static final String TAG = "CainShortVideoEditor";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("video_editor");
        native_init();
    }

    // The field below is accessed by native methods
    @AccessedByNative
    private long mNativeContext;

    private EventHandler mEventHandler;

    public CainShortVideoEditor() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
        native_setup(new WeakReference<CainShortVideoEditor>(this));
    }

    /**
     * 将pcm文件转成aac文件
     * @param pcmPath
     * @param sampleRate
     * @param channel
     * @return
     */
    public String pcmToAAC(String pcmPath, int sampleRate, int channel) {
        List<String> cmdList = new ArrayList<String>();

        String dstPath = VideoEditorUtil.createFileInBox("m4a");
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
        // 指定采样率48000hz
        cmdList.add("-b:a");
        cmdList.add("48000");

        cmdList.add("-y");
        cmdList.add(dstPath);

        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int ret = execute(command);
        if (ret == 0) {
            return dstPath;
        }else{
            FileUtils.deleteFile(dstPath);
            return null;
        }
    }

    /**
     * 将两个PCM音频混合
     * @param srcPath
     * @param sampleRate
     * @param channel
     * @param srcPath1
     * @param sampleRate1
     * @param channel1
     * @param volume
     * @param volume1
     * @return
     */
    public String pcmMix(String srcPath, int sampleRate, int channel,
                         String srcPath1, int sampleRate1, int channel1,
                         float volume, float volume1) {
        List<String> cmdList = new ArrayList<String>();

        String filter = String.format(Locale.getDefault(),
                "[0:a]volume=volume=%f[a1]; [1:a]volume=volume=%f[a2]; " +
                "[a1][a2]amix=inputs=2:duration=first:dropout_transition=2", volume, volume1);

        String dstPath = VideoEditorUtil.createFileInBox("pcm");
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

        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int  ret= execute(command);
        if(ret==0){
            return dstPath;
        }else{
            FileUtils.deleteFile(dstPath);
            return null;
        }
    }

    /**
     * 将两个音频延时混合，混合得到AAC格式的文件
     * @param audioPath
     * @param audioPath1
     * @param leftDelayMs   第二个音频左声道相对于第一个音频的延时
     * @param rightDelayMs  第二个音频右声道相对于第一个音频的延时
     * @return
     */
    public String audioDelayMix(String audioPath, String audioPath1, int leftDelayMs, int rightDelayMs) {
        List<String> cmdList = new ArrayList<String>();
        String overlayXY = String.format(Locale.getDefault(),
                "[1:a]adelay=%d|%d[delaya1]; " +
                "[0:a][delaya1]amix=inputs=2:duration=first:dropout_transition=2",
                leftDelayMs, rightDelayMs);

        String dstPath = VideoEditorUtil.createFileInBox("m4a");
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

        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int ret = execute(command);
        if (ret == 0) {
            return dstPath;
        } else {
            FileUtils.deleteFile(dstPath);
            return null;
        }
    }

    /**
     * 两个音频文件按照不同音量进行合成
     * @param audioPath1
     * @param audioPath2
     * @param volume1
     * @param volume2
     * @return
     */
    public String audioVolumeMix(String audioPath1, String audioPath2, float volume1, float volume2) {
        List<String> cmdList = new ArrayList<String>();

        String filter = String.format(Locale.getDefault(),
                "[0:a]volume=volume=%f[a1]; [1:a]volume=volume=%f[a2]; " +
                "[a1][a2]amix=inputs=2:duration=first:dropout_transition=2", volume1, volume2);

        String dstPath = VideoEditorUtil.createFileInBox("m4a");
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

        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int ret = execute(command);
        if (ret == 0) {
            return dstPath;
        } else {
            FileUtils.deleteFile(dstPath);
            return null;
        }
    }

    /**
     * 将pcm和video文件合并，视频文件不能有音频流
     * @param pcmPath       PCM文件路径
     * @param sampleRate    采样率
     * @param channel       通道
     * @param videoPath     视频文件路径
     * @return              返回合成的mp4文件，失败返回null
     */
    public String pcmVideoCombine(String pcmPath, int sampleRate, int channel, String videoPath) {
        List<String> cmdList = new ArrayList<String>();

        String dstPath = VideoEditorUtil.createFileInBox("mp4");
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


        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int ret = execute(command);
        if (ret == 0) {
            return dstPath;
        } else {
            FileUtils.deleteFile(dstPath);
            return null;
        }
    }

    /**
     * 音頻和視頻按不同音量进行混合
     * @param videoPath     源视频路径
     * @param audioPath     源音频路径
     * @param videoVolume   视频音量 0.0 ~ 1.0
     * @param audioVolume   音频音量 0.0 ~ 1.0
     * @return  返回混合后的视频文件，null表示失败
     */
    public String audioVideoMix(String videoPath, String audioPath,
                                float videoVolume, float audioVolume) {

        if (FileUtils.fileExists(videoPath) && FileUtils.fileExists(audioPath)) {

            String dstPath = VideoEditorUtil.createFileInBox("mp4");

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
                        "[0:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a0]; " +
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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstPath;
            } else {
                FileUtils.deleteFile(dstPath);
                return null;
            }

        }
        return null;
    }

    /**
     * 分离音频文件
     * @param srcPath
     * @return  返回音频文件
     */
    public String splitAudioTrack(String srcPath) {

        // 判断输入类型是否mp3/m4a文件
        String audioPath = null;
        String suffix = FileUtils.extractFileSuffix(srcPath);
        if (suffix.equals("m4a") || suffix.equals("mp3")) {
            audioPath = VideoEditorUtil.createFileInBox(suffix);
        } else { // 如果是mp4文件，则转码成aac
            audioPath = VideoEditorUtil.createFileInBox("aac");
        }
        if (audioPath == null) {
            return null;
        }

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-acodec");
        cmdList.add("copy");
        cmdList.add("-vn");

        cmdList.add("-y");
        cmdList.add(audioPath);

        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int ret = execute(command);
        if (ret == 0) {
            return audioPath;
        } else {
            FileUtils.deleteFile(audioPath);
            return null;
        }
    }

    /**
     * 分离视频文件
     * @param srcPath
     * @return
     */
    public String splitVideoTrack(String srcPath) {

        String videoPath  = VideoEditorUtil.createFileInBox("mp4");

        List<String> cmdList = new ArrayList<String>();
        cmdList.add("ffmpeg");

        cmdList.add("-i");
        cmdList.add(srcPath);

        cmdList.add("-vcodec");
        cmdList.add("copy");
        cmdList.add("-an");
        cmdList.add("-y");
        cmdList.add(videoPath);

        String[] command = new String[cmdList.size()];
        for (int i = 0; i < cmdList.size(); i++) {
            command[i] = (String) cmdList.get(i);
        }
        int ret = execute(command);
        if (ret == 0) {
            return videoPath;
        } else {
            FileUtils.deleteFile(videoPath);
            return null;
        }
    }

    /**
     * 音频裁剪，输出后缀跟输入后缀一致
     * @param srcPath
     * @param start     起始秒
     * @param duration  结束秒
     * @return
     */
    public String audioCut(String srcPath, float start, float duration) {

        if (FileUtils.fileExists(srcPath)) {

            List<String> cmdList = new ArrayList<String>();

            String dstFile = VideoEditorUtil.createFileInBox(FileUtils.extractFileSuffix(srcPath));
            cmdList.add("ffmpeg");

            cmdList.add("-i");
            cmdList.add(srcPath);

            cmdList.add("-ss");
            cmdList.add(String.valueOf(start));

            cmdList.add("-t");
            cmdList.add(String.valueOf(duration));

            cmdList.add("-acodec");
            cmdList.add("copy");

            cmdList.add("-y");
            cmdList.add(dstFile);

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstFile;
            } else {
                FileUtils.deleteFile(dstFile);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 视频裁剪
     * @param srcPath    源媒体路径
     * @param start     起始位置，秒
     * @param duration  时长，秒
     */
    public String videoCut(String srcPath, float start, float duration) {
        if (FileUtils.fileExists(srcPath)) {

            String dstFile = VideoEditorUtil.createFileInBox("mp4");

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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstFile;
            } else {
                FileUtils.deleteFile(dstFile);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 调整视频的播放速度，备注：1080P 20秒视频 4倍速需要将近30秒的时间，而2倍速则要将近40秒，实在太慢了
     * 建议使用videoCut方法调整速度
     * @param srcPath   源视频路径
     * @param speed 0.5 ~ 4.0
     * @return
     */
    public String adjustVideoSpeed(String srcPath, float speed) {
        if (FileUtils.fileExists(srcPath)) {

            String dstPath = VideoEditorUtil.createFileInBox("mp4");

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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstPath;
            } else {
                FileUtils.deleteFile(dstPath);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 调整音频速度，这里比较快，20秒1080P只需要800毫秒不到的时间
     * @param srcPath
     * @param speed
     * @return
     */
    public String adjustAudioSpeed(String srcPath, float speed) {
        if (FileUtils.fileExists(srcPath)) {

            if (speed < 0.25f) {
                speed = 0.25f;
            } else if (speed > 4.0f) {
                speed = 4.0f;
            }

            String dstPath = VideoEditorUtil.createFileInBox("aac");
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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstPath;
            } else {
                FileUtils.deleteFile(dstPath);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 裁剪视频的并调整速度，这里是用MediaCodec对视频转码处理的，速度非常快，但支持的格式比较少
     * @param srcPath
     * @param start
     * @param duration
     * @param speed
     */
    public String videoCut(String srcPath, float start, float duration, float speed)
            throws IllegalStateException {

        if (FileUtils.fileExists(srcPath)) {

            String tmpVideo = videoCut(srcPath, start, duration);

            if (tmpVideo == null) {
                return null;
            }
            // 获取倍速调整后的音频文件，这里不是性能瓶颈
            String audioPath = adjustAudioSpeed(tmpVideo, speed);
            // 分离视频流文件
            String videoTmpPath = splitVideoTrack(tmpVideo);
            // 删除裁剪中间文件
            FileUtils.deleteFile(tmpVideo);

            if (videoTmpPath == null && audioPath == null) {
                return null;
            }

            // 采用MediaExtractor、MediaCodec和MediaMuxer来对视频流进行倍速转码处理
            String videoPath = null;
            if (videoTmpPath != null) {
                boolean hasVideo = true;
                MediaExtractor extractor = new MediaExtractor();
                try {
                    extractor.setDataSource(videoTmpPath);
                } catch (Exception e) {
                    hasVideo = false;
                }
                int videoTrack = selectTrack(extractor, "video/");
                if (videoTrack < 0) {
                    hasVideo = false;
                }
                if (!hasVideo) {
                    FileUtils.deleteFile(audioPath);
                    FileUtils.deleteFile(videoTmpPath);
                    return null;
                }
                extractor.selectTrack(videoTrack);
                MediaFormat videoFormat = extractor.getTrackFormat(videoTrack);
                ByteBuffer mReadBuf = ByteBuffer.allocate(MAX_BUFF_SIZE);
                videoPath = VideoEditorUtil.createFileInBox("mp4");
                MediaMuxer muxer = null;
                int outVideoTrack = -1;
                try {
                    muxer = new MediaMuxer(videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    outVideoTrack = muxer.addTrack(videoFormat);
                } catch (IOException e) {
                    FileUtils.deleteFile(videoPath);
                    return null;
                }
                int framSize;
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                muxer.start();
                long pts = 0;
                // 读取数据处理
                while (true) {

                    if (!hasVideo) {
                        break;
                    }

                    mReadBuf.rewind();
                    framSize = extractor.readSampleData(mReadBuf, 0);
                    if (framSize < 0) {
                        hasVideo = false;
                    } else {
                        if (extractor.getSampleTrackIndex() == videoTrack) {
                            info.offset = 0;
                            info.size = framSize;
                            // 调整pts
                            pts = extractor.getSampleTime();
                            info.presentationTimeUs = (long)(pts / speed);
                            // 设置关键帧
                            if ((extractor.getSampleFlags() & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                                info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                            }
                            mReadBuf.rewind();
                            // 写入文件
                            muxer.writeSampleData(outVideoTrack, mReadBuf, info);
                            extractor.advance();
                            Message msg = mEventHandler.obtainMessage(EDITOR_PROCESSING, (int)(pts / 1000000), 0, null);
                            mEventHandler.sendMessage(msg);
                        }
                    }
                }
                extractor.release();
                if (muxer != null) {
                    try {
                        muxer.stop();
                        muxer.release();
                    } catch (Exception e) {
                        Log.e(TAG, "Muxer close error. No data was written");
                    } finally {
                        muxer = null;
                    }
                }

                // 音视频混合
                String dstPath = audioVideoMix(videoPath, audioPath, 0.0f, 1.0f);
                // 删除缓存文件
                FileUtils.deleteFile(videoTmpPath);
                FileUtils.deleteFile(videoPath);
                FileUtils.deleteFile(audioPath);
                return dstPath;

            } else {
                FileUtils.deleteFile(audioPath);
                return null;
            }
        } else {
            return null;
        }

    }

    private static final int MAX_BUFF_SIZE = 1048576;

    /**
     * 选择轨道
     * @param extractor
     * @param mimePrefix
     * @return
     */
    private int selectTrack(MediaExtractor extractor, String mimePrefix) {
        // 获取轨道总数
        int numTracks = extractor.getTrackCount();
        // 遍历查找包含mimePrefix的轨道
        for(int i = 0; i < numTracks; ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString("mime");
            if (mime.startsWith(mimePrefix)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 带速度调整的裁剪方法
     * @param srcPath
     * @param start
     * @param duration
     * @param speed
     * @return
     */
    public int videoCutSpeed(String srcPath, String dstPath, float start, float duration, float speed) {
        if (FileUtils.fileExists(srcPath)) {
            return _videoCut(srcPath, dstPath, start, duration, speed);
        } else {
            return -1;
        }
    }

    /**
     * native层的裁剪代码
     * @param srcPath       原始路径
     * @param dstPath       目标路径
     * @param start         起始位置，毫秒
     * @param duration      结束位置，毫秒
     * @param speed
     * @return
     */
    private native int _videoCut(String srcPath, String dstPath, float start, float duration, float speed);

    /**
     * 将图片转成视频
     * @param picPath
     * @param duration
     * @return
     */
    public String pictureTransVideo(String picPath, float duration) {
        if (FileUtils.fileExists(picPath)) {

            String dstFile = VideoEditorUtil.createFileInBox("mp4");

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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstFile;
            } else {
                FileUtils.deleteFile(dstFile);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 精确提取一帧图片
     * @param srcPath
     * @param position  指定的时间，单位：秒
     * @return
     */
    public String getOneFrameExactly(String srcPath, float position) {
        if (FileUtils.fileExists(srcPath)) {

            List<String> cmdList = new ArrayList<String>();

            String pngPath = VideoEditorUtil.createFileInBox("png");
            cmdList.add("ffmpeg");

            cmdList.add("-i");
            cmdList.add(srcPath);

            cmdList.add("-ss");
            cmdList.add(String.valueOf(position));

            cmdList.add("-vframes");
            cmdList.add("1");

            cmdList.add("-y");
            cmdList.add(pngPath);

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret= execute(command);
            if (ret == 0) {
                return pngPath;
            } else {
                FileUtils.deleteFile(pngPath);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 精确提取一帧图片，带缩放宽高
     * @param srcPath
     * @param position
     * @param scaleWidth
     * @param scaleHeight
     * @return
     */
    public String getOneScaleFrameExactly(String srcPath, float position, int scaleWidth, int scaleHeight) {
        if (FileUtils.fileExists(srcPath)) {

            List<String> cmdList = new ArrayList<String>();

            String pngPath = VideoEditorUtil.createFileInBox("png");

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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return pngPath;
            } else {
                FileUtils.deleteFile(pngPath);
                return null;
            }
        }
        return null;
    }

    /**
     * 將mp3转码成aac格式，存在耗时
     * @param srcPath
     * @return
     */
    public String mp3TransAAC(String srcPath) {
        if (FileUtils.fileExists(srcPath)) {

            String dstFile = VideoEditorUtil.createFileInBox("m4a");

            List<String> cmdList = new ArrayList<String>();
            cmdList.add("ffmpeg");

            cmdList.add("-i");
            cmdList.add(srcPath);

            cmdList.add("-acodec");
            cmdList.add("libfdk-aac"); // 这里使用libfdk-aac进行转码

            cmdList.add("-y");
            cmdList.add(dstFile);

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstFile;
            } else {
                FileUtils.deleteFile(dstFile);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 将mp3转码成aac格式，带时间
     * @param srcPath
     * @param start
     * @param duration
     * @return
     */
    public String mp3TransAAC(String srcPath, float start, float duration) {
        if (FileUtils.fileExists(srcPath)) {

            String dstPath = VideoEditorUtil.createFileInBox("m4a");

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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstPath;
            } else {
                FileUtils.deleteFile(dstPath);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * mp4转码成ts流，内部使用
     * @param srcPath
     * @return
     */
    protected String mp4TransTs(String srcPath) {
        if (FileUtils.fileExists(srcPath)) {

            List<String> cmdList = new ArrayList<String>();

            String dstTs = VideoEditorUtil.createFileInBox("ts");
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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstTs;
            } else {
                FileUtils.deleteFile(dstTs);
                return null;
            }
        }
        return null;
    }

    /**
     * 将ts流转码成MP4
     * @param tsArray
     * @return
     */
    protected String tsTransMP4(String[] tsArray) {
        if (FileUtils.fileExists(tsArray)) {

            String dstPath = VideoEditorUtil.createFileInBox(".mp4");
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

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            int ret = execute(command);
            if (ret == 0) {
                return dstPath;
            } else {
                FileUtils.deleteFile(dstPath);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 将分段录制的视频拼接在一起，视频的帧率、宽高大小等需要保证一致
     * @param mp4Array
     * @return
     */
    public String concatMp4(String[] mp4Array) {
        ArrayList<String> tsPathArray = new ArrayList<String>();
        // 1、转码ts
        for (int i = 0; i < mp4Array.length; i++) {
            String segTs1 = mp4TransTs(mp4Array[i]);
            tsPathArray.add(segTs1);
        }

        //2、把ts流转码成mp4
        String[] tsPaths = new String[tsPathArray.size()];
        for (int i = 0; i < tsPathArray.size(); i++) {
            tsPaths[i] = (String) tsPathArray.get(i);
        }
        String dstPath = tsTransMP4(tsPaths);

        //3、删除ts文件.
        for (int i = 0; i < tsPathArray.size(); i++) {
            FileUtils.deleteFile(tsPathArray.get(i));
        }
        return dstPath;
    }

    /**
     * 裁剪视频画面
     * @param srcPath
     * @param x             起始x轴位置
     * @param y             起始y轴位置
     * @param cropWidth     裁剪宽度
     * @param cropHeight    裁剪高度
     * @return
     */
    public String cropFrame(String srcPath, int x, int y, int cropWidth, int cropHeight) {
        // TODO
        return null;
    }

    /**
     * 缩放视频画面
     * @param srcPath
     * @param scaleWidth
     * @param scaleHeight
     * @return
     */
    public String scaleFrame(String srcPath, int scaleWidth, int scaleHeight) {
        // TODO
        return null;
    }

    /**
     * 给视频添加水印
     * @param srcPath
     * @param picPath
     * @param x
     * @param y
     * @return
     */
    public String overlayFrame(String srcPath, String picPath, int x, int y) {
        // TODO
        return null;
    }

    /**
     * 视频逆序, TODO 不能用命令行来处理，分辨率较大时执行到一半就会崩掉，这里需要自行用代码实现。
     * @param srcPath
     * @return
     */
    public String reverseVideo(String srcPath) {
        // TODO 只能解码然后在做处理。
        return null;
    }


    /**
     * 执行命令行，执行成功返回0，失败返回错误码。
     * @param command  命令行数组
     * @return  执行结果
     */
    private native int execute(String[] command);

    /**
     * Call it when one is done with the object. This method releases the memory
     * allocated internally.
     */
    public void release() {
        mOnVideoEditorProcessListener = null;
        _release();
    }

    private native void _release();

    private native void native_setup(Object editor_this);
    private static native void native_init();

    private native final void native_finalize();

    @Override
    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }

    public static class VideoEditorUtil {

        public static final String TAG = "VideoEditorUtil";

        public static String TMP_DIR = "/storage/emulated/0/Android/data/com.cgfay.caincamera/cache";

        private VideoEditorUtil() {

        }

        /**
         * 获取缓存目录
         * @return
         */
        public static String getTempPath() {
            File file = new File(TMP_DIR);
            if (file.exists() == false) {
                file.mkdirs();
            }
            return TMP_DIR;
        }

        /**
         * 创建文件路径
         * @param dir
         * @param suffix
         * @return
         */
        public static String createPath(String dir, String suffix) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int second = c.get(Calendar.SECOND);
            int millisecond = c.get(Calendar.MILLISECOND);
            year = year - 2000;
            String name = dir;
            File d = new File(name);

            // 如果目录不中存在，创建这个目录
            if (!d.exists())
                d.mkdir();
            name += "/";


            name += String.valueOf(year);
            name += String.valueOf(month);
            name += String.valueOf(day);
            name += String.valueOf(hour);
            name += String.valueOf(minute);
            name += String.valueOf(second);
            name += String.valueOf(millisecond);
            if (!suffix.startsWith(".")) {
                name += ".";
            }
            name += suffix;
            return name;
        }

        /**
         * 创建文件路径
         * @param suffix
         * @return
         */
        public static String createPathInBox(String suffix) {
            return createPath(TMP_DIR, suffix);
        }

        /**
         * 在指定目录创建指定后缀文件
         * @param dir
         * @param suffix
         * @return
         */
        public static String createFile(String dir, String suffix) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int second = c.get(Calendar.SECOND);
            int millisecond = c.get(Calendar.MILLISECOND);
            year = year - 2000;
            String name = dir;
            File d = new File(name);

            // 如果目录不中存在，创建这个目录
            if (!d.exists())
                d.mkdir();
            name += "/";


            name += String.valueOf(year);
            name += String.valueOf(month);
            name += String.valueOf(day);
            name += String.valueOf(hour);
            name += String.valueOf(minute);
            name += String.valueOf(second);
            name += String.valueOf(millisecond);
            if (suffix.startsWith(".") == false) {
                name += ".";
            }
            name += suffix;

            try {
                Thread.sleep(1);  //保持文件名的唯一性.
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            File file = new File(name);
            if (file.exists() == false) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return name;
        }

        /**
         * 创建指定后缀的文件
         * @param suffix
         * @return
         */
        public static String createFileInBox(String suffix) {
            return createFile(TMP_DIR, suffix);
        }
    }


    // 对应Native层CainShortVideoEditor.h中的枚举 editor_event_type
    // 处理过程回调
    private static final int EDITOR_PROCESSING = 1;
    // 处理出错回调
    private static final int EDITOR_ERROR = 100;

    private class EventHandler extends Handler {

        private CainShortVideoEditor mVideoEditor;

        public EventHandler(CainShortVideoEditor editor, Looper looper) {
            super(looper);
            mVideoEditor = editor;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mVideoEditor.mNativeContext == 0) {
                Log.w(TAG, "videoeditor went away with unhandled events");
                return;
            }

            switch (msg.what) {

                case EDITOR_PROCESSING: {
                    if (mOnVideoEditorProcessListener != null) {
                        mOnVideoEditorProcessListener.onProcessing(msg.arg1);
                    }
                    return;
                }

                case EDITOR_ERROR: {
                    Log.e(TAG, "Error ( " + msg.arg1 + "," + msg.arg2 + ")");
                    if (mOnVideoEditorProcessListener != null) {
                        mOnVideoEditorProcessListener.onError();
                    }
                    return;
                }

                default:{
                    Log.e(TAG, "Unknown message type " + msg.what);
                    return;
                }
            }
        }
    }

    /**
     * Called from native code when an interesting event happens.  This method
     * just uses the EventHandler system to post the event back to the main app thread.
     * We use a weak reference to the original MediaPlayer object so that the native
     * code is safe from the object disappearing from underneath it.  (This is
     * the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object videoeditor_ref,
                                            int what, int arg1, int arg2, Object obj) {
        final CainShortVideoEditor editor = (CainShortVideoEditor)((WeakReference)videoeditor_ref).get();
        if (editor == null) {
            return;
        }

        if (editor.mEventHandler != null) {
            Message m = editor.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            editor.mEventHandler.sendMessage(m);
        }
    }

    /**
     * 视频编辑器处理监听器
     */
    public interface OnVideoEditorProcessListener {

        // time正在处理的秒
        void onProcessing(int time);

        void onError();
    }

    public void setOnVideoEditorProcessListener(OnVideoEditorProcessListener listener) {
        mOnVideoEditorProcessListener = listener;
    }

    private OnVideoEditorProcessListener mOnVideoEditorProcessListener;

    /**
     * 视频裁剪监听器
     */
    public interface OnVideoCutListener {

        // 裁剪进度
        void onVideoCutting(long current, long duration);

        // 裁剪完成
        void onVideoCutFinish();

        // 裁剪出错
        void onVideoCutError();
    }

    public void setOnVideoCutListener(OnVideoCutListener listener) {
        mOnVideoCutListener = listener;
    }

    private OnVideoCutListener mOnVideoCutListener;
}
