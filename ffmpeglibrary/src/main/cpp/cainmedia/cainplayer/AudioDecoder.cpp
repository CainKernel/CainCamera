//
// Created by Administrator on 2018/3/21.
//

#include "AudioDecoder.h"
#include "native_log.h"

AudioDecoder::AudioDecoder() : BaseDecoder() {

}

/**
 * 音频解码
 */
void AudioDecoder::decodeFrame() {
    int ret = 0;
    int got_frame = 0;
    AVRational tb;
    // 还没开始
    while (!mPrepared) {
        continue;
    }
    while (true) {
        // 停止
        if (mAbortRequest) {
            break;
        }
        // 暂停
        if (mPaused) {
            continue;
        }
        // 解码得到音频帧
        AVFrame *frame = av_frame_alloc();
        if ((got_frame = decodeAudioFrame(frame)) < 0) {
            av_frame_free(&frame);
            break;
        }
        // 将解码得到的音频帧入队
        if (got_frame && mFrameQueue) {
            mFrameQueue->put(frame);
        } else {
            av_frame_free(&frame);
        }
    }
}

/**
 * 解码音频帧
 * @param frame
 * @return
 */
int AudioDecoder::decodeAudioFrame(AVFrame *frame) {
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
            av_packet_unref(&packet);
            packet = pkt_temp = pkt;
            mPacketPending = true;
        }

        ret = avcodec_decode_audio4(mCodecCtx, frame, &got_frame, &pkt_temp);
        if (got_frame) {
            AVRational tb = (AVRational) {1, frame->sample_rate};
            // 更新时间戳
            if (frame->pts != AV_NOPTS_VALUE) {
                frame->pts = av_rescale_q(frame->pts, av_codec_get_pkt_timebase(mCodecCtx), tb);
            } else if (next_pts != AV_NOPTS_VALUE) {
                frame->pts = av_rescale_q(next_pts, next_pts_tb, tb);
            }
            if (frame->pts != AV_NOPTS_VALUE) {
                next_pts = frame->pts + frame->nb_samples;
                next_pts_tb = tb;
            }
        }

        if (ret < 0) {
            mPacketPending = false;
        } else {
            pkt_temp.dts = pkt_temp.pts = AV_NOPTS_VALUE;
            if (pkt_temp.data) {

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
