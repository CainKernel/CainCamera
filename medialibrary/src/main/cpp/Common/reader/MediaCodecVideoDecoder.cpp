//
// Created by CainHuang on 2020/1/12.
//

#include "MediaCodecVideoDecoder.h"

MediaCodecVideoDecoder::MediaCodecVideoDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer)
        : AVMediaDecoder(mediaDemuxer) {
    pCodecName = "h264_mediacodec";
}

void MediaCodecVideoDecoder::setDecoder(const char *name) {
    // do nothing

}

int MediaCodecVideoDecoder::openDecoder(std::map<std::string, std::string> decodeOptions) {
    int ret;
    auto mediaDemuxer = mWeakDemuxer.lock();
    if (!mediaDemuxer || !mediaDemuxer->getContext()) {
        LOGE("Failed to find media demuxer");
        return -1;
    }

    // 查找媒体流
    ret = av_find_best_stream(mediaDemuxer->getContext(), getMediaType(), -1, -1, nullptr, 0);
    if (ret < 0) {
        LOGE("Failed to av_find_best_stream: %s", av_err2str(ret));
        return ret;
    }
    // 获取媒体流
    mStreamIndex = ret;
    pStream = mediaDemuxer->getContext()->streams[mStreamIndex];

    // 查找解码器
    pCodec = avcodec_find_encoder_by_name(pCodecName);
    if (!pCodec) {
        LOGE("Failed to find codec: %s", pCodecName);
        return AVERROR(ENOMEM);
    }
    pStream->codecpar->codec_id = pCodec->id;

    // 创建解码上下文
    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (!pCodecCtx) {
        LOGE("Failed to alloc the %s codec context", av_get_media_type_string(getMediaType()));
        return AVERROR(ENOMEM);
    }

    // 复制媒体流参数到解码上下文中
    if ((ret = avcodec_parameters_to_context(pCodecCtx, pStream->codecpar)) < 0) {
        LOGE("Failed to copy %s codec parameters to decoder context, result: %d",
             av_get_media_type_string(getMediaType()), ret);
        return ret;
    }

//    // TODO 需要将ffmpeg升级到4.0以上
//    // 打开解码器之前，创建MediaCodec的硬解码上下文，用于绑定一个Java层传过来的Surface对象
//    // 然后将硬解码上下文赋值给解码器上下文
//    jobject surface = ...; // Java层的Surface对象
//    AVBufferRef *device_ref = av_hwdevice_ctx_alloc(AV_HWDEVICE_TYPE_MEDIACODEC);
//    AVHWDeviceContext *ctx = (AVHWDeviceContext *)device_ref->data;
//    AVMediaCodecDeviceContext *hwctx = ctx->hwctx;
//    hwctx->surface = (void *)(intptr_t)surface;
//    av_hwdevice_ctx_init(device_ref);
//    pCodecCtx->hw_device_ctx = device_ref;

    // 打开解码器
    AVDictionary *options = nullptr;
    auto it = decodeOptions.begin();
    for (; it != decodeOptions.end(); it++) {
        av_dict_set(&options, (*it).first.c_str(), (*it).second.c_str(), 0);
    }
    if ((ret = avcodec_open2(pCodecCtx, pCodec, &options)) < 0) {
        LOGE("Failed to open %s codec, result: %d", av_get_media_type_string(getMediaType()), ret);
        av_dict_free(&options);
        return ret;
    }
    av_dict_free(&options);

    // 初始化媒体数据
    initMetadata();

    return 0;
}

int MediaCodecVideoDecoder::decodePacket(AVPacket *packet, OnDecodeListener *listener,
                                         int *gotFrame) {

    return 0;
}

AVMediaType MediaCodecVideoDecoder::getMediaType() {
    return AVMEDIA_TYPE_VIDEO;
}

void MediaCodecVideoDecoder::initMetadata() {
    mWidth = pCodecCtx->width;
    mHeight = pCodecCtx->height;
    mPixelFormat = pCodecCtx->pix_fmt;
    auto demuxer = mWeakDemuxer.lock().get();
    if (demuxer) {
        mFrameRate = (int) av_q2d(av_guess_frame_rate(demuxer->getContext(), pStream, nullptr));
        pCodecCtx->time_base = av_inv_q(av_d2q(mFrameRate, 100000));
    } else {
        mFrameRate = 30;
        pCodecCtx->time_base = av_inv_q(av_d2q(mFrameRate, 100000));
    }
}

/**
 * 直接把AVFrame的数据渲染到Surface中，需要ffmpeg4.0以上才能支持
 * @param frame
 */
void MediaCodecVideoDecoder::renderFrame(AVFrame *frame) {
    // TODO need to update ffmpeg 4.0
//    if (frame->format == AV_PIX_FMT_MEDIACODEC) {
//        AVMediaCodecBuffer *buffer = (AVMediaCodecBuffer *) frame->data[3];
//        // 丢弃该帧
//        av_mediacodec_release_buffer(buffer, 0);
//        // 直接渲染到Surface上
//        av_mediacodec_release_buffer(buffer, 1);
//        // 在某个时间节点渲染到Surface上
//        av_mediacodec_render_buffer_at_time(buffer, nanotime);
//    }
}