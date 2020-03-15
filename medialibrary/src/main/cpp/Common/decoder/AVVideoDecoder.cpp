//
// Created by CainHuang on 2020-01-10.
//

#include "AVVideoDecoder.h"

AVVideoDecoder::AVVideoDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer)
        : AVMediaDecoder(mediaDemuxer) {
    pCodecName = "h264_mediacodec";
}

AVVideoDecoder::~AVVideoDecoder() {

}

AVMediaType AVVideoDecoder::getMediaType() {
    return AVMEDIA_TYPE_VIDEO;
}

void AVVideoDecoder::initMetadata() {
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

int AVVideoDecoder::getWidth() {
    return mWidth;
}

int AVVideoDecoder::getHeight() {
    return mHeight;
}

int AVVideoDecoder::getFrameRate() {
    return mFrameRate;
}

AVPixelFormat AVVideoDecoder::getFormat() {
    return mPixelFormat;
}

/**
 * 获取视频的旋转角度
 * @return
 */
double AVVideoDecoder::getRotation() {
    if (!pStream) {
        return 0;
    }
    AVDictionaryEntry *rotate_tag = av_dict_get(pStream->metadata, "rotate", NULL, 0);
    uint8_t* displaymatrix = av_stream_get_side_data(pStream, AV_PKT_DATA_DISPLAYMATRIX, NULL);
    double theta = 0;

    if (rotate_tag && *rotate_tag->value && strcmp(rotate_tag->value, "0")) {
        char *tail;
        theta = av_strtod(rotate_tag->value, &tail);
        if (*tail) {
            theta = 0;
        }
    }
    if (displaymatrix && !theta){
        theta = -av_display_rotation_get((int32_t*) displaymatrix);
    }

    theta -= 360*floor(theta/360 + 0.9/360);

    if (fabs(theta - 90 * round(theta /90)) > 2) {

#if defined(__ANDROID__)
        LOGW("Odd rotation angle.\n"
              "If you want to help, upload a sample "
              "of this file to ftp://upload.ffmpeg.org/incoming/ "
              "and contact the ffmpeg-devel mailing list. (ffmpeg-devel@ffmpeg.org)");
#else
        av_log(NULL, AV_LOG_WARNING, "Odd rotation angle.\n"
                "If you want to help, upload a sample "
                "of this file to ftp://upload.ffmpeg.org/incoming/ "
                "and contact the ffmpeg-devel mailing list. (ffmpeg-devel@ffmpeg.org)");
#endif
    }

    return theta;
}