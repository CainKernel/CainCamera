//
// Created by cain on 2018/1/1.
//

#include "media_muxer.h"
#include "h264_encoder.h"
#include "aac_encoder.h"
#include "native_log.h"

/**
     * 构造函数
     * @param params
     */
EncoderMuxer::EncoderMuxer(EncoderParams *params) : mEncoderParams(params) {
    init();
}

/**
 * 初始化复用器
 * @return
 */
int EncoderMuxer::init() {
    // 获取输出路径（这里创建的话，应该是的混合器创建的）
    size_t pathLength = strlen(mEncoderParams->mMediaPath);
    char *outFile = (char *) malloc(pathLength + 1);
    strcpy(outFile, mEncoderParams->mMediaPath);

    // 注册
    av_register_all();

    // 创建复用上下文
    avformat_alloc_output_context2(&mFormatCtx, NULL, NULL, outFile);
    if (mFormatCtx == NULL) {
        LOGE("avformat_alloc_output_context2() error: Could not allocate format context");
        return -1;
    }
    // 输出格式
    mOutputFormat = mFormatCtx->oformat;

//        // 获取格式
//        mOutputFormat = av_guess_format(NULL, outFile, NULL);
//        if (mOutputFormat == NULL) {
//            LOGE("av_guess_format() error: Could not guess output format for %s\n", outFile);
//            return -1;
//        }
//        // 创建复用上下文
//        mFormatCtx = avformat_alloc_context();
//        if (mFormatCtx == NULL) {
//            LOGE("avformat_alloc_context() error: Could not allocate format context");
//            return -1;
//        }
//        mFormatCtx->oformat = mOutputFormat;
}

/**
 * 复用器打开并写入文件头
 * @return
 */
int EncoderMuxer::muxerHeader() {
    // 获取输出路径（这里创建的话，应该是的混合器创建的）
    size_t pathLength = strlen(mEncoderParams->mMediaPath);
    char *outFile = (char *) malloc(pathLength + 1);
    strcpy(outFile, mEncoderParams->mMediaPath);
    // 打开输出文件
    if (avio_open(&mFormatCtx->pb, outFile, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("failed to open output file!\n");
        return -1;
    }
    // 输出信息
    av_dump_format(mFormatCtx, 0, outFile, 1);

    // 写入文件的头部信息
    avformat_write_header(mFormatCtx, NULL);
}


/**
 * 复用器写入文件结尾
 * @return
 */
int EncoderMuxer::muxerTrailer() {
    // 写入结尾帧
    if (interleaved) {
        av_interleaved_write_frame(mFormatCtx, NULL);
    } else {
        av_write_frame(mFormatCtx, NULL);
    }
    // 写入文件尾
    av_write_trailer(mFormatCtx);
    // 关闭IO
    avio_close(mFormatCtx->pb);
    // 释放资源
    avformat_free_context(mFormatCtx);
}
