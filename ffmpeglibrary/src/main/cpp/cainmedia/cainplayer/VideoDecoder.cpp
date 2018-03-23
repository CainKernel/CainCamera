//
// Created by Administrator on 2018/3/21.
//

#include "VideoDecoder.h"

VideoDecoder::VideoDecoder() : BaseDecoder() {

}

/**
 * 设置帧率
 * @param frame_rate
 */
void VideoDecoder::setFrameRate(AVRational frame_rate) {
    mFrameRate = frame_rate;
}

/**
 * 视频解码
 */
void VideoDecoder::decodeFrame() {
    int ret = 0;
    // 还没开始
    while (!mPrepared) {
        continue;
    }

    // 不断地解码得到视频帧AVFrame
    while (true) {
        // 停止
        if (mAbortRequest) {
            break;
        }
        // 暂停
        if (mPaused) {
            continue;
        }
        // 获取视频帧
        AVFrame *frame = av_frame_alloc();
        ret = getVideoFrame(frame);
        if (ret < 0) {
            av_frame_free(&frame);
            break;
        }
        if (!ret) {
            av_frame_free(&frame);
            continue;
        }
        // 将已解码的视频帧入队
        if (mFrameQueue != NULL) {
            mFrameQueue->put(frame);
        } else {
            av_frame_free(&frame);
        }
    }
}

/**
 * 获取视频帧
 * @param frame
 * @return
 */
int VideoDecoder::getVideoFrame(AVFrame *frame) {
    int got_picture;
    if ((got_picture = decodeVideoFrame(frame)) < 0) {
        return -1;
    }
    // 成功解码一帧数据
    if (got_picture) {
        double dpts = NAN;

        if (frame->pts == AV_NOPTS_VALUE) {
            dpts = av_q2d(mStream->time_base) * frame->pts;
        }

        // TODO 是否需要舍弃帧

    }
    return got_picture;
}

/**
 * 解码视频帧
 * @param frame
 * @return
 */
int VideoDecoder::decodeVideoFrame(AVFrame *frame) {
    int got_frame = 0;
    do {
        int ret = -1;
        if (mPacketQueue->isAbort()) {
            return -1;
        }
        if (!mPacketPending) {
            AVPacket pkt;
            if (mPacketQueue != NULL) {
                mPacketQueue->get(&pkt);
            } else {
                return -1;
            }
            av_packet_unref(&this->packet);
            pkt_temp = packet = pkt;
            mPacketPending = true;
        }
        // 解码视频帧
        ret = avcodec_decode_video2(mCodecCtx, frame, &got_frame, &pkt_temp);
        if (got_frame) {
            frame->pts = av_frame_get_best_effort_timestamp(frame);
        }

        if (ret < 0) {
            mPacketPending = false;
        } else {
            pkt_temp.dts = pkt_temp.pts = AV_NOPTS_VALUE;
            if (pkt_temp.data) {
                ret = pkt_temp.size;

                pkt_temp.data += ret;
                pkt_temp.size -= ret;

                if (pkt_temp.size <= 0) {
                    mPacketPending = false;
                }
            } else {
                if (!got_frame) {
                    mPacketPending = false;
                }
            }
        }
    } while (!got_frame);

    return got_frame;
}
