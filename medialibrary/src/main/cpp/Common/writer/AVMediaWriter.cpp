//
// Created by CainHuang on 2019/8/11.
//

#include <sstream>
#include "AVMediaWriter.h"

AVMediaWriter::AVMediaWriter() {
    reset();
}

AVMediaWriter::~AVMediaWriter() {
    release();
}

/**
 * 设置输出文件路径
 * @param dstUrl
 */
void AVMediaWriter::setOutputPath(const char *dstUrl) {
    mDstUrl = av_strdup(dstUrl);
}

/**
 * 设置是否使用时间戳计算pts
 * @param use
 */
void AVMediaWriter::setUseTimeStamp(bool use) {
    mUseTimeStamp = use;
}

/**
 * 添加编码参数
 * @param key
 * @param value
 */
void AVMediaWriter::addEncodeOptions(std::string key, std::string value) {
    mEncodeOptions[key] = value;
}

/**
 * 指定音频编码器名称
 * @param encoder
 */
void AVMediaWriter::setAudioEncoderName(const char *encoder) {
    mAudioEncodeName = av_strdup(encoder);
}

/**
 * 指定视频编码器名称
 * @param encoder
 */
void AVMediaWriter::setVideoEncoderName(const char *encoder) {
    mVideoEncodeName = av_strdup(encoder);
}

/**
 * 设置最大比特率
 * @param maxBitRate
 */
void AVMediaWriter::setMaxBitRate(int maxBitRate) {
    mMaxBitRate = maxBitRate;
}

/**
 * 设置质量系数
 * @param quality
 */
void AVMediaWriter::setQuality(int quality) {
    std::stringstream ss;
    ss << quality;
    std::string str;
    ss >> str;
    mEncodeOptions["crf"] = str;
}

/**
 * 设置视频输出参数
 */
void AVMediaWriter::setOutputVideo(int width, int height, int frameRate,
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

/**
 * 设置音频输出参数
 * @param sampleRate
 * @param channels
 * @param sampleFormat
 */
void AVMediaWriter::setOutputAudio(int sampleRate, int channels, AVSampleFormat sampleFormat) {
    mSampleRate = sampleRate;
    mChannels = channels;
    mSampleFormat = sampleFormat;
    if (mSampleRate > 0 || mChannels > 0 && mSampleFormat != AV_SAMPLE_FMT_NONE) {
        mHasAudio = true;
    } else {
        mHasAudio = false;
    }
}

/**
 * 准备编码器
 * @return
 */
int AVMediaWriter::prepare() {

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
int AVMediaWriter::openOutputFile() {
    int ret;
    av_register_all();

    if (mMediaMuxer != nullptr) {
        mMediaMuxer.reset();
    }
    mMediaMuxer = std::make_shared<AVMediaMuxer>();
    mMediaMuxer->setOutputPath(mDstUrl);
    ret = mMediaMuxer->init();
    if (ret < 0) {
        LOGI("failed to init media muxer");
        return AVERROR_UNKNOWN;
    }

    // 打开视频编码器
    if (mHasVideo && (ret = openEncoder(AVMEDIA_TYPE_VIDEO)) < 0) {
        LOGE("failed to Open video encoder context: %s", av_err2str(ret));
        return ret;
    }

    // 打开音频编码器
    if (mHasAudio && (ret = openEncoder(AVMEDIA_TYPE_AUDIO)) < 0) {
        LOGE("Failed to Open audio encoder context: %s", av_err2str(ret));
        return ret;
    }

    // 如果存在视频流，则创建视频缓冲帧对象
    if (mHasVideo) {
        mImageFrame = av_frame_alloc();
        if (!mImageFrame) {
            LOGE("Failed to allocate video frame");
            return -1;
        }
        mImageFrame->format = mPixelFormat;
        mImageFrame->width = mWidth;
        mImageFrame->height = mHeight;
        mImageFrame->pts = 0;

        int size = av_image_get_buffer_size(mPixelFormat, mWidth, mHeight, 1);
        if (size < 0) {
            LOGE("Failed to get image buffer size: %s", av_err2str(size));
            return -1;
        }
        mImageBuffer = (uint8_t *) av_malloc((size_t) size);
        if (!mImageBuffer) {
            LOGE("Failed to allocate image buffer");
            return -1;
        }
    }

    // if has audio, create audio resampler
    if (mHasAudio) {
        if (mResampler != nullptr) {
            mResampler.reset();
        }
        AVCodecContext *pAudioCodecCtx = mAudioEncoder->getContext();
        mResampler = std::make_shared<Resampler>();
        mResampler->setInput(mSampleRate, mChannels, mSampleFormat);
        mResampler->setOutput(pAudioCodecCtx->sample_rate, pAudioCodecCtx->channel_layout,
                pAudioCodecCtx->sample_fmt, pAudioCodecCtx->channels, pAudioCodecCtx->frame_size);
        ret = mResampler->init();
        if (ret < 0) {
            LOGE("Failed to init audio convertor.");
            return ret;
        }
    }

    // print muxer info
    mMediaMuxer->printInfo();

    // open media muxer
    ret = mMediaMuxer->openMuxer();
    if (ret < 0) {
        LOGE("Failed to open media muxer");
        return ret;
    }

    // write file global header
    ret = mMediaMuxer->writeHeader();
    if (ret < 0) {
        LOGE("Failed to write header");
        return ret;
    }
    return ret;
}

/**
 * 打开编码器
 * @param mediaType
 * @return
 */
int AVMediaWriter::openEncoder(AVMediaType mediaType) {

    // 仅支持音频和视频编码器创建
    if (mediaType != AVMEDIA_TYPE_AUDIO && mediaType != AVMEDIA_TYPE_VIDEO) {
        return -1;
    }

    int ret;

    if (mediaType == AVMEDIA_TYPE_VIDEO) {
        // 创建编码器对象
        mVideoEncoder = std::make_shared<AVVideoEncoder>(mMediaMuxer);
        // 设置编码器名称
        mVideoEncoder->setEncoder(mVideoEncodeName);
        // 创建编码器
        ret = mVideoEncoder->createEncoder();
        if (ret < 0) {
            LOGE("Failed to create video encoder");
            return ret;
        }
        // 设置视频参数
        mVideoEncoder->setVideoParams(mWidth, mHeight, mPixelFormat, mFrameRate,
                mMaxBitRate, mUseTimeStamp, mVideoMetadata);
        // 打开编码器
        ret = mVideoEncoder->openEncoder(mEncodeOptions);
        if (ret < 0) {
            LOGE("Failed to open video encoder");
            return ret;
        }
        return 0;
    } else {
        // 创建编码器对象
        mAudioEncoder = std::make_shared<AVAudioEncoder>(mMediaMuxer);
        mAudioEncoder->setEncoder(mAudioEncodeName);
        ret = mAudioEncoder->createEncoder();
        if (ret < 0) {
            LOGE("Failed to create audio encoder");
            return ret;
        }
        mAudioEncoder->setAudioParams(mAudioBitRate, mSampleRate, mChannels);
        ret = mAudioEncoder->openEncoder(mEncodeOptions);
        if (ret < 0) {
            LOGE("Failed to open audio encoder");
            return ret;
        }
        return 0;
    }
}

/**
 * 编码媒体数据
 * @param mediaData
 * @return
 */
int AVMediaWriter::encodeMediaData(AVMediaData *mediaData) {
    return encodeMediaData(mediaData, nullptr);
}

/**
 * 编码媒体数据
 * @param mediaData
 * @param gotFrame
 * @return
 */
int AVMediaWriter::encodeMediaData(AVMediaData *mediaData, int *gotFrame) {
    int ret = 0, gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;

    bool isVideo = (mediaData->type == MediaVideo);
    AVFrame *frame = isVideo ? mImageFrame : mResampler->getConvertedFrame();
    uint8_t *data = isVideo ? mediaData->image : mediaData->sample;
    const char *type = isVideo ? "video" : "audio";

    // 判断是否支持编码
    if ((isVideo && !mHasVideo) || (!isVideo && !mHasAudio)) {
        LOGE("no support current type: %s", type);
        return 0;
    }

    // 填充数据
    if (data != nullptr) {
        ret = isVideo ? fillImage(mediaData) : fillSample(mediaData);
        if (ret < 0) {
            return -1;
        }
    }

    if (isVideo) {
        return mVideoEncoder->encodeFrame(frame, gotFrame);
    } else {
        return mAudioEncoder->encodeFrame(frame, gotFrame);
    }
}

/**
 * 编码一帧数据
 * @param frame
 * @param type
 * @return
 */
int AVMediaWriter::encodeFrame(AVFrame *frame, AVMediaType type) {
    return encodeFrame(frame, type, nullptr);
}

/**
 * 编码一帧数据
 * @param frame
 * @param type
 * @param gotFrame
 * @return
 */
int AVMediaWriter::encodeFrame(AVFrame *frame, AVMediaType type, int *gotFrame) {
    // 仅支持音频流和视频流
    if (type != AVMEDIA_TYPE_AUDIO && type != AVMEDIA_TYPE_VIDEO) {
        return -1;
    }
    int gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;
    bool isVideo = (type == AVMEDIA_TYPE_VIDEO);

    // 判断是否支持编码
    if ((isVideo && !mHasVideo) || (!isVideo && !mHasAudio)) {
        LOGE("no support current type: %s", type);
        return 0;
    }

    if (isVideo) {
        return mVideoEncoder->encodeFrame(frame, gotFrame);
    } else {
        return mAudioEncoder->encodeFrame(frame, gotFrame);
    }
}

/**
 * 将数据填充到视频缓冲帧中
 * @param data
 * @return
 */
int AVMediaWriter::fillImage(AVMediaData *data) {
    int ret;
    ret = av_image_fill_arrays(mImageFrame->data, mImageFrame->linesize, data->image,
                               getPixelFormat((PixelFormat)data->pixelFormat),
                               data->width, data->height, 1);
    if (ret < 0) {
        LOGE("av_image_fill_arrays error: %s, [%d, %d, %s], [%d, %d], [%d, %d, %s]",
             av_err2str(ret), mImageFrame->width, mImageFrame->height,
             av_get_pix_fmt_name((AVPixelFormat) mImageFrame->format), mWidth, mHeight,
             data->width, data->height, av_get_pix_fmt_name(getPixelFormat((PixelFormat)data->pixelFormat)));
        return -1;
    }
    if (!mUseTimeStamp) {
        mImageFrame->pts = mImageCount++;
    } else {
        if (mStartPts == 0) {
            mImageFrame->pts = 0;
            mStartPts = data->pts;
        } else {
            mImageFrame->pts = data->pts - mStartPts;
        }
        if (mImageFrame->pts == mLastPts) {
            mImageFrame->pts += 10;
        }
        mLastPts = mImageFrame->pts;
    }

    return 0;
}

/**
 * 将数据填充到音频缓冲帧中
 * @param data
 * @return
 */
int AVMediaWriter::fillSample(AVMediaData *data) {
    if (mResampler != nullptr) {
        int ret = mResampler->resample(data->sample, mAudioEncoder->getContext()->frame_size);
        if (ret < 0) {
            LOGE("resample error!");
        }
    }
    return 0;
}

/**
 * 停止编码
 * @return
 */
int AVMediaWriter::stop() {
    int ret = 0, gotFrame;
    LOGI("Flushing video encoder");
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
        LOGI("Flushing audio encoder");
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

    // 写入文件尾
    if (mMediaMuxer != nullptr) {
        mMediaMuxer->writeTrailer();
    }

    return 0;
}

/**
 * 重置所有参数
 */
void AVMediaWriter::reset() {

    mDstUrl = nullptr;
    mWidth = 0;
    mHeight = 0;
    mFrameRate = 0;
    mPixelFormat = AV_PIX_FMT_NONE;
    mVideoEncodeName = nullptr;
    mUseTimeStamp = false;
    mHasVideo = false;

    mSampleRate = 0;
    mChannels = 0;
    mAudioBitRate = AUDIO_BIT_RATE;
    mSampleFormat = AV_SAMPLE_FMT_NONE;
    mAudioEncodeName = nullptr;
    mHasAudio = false;

    mImageFrame = nullptr;
    mImageBuffer = nullptr;
    mImageCount = 0;
    mStartPts = 0;
    mLastPts = -1;
}

/**
 * 释放资源
 */
void AVMediaWriter::release() {
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

