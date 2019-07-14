package com.cgfay.filter.recorder;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cgfay.media.FFmpegUtils;
import com.cgfay.uitls.utils.FileUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频命令行执行队列
 * @author CainHuang
 * @date 2019/6/30
 */
public class VideoCommandQueue {

    private static final String TAG = "VideoCommandQueue";

    private Handler mHandler;

    public VideoCommandQueue() {
        HandlerThread thread = new HandlerThread("video_command_queue");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public void release() {
        if (mHandler == null) {
            return;
        }
        mHandler.getLooper().quitSafely();
        mHandler = null;
    }

    public void execCommand(String[] cmd, CommandCallback callback) {
        mHandler.post(() -> {
            int ret = FFmpegUtils.execute(cmd);
            if (callback != null) {
                callback.onResult(ret);
            }
        });
    }

    public interface CommandCallback {
        void onResult(int result);
    }

    /**
     * 合并音视频命令
     * @param videoPath
     * @param audioPath
     * @param output
     * @return
     */
    public static String[] mergeAudioVideo(String videoPath, String audioPath, String output) {
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
     * 合并多个视频，需要分辨率相同，帧率和码率也得相同
     * @param videos
     * @param output
     * @return
     */
    public static String[] mergeVideo(@NonNull Context context, @NonNull List<String> videos, @NonNull String output) {
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
     * 写入concat内容
     * @param content
     * @param fileName
     */
    public static void writeConcatToFile(List<String> content, String fileName) {
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
    public static String generateConcatPath(Context context) {
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
     * 获取时长
     * @param url
     * @return
     */
    private static long getDuration(String url) {
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
