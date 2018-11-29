package com.cgfay.ffmpeglibrary.Metadata;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;

/**
 * 基于FFmpeg实现的MediaMetadataRetriever
 * 参数可以参考ffmpeg官方文档： http://ffmpeg.org/ffmpeg-protocols.html
 * 以及Android自带的 MediaMetadataRetriever
 */
public class AVMediaMetadataRetriever {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("metadata_retriever");
    }

    // 文件名
    public static final String METADATA_KEY_FILENAME = "filename";

    // 文件创建者
    public static final String METADATA_KEY_ENCODED_BY = "encoded_by";

    // 编码器
    public static final String METADATA_KEY_ENCODER = "encoder";

    // 唱片
    public static final String METADATA_KEY_DISC_NUMBER = "disc";

    // 专辑
    public static final String METADATA_KEY_ALBUM = "album";

    // 专辑作者
    public static final String METADATA_KEY_ALBUMARTIST = "album_artist";

    // 艺术家
    public static final String METADATA_KEY_ARTIST = "artist";

    // 作曲
    public static final String METADATA_KEY_COMPOSER = "composer";

    // 演唱
    public static final String METADATA_KEY_PREFORMER = "performer";

    // 类型
    public static final String METADATA_KEY_GENRE = "genre";

    // 标题
    public static final String METADATA_KEY_TITLE = "title";

    // 描述
    public static final String METADATA_KEY_COMMENT = "comment";

    // 语言
    public static final String METADATA_KEY_LANGUAGE = "language";

    // 出版商
    public static final String METADATA_KEY_PUBLISHER = "publisher";

    // 广播名
    public static final String METADATA_KEY_SERVICE_NAME = "service_name";

    // 广播服务提供商
    public static final String METADATA_KEY_SERVICE_PROVIDER = "service_provider";

    // 版权
    public static final String METADATA_KEY_COPYRIGHT =  "copyright";

    // 日期
    public static final String METADATA_KEY_YEAR = "date";

    // 轨道数
    public static final String METADATA_KEY_NUM_TRACKS = "track";

    // 比特率
    public static final String METADATA_KEY_BITRATE = "bitrate";

    // 创建时间
    public static final String METADATA_KEY_CREATION_TIME = "creation_time";

    // 视频编解码器
    public static final String METADATA_KEY_VIDEO_CODEC = "video_codec";

    // 视频宽度
    public static final String METADATA_KEY_VIDEO_WIDTH = "video_width";

    // 视频高度
    public static final String METADATA_KEY_VIDEO_HEIGHT = "video_height";

    // 视频旋转角度
    public static final String METADATA_KEY_VIDEO_ROTATE = "rotate";

    // 容器信息major_brand
    public static final String METADATA_KEY_MAJOR_BRAND = "major_brand";

    // 容器信息minor version
    public static final String METADATA_KEY_MINOR_VERSION = "minor_version";

    // 容器信息compatible_brands
    public static final String METADATA_KEY_COMPATIBLE_BRANDS = "compatible_brands";

    // 音频编解码器
    public static final String METADATA_KEY_AUDIO_CODEC = "audio_codec";

    // 时长
    public static final String METADATA_KEY_DURATION = "duration";

    // 帧率
    public static final String METADATA_KEY_CAPTURE_FRAMERATE = "frame_rate";

    // 文件大小
    public static final String METADATA_KEY_FILE_SIZE = "file_size";

    // icy信息
    public static final String METADATA_KEY_ICY_METADATA = "icy_metadata";

    // chapter数量
    public static final String METADATA_KEY_CHAPTER_COUNT = "chapter_count";

    // chapter起始时间
    public static final String METADATA_KEY_CHAPTER_START = "chapter_start";

    // chapter结束时间
    public static final String METADATA_KEY_CHAPTER_END = "chapter_end";


    public AVMediaMetadataRetriever() {
        nativeSetup();
    }

    @Override
    protected void finalize() throws Throwable {
        nativeRelease();
        super.finalize();
    }

    /**
     * 销毁
     */
    public void release() {
        nativeRelease();
    }

    /**
     * 设置数据源
     * @param path
     */
    public void setDataSource(String path) {
        nativeSetDataSource(path);
    }

    /**
     * 获取metedata数据
     * @param key
     * @return
     */
    public String getMetadata(String key) {
        return nativeGetMetadata(key);
    }

    /**
     * 获取metadata数据
     * @param key
     * @param chapter
     * @return
     */
    public String getMetadata(String key, int chapter) {
        return nativeGetMetadata(key, chapter);
    }

    /**
     * 获取媒体数据
     * @return
     */
    public AVMediaMetadata getMetadata() {
        HashMap<String, String> hashMap = nativeGetMetadata();
        if (hashMap != null) {
            AVMediaMetadata mediaMetadata = new AVMediaMetadata(hashMap);
            return mediaMetadata;
        }
        return null;
    }

    /**
     * 提取一帧数据
     * @param timeUs
     * @return
     */
    public Bitmap getFrame(long timeUs) {
        byte[] data = nativeGetFrame(timeUs);
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }

    /**
     * 按照特定宽高提取一帧数据
     * @param timeUs
     * @param width
     * @param height
     * @return
     */
    public Bitmap getFrame(long timeUs, int width, int height) {
        byte[] data = nativeGetFrame(timeUs, width, height);
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }

    /**
     * 获取专辑/封面图片
     * @return
     */
    public Bitmap getCoverPicture() {
        byte[] data = nativeGetCoverPicture();
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }

    // native 方法
    private native void nativeSetup();
    private native void nativeRelease();
    private native void nativeSetDataSource(String path);
    private native String nativeGetMetadata(String key);
    private native String nativeGetMetadata(String key, int chapter);
    private native HashMap<String, String> nativeGetMetadata();
    private native byte[] nativeGetFrame(long timeUs);
    private native byte[] nativeGetFrame(long timeUs, int width, int height);
    private native byte[] nativeGetCoverPicture();
}
