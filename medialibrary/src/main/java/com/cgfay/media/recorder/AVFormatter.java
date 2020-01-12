package com.cgfay.media.recorder;

import android.media.AudioFormat;

/**
 * 格式参数，跟Native层的基类对齐，不能修改
 */
public final class AVFormatter {

    // 图像像素格式
    public static final int PIXEL_FORMAT_NONE = 0;
    public static final int PIXEL_FORMAT_NV21 = 1;
    public static final int PIXEL_FORMAT_YV12 = 2;
    public static final int PIXEL_FORMAT_NV12 = 3;
    public static final int PIXEL_FORMAT_YUV420P = 4;
    public static final int PIXEL_FORMAT_YUV420SP = 5;
    public static final int PIXEL_FORMAT_ARGB = 6;
    public static final int PIXEL_FORMAT_ABGR = 7;
    public static final int PIXEL_FORMAT_RGBA = 8;

    // 音频采样格式
    public static final int SAMPLE_FORMAT_NONE = 0;
    public static final int SAMPLE_FORMAT_8BIT = 8;
    public static final int SAMPLE_FORMAT_16BIT = 16;
    public static final int SAMPLE_FORMAT_FLOAT = 32;

    /**
     * 获取采样格式
     * @param audioFormat AudioFormat格式
     * @return
     */
    public static int getSampleFormat(int audioFormat) {
        int sampleFormat = SAMPLE_FORMAT_NONE;
        switch (audioFormat) {
            case AudioFormat.ENCODING_PCM_8BIT: {
                sampleFormat = SAMPLE_FORMAT_8BIT;
                break;
            }

            case AudioFormat.ENCODING_PCM_16BIT: {
                sampleFormat = SAMPLE_FORMAT_16BIT;
                break;
            }

            case AudioFormat.ENCODING_PCM_FLOAT: {
                sampleFormat = SAMPLE_FORMAT_FLOAT;
                break;
            }
        }
        return sampleFormat;
    }
}
