//
// Created by Administrator on 2018/1/4.
//

#include "cain_recorder.h"

/**
 * 构造方法
 * @param params
 */
CainRecorder::CainRecorder(EncoderParams * params) : params(params) {

}

//Output FFmpeg's av_log()
void custom_log(void *ptr, int level, const char *fmt, va_list vl) {
    FILE *fp = fopen("/storage/emulated/0/av_log.txt", "a+");
    if (fp) {
        vfprintf(fp, fmt, vl);
        fflush(fp);
        fclose(fp);
    }
}

/**
 * 初始化录制器
 * @return
 */
int CainRecorder::initRecorder() {
    if (!params) {
        LOGE("error! EncoderParams is empty!");
        return -1;
    }

    // ------------------------------ 复用器初始化部分 ----------------------------------------------
    av_log_set_callback(custom_log);
    // 初始化
    av_register_all();

    // 创建复用上下文
    avformat_alloc_output_context2(&pFormatCtx, NULL, NULL, params->mediaPath);
    pOutputFormat = pFormatCtx->oformat;

    // ----------------------------- 视频编码器初始化部分 -------------------------------------------
    // 设置视频编码器的ID
    pOutputFormat->video_codec = AV_CODEC_ID_H264;

    // 打开输出文件
    int ret = avio_open(&pFormatCtx->pb, params->mediaPath, AVIO_FLAG_READ_WRITE);
    if (ret < 0) {
        LOGE("avio_open error() error %d : Could not open %s", ret, params->mediaPath);
        return -1;
    }

    // 创建视频码流
    videoStream = avformat_new_stream(pFormatCtx, 0);
    if (videoStream == NULL) {
        LOGE("avformat_new_stream() error: Could not allocate video stream!\n");
        return -1;
    }

    // 获取编码上下文，并设置相关参数
    videoCodecContext = videoStream->codec;
    videoCodecContext->codec_id = AV_CODEC_ID_H264;
    videoCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    videoCodecContext->pix_fmt = AV_PIX_FMT_YUV420P;
    videoCodecContext->width = params->previewWidth;
    videoCodecContext->height = params->previewHeight;

    videoCodecContext->bit_rate = params->bitRate;
    videoCodecContext->gop_size = 50;
    videoCodecContext->thread_count = 12;

    videoCodecContext->time_base.num = 1;
    videoCodecContext->time_base.den = params->frameRate;
    videoCodecContext->qmin = 10;
    videoCodecContext->qmax = 51;

    videoCodecContext->max_b_frames = 3;

    // Set Option
    AVDictionary *param = 0;
    // H.264
    if (videoCodecContext->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "tune", "zerolatency", 0);
        av_opt_set(videoCodecContext->priv_data, "preset", "ultrafast", 0);
        av_dict_set(&param, "profile", "baseline", 0);
    }

    // 查找编码器
    videoCodec = avcodec_find_encoder(videoCodecContext->codec_id);
    if (!videoCodec) {
        LOGE("can not find encoder !\n");
        return -1;
    }

    // 判断视频码流是否存在，打开编码器
    ret = avcodec_open2(videoCodecContext, videoCodec, NULL);
    if (ret < 0) {
        LOGE("avcodec_open2() error %d: Could not open video codec.", ret);
        return -1;
    }

    videoFrame = av_frame_alloc();
    int picture_size = avpicture_get_size(videoCodecContext->pix_fmt,
                                          videoCodecContext->width, videoCodecContext->height);
    LOGI("picture_size: %d", picture_size);
    uint8_t *buf = (uint8_t *) av_malloc(picture_size);
    avpicture_fill((AVPicture *) videoFrame, buf, videoCodecContext->pix_fmt, videoCodecContext->width,
                   videoCodecContext->height);

    // ------------------------------ 音频编码初始化部分 --------------------------------------------
    // 设置音频编码格式
    pOutputFormat->audio_codec = AV_CODEC_ID_AAC;

    // 创建音频编码器
    audioCodec = avcodec_find_encoder(pOutputFormat->audio_codec);
    if (!audioCodec) {
        LOGE("avcodec_find_encoder() error: Audio codec not found.");
        return -1;
    }

    // 创建音频码流
    audioStream = avformat_new_stream(pFormatCtx, audioCodec);
    if (!audioStream) {
        LOGE("avformat_new_stream() error: Could not allocate audio stream.");
        return -1;
    }

    // 获取音频编码上下文
    audioCodecContext = audioStream->codec;
    audioCodecContext->codec_id = pOutputFormat->audio_codec;
    audioCodecContext->codec_type = AVMEDIA_TYPE_AUDIO;
    audioCodecContext->bit_rate = params->audioBitRate;
    audioCodecContext->sample_rate = params->audioSampleRate;
    audioCodecContext->channel_layout = AV_CH_LAYOUT_MONO;
    audioCodecContext->channels =
            av_get_channel_layout_nb_channels(audioCodecContext->channel_layout);
    audioCodecContext->sample_fmt = AV_SAMPLE_FMT_S16;
    audioCodecContext->bits_per_raw_sample = 16;
    // 设置码率
    audioCodecContext->time_base.num = 1;
    audioCodecContext->time_base.den = params->audioSampleRate;

    // 设置码流的码率
    audioStream->time_base.num = 1;
    audioStream->time_base.den = params->audioSampleRate;

    // 创建音频帧
    audioFrame = av_frame_alloc();
    if (!audioFrame) {
        LOGE("av_frame_alloc() error: Could not allocate audio frame.");
        return -1;
    }
    audioFrame->pts = 0;

    // 创建缓冲
    sampleSize = av_samples_get_buffer_size(NULL, audioCodecContext->channels,
                                             audioCodecContext->frame_size,
                                             audioCodecContext->sample_fmt, 1);
    audioBuffer = (uint8_t *) av_malloc(sampleSize);
    avcodec_fill_audio_frame(audioFrame, audioCodecContext->channels,
                             audioCodecContext->sample_fmt,
                             (const uint8_t *)audioBuffer, sampleSize, 1);
    // 写入文件头
    avformat_write_header(pFormatCtx, NULL);
    av_new_packet(&videoPacket, picture_size);

    return 0;

}

/**
 * 开始录制
 */
void CainRecorder::startRecord() {
    recorderState = RECORDER_STARTED;
    startTime = av_gettime();
    // 启动编码线程
    pthread_t thread;
    pthread_create(&thread, NULL, CainRecorder::encodeThread, this);
}

/**
 * 停止录制
 */
void CainRecorder::stopRecord() {
    recorderState = RECORDER_STOPPED;
    LOGI("stopRecord = %d", recorderState);
    startTime = 0;
}

/**
 * 关闭录制器
 */
void CainRecorder::closeRecorder() {
    recorderState = RECORDER_RELEASE;
}

/**
 * 录制结尾
 */
void CainRecorder::recordEndian() {
    int ret = flushFrame(pFormatCtx, 0);
    if (ret < 0) {
        LOGE("flushing encoder failed!\n");
    }
    // 写入文件尾
    av_write_trailer(pFormatCtx);
    // 释放资源
    release();
}

/**
 * 发送编码帧
 * @param data
 */
void CainRecorder::sendFrame(uint8_t *data, int type, int len) {
    // 如果还没有开始，则不能调用
    if (recorderState != RECORDER_STARTED) {
        return;
    }
    if (type == FRAME_YUV) {
        int in_y_size = params->previewWidth * params->previewHeight;
        uint8_t *newData = (uint8_t *) malloc(in_y_size * 3 / 2);
        memcpy(newData, data, in_y_size * 3 / 2);
        frameQueue.push(data);
    } else if (type == FRAME_PCM) {
        // TODO PCM编码

    }
}

/**
 * 编码线程（静态方法）
 * @param obj
 * @return
 */
void* CainRecorder::encodeThread(void *obj) {
    CainRecorder * recorder = (CainRecorder *) obj;
    // 录制不空闲或者编码队列不为空时
    while (recorder->recorderState != RECORDER_STOPPED || !recorder->frameQueue.empty()) {
        // 释放资源状态
        if (recorder->recorderState == RECORDER_RELEASE) {
            // 写入文件尾部
            av_write_trailer(recorder->pFormatCtx);
            // 释放资源
            if (recorder->videoStream) {
                avcodec_close(recorder->videoStream->codec);
                av_free(recorder->videoFrame);
            }
            if (recorder->audioStream) {
                avcodec_close(recorder->audioStream->codec);
                av_free(recorder->audioFrame);
            }
            avio_close(recorder->pFormatCtx->pb);
            avformat_free_context(recorder->pFormatCtx);
            delete recorder;
            return 0;
        }

        // 如果队列为空，则等待
        if (recorder->frameQueue.empty()) {
            continue;
        }
        // h264编码
        recorder->avcEncode(recorder);

        // TODO aac编码

    }
    // 停止录制
    if (recorder->recorderState == RECORDER_STOPPED) {
        recorder->recordEndian();
        delete recorder;
        return 0;
    }
}



/**
 * 刷出剩余编码帧
 * @return
 */
int CainRecorder::flushFrame(AVFormatContext *fmt_ctx, int streamIndex) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[streamIndex]->codec->codec->capabilities &
          CODEC_CAP_DELAY))
        return 0;
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_video2(fmt_ctx->streams[streamIndex]->codec, &enc_pkt,
                                    NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0)
            break;
        if (!got_frame) {
            ret = 0;
            break;
        }
        LOGI("flush Encoder: succeed to encode 1 frame video!\tsize:%5d\n", enc_pkt.size);
        // 写入文件中
        ret = av_interleaved_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0)
            break;
    }

    return ret;
}

/**
 * h264编码
 * @param data yuv原始数据
 * @return
 */
int CainRecorder::avcEncode(CainRecorder *recorder) {

    // 填充数据，摄像头数据是NV21格式的，这里转换为YUV420P格式
    uint8_t *frameData = *recorder->frameQueue.wait_and_pop().get();
    int y_size = recorder->params->previewWidth * recorder->params->previewHeight;
    int uv_length = y_size / 4;
    memcpy(recorder->videoFrame->data[0], frameData, y_size); // 复制Y帧
    int i;
    for ( i = 0; i < uv_length; i++) {
        *(recorder->videoFrame->data[2] + i) = *(frameData + y_size + i * 2);
        *(recorder->videoFrame->data[1] + i) = *(frameData + y_size + i * 2 + 1);
    }

    // 设置宽高
    recorder->videoFrame->format = AV_PIX_FMT_YUV420P;
    recorder->videoFrame->width = recorder->params->previewHeight;
    recorder->videoFrame->height = recorder->params->previewWidth;
    recorder->videoFrame->quality = recorder->videoCodecContext->global_quality;

    // 将YUV数据编码为H264
    av_init_packet(&recorder->videoPacket);
    int got_packet;
    int ret = avcodec_encode_video2(recorder->videoCodecContext, &recorder->videoPacket,
                                    recorder->videoFrame, &got_packet);
    if (ret < 0) {
        LOGE("Error encoding video frame: %s", av_err2str(ret))
        return -1;
    }
    // 将编码后的数据写入文件中
    if (got_packet) {
        recorder->frameCount++;
        recorder->videoPacket.stream_index = recorder->videoStream->index;
        // 写入文件中
        ret = av_interleaved_write_frame(recorder->pFormatCtx, &recorder->videoPacket);
        av_free_packet(&recorder->videoPacket);
        LOGI("av_interleaved_write_frame success ? %d", ret);
    }

    return 0;
}

/**
 * aac编码
 */
int CainRecorder::aacEncode(CainRecorder *recorder) {
    // 是否允许编码
    if (!params->enableAudio) {
        return 0;
    }
    // 将pcm数据复制到audio buffer中
    uint8_t *frameData = *frameQueue.wait_and_pop().get();
    memcpy(audioBuffer, frameData, sampleSize);
    // 设置audio frame
    audioFrame->data[0] = audioBuffer;
    audioFrame->pts++;
    audioFrame->quality = audioCodecContext->global_quality;
    // 创建一个AVPacket
    av_init_packet(audioPacket);
    // PCM音频编码为AAC
    int got_packet;
    int ret = avcodec_encode_audio2(audioCodecContext, audioPacket, audioFrame, &got_packet);
    if (ret < 0) {
        LOGE("avcodec_encode_audio2() error %d: Could not encode audio packet.", ret);
        return -1;
    }
    audioFrame->pts = audioFrame->pts + audioFrame->nb_samples;
    if (got_packet) {
        // 设置音频的pts
        if (audioPacket->pts != AV_NOPTS_VALUE) {
            audioPacket->pts = av_rescale_q(audioPacket->pts,
                                             audioCodecContext->time_base,
                                             audioStream->time_base);
        }
        // 设置音频的dts
        if (audioPacket->dts != AV_NOPTS_VALUE) {
            audioPacket->dts = av_rescale_q(audioPacket->dts,
                                             audioCodecContext->time_base,
                                             audioStream->time_base);
        }
        audioPacket->stream_index = audioStream->index;
        audioPacket->flags = audioPacket->flags | AV_PKT_FLAG_KEY;
        // 写入编码数据
        ret = av_interleaved_write_frame(pFormatCtx, audioPacket);
        if (ret < 0) {
            LOGE("av_interleaved_write_frame() error %d while writing audio frame.", ret);
            return -1;
        }
    }
    // 释放资源
    av_packet_free(&audioPacket);

    return 0;
}


/**
 * 释放资源
 */
void CainRecorder::release() {
    // 释放视频码流
    if (videoStream) {
        avcodec_close(videoStream->codec);
        av_free(videoFrame);
    }
    // 释放音频码流
    if (audioStream) {
        avcodec_close(audioStream->codec);
        av_free(audioStream);
    }
    // 关闭io
    avio_close(pFormatCtx->pb);
    // 释放上下文
    avformat_free_context(pFormatCtx);
}