//
// Created by CainHuang on 2019/9/15.
//
#if defined(__ANDROID__)

#include "MediaCodecWriter.h"

// /** Constant quality mode */
// public static final int BITRATE_MODE_CQ = 0;
// /** Variable bitrate mode */
// public static final int BITRATE_MODE_VBR = 1;
// /** Constant bitrate mode */
// public static final int BITRATE_MODE_CBR = 2;

// OMX_VIDEO_AVCPROFILETYPE
// public static final int AVCProfileBaseline = 0x01;
// public static final int AVCProfileMain     = 0x02;
// public static final int AVCProfileExtended = 0x04;
// public static final int AVCProfileHigh     = 0x08;
// public static final int AVCProfileHigh10   = 0x10;
// public static final int AVCProfileHigh422  = 0x20;
// public static final int AVCProfileHigh444  = 0x40;

// OMX_VIDEO_AVCLEVELTYPE
// public static final int AVCLevel1       = 0x01;
// public static final int AVCLevel1b      = 0x02;
// public static final int AVCLevel11      = 0x04;
// public static final int AVCLevel12      = 0x08;
// public static final int AVCLevel13      = 0x10;
// public static final int AVCLevel2       = 0x20;
// public static final int AVCLevel21      = 0x40;
// public static final int AVCLevel22      = 0x80;
// public static final int AVCLevel3       = 0x100;
// public static final int AVCLevel31      = 0x200;
// public static final int AVCLevel32      = 0x400;
// public static final int AVCLevel4       = 0x800;
// public static final int AVCLevel41      = 0x1000;
// public static final int AVCLevel42      = 0x2000;
// public static final int AVCLevel5       = 0x4000;
// public static final int AVCLevel51      = 0x8000;
// public static final int AVCLevel52      = 0x10000;

// OMX_VIDEO_HEVCPROFILETYPE
// public static final int HEVCProfileMain        = 0x01;
// public static final int HEVCProfileMain10      = 0x02;
// public static final int HEVCProfileMain10HDR10 = 0x1000;

// OMX_VIDEO_HEVCLEVELTYPE
// public static final int HEVCMainTierLevel1  = 0x1;
// public static final int HEVCHighTierLevel1  = 0x2;
// public static final int HEVCMainTierLevel2  = 0x4;
// public static final int HEVCHighTierLevel2  = 0x8;
// public static final int HEVCMainTierLevel21 = 0x10;
// public static final int HEVCHighTierLevel21 = 0x20;
// public static final int HEVCMainTierLevel3  = 0x40;
// public static final int HEVCHighTierLevel3  = 0x80;
// public static final int HEVCMainTierLevel31 = 0x100;
// public static final int HEVCHighTierLevel31 = 0x200;
// public static final int HEVCMainTierLevel4  = 0x400;
// public static final int HEVCHighTierLevel4  = 0x800;
// public static final int HEVCMainTierLevel41 = 0x1000;
// public static final int HEVCHighTierLevel41 = 0x2000;
// public static final int HEVCMainTierLevel5  = 0x4000;
// public static final int HEVCHighTierLevel5  = 0x8000;
// public static final int HEVCMainTierLevel51 = 0x10000;
// public static final int HEVCHighTierLevel51 = 0x20000;
// public static final int HEVCMainTierLevel52 = 0x40000;
// public static final int HEVCHighTierLevel52 = 0x80000;
// public static final int HEVCMainTierLevel6  = 0x100000;
// public static final int HEVCHighTierLevel6  = 0x200000;
// public static final int HEVCMainTierLevel61 = 0x400000;
// public static final int HEVCHighTierLevel61 = 0x800000;
// public static final int HEVCMainTierLevel62 = 0x1000000;
// public static final int HEVCHighTierLevel62 = 0x2000000;


// COLOR_FormatSurface indicates that the data will be a GraphicBuffer metadata reference.
// In OMX this is called OMX_COLOR_FormatAndroidOpaque.
//public static final int COLOR_FormatSurface                   = 0x7F000789;
/**
 * Flexible 12 bits per pixel, subsampled YUV color format with 8-bit chroma and luma
 * components.
 * <p>
 * Chroma planes are subsampled by 2 both horizontally and vertically.
 * Use this format with {@link Image}.
 * This format corresponds to {@link android.graphics.ImageFormat#YUV_420_888},
 * and can represent the {@link #COLOR_FormatYUV411Planar},
 * {@link #COLOR_FormatYUV411PackedPlanar}, {@link #COLOR_FormatYUV420Planar},
 * {@link #COLOR_FormatYUV420PackedPlanar}, {@link #COLOR_FormatYUV420SemiPlanar}
 * and {@link #COLOR_FormatYUV420PackedSemiPlanar} formats.
 *
 * @see Image#getFormat
 */
//public static final int COLOR_FormatYUV420Flexible            = 0x7F420888;


/**
 * If this codec is to be used as an encoder, pass this flag.
 */
//public static final int CONFIGURE_FLAG_ENCODE = 1;

MediaCodecWriter::MediaCodecWriter() {
    reset();
}

MediaCodecWriter::~MediaCodecWriter() {
    release();
}

void MediaCodecWriter::setOutputPath(const char *dstUrl) {

}

void MediaCodecWriter::setUseTimeStamp(bool use) {

}

void MediaCodecWriter::setMaxBitRate(int maxBitRate) {

}

void MediaCodecWriter::setOutputVideo(int width, int height, int frameRate, AVPixelFormat pixelFormat) {

}

void MediaCodecWriter::setOutputAudio(int sampleRate, int channels, AVSampleFormat sampleFormat) {

}

int MediaCodecWriter::prepare() {

    return 0;
}

int MediaCodecWriter::encodeMediaData(AVMediaData *mediaData) {
    return encodeMediaData(mediaData, nullptr);
}

int MediaCodecWriter::encodeMediaData(AVMediaData *mediaData, int *gotFrame) {
    return 0;
}

int MediaCodecWriter::encodeFrame(AVFrame *frame, AVMediaType type) {
    return encodeFrame(frame, type, nullptr);
}

int MediaCodecWriter::encodeFrame(AVFrame *frame, AVMediaType type, int *gotFrame) {

    return 0;
}

int MediaCodecWriter::stop() {
    return 0;
}

void MediaCodecWriter::release() {

}

void MediaCodecWriter::reset() {
    mMediaMuxer = nullptr;
    mVideoCodec = nullptr;
    mAudioCodec = nullptr;
}

void MediaCodecWriter::flush() {
    if (mVideoCodec != nullptr) {
        AMediaCodec_flush(mVideoCodec);
    }
    if (mAudioCodec != nullptr) {
        AMediaCodec_flush(mAudioCodec);
    }
}

/**
 * 打开音频编码器
 * @return
 */
int MediaCodecWriter::openAudioEncoder() {

    return 0;
}

/**
 * 打开视频编码器
 * @return
 */
int MediaCodecWriter::openVideoEncoder() {
    if (!mHasVideo) {
        return -1;
    }

    int ret;

    mVideoCodec = AMediaCodec_createDecoderByType(VIDEO_MIME_TYPE);
    AMediaFormat *mediaFormat = AMediaFormat_new();
    AMediaFormat_setString(mediaFormat, AMEDIAFORMAT_KEY_MIME, VIDEO_MIME_TYPE);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_WIDTH, mWidth);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_HEIGHT, mHeight);
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_BIT_RATE, (int32_t)(mMaxBitRate/2));
    AMediaFormat_setInt32(mediaFormat, "max-bitrate", (int32_t)mMaxBitRate);
    AMediaFormat_setInt32(mediaFormat, "bitrate-mode", 2); // BITRATE_MODE_CBR
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_FRAME_RATE, mFrameRate);
    // 设置色彩格式
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_COLOR_FORMAT, 0x7F000789); // COLOR_FormatSurface
    AMediaFormat_setInt32(mediaFormat, AMEDIAFORMAT_KEY_I_FRAME_INTERVAL, 1);   // GOP 1s
    int profile = 0;
    int level = 0;
    if (!strcmp("video/avc", VIDEO_MIME_TYPE)) {
        profile = 0x08; // AVCProfileHigh
        level = 0x100;  // AVCLevel30
        if (mWidth * mHeight >= 1280 * 720) {
            level = 0x200; //AVCLevel31
        }
        if (mWidth * mHeight >= 1920 * 1080) {
            level = 0x800; // AVCLevel40
        }
    } else if (!strcmp("video/hevc", VIDEO_MIME_TYPE)) {
        profile = 0x01; // HEVCProfileMain
        level = 0x80;   // HEVCHighTierLevel30
        if (mWidth * mHeight >= 1280 * 720) {
            level = 0x200; // HEVCHighTierLevel31
        }
        if (mWidth * mHeight >= 1920 * 1080) {
            level = 0x800;  // HEVCHighTierLevel40
        }
    }
    AMediaFormat_setInt32(mediaFormat, "profile", profile);
    AMediaFormat_setInt32(mediaFormat, "level", level);

    // 配置
    ret = AMediaCodec_configure(mVideoCodec, mediaFormat, nullptr, nullptr, 1); // CONFIGURE_FLAG_ENCODE
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_configure error: %d", ret);
        AMediaFormat_delete(mediaFormat);
        return ret;
    }

    // 打开解码器
    ret = AMediaCodec_start(mVideoCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_start error: %d", ret);
        AMediaFormat_delete(mediaFormat);
        return ret;
    }

    // 刷新缓冲区
    ret = AMediaCodec_flush(mVideoCodec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_start error: %d", ret);
        AMediaFormat_delete(mediaFormat);
        return ret;
    }

    AMediaFormat_delete(mediaFormat);
    return 0;
}

/**
 * 关闭编码器
 * @param codec
 * @return
 */
int MediaCodecWriter::closeEncoder(AMediaCodec *codec) {
    if (!codec) {
        return 0;
    }

    int ret;
    ret = AMediaCodec_flush(codec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_flush error: %d", ret);
        return ret;
    }

    ret = AMediaCodec_stop(codec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_stop", ret);
        return ret;
    }

    ret = AMediaCodec_delete(codec);
    if (ret != AMEDIA_OK) {
        LOGE("AMediaCodec_delete error: %d", ret);
        return ret;
    }
    codec = nullptr;

    return 0;
}

/**
 * 计算时间戳
 * @return
 */
uint64_t MediaCodecWriter::computePresentationTime() {
    mFrameIndex++;
    return mFrameIndex * 1000000L / mFrameRate + 132L;
}

/**
 * 查找起始码
 * @param data
 * @param offset
 * @param end
 * @return
 */
int MediaCodecWriter::avcFindStartCode(uint8 *data, int offset, int end) {
    int a = offset + 4 - (offset & 3);

    for (end -= 3; offset < a && offset < end; offset++) {
        if (data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 1){
            return offset;
        }
    }

    for (end -= 3; offset < end; offset += 4) {
        int x = ((data[offset] << 8 | data[offset + 1]) << 8 | data[offset + 2]) << 8 | data[offset + 3];
        // if ((x - 0x01000100) & (~x) & 0x80008000) // little endian
        // if ((x - 0x00010001) & (~x) & 0x00800080) // big endian
        if (((x - 0x01010101) & (~x) & 0x80808080) != 0) { // generic
            if (data[offset + 1] == 0) {
                if (data[offset] == 0 && data[offset + 2] == 1)
                    return offset;
                if (data[offset + 2] == 0 && data[offset + 3] == 1)
                    return offset + 1;
            }
            if (data[offset + 3] == 0) {
                if (data[offset + 2] == 0 && data[offset + 4] == 1)
                    return offset + 2;
                if (data[offset + 4] == 0 && data[offset + 5] == 1)
                    return offset + 3;
            }
        }
    }

    for (end += 3; offset < end; offset++) {
        if (data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 1){
            return offset;
        }
    }

    return end + 3;
}

/**
 * 查找起始码
 * @param data
 * @param offset
 * @param end
 * @return
 */
int MediaCodecWriter::findStartCode(uint8 *data, int offset, int end) {
    int out = avcFindStartCode(data, offset, end);
    if (offset < out && out < end && data[out - 1] == 0){
        out--;
    }
    return out;
}


#endif /* defined(__ANDROID__) */