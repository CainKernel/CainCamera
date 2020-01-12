//
// Created by CainHuang on 2020/1/11.
//

#include "NdkMediaWriter.h"

NdkMediaWriter::NdkMediaWriter() {
    reset();
}

NdkMediaWriter::~NdkMediaWriter() {
    release();
}

void NdkMediaWriter::setOutputPath(const char *dstUrl) {
    mDstUrl = av_strdup(dstUrl);
}

void NdkMediaWriter::setUseTimeStamp(bool use) {
    mUseTimeStamp = use;
}

void NdkMediaWriter::setMaxBitRate(int maxBitRate) {
    mMaxBitRate = maxBitRate;
}

void NdkMediaWriter::setOutputVideo(int width, int height, int frameRate,
                                        AVPixelFormat pixelFormat) {
    mWidth = width;
    mHeight = height;
    mPixelFormat = pixelFormat;
    mFrameRate = frameRate;
    if (mWidth > 0 && mHeight > 0 && mPixelFormat != AV_PIX_FMT_NONE) {
        mHasVideo = true;
    } else {
        mHasVideo = false;
    }
}

void NdkMediaWriter::setOutputAudio(int sampleRate, int channels,
                                        AVSampleFormat sampleFormat) {
    mSampleRate = sampleRate;
    mChannels = channels;
    mSampleFormat = sampleFormat;
    if (mSampleRate > 0 || mChannels > 0 && mSampleFormat != AV_SAMPLE_FMT_NONE) {
        mHasAudio = true;
    } else {
        mHasAudio = false;
    }
}

int NdkMediaWriter::prepare() {
    // 去掉奇数宽度
    if (mWidth % 2 == 1) {
        if (mHeight >= mWidth) {
            mHeight = (int) (1.0 * (mWidth - 1) / mWidth * mHeight);
            mHeight = mHeight % 2 == 1 ? mHeight - 1 : mHeight;
        }
        mWidth--;
    }

    // 去掉奇数高度
    if (mHeight % 2 == 1) {
        if (mWidth >= mHeight) {
            mWidth = (int) (1.0 * (mHeight - 1) / mHeight * mWidth);
            mWidth = mWidth % 2 == 1 ? mWidth - 1 : mWidth;
        }
        mHeight--;
    }

    // 打开输出文件
    return openOutputFile();
}

/**
 * 打开输出文件
 * @return
 */
int NdkMediaWriter::openOutputFile() {
    int ret;
    av_register_all();

    if (mMediaMuxer != nullptr) {
        mMediaMuxer.reset();
    }
    mMediaMuxer = std::make_shared<NdkMediaCodecMuxer>();
    mMediaMuxer->setOutputPath(mDstUrl);

    // 打开音频编码器
    if (mHasAudio && (ret = openEncoder(AVMEDIA_TYPE_AUDIO)) < 0) {
        LOGE("MediaCodecWriter - failed to Open audio encoder context: %s", av_err2str(ret));
        return ret;
    }

    // 打开视频编码器
    if (mHasVideo && (ret = openEncoder(AVMEDIA_TYPE_VIDEO)) < 0) {
        LOGE("MediaCodecWriter - failed to Open video encoder context: %s", av_err2str(ret));
        return ret;
    }

    // 如果存在视频流，则创建视频缓冲帧对象
    if (mHasVideo) {
        mImageFrame = av_frame_alloc();
        if (!mImageFrame) {
            LOGE("MediaCodecWriter - failed to allocate video frame");
            return -1;
        }
        mImageFrame->format = mPixelFormat;
        mImageFrame->width = mWidth;
        mImageFrame->height = mHeight;
        mImageFrame->pts = 0;

        int size = av_image_get_buffer_size(mPixelFormat, mWidth, mHeight, 1);
        if (size < 0) {
            LOGE("MediaCodecWriter - failed to get image buffer size: %s", av_err2str(size));
            return -1;
        }
        mImageBuffer = (uint8_t *) av_malloc((size_t) size);
        if (!mImageBuffer) {
            LOGE("MediaCodecWriter - failed to allocate image buffer");
            return -1;
        }
    }

    // if has audio, create audio resampler
    if (mHasAudio) {
        if (mResampler != nullptr) {
            mResampler.reset();
        }
        mResampler = std::make_shared<Resampler>();
        mResampler->setInput(mSampleRate, mChannels, mSampleFormat);
        mResampler->setOutput(mSampleRate, av_get_default_channel_layout(mChannels),
                              AV_SAMPLE_FMT_S16, mChannels, getBufferSize());
        ret = mResampler->init();
        if (ret < 0) {
            LOGE("MediaCodecWriter - failed to init audio convertor.");
            return ret;
        }
    }

    // if has audio or video
    mMediaMuxer->setHasAudio(mHasAudio);
    mMediaMuxer->setHasVideo(mHasVideo);

    // open media muxer
    ret = mMediaMuxer->openMuxer();
    if (ret < 0) {
        LOGE("MediaCodecWriter - failed to open media muxer");
        return ret;
    }
    return ret;
}

/**
 * 打开编码器
 * @param mediaType
 * @return
 */
int NdkMediaWriter::openEncoder(AVMediaType mediaType) {
    // 仅支持音频和视频编码器创建
    if (mediaType != AVMEDIA_TYPE_AUDIO && mediaType != AVMEDIA_TYPE_VIDEO) {
        return -1;
    }

    int ret;

    if (mediaType == AVMEDIA_TYPE_VIDEO) {
        // 创建编码器对象
        mVideoEncoder = std::make_shared<NdkVideoEncoder>(mMediaMuxer);
        // 设置视频参数
        mVideoEncoder->setVideoParams(mWidth, mHeight, mMaxBitRate, mFrameRate);
        // 打开编码器
        ret = mVideoEncoder->openEncoder();
        if (ret < 0) {
            LOGE("MediaCodecWriter - failed to open video encoder");
            return ret;
        }
        return 0;
    } else {
        // 创建编码器对象
        mAudioEncoder = std::make_shared<NdkAudioEncoder>(mMediaMuxer);
        mAudioEncoder->setAudioParams(mAudioBitRate, mSampleRate, mChannels);
        ret = mAudioEncoder->openEncoder();
        if (ret < 0) {
            LOGE("MediaCodecWriter - failed to open audio encoder");
            return ret;
        }
        return 0;
    }
}

/**
 * 编码并写入媒体数据
 * @param mediaData
 * @return
 */
int NdkMediaWriter::encodeMediaData(AVMediaData *mediaData) {
    return encodeMediaData(mediaData, nullptr);
}

/**
 * 编码并写入媒体数据
 * @param mediaData
 * @param gotFrame
 * @return
 */
int NdkMediaWriter::encodeMediaData(AVMediaData *mediaData, int *gotFrame) {
    int gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;
    bool isVideo = (mediaData->type == MediaVideo);
    const char *type = isVideo ? "video" : "audio";
    // 判断是否支持编码
    if ((isVideo && !mHasVideo) || (!isVideo && !mHasAudio)) {
        LOGE("MediaCodecWriter - no support current type: %s", type);
        return 0;
    }
    if (isVideo) {
        return mVideoEncoder->encodeMediaData(mediaData, gotFrame);
    } else {
        return mAudioEncoder->encodeMediaData(mediaData, gotFrame);
    }
}

/**
 * 编码一帧数据
 * @param frame
 * @param type
 * @return
 */
int NdkMediaWriter::encodeFrame(AVFrame *frame, AVMediaType type) {
    return encodeFrame(frame, type, nullptr);
}

/**
 * 编码一阵数据
 * @param frame
 * @param type
 * @param gotFrame
 * @return
 */
int NdkMediaWriter::encodeFrame(AVFrame *frame, AVMediaType type, int *gotFrame) {
    auto mediaData = new AVMediaData();
    if (type == AVMEDIA_TYPE_VIDEO) {
        auto yuvData = convertToYuvData(frame);
        fillVideoData(mediaData, yuvData, yuvData->width, yuvData->height);
        delete yuvData;
    } else {
        // todo 填充音频帧数据
    }
    int ret = encodeMediaData(mediaData, gotFrame);
    mediaData->free();
    delete mediaData;
    return ret;
}

int NdkMediaWriter::stop() {
    int ret = 0, gotFrame;
    LOGI("MediaCodecWriter - flushing video encoder");
    AVMediaData *data = new AVMediaData();
    if (mHasVideo) {
        data->type = MediaVideo;
        while (true) {
            ret = encodeMediaData(data, &gotFrame);
            if (ret < 0 || !gotFrame) {
                break;
            }
        }
    }

    if (mHasAudio) {
        LOGI("MediaCodecWriter - flushing audio encoder");
        data->type = MediaAudio;
        while (true) {
            ret = encodeMediaData(data, &gotFrame);
            if (ret < 0 || !gotFrame) {
                break;
            }
        }
    }

    // 删除对象
    delete data;

    return 0;
}

void NdkMediaWriter::release() {
    if (mImageFrame != nullptr) {
        av_frame_free(&mImageFrame);
        mImageFrame = nullptr;
    }
    if (mImageBuffer != nullptr) {
        av_free(mImageBuffer);
        mImageBuffer = nullptr;
    }
    if (mAudioEncoder != nullptr) {
        mAudioEncoder->closeEncoder();
        mAudioEncoder.reset();
        mAudioEncoder = nullptr;
    }
    if (mVideoEncoder != nullptr) {
        mVideoEncoder->closeEncoder();
        mVideoEncoder.reset();
        mVideoEncoder = nullptr;
    }
    if (mMediaMuxer != nullptr) {
        mMediaMuxer->closeMuxer();
        mMediaMuxer.reset();
        mMediaMuxer = nullptr;
    }
    if (mResampler != nullptr) {
        mResampler->release();
        mResampler.reset();
        mResampler = nullptr;
    }
}

void NdkMediaWriter::reset() {
    mDstUrl = nullptr;
    mWidth = 0;
    mHeight = 0;
    mFrameRate = 0;
    mPixelFormat = AV_PIX_FMT_NONE;
    mUseTimeStamp = false;
    mHasVideo = false;

    mSampleRate = 0;
    mChannels = 0;
    mAudioBitRate = AUDIO_BIT_RATE;
    mSampleFormat = AV_SAMPLE_FMT_NONE;
    mHasAudio = false;

    mImageFrame = nullptr;
    mImageBuffer = nullptr;
    mImageCount = 0;
    mStartPts = 0;
    mLastPts = -1;
}

int NdkMediaWriter::getBufferSize() {
    return 1024 * mChannels * (mSampleFormat == AV_SAMPLE_FMT_S16 ? 2 : 1);
}