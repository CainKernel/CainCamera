//
// Created by CainHuang on 2019/8/11.
//

#include <sstream>
#include "AVMediaWriter.h"
#include "../AVFormatter.h"

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
    mVideoCodecID = AV_CODEC_ID_H264;
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
    mAudioCodecID = AV_CODEC_ID_AAC;
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

    // 去掉奇数告诉
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

    ret = avformat_alloc_output_context2(&pFormatCtx, nullptr, nullptr, mDstUrl);
    if (!pFormatCtx || ret < 0) {
        LOGI("failed to call avformat_alloc_output_context2: %s", av_err2str(ret));
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

    // 如果存在音频流，则创建音频缓冲帧对象
    if (mHasAudio) {
        mSampleFrame = av_frame_alloc();
        if (!mSampleFrame) {
            LOGE("Failed to allocate audio frame");
            return -1;
        }
        mSampleFrame->format = pAudioCodecCtx->sample_fmt;
        mSampleFrame->nb_samples = pAudioCodecCtx->frame_size;
        mSampleFrame->channel_layout = pAudioCodecCtx->channel_layout;
        mSampleFrame->pts = 0;

        // 是否多声道
        mSamplePlanes = av_sample_fmt_is_planar(pAudioCodecCtx->sample_fmt) ? pAudioCodecCtx->channels : 1;

        // 缓冲区大小
        mSampleSize = av_samples_get_buffer_size(nullptr, pAudioCodecCtx->channels,
                                                 pAudioCodecCtx->frame_size,
                                                 pAudioCodecCtx->sample_fmt, 1) / mSamplePlanes;

        // 初始采样缓冲区大小
        mSampleBuffer = new uint8_t *[mSamplePlanes];
        for (int i = 0; i < mSamplePlanes; i++) {
            mSampleBuffer[i] = (uint8_t *) av_malloc((size_t) mSampleSize);
            if (mSampleBuffer[i] == nullptr) {
                LOGE("Failed to allocate sample buffer");
                return -1;
            }
        }

        // 创建音频重采样上下文
        pSampleConvertCtx = swr_alloc_set_opts(pSampleConvertCtx,
                                               pAudioCodecCtx->channel_layout,
                                               pAudioCodecCtx->sample_fmt,
                                               pAudioCodecCtx->sample_rate,
                                               av_get_default_channel_layout(mChannels),
                                               mSampleFormat, mSampleRate, 0, nullptr);
        if (!pSampleConvertCtx) {
            LOGE("Failed to allocate SwrContext");
        } else if (swr_init(pSampleConvertCtx) < 0) {
            LOGE("Failed to call swr_init");
        }
    }

    // 打印信息
    av_dump_format(pFormatCtx, 0, mDstUrl, 1);

    // 打开输出文件
    if (!(pFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if ((ret = avio_open(&pFormatCtx->pb, mDstUrl, AVIO_FLAG_WRITE)) < 0) {
            LOGE("Failed to open output file '%s'", mDstUrl);
            return ret;
        }
    }

    // 写入文件头部信息
    ret = avformat_write_header(pFormatCtx, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avformat_write_header: %s", av_err2str(ret));
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
    AVCodecContext *codecCtx = nullptr;
    AVStream *stream = nullptr;
    AVCodec *encoder = nullptr;
    const char *encodeName = nullptr;

    // 根据指定编码器名称查找 编码器
    if (mediaType == AVMEDIA_TYPE_AUDIO) {
        encodeName = mAudioEncodeName;
    } else {
        encodeName = mVideoEncodeName;
    }
    if (encodeName != nullptr) {
        encoder = avcodec_find_encoder_by_name(encodeName);
    }

    // 如果编码器不存在，则根据编码器id查找
    if (encoder == nullptr) {
        if (encodeName != nullptr) {
            LOGE("Failed to find encoder by name: %s", encodeName);
        }
        if (mediaType == AVMEDIA_TYPE_AUDIO) {
            encoder = avcodec_find_encoder(mAudioCodecID);
        } else {
            encoder = avcodec_find_encoder(mVideoCodecID);
        }
    }

    // 经过前面的处理，如果编码器还不存在，则找不到编码器，直接退出
    if (!encoder) {
        LOGE("Failed to find encoder");
        return AVERROR_INVALIDDATA;
    }

    // 创建编码上下文
    codecCtx = avcodec_alloc_context3(encoder);
    if (!codecCtx) {
        LOGE("Failed to allocate the encoder context");
        return AVERROR(ENOMEM);
    }

    // 创建媒体流
    stream = avformat_new_stream(pFormatCtx, encoder);
    if (!stream) {
        LOGE("Failed to allocate stream.");
        return -1;
    }

    // 处理参数
    if (mediaType == AVMEDIA_TYPE_VIDEO) {
        codecCtx->width = mWidth;
        codecCtx->height = mHeight;
        codecCtx->pix_fmt = mPixelFormat;
        codecCtx->gop_size = mFrameRate;

        // 设置是否使用时间戳作为pts，两种的time_base不一样
        if (mUseTimeStamp) {
            codecCtx->time_base = (AVRational) {1, 1000};
        } else {
            codecCtx->time_base = (AVRational) {1, mFrameRate};
        }

        // 设置最大比特率
        if (mMaxBitRate > 0) {
            codecCtx->rc_max_rate = mMaxBitRate;
            codecCtx->rc_buffer_size = (int) mMaxBitRate;
        }

        // 设置媒体流meta参数
        auto it = mVideoMetadata.begin();
        for (; it != mVideoMetadata.end(); it++) {
            av_dict_set(&stream->metadata, (*it).first.c_str(), (*it).second.c_str(), 0);
        }

    } else {
        codecCtx->sample_rate = mSampleRate;
        codecCtx->channels = mChannels;
        codecCtx->channel_layout = (uint64_t) av_get_default_channel_layout(mChannels);
        codecCtx->sample_fmt = encoder->sample_fmts[0];
        codecCtx->time_base = AVRational{1, codecCtx->sample_rate};
    }

    // 设置时钟基准
    stream->time_base = codecCtx->time_base;

    // 设置编码器的全局信息
    if (pFormatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
        codecCtx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    // 获取自定义的编码参数
    AVDictionary *options = nullptr;
    auto it = mEncodeOptions.begin();
    for (; it != mEncodeOptions.end(); it++) {
        av_dict_set(&options, (*it).first.c_str(), (*it).second.c_str(), 0);
    }

    // 打开编码器
    ret = avcodec_open2(codecCtx, encoder, &options);
    if (ret < 0) {
        LOGE("Could not open %s codec: %s", mediaType == AVMEDIA_TYPE_VIDEO ? "video" : "audio",
             av_err2str(ret));
        av_dict_free(&options);
        return ret;
    }
    av_dict_free(&options);

    // 将编码器参数复制到媒体流中
    ret = avcodec_parameters_from_context(stream->codecpar, codecCtx);
    if (ret < 0) {
        LOGE("Failed to copy encoder parameters to video stream");
        return ret;
    }

    // 绑定编码器和媒体流
    if (mediaType == AVMEDIA_TYPE_VIDEO) {
        pVideoCodecCtx = codecCtx;
        pVideoStream = stream;
    } else {
        pAudioCodecCtx = codecCtx;
        pAudioStream = stream;
    }

    return ret;
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
    AVCodecContext *codecCtx = isVideo ? pVideoCodecCtx : pAudioCodecCtx;
    AVStream *stream = isVideo ? pVideoStream : pAudioStream;
    AVFrame *frame = isVideo ? mImageFrame : mSampleFrame;
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

    // 初始化数据包
    AVPacket packet;
    packet.data = nullptr;
    packet.size = 0;
    av_init_packet(&packet);

    // 送去编码
    ret = avcodec_send_frame(codecCtx, data == nullptr ? nullptr : frame);
    if (ret < 0) {
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return 0;
        }
        LOGE("Failed to call avcodec_send_frame: %s", av_err2str(ret));
        return ret;
    }

    while (ret >= 0) {
        // 取出解码后的数据包
        ret = avcodec_receive_packet(codecCtx, &packet);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            break;
        } else if (ret < 0) {
            LOGE("Failed to call avcodec_receive_packet: %s, type: %s", av_err2str(ret), type);
            return ret;
        }

        // 计算输出的pts
        av_packet_rescale_ts(&packet, codecCtx->time_base, stream->time_base);
        packet.stream_index = stream->index;

        // 写入文件
        ret = av_interleaved_write_frame(pFormatCtx, &packet);
        if (ret < 0) {
            LOGE("Failed to call av_interleaved_write_frame: %s, type: %s", av_err2str(ret), type);
            return ret;
        }
        *gotFrame = 1;
    }

    return 0;
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
    int ret, gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;
    bool isVideo = (type == AVMEDIA_TYPE_VIDEO);
    AVCodecContext *codecCtx = isVideo ? pVideoCodecCtx : pAudioCodecCtx;
    AVStream *stream = isVideo ? pVideoStream : pAudioStream;

    // 判断是否支持编码
    if ((isVideo && !mHasVideo) || (!isVideo && !mHasAudio)) {
        LOGE("no support current type: %s", type);
        return 0;
    }

    // 初始化数据包
    AVPacket packet;
    packet.data = nullptr;
    packet.size = 0;
    av_init_packet(&packet);

    // 送去编码
    ret = avcodec_send_frame(codecCtx, frame);
    if (ret < 0) {
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return 0;
        }
        LOGE("Failed to call avcodec_send_frame: %s", av_err2str(ret));
        return ret;
    }

    while (ret >= 0) {
        // 取出解码后的数据包
        ret = avcodec_receive_packet(codecCtx, &packet);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            break;
        } else if (ret < 0) {
            LOGE("Failed to call avcodec_receive_packet: %s, type: %s", av_err2str(ret), type);
            return ret;
        }

        // 计算输出的pts
        av_packet_rescale_ts(&packet, codecCtx->time_base, stream->time_base);
        packet.stream_index = stream->index;

        // 写入文件
        ret = av_interleaved_write_frame(pFormatCtx, &packet);
        if (ret < 0) {
            LOGE("Failed to call av_interleaved_write_frame: %s, type: %s", av_err2str(ret), type);
            return ret;
        }
        *gotFrame = 1;
    }

    return 0;
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
    int ret;
    if (pAudioCodecCtx->channels != mChannels || pAudioCodecCtx->sample_fmt != mSampleFormat ||
        pAudioCodecCtx->sample_rate != mSampleRate) {
        ret = swr_convert(pSampleConvertCtx, mSampleBuffer, pAudioCodecCtx->frame_size,
                          (const uint8_t **) &data->sample, pAudioCodecCtx->frame_size);
        if (ret <= 0) {
            LOGE("swr_convert error: %s", av_err2str(ret));
            return -1;
        }
        // 将数据复制到采样帧中
        avcodec_fill_audio_frame(mSampleFrame, mChannels, pAudioCodecCtx->sample_fmt, mSampleBuffer[0],
                                 mSampleSize, 0);
        for (int i = 0; i < mSamplePlanes; i++) {
            mSampleFrame->data[i] = mSampleBuffer[i];
            mSampleFrame->linesize[i] = mSampleSize;
        }
    } else {
        // 直接将数据复制到采样帧中
        ret = av_samples_fill_arrays(mSampleFrame->data, mSampleFrame->linesize, data->sample,
                                     pAudioCodecCtx->channels, mSampleFrame->nb_samples,
                                     pAudioCodecCtx->sample_fmt, 1);
    }
    if (ret < 0) {
        LOGE("Failed to call av_samples_fill_arrays: %s", av_err2str(ret));
        return -1;
    }
    mSampleFrame->pts = mNbSamples;
    mNbSamples += mSampleFrame->nb_samples;

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
        while (1) {
            ret = encodeMediaData(data, &gotFrame);
            if (ret < 0 || !gotFrame) {
                break;
            }
        }
    }

    // 删除对象
    delete data;

    // 写入文件尾
    av_write_trailer(pFormatCtx);

    return ret;
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
    mVideoCodecID = AV_CODEC_ID_NONE;
    mVideoEncodeName = nullptr;
    mUseTimeStamp = false;
    mHasVideo = false;

    mSampleRate = 0;
    mChannels = 0;
    mSampleFormat = AV_SAMPLE_FMT_NONE;
    mAudioEncodeName = nullptr;
    mAudioCodecID = AV_CODEC_ID_NONE;
    mHasAudio = false;

    pFormatCtx = nullptr;
    pVideoCodecCtx = nullptr;
    pAudioCodecCtx = nullptr;
    pVideoStream = nullptr;
    pAudioStream = nullptr;

    mNbSamples = 0;
    mSampleFrame = nullptr;
    mSampleBuffer = nullptr;
    mSampleSize = 0;
    mSamplePlanes = 0;
    pSampleConvertCtx = nullptr;

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
    if (mSampleFrame != nullptr) {
        av_frame_free(&mSampleFrame);
        mSampleFrame = nullptr;
    }
    if (mSampleBuffer != nullptr) {
        for (int i = 0; i < mSamplePlanes; i++) {
            if (mSampleBuffer[i] != nullptr) {
                av_free(mSampleBuffer[i]);
                mSampleBuffer[i] = nullptr;
            }
        }
        delete[] mSampleBuffer;
        mSampleBuffer = nullptr;
    }
    if (pVideoCodecCtx != nullptr) {
        avcodec_free_context(&pVideoCodecCtx);
        pVideoCodecCtx = nullptr;
    }
    if (pAudioCodecCtx != nullptr) {
        avcodec_free_context(&pAudioCodecCtx);
        pAudioCodecCtx = nullptr;
    }
    if (pFormatCtx && !(pFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_closep(&pFormatCtx->pb);
        avformat_close_input(&pFormatCtx);
        pFormatCtx = nullptr;
    }
    if (pSampleConvertCtx != nullptr) {
        swr_free(&pSampleConvertCtx);
        pSampleConvertCtx = nullptr;
    }
    if (pVideoStream != nullptr && pVideoStream->metadata != nullptr) {
        av_dict_free(&pVideoStream->metadata);
        pVideoStream->metadata = nullptr;
    }
    pVideoStream = nullptr;
    pAudioStream = nullptr;
}

