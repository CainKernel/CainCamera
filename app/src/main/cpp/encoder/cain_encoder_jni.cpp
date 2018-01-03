//
// Created by Administrator on 2018/1/3.
//

#include"jni.h"
#include <string>
#include <malloc.h>
#include "common_encoder.h"
#include "encoder_params.h"
#include "native_log.h"

using namespace std;


extern "C" {

/**
 * 初始化编码器
 * @param env
 * @param obj
 * @param videoPath_    视频路径
 * @param previewWidth  预览宽度
 * @param previewHeight 预览高度
 * @param videoWidth    录制视频宽度
 * @param videoHeight   录制视频高度
 * @param frameRate     帧率
 * @param bitRate       视频比特率
 * @param audioBitRate  音频比特率
 * @param audioSampleRate  音频采样频率
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_initMediaRecorder
        (JNIEnv *env, jclass obj, jstring videoPath, jint previewWidth, jint previewHeight,
         jint videoWidth, jint videoHeight, jint frameRate, jint bitRate,
         jint audioBitRate, jint audioSampleRate);

/**
 * 开始录制
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_startRecorder(JNIEnv *env, jclass obj);

/**
 * 发送YUV数据进行编码
 * @param env
 * @param obj
 * @param yuvArray
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_encoderYUVFrame
        (JNIEnv *env, jclass obj, jbyteArray yuvArray);

/**
 * 发送PCM数据进行编码
 * @param env
 * @param obj
 * @param pcmArray
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_sendPCMFrame
        (JNIEnv *env, jclass obj, jbyteArray pcmArray);

/**
 * 发送停止命令
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_stopRecorder(JNIEnv *env, jclass obj);

/**
 * 释放资源
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_release(JNIEnv *env, jclass obj);

} // extern "C"


// 参数保存
EncoderParams *params;
// muxer部分
AVFormatContext *mFormatCtx;
AVOutputFormat *mOutputFormat;
// 视频编码器部分
AVCodec *mVideoCodec;
AVCodecContext *mVideoCodecContext;
AVStream *mVideoStream;
AVFrame *mVideoFrame;
AVPacket *mVideoPacket;
uint8_t  *mVideoOutBuffer;
int mVideoSize;
// 图像转换上下文
SwsContext *img_convert_ctx;

// 音频编码器部分
AVCodec *mAudioCodec;
AVCodecContext *mAudioCodecContext;
AVStream *mAudioStream;
AVFrame *mAudioFrame;
AVPacket *mAudioPacket;
uint8_t  *mAudioBuffer;
int mSampleSize; // 采样缓冲大小
// 音频转换上下文
SwrContext *samples_convert_ctx;

/**
 * 初始化编码器
 * @param env
 * @param obj
 * @param videoPath_    视频路径
 * @param previewWidth  预览宽度
 * @param previewHeight 预览高度
 * @param videoWidth    录制视频宽度
 * @param videoHeight   录制视频高度
 * @param frameRate     帧率
 * @param bitRate       视频比特率
 * @param audioBitRate  音频比特率
 * @param audioSampleRate  音频采样频率
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_initMediaRecorder
        (JNIEnv *env, jclass obj, jstring videoPath_, jint previewWidth, jint previewHeight,
         jint videoWidth, jint videoHeight, jint frameRate, jint bitRate,
         jint audioBitRate, jint audioSampleRate) {

    // 配置参数
    const char * videoPath = env->GetStringUTFChars(videoPath_, 0);
    params->mMediaPath = videoPath;
    params->mPreviewWidth = previewWidth;
    params->mPreviewHeight = previewHeight;
    params->mVideoWidth = videoWidth;
    params->mVideoHeight = videoHeight;
    params->mFrameRate = frameRate;
    params->mBitRate = bitRate;
    params->mAudioBitRate = audioBitRate;
    params->mAudioSampleRate = audioSampleRate;

    // 初始化
    av_register_all();

    // 获取格式
    mOutputFormat = av_guess_format(NULL, videoPath, NULL);
    if (mOutputFormat == NULL) {
        LOGE("av_guess_format() error: Could not guess output format for %s\n", videoPath);
        return -1;
    }
    // 创建复用上下文
    mFormatCtx = avformat_alloc_context();
    if (mFormatCtx == NULL) {
        LOGE("avformat_alloc_context() error: Could not allocate format context");
        return -1;
    }
    mFormatCtx->oformat = mOutputFormat;

    // ----------------------------- 视频编码器初始化部分 --------------------------------------
    // 设置视频编码器的ID
    mOutputFormat->video_codec = AV_CODEC_ID_H264;
    // 查找编码器
    mVideoCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (!mVideoCodec) {
        LOGE("can not find encoder !\n");
        return -1;
    }

    // 创建视频码流
    mVideoStream = avformat_new_stream(mFormatCtx, mVideoCodec);
    if (!mVideoStream) {
        LOGE("avformat_new_stream() error: Could not allocate video stream!\n");
        return -1;
    }

    // 获取编码上下文，并设置相关参数
    mVideoCodecContext = mVideoStream->codec;
    mVideoCodecContext->codec_id = mOutputFormat->video_codec;
    mVideoCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    mVideoCodecContext->pix_fmt = AV_PIX_FMT_YUV420P;

    mVideoCodecContext->width = params->mVideoWidth;
    mVideoCodecContext->height = params->mVideoHeight;

    mVideoCodecContext->bit_rate = params->mBitRate;
    mVideoCodecContext->gop_size = 30;
    mVideoCodecContext->thread_count = 12;
    mVideoCodecContext->time_base.num = 1;
    mVideoCodecContext->time_base.den = params->mFrameRate;
    mVideoCodecContext->qmin = 10;
    mVideoCodecContext->qmax = 51;
    mVideoCodecContext->max_b_frames = 0;

    // 设置H264的profile
    AVDictionary *param = 0;
    if (mVideoCodecContext->codec_id == AV_CODEC_ID_H264) {
        av_dict_set(&param, "tune", "zerolatency", 0);
        av_opt_set(mVideoCodecContext->priv_data, "preset", "ultrafast", 0);
        av_dict_set(&param, "profile", "baseline", 0);
    }

    // 设置码流的timebase
    mVideoStream->time_base.num = 1;
    mVideoStream->time_base.den = params->mFrameRate;

    // 判断视频码流是否存在，打开编码器
    int ret = avcodec_open2(mVideoCodecContext, mVideoCodec, NULL);
    if (ret < 0) {
        LOGE("avcodec_open2() error %d: Could not open video codec.", ret);
        return -1;
    }

    // ------------------------------ 音频编码初始化部分 --------------------------------------------
    // 设置音频编码格式
    mOutputFormat->audio_codec = AV_CODEC_ID_AAC;

    // 创建音频编码器
    mAudioCodec = avcodec_find_encoder(mOutputFormat->audio_codec);
    if (!mAudioCodec) {
        LOGE("avcodec_find_encoder() error: Audio codec not found.");
        return -1;
    }

    // 创建音频码流
    mAudioStream = avformat_new_stream(mFormatCtx, mAudioCodec);
    if (!mAudioStream) {
        LOGE("avformat_new_stream() error: Could not allocate audio stream.");
        return -1;
    }

    // 获取音频编码上下文
    mAudioCodecContext = mAudioStream->codec;
    mAudioCodecContext->codec_id = mOutputFormat->audio_codec;
    mAudioCodecContext->codec_type = AVMEDIA_TYPE_AUDIO;
    mAudioCodecContext->bit_rate = params->mAudioBitRate;
    mAudioCodecContext->sample_rate = params->mAudioSampleRate;
    mAudioCodecContext->channel_layout = AV_CH_LAYOUT_MONO;
    mAudioCodecContext->channels =
            av_get_channel_layout_nb_channels(mAudioCodecContext->channel_layout);
    mAudioCodecContext->sample_fmt = AV_SAMPLE_FMT_S16;
    mAudioCodecContext->bits_per_raw_sample = 16;
    // 设置码率
    mAudioCodecContext->time_base.num = 1;
    mAudioCodecContext->time_base.den = params->mAudioSampleRate;

    // 设置码流的码率
    mAudioStream->time_base.num = 1;
    mAudioStream->time_base.den = params->mAudioSampleRate;

    // 创建音频帧
    mAudioFrame = av_frame_alloc();
    if (!mAudioFrame) {
        LOGE("av_frame_alloc() error: Could not allocate audio frame.");
        return -1;
    }
    mAudioFrame->pts++;


    // 创建缓冲
    mSampleSize = av_samples_get_buffer_size(NULL, mAudioCodecContext->channels,
                                               mAudioCodecContext->frame_size,
                                               mAudioCodecContext->sample_fmt, 1);
    mAudioBuffer = (uint8_t *) av_malloc(mSampleSize);
    avcodec_fill_audio_frame(mAudioFrame, mAudioCodecContext->channels,
                             mAudioCodecContext->sample_fmt,
                             (const uint8_t *)mAudioBuffer, mSampleSize, 1);


    // ------------------------------------- 复用器写入文件初始化 ------------------------------------

    // 打开输出文件
    ret = avio_open(&mFormatCtx->pb, videoPath, AVIO_FLAG_WRITE);
    if (ret < 0) {
        LOGE("avio_open error() error %d : Could not open %s", ret, videoPath);
        return -1;
    }

    // 写入文件头
    avformat_write_header(mFormatCtx, NULL);

}

/**
 * 开始录制
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_startRecorder(JNIEnv *env, jclass obj) {

}

/**
 * 发送YUV数据进行编码
 * @param env
 * @param obj
 * @param yuvData
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_encoderYUVFrame
        (JNIEnv *env, jclass obj, jbyteArray yuvData) {
    jbyte *yuv = env->GetByteArrayElements(yuvData, 0);

    // 创建视频帧
    mVideoFrame = av_frame_alloc();
    if (!mVideoFrame) {
        LOGE("av_frame_alloc() error: Could not allocate video frame.");
        return -1;
    }

    // 创建缓冲
    mVideoOutBuffer = (uint8_t *) av_malloc(avpicture_get_size(
            AV_PIX_FMT_YUV420P, params->mPreviewWidth, params->mPreviewHeight));
    avpicture_fill((AVPicture *) mVideoFrame, mVideoOutBuffer, AV_PIX_FMT_YUV420P,
                   mVideoCodecContext->width, mVideoCodecContext->height);

    // 填充数据，摄像头数据是NV21格式的，这里转换为YUV420P格式
    int y_size = params->mPreviewWidth * params->mPreviewHeight;
    int uv_length = y_size / 4;
    memcpy(mVideoFrame->data[0], yuv, y_size); // 复用Y帧
    for (int i = 0; i < uv_length; ++i) {
        *(mVideoFrame->data[2] + i) = *(yuv + y_size + i * 2);
        *(mVideoFrame->data[1] + i) = *(yuv + y_size + i * 2 + 1);
    }
    mVideoFrame->format = AV_PIX_FMT_YUV420P;
    // TODO 缩放为 videoWidth，videoHeight
    mVideoFrame->width = params->mPreviewWidth;
    mVideoFrame->height = params->mPreviewHeight;
    mVideoFrame->quality = mVideoCodecContext->global_quality;
    mVideoFrame->pts = mVideoFrame->pts + 1;

    // 将YUV数据编码为H264
    av_init_packet(mVideoPacket);
    int got_packet;
    int ret = avcodec_encode_video2(mVideoCodecContext, mVideoPacket, mVideoFrame, &got_packet);
    if (ret < 0) {
        LOGE("Error encoding video frame: %s", av_err2str(ret))
        return -1;
    }
    // 将编码后的数据写入文件中
    if (got_packet) {
        // 写入pts
        if (mVideoPacket->pts != AV_NOPTS_VALUE) {
            mVideoPacket->pts = av_rescale_q(mVideoPacket->pts,
                                             mVideoCodecContext->time_base, mVideoStream->time_base);
        }
        // 写入dts
        if (mVideoPacket->dts != AV_NOPTS_VALUE) {
            mVideoPacket->dts = av_rescale_q(mVideoPacket->dts,
                                             mVideoCodecContext->time_base, mVideoStream->time_base);
        }
        mVideoPacket->stream_index = mVideoStream->index;
        // 写入文件中
        ret = av_interleaved_write_frame(mFormatCtx, mVideoPacket);
    } else {
        ret = 0;
    }
    // 释放帧
    av_packet_free(&mVideoPacket);

    // 释放数据
    env->ReleaseByteArrayElements(yuvData, yuv, 0);
    return 0;
}

/**
 * 发送PCM数据进行编码
 * @param env
 * @param obj
 * @param pcmData
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_sendPCMFrame
        (JNIEnv *env, jclass obj, jbyteArray pcmData) {
    jbyte *pcm = env->GetByteArrayElements(pcmData, 0);
    // 将pcm数据复制到audio buffer中
    memcpy(mAudioBuffer, pcm, mSampleSize);
    // 设置audio frame
    mAudioFrame->data[0] = mAudioBuffer;
    mAudioFrame->pts++;
    mAudioFrame->quality = mAudioCodecContext->global_quality;
    // 创建一个AVPacket
    av_init_packet(mAudioPacket);
    // PCM音频编码为AAC
    int got_packet;
    int ret = avcodec_encode_audio2(mAudioCodecContext, mAudioPacket, mAudioFrame, &got_packet);
    if (ret < 0) {
        LOGE("avcodec_encode_audio2() error %d: Could not encode audio packet.", ret);
        return -1;
    }
    mAudioFrame->pts = mAudioFrame->pts + mAudioFrame->nb_samples;
    if (got_packet) {
        // 设置音频的pts
        if (mAudioPacket->pts != AV_NOPTS_VALUE) {
            mAudioPacket->pts = av_rescale_q(mAudioPacket->pts,
                                             mAudioCodecContext->time_base,
                                             mAudioStream->time_base);
        }
        // 设置音频的dts
        if (mAudioPacket->dts != AV_NOPTS_VALUE) {
            mAudioPacket->dts = av_rescale_q(mAudioPacket->dts,
                                             mAudioCodecContext->time_base,
                                             mAudioStream->time_base);
        }
        mAudioPacket->stream_index = mAudioStream->index;
        mAudioPacket->flags = mAudioPacket->flags | AV_PKT_FLAG_KEY;
        // 写入编码数据
        ret = av_interleaved_write_frame(mFormatCtx, mAudioPacket);
        if (ret < 0) {
            LOGE("av_interleaved_write_frame() error %d while writing audio frame.", ret);
            return -1;
        }
    }
    // 释放资源
    av_packet_free(&mAudioPacket);
    // 释放资源
    env->ReleaseByteArrayElements(pcmData, pcm, 0);
    return 0;
}

/**
 * 发送停止命令
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_stopRecorder(JNIEnv *env, jclass obj) {
    if (mFormatCtx != NULL) {
        // TODO 刷出所有剩余的缓冲数据

        // 写入结尾帧
        av_interleaved_write_frame(mFormatCtx, NULL);

        // 写入文件尾部信息
        av_write_trailer(mFormatCtx);

        // TODO 释放资源
        
    }
}

/**
 * 释放资源
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_release(JNIEnv *env, jclass obj) {

}
