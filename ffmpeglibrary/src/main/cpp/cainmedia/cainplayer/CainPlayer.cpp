//
// Created by cain on 2018/2/8.
//

#include "CainCmdUtils.h"
#include "CainPlayer.h"

#include "CainClock.h"
#include "CainFrameQueue.h"
#include "CainPacketQueue.h"
/**
 * 构造器
 */
CainPlayer::CainPlayer() {
    Reset();
}

/**
 * 析构器
 */
CainPlayer::~CainPlayer() {
    Release();
}

/**
 * 设置数据源
 * @param path
 */
void CainPlayer::SetDataSource(const char *path) {
    input_filename = av_strdup(path);
}

/**
 * 获取当前位置
 */
int CainPlayer::GetCurrentPosition() {
}

/**
 * 获取音频Session Id
 * @return
 */
int CainPlayer::GetAudioSessionId() {
}

/**
 * 获取时长
 * @return
 */
int CainPlayer::GetDuration() {

}

/**
 * 是否循环播放
 * @return
 */
bool CainPlayer::IsLooping() {
    return loop == 1;
}

/**
 * 是否正在播放
 * @return
 */
bool CainPlayer::IsPlaying() {

}

/**
 * 暂停
 */
void CainPlayer::Pause() {

}

/**
 * 暂停播放声音
 */
void CainPlayer::PauseAudio() {
    if (is) {
        is->muted = 1;
    }
}

/**
 * 开始
 */
void CainPlayer::Start() {

}

/**
 * 停止
 */
void CainPlayer::Stop() {

}

/**
 * 异步装载流媒体
 */
void CainPlayer::PrepareAsync() {
    assert(!is);
    assert(input_filename);
    is = stream_open(input_filename, NULL);
}

/**
 * 重置到未初始化状态
 */
void CainPlayer::Reset() {

}

/**
 * 释放资源
 */
void CainPlayer::Release() {

}

/**
 * 指定播放区域
 * @param msec
 */
void CainPlayer::SeekTo(int msec) {
}

/**
 * 指定播放区域
 * @param lmsec
 * @param rmsec
 */
void CainPlayer::SeekToRegion(int lmsec, int rmsec) {

}

/**
 * 设置是否循环播放
 * @param loop
 */
void CainPlayer::SetLooping(bool loop) {
    this->loop = loop;
}

/**
 * 设置是否倒放
 * @param reverse
 */
void CainPlayer::SetReverse(bool reverse) {

}

/**
 * 设置是否播放声音
 * @param play
 */
void CainPlayer::SetPlayAudio(bool play) {

}

/**
 * 改变大小
 * @param width
 * @param height
 */
void CainPlayer::ChangedSize(int width, int height) {

}


/**
 * 解码器初始化
 * @param d
 * @param avctx
 * @param queue
 * @param empty_queue_cond
 */
void CainPlayer::decoder_init(Decoder *d, AVCodecContext *avctx,
                  PacketQueue *queue, Cond *empty_queue_cond) {
    // 为解码器分配内存
    memset(d, 0, sizeof(Decoder));
    // 设置解码上下文
    d->avctx = avctx;
    // 设置解码队列
    d->queue = queue;
    // 设置空队列条件锁
    d->empty_queue_cond = empty_queue_cond;
    // 设置开始的时间戳
    d->start_pts = AV_NOPTS_VALUE;
}


/**
 * 解码方法
 * @param d
 * @param frame
 * @param sub
 */
int CainPlayer::decoder_decode_frame(Decoder *d, AVFrame *frame, AVSubtitle *sub) {
    int got_frame = 0;

    do {
        int ret = -1;
        // 如果处于舍弃状态，直接返回
        if (d->queue->abort_request) {
            return -1;
        }
        // 如果当前没有包在等待，或者队列的序列不相同时，取出下一帧
        if (!d->packet_pending || d->queue->serial != d->pkt_serial) {
            AVPacket pkt;
            do {
                // 队列为空
                if (d->queue->nb_packets == 0) {
                    Cain_CondSignal(d->empty_queue_cond);
                }
                // 没能出列数据
                if (packet_queue_get(d->queue, &pkt, 1, &d->pkt_serial) < 0) {
                    return -1;
                }
                // 刷新数据
                if (pkt.data == flush_pkt.data) {
                    avcodec_flush_buffers(d->avctx);
                    d->finished = 0;
                    d->next_pts = d->start_pts;
                    d->next_pts_tb = d->start_pts_tb;
                }

            } while (pkt.data == flush_pkt.data || d->queue->serial != d->pkt_serial);
            // 释放包
            av_packet_unref(&d->pkt);
            // 更新包
            d->pkt_temp = d->pkt = pkt;
            // 包等待标志
            d->packet_pending = 1;
        }

        // 根据解码器类型判断是音频还是视频还是字幕
        switch (d->avctx->codec_type) {
            // 视频解码
            case AVMEDIA_TYPE_VIDEO:
                // 解码视频
                ret = avcodec_decode_video2(d->avctx, frame, &got_frame, &d->pkt_temp);
                // 解码成功，更新时间戳
                if (got_frame) {
                    if (decoder_reorder_pts == -1) {
                        frame->pts = av_frame_get_best_effort_timestamp(frame);
                    } else if (!decoder_reorder_pts) {		// 如果不重新排列时间戳，则需要更新帧的pts
                        frame->pts = frame->pkt_dts;
                    }
                }
                break;

                // 音频解码
            case AVMEDIA_TYPE_AUDIO:
                // 音频解码
                ret = avcodec_decode_audio4(d->avctx, frame, &got_frame, &d->pkt_temp);
                // 音频解码完成，更新时间戳
                if (got_frame) {
                    AVRational tb = (AVRational){1, frame->sample_rate};
                    // 更新帧的时间戳
                    if (frame->pts != AV_NOPTS_VALUE)
                        frame->pts = av_rescale_q(frame->pts, av_codec_get_pkt_timebase(d->avctx), tb);
                    else if (d->next_pts != AV_NOPTS_VALUE)
                        frame->pts = av_rescale_q(d->next_pts, d->next_pts_tb, tb);
                    // 更新下一时间戳
                    if (frame->pts != AV_NOPTS_VALUE) {
                        d->next_pts = frame->pts + frame->nb_samples;
                        d->next_pts_tb = tb;
                    }
                }
                break;
                // 字幕解码
            case AVMEDIA_TYPE_SUBTITLE:
                ret = avcodec_decode_subtitle2(d->avctx, sub, &got_frame, &d->pkt_temp);
                break;

            default:
                break;
        }

        // 判断是否解码成功
        if (ret < 0) {
            d->packet_pending = 0;
        } else {
            d->pkt_temp.dts = d->pkt_temp.pts = AV_NOPTS_VALUE;
            if (d->pkt_temp.data) {
                if (d->avctx->codec_type != AVMEDIA_TYPE_AUDIO)
                    ret = d->pkt_temp.size;

                d->pkt_temp.data += ret;
                d->pkt_temp.size -= ret;

                if (d->pkt_temp.size <= 0)
                    d->packet_pending = 0;
            } else {
                if (!got_frame) {
                    d->packet_pending = 0;
                    d->finished = d->pkt_serial;
                }
            }
        }
    } while (!got_frame && !d->finished);

    return got_frame;
}

/**
 * 销毁解码器
 * @param d
 */
void CainPlayer::decoder_destroy(Decoder *d) {
    // 释放包
    av_packet_unref(&d->pkt);
    // 释放解码上下文
    avcodec_free_context(&d->avctx);
}

/**
 * 取消解码
 * @param d
 * @param fq
 */
void CainPlayer::decoder_abort(Decoder *d, FrameQueue *fq) {
    packet_queue_abort(d->queue);
    frame_queue_signal(fq);
    Cain_WaitThread(d->decoder_tid, NULL);
    d->decoder_tid = NULL;
    packet_queue_flush(d->queue);
}

/**
 * 显示画面 TODO 显示部分还没有实现
 * @param is
 */
void CainPlayer::video_image_display(VideoState *is) {

}

/**
 * 关闭组件的流
 * @param is
 * @param stream_index
 */
void CainPlayer::stream_component_close(VideoState *is, int stream_index) {
    AVFormatContext *ic = is->ic;
    AVCodecParameters *codecpar;

    if (stream_index < 0 || stream_index >= ic->nb_streams) {
        return;
    }
    codecpar = ic->streams[stream_index]->codecpar;
    // 根据解码类型销毁不同的解码器、释放资源等
    switch (codecpar->codec_type) {
        // 关闭音频流
        case AVMEDIA_TYPE_AUDIO:
            decoder_abort(&is->auddec, &is->sampq);
            // TODO 停止音频
            // Cain_CloseAudio();
            decoder_destroy(&is->auddec);
            swr_free(&is->swr_ctx);
            av_freep(&is->audio_buf1);
            is->audio_buf1_size = 0;
            is->audio_buf = NULL;
            break;

        // 关闭视频流
        case AVMEDIA_TYPE_VIDEO:
            decoder_abort(&is->viddec, &is->pictq);
            decoder_destroy(&is->viddec);
            break;

        // 关闭字幕流
        case AVMEDIA_TYPE_SUBTITLE:
            decoder_abort(&is->subdec, &is->subpq);
            decoder_destroy(&is->subdec);
            break;

        default:
            break;
    }

    // 销毁流
    ic->streams[stream_index]->discard = AVDISCARD_ALL;
    // 重置流的状态
    switch (codecpar->codec_type) {
        case AVMEDIA_TYPE_AUDIO:
            is->audio_st = NULL;
            is->audio_stream = -1;
            break;
        case AVMEDIA_TYPE_VIDEO:
            is->video_st = NULL;
            is->video_stream = -1;
            break;
        case AVMEDIA_TYPE_SUBTITLE:
            is->subtitle_st = NULL;
            is->subtitle_stream = -1;
            break;

        default:
            break;
    }
}

/**
 * 关闭流
 * @param is
 */
void CainPlayer::stream_close(VideoState *is) {
    // 1、等待读取线程执行完成
    is->abort_request = 1;
    Cain_WaitThread(is->read_tid, NULL);

    // 2、关闭每个码流
    if (is->audio_stream >= 0) {
        stream_component_close(is, is->audio_stream);
    }

    if (is->video_stream >= 0) {
        stream_component_close(is, is->video_stream);
    }
    if (is->subtitle_stream >= 0) {
        stream_component_close(is, is->subtitle_stream);
    }

    // 关闭输入上下文
    avformat_close_input(&is->ic);

    // 销毁待解码包队列
    packet_queue_destroy(&is->videoq);
    packet_queue_destroy(&is->audioq);
    packet_queue_destroy(&is->subtitleq);

    // 销毁已解码帧队列
    frame_queue_destory(&is->pictq);
    frame_queue_destory(&is->sampq);
    frame_queue_destory(&is->subpq);

    // 销毁连续读/解复用线程
    Cain_DestroyCond(is->continue_read_thread);
    // 释放转码上下文
    sws_freeContext(is->img_convert_ctx);
    av_free(is->filename);
    av_free(is);
}

/**
 * 查找/定位流
 * @param is
 * @param pos
 * @param rel
 * @param seek_by_bytes
 */
void CainPlayer::stream_seek(VideoState *is, int64_t pos, int64_t rel, int seek_by_bytes) {
    if (!is->seek_req) {
        is->seek_pos = pos;
        is->seek_rel = rel;
        is->seek_flags &= ~AVSEEK_FLAG_BYTE;
        if (seek_by_bytes) {
            is->seek_flags |= AVSEEK_FLAG_BYTE;
        }
        is->seek_req = 1;
        Cain_CondSignal(is->continue_read_thread);
    }
}

/**
 * 暂停/播放视频流
 * @param is
 */
void CainPlayer::stream_toggle_pause(VideoState *is) {

    if (is->paused) {
        is->frame_timer += av_gettime_relative() / 1000000.0 - is->vidclk.last_updated;
        set_clock(&is->vidclk, get_clock(&is->vidclk), is->vidclk.serial);
    }

    set_clock(&is->extclk, get_clock(&is->extclk), is->extclk.serial);
    is->paused = is->audclk.paused = is->vidclk.paused = is->extclk.paused = !is->paused;
}

/**
 *  暂停播放
 * @param is
 */
void CainPlayer::toggle_pause(VideoState *is) {
    stream_toggle_pause(is);
    is->step = 0;
}

/**
 * 是否静音
 * @param is
 */
void CainPlayer::toggle_mute(VideoState *is) {
    is->muted = !is->muted;
}

/**
 * 更新音频
 * @param is
 * @param sign
 * @param step
 */
void CainPlayer::update_volume(VideoState *is, int sign, double step) {
    double volume_level = is->audio_volume ? (20 * log(is->audio_volume / (double)MIX_MAXVOLUME) / log(10)) : -1000.0;
    int new_volume = lrint(MIX_MAXVOLUME * pow(10.0, (volume_level + sign * step) / 20.0));
    is->audio_volume = av_clip(is->audio_volume == new_volume ? (is->audio_volume + sign) : new_volume, 0, MIX_MAXVOLUME);
}

/**
 * 下一帧
 * @param is
 */
void CainPlayer::step_to_next_frame(VideoState *is) {
    if (is->paused) {
        stream_toggle_pause(is);
    }
    is->step = 1;
}

/**
 * 计算延时
 * @param delay
 * @param is
 * @return
 */
double CainPlayer::compute_target_delay(double delay, VideoState *is) {
    double sync_threshold, diff = 0;

    /* update delay to follow master synchronisation source */
    // 如果不是以视频做为同步基准，则计算延时
    if (get_master_sync_type(is) != AV_SYNC_VIDEO_MASTER) {
        /* if video is slave, we try to correct big delays by
           duplicating or deleting a frame */
        // 计算时间差
        diff = get_clock(&is->vidclk) - get_master_clock(is);

        /* skip or repeat frame. We take into account the
           delay to compute the threshold. I still don't know
           if it is the best guess */
        // 计算同步预支
        sync_threshold = FFMAX(AV_SYNC_THRESHOLD_MIN, FFMIN(AV_SYNC_THRESHOLD_MAX, delay));
        // 判断时间差是否在许可范围内
        if (!isnan(diff) && fabs(diff) < is->max_frame_duration) {

            if (diff <= -sync_threshold) { // 滞后
                delay = FFMAX(0, delay + diff);
            } else if (diff >= sync_threshold && delay > AV_SYNC_FRAMEDUP_THRESHOLD) { // 超前
                delay = delay + diff;
            } else if (diff >= sync_threshold) { // 超过了理论阈值
                delay = 2 * delay;
            }
        }
    }

    av_log(NULL, AV_LOG_TRACE, "video: delay=%0.3f A-V=%f\n",
           delay, -diff);

    return delay;
}

/**
 * 计算显示时长，限定在0 ~ max_frame_duration之间
 * @param is
 * @param vp
 * @param nextvp
 * @return
 */
double CainPlayer::vp_duration(VideoState *is, Frame *vp, Frame *nextvp) {
    if (vp->serial == nextvp->serial) {
        double duration = nextvp->pts - vp->pts;
        if (isnan(duration) || duration <= 0 || duration > is->max_frame_duration) {
            return vp->duration;
        } else {
            return duration;
        }
    } else {
        return 0.0;
    }
}

/**
 * 更新视频的pts
 * @param is
 * @param pts
 * @param pos
 * @param serial
 */
void CainPlayer::update_video_pts(VideoState *is, double pts, int64_t pos, int serial) {
    /* update current video pts */
    set_clock(&is->vidclk, pts, serial);
    sync_clock_to_slave(&is->extclk, &is->vidclk);
}

/**
 * 刷新视频帧，显示线程执行实体
 * @param opaque
 * @param remaining_time
 */
void CainPlayer::video_refresh(void *opaque, double *remaining_time) {
    VideoState *is = (VideoState *)opaque;
    double time;

    // 主同步类型是外部时钟同步，并且是实时码流，则检查外部时钟速度
    if (!is->paused && get_master_sync_type(is) == AV_SYNC_EXTERNAL_CLOCK && is->realtime) {
        check_external_clock_speed(is);
    }

    // 音频码流
    if (!display_disable && is->show_mode != SHOW_MODE_VIDEO && is->audio_st) {
        time = av_gettime_relative() / 1000000.0;
        if (is->force_refresh || is->last_vis_time + rdftspeed < time) {
            // 如果视频流存在，则显示画面
            if (is->video_st) {
                video_image_display(is);
            }
            is->last_vis_time = time;
        }
        // 剩余时间
        *remaining_time = FFMIN(*remaining_time, is->last_vis_time + rdftspeed - time);
    }

    // 视频码流
    if (is->video_st) {
retry:
        if (frame_queue_nb_remaining(&is->pictq) == 0) {
            // nothing to do, no picture to display in the queue
        } else {
            double last_duration, duration, delay;
            Frame *vp, *lastvp;

            /* dequeue the picture */
            lastvp = frame_queue_peek_last(&is->pictq);
            vp = frame_queue_peek(&is->pictq);

            // 视频队列
            if (vp->serial != is->videoq.serial) {
                frame_queue_next(&is->pictq);
                goto retry;
            }

            // seek操作时才会产生变化
            if (lastvp->serial != vp->serial) {
                is->frame_timer = av_gettime_relative() / 1000000.0;
            }

            // 如果处于停止
            if (is->paused) {
                goto display;
            }

            /* compute nominal last_duration */
            // 计算上一个时长
            last_duration = vp_duration(is, lastvp, vp);
            // 计算目标延时
            delay = compute_target_delay(last_duration, is);
            // 获取时间
            time= av_gettime_relative()/1000000.0;
            // 如果时间小于帧时间加延时，则获取剩余时间，继续显示当前的帧
            if (time < is->frame_timer + delay) {
                // 获取剩余时间
                *remaining_time = FFMIN(is->frame_timer + delay - time, *remaining_time);
                goto display;
            }
            // 计算帧的计时器
            is->frame_timer += delay;
            // 判断当前的时间是否大于同步阈值，如果大于，则使用当前的时间作为帧的计时器
            if (delay > 0 && time - is->frame_timer > AV_SYNC_THRESHOLD_MAX) {
                is->frame_timer = time;
            }

            // 更新显示时间戳
            Cain_LockMutex(is->pictq.mutex);
            if (!isnan(vp->pts)) {
                update_video_pts(is, vp->pts, vp->pos, vp->serial);
            }
            Cain_UnlockMutex(is->pictq.mutex);

            // 判断是否还有剩余的帧
            if (frame_queue_nb_remaining(&is->pictq) > 1) {
                // 取得下一帧
                Frame *nextvp = frame_queue_peek_next(&is->pictq);
                // 取得下一帧的时长
                duration = vp_duration(is, vp, nextvp);
                // 判断是否需要丢弃一部分帧
                if(!is->step && (framedrop>0 || (framedrop && get_master_sync_type(is) != AV_SYNC_VIDEO_MASTER)) && time > is->frame_timer + duration){
                    is->frame_drops_late++;
                    frame_queue_next(&is->pictq);
                    goto retry;
                }
            }

            frame_queue_next(&is->pictq);
            is->force_refresh = 1;

            if (is->step && !is->paused) {
                stream_toggle_pause(is);
            }
        }
display:
        /* display picture */
        // 显示视频画面
        if (!display_disable && is->video_st && is->force_refresh
            && is->show_mode == SHOW_MODE_VIDEO && is->pictq.rindex_shown) {
            video_image_display(is);
        }
    }
    is->force_refresh = 0;
    if (show_status) {
        static int64_t last_time;
        int64_t cur_time;
        int aqsize, vqsize, sqsize;
        double av_diff;

        cur_time = av_gettime_relative();
        if (!last_time || (cur_time - last_time) >= 30000) {
            aqsize = 0;
            vqsize = 0;
            sqsize = 0;
            if (is->audio_st) {
                aqsize = is->audioq.size;
            }
            if (is->video_st) {
                vqsize = is->videoq.size;
            }
            if (is->subtitle_st) {
                sqsize = is->subtitleq.size;
            }
            av_diff = 0;
            if (is->audio_st && is->video_st) {
                av_diff = get_clock(&is->audclk) - get_clock(&is->vidclk);
            } else if (is->video_st) {
                av_diff = get_master_clock(is) - get_clock(&is->vidclk);
            } else if (is->audio_st) {
                av_diff = get_master_clock(is) - get_clock(&is->audclk);
            }
            av_log(NULL, AV_LOG_INFO,
                   "%7.2f %s:%7.3f fd=%4d aq=%5dKB vq=%5dKB sq=%5dB f=%"PRId64"/%"PRId64"   \r",
                   get_master_clock(is),
                   (is->audio_st && is->video_st) ? "A-V" : (is->video_st ? "M-V" : (is->audio_st ? "M-A" : "   ")),
                   av_diff,
                   is->frame_drops_early + is->frame_drops_late,
                   aqsize / 1024,
                   vqsize / 1024,
                   sqsize,
                   is->video_st ? is->viddec.avctx->pts_correction_num_faulty_dts : 0,
                   is->video_st ? is->viddec.avctx->pts_correction_num_faulty_pts : 0);
            fflush(stdout);
            last_time = cur_time;
        }
    }
}

/**
 *  将已经解码帧压入队列
 * @param is
 * @param src_frame
 * @param pts
 * @param duration
 * @param pos
 * @param serial
 * @return
 */
int CainPlayer::queue_picture(VideoState *is, AVFrame *src_frame, double pts,
                              double duration, int64_t pos, int serial) {
    Frame *vp;

    // 查找是否存在可写的帧
    if (!(vp = frame_queue_peek_writable(&is->pictq))) {
        return -1;
    }

    vp->sar = src_frame->sample_aspect_ratio;
    vp->uploaded = 0;

    vp->width = src_frame->width;
    vp->height = src_frame->height;
    vp->format = src_frame->format;

    vp->pts = pts;
    vp->duration = duration;
    vp->pos = pos;
    vp->serial = serial;

    // TODO 设置大小
//    set_default_window_size(vp->width, vp->height, vp->sar);

    // 复制对象数据
    av_frame_move_ref(vp->frame, src_frame);
    // 将已解码帧对象入队
    frame_queue_push(&is->pictq);
    return 0;
}

/**
 * 获取视频帧
 * @param is
 * @param frame
 * @return
 */
int CainPlayer::get_video_frame(VideoState *is, AVFrame *frame) {
    int got_picture;
    // 视频解码
    if ((got_picture = decoder_decode_frame(&is->viddec, frame, NULL)) < 0) {
        return -1;
    }
    // 判断是否解码成功
    if (got_picture) {
        double dpts = NAN;

        if (frame->pts != AV_NOPTS_VALUE) {
            dpts = av_q2d(is->video_st->time_base) * frame->pts;
        }
        // 计算长宽比
        frame->sample_aspect_ratio = av_guess_sample_aspect_ratio(is->ic, is->video_st, frame);
        // 判断是否需要舍弃该帧
        if (framedrop > 0 || (framedrop && get_master_sync_type(is) != AV_SYNC_VIDEO_MASTER)) {
            if (frame->pts != AV_NOPTS_VALUE) {
                double diff = dpts - get_master_clock(is);
                if (!isnan(diff) && fabs(diff) < AV_NOSYNC_THRESHOLD &&
                    diff - is->frame_last_filter_delay < 0 &&
                    is->viddec.pkt_serial == is->vidclk.serial &&
                    is->videoq.nb_packets) {
                    is->frame_drops_early++;
                    av_frame_unref(frame);
                    got_picture = 0;
                }
            }
        }
    }

    // 返回解码结果
    return got_picture;
}


/**
 * 解码器开始，创建一个新线程
 * @param d     解码器
 * @param fn    绑定的线程执行函数
 * @param arg   数据，这里是CainPlayer对象
 * @return      返回0时，正常创建，否则返回-1
 */
int CainPlayer::decoder_start(Decoder *d, int (*fn)(void *), void *arg) {
    // 待解码队列开始
    packet_queue_start(d->queue, flush_pkt);
    // 创建解码线程
    CainThread thread;
    d->decoder_tid = Cain_CreateThread(&thread, fn, arg, "decoder");
    if (!d->decoder_tid) {
        av_log(NULL, AV_LOG_ERROR, "SDL_CreateThread(): %s\n", Cain_GetError());
        return AVERROR(ENOMEM);
    }
    return 0;
}

/**
 * 同步音频
 * @param is
 * @param nb_samples
 * @return
 */
int CainPlayer::synchronize_audio(VideoState *is, int nb_samples) {
    int wanted_nb_samples = nb_samples;

    /* if not master, then we try to remove or add samples to correct the clock */
    // 如果不是以音频同步，则尝试通过移除或增加采样来纠正时钟
    if (get_master_sync_type(is) != AV_SYNC_AUDIO_MASTER) {
        double diff, avg_diff;
        int min_nb_samples, max_nb_samples;
        // 获取音频时钟跟主时钟的差值
        diff = get_clock(&is->audclk) - get_master_clock(is);
        // 判断差值是否存在，并且在非同步阈值范围内
        if (!isnan(diff) && fabs(diff) < AV_NOSYNC_THRESHOLD) {
            // 计算新的差值
            is->audio_diff_cum = diff + is->audio_diff_avg_coef * is->audio_diff_cum;
            // 记录差值的数量
            if (is->audio_diff_avg_count < AUDIO_DIFF_AVG_NB) {
                /* not enough measures to have a correct estimate */
                is->audio_diff_avg_count++;
            } else {
                /* estimate the A-V difference */
                // 估计音频和视频的时钟差值
                avg_diff = is->audio_diff_cum * (1.0 - is->audio_diff_avg_coef);
                // 判断平均差值是否超过了音频差的阈值，如果超过，则计算新的采样值
                if (fabs(avg_diff) >= is->audio_diff_threshold) {
                    wanted_nb_samples = nb_samples + (int)(diff * is->audio_src.freq);
                    min_nb_samples = ((nb_samples * (100 - SAMPLE_CORRECTION_PERCENT_MAX) / 100));
                    max_nb_samples = ((nb_samples * (100 + SAMPLE_CORRECTION_PERCENT_MAX) / 100));
                    wanted_nb_samples = av_clip(wanted_nb_samples, min_nb_samples, max_nb_samples);
                }
                av_log(NULL, AV_LOG_TRACE, "diff=%f adiff=%f sample_diff=%d apts=%0.3f %f\n",
                       diff, avg_diff, wanted_nb_samples - nb_samples,
                       is->audio_clock, is->audio_diff_threshold);
            }
        } else { // 如果差值过大，重置防止pts出错
            /* too big difference : may be initial PTS errors, so
               reset A-V filter */
            is->audio_diff_avg_count = 0;
            is->audio_diff_cum       = 0;
        }
    }

    return wanted_nb_samples;
}

/**
 *  从解码后的音频缓存队列中读取一帧，并做重采样处理(转码、变声、变速等操作)
 * @param is
 * @return
 */
int CainPlayer::audio_decode_frame(VideoState *is) {
    int data_size, resampled_data_size;
    int64_t dec_channel_layout;
    av_unused double audio_clock0;
    int wanted_nb_samples;
    Frame *af;

    // 处于暂停状态，直接返回
    if (is->paused) {
        return -1;
    }

    do {
        // 判断已解码的缓存队列是否可读
        if (!(af = frame_queue_peek_readable(&is->sampq))) {
            return -1;
        }
        // 缓存队列的下一帧
        frame_queue_next(&is->sampq);
    } while (af->serial != is->audioq.serial);
    // 获取数据的大小
    data_size = av_samples_get_buffer_size(NULL, av_frame_get_channels(af->frame),
                                           af->frame->nb_samples,
                                           (AVSampleFormat) af->frame->format, 1);
    // 解码的声道格式
    dec_channel_layout =
            (af->frame->channel_layout && av_frame_get_channels(af->frame) == av_get_channel_layout_nb_channels(af->frame->channel_layout)) ?
            af->frame->channel_layout : av_get_default_channel_layout(av_frame_get_channels(af->frame));

    // 同步音频并获取采样的大小
    wanted_nb_samples = synchronize_audio(is, af->frame->nb_samples);

    // 如果跟源音频的格式、声道格式、采样率、采样大小等不相同，则需要做重采样处理
    if (af->frame->format        != is->audio_src.fmt            ||
        dec_channel_layout       != is->audio_src.channel_layout ||
        af->frame->sample_rate   != is->audio_src.freq           ||
        (wanted_nb_samples       != af->frame->nb_samples && !is->swr_ctx)) {
        // 释放旧的重采样上下文
        swr_free(&is->swr_ctx);
        // 重新创建重采样上下文
        is->swr_ctx = swr_alloc_set_opts(NULL,
                                         is->audio_tgt.channel_layout, is->audio_tgt.fmt,
                                         is->audio_tgt.freq, dec_channel_layout,
                                         (AVSampleFormat) af->frame->format,
                                         af->frame->sample_rate, 0, NULL);
        // 判断是否创建成功
        if (!is->swr_ctx || swr_init(is->swr_ctx) < 0) {
            av_log(NULL, AV_LOG_ERROR,
                   "Cannot create sample rate converter for conversion of %d Hz %s %d channels to %d Hz %s %d channels!\n",
                   af->frame->sample_rate,
                   av_get_sample_fmt_name((AVSampleFormat) af->frame->format),
                   av_frame_get_channels(af->frame),
                   is->audio_tgt.freq,
                   av_get_sample_fmt_name(is->audio_tgt.fmt),
                   is->audio_tgt.channels);
            swr_free(&is->swr_ctx);
            return -1;
        }

        // 重新设置音频的声道数、声道格式、采样频率、采样格式等
        is->audio_src.channel_layout = dec_channel_layout;
        is->audio_src.channels       = av_frame_get_channels(af->frame);
        is->audio_src.freq = af->frame->sample_rate;
        is->audio_src.fmt = (AVSampleFormat) af->frame->format;
    }

    // 如果重采样上下文存在，则进行重采样，否则直接复制当前的数据
    if (is->swr_ctx) {
        const uint8_t **in = (const uint8_t **)af->frame->extended_data;
        uint8_t **out = &is->audio_buf1;
        int out_count = (int64_t)wanted_nb_samples * is->audio_tgt.freq / af->frame->sample_rate + 256;
        int out_size  = av_samples_get_buffer_size(NULL, is->audio_tgt.channels, out_count, is->audio_tgt.fmt, 0);
        int len2;
        if (out_size < 0) {
            av_log(NULL, AV_LOG_ERROR, "av_samples_get_buffer_size() failed\n");
            return -1;
        }
        // 如果想要的采样大小跟帧的采样大小，需要做补偿处理
        if (wanted_nb_samples != af->frame->nb_samples) {
            if (swr_set_compensation(is->swr_ctx, (wanted_nb_samples - af->frame->nb_samples) * is->audio_tgt.freq / af->frame->sample_rate,
                                     wanted_nb_samples * is->audio_tgt.freq / af->frame->sample_rate) < 0) {
                av_log(NULL, AV_LOG_ERROR, "swr_set_compensation() failed\n");
                return -1;
            }
        }
        av_fast_malloc(&is->audio_buf1, &is->audio_buf1_size, out_size);
        if (!is->audio_buf1) {
            return AVERROR(ENOMEM);
        }
        // 音频重采样
        len2 = swr_convert(is->swr_ctx, out, out_count, in, af->frame->nb_samples);
        if (len2 < 0) {
            av_log(NULL, AV_LOG_ERROR, "swr_convert() failed\n");
            return -1;
        }

        // 音频buffer缓冲太小了？
        if (len2 == out_count) {
            av_log(NULL, AV_LOG_WARNING, "audio buffer is probably too small\n");
            if (swr_init(is->swr_ctx) < 0)
                swr_free(&is->swr_ctx);
        }

        // 设置音频缓冲
        is->audio_buf = is->audio_buf1;
        // 计算重采样后的大小
        resampled_data_size = len2 * is->audio_tgt.channels * av_get_bytes_per_sample(is->audio_tgt.fmt);

        // TODO 这里可以做变声变速处理

    } else {
        is->audio_buf = af->frame->data[0];
        resampled_data_size = data_size;
    }

    audio_clock0 = is->audio_clock;
    /* update the audio clock with the pts */
    // 判断audioFrame 的pts 是否存在，如果存在，则更新音频时钟，否则置为无穷大(NAN)
    if (!isnan(af->pts)) {
        is->audio_clock = af->pts + (double) af->frame->nb_samples / af->frame->sample_rate;
    } else {
        is->audio_clock = NAN;
    }
    // 更新音频时钟序列
    is->audio_clock_serial = af->serial;

    // 打印输出
    {
        static double last_clock;
        ALOGD("audio: delay=%0.3f clock=%0.3f clock0=%0.3f\n",
               is->audio_clock - last_clock,
               is->audio_clock, audio_clock0);
        last_clock = is->audio_clock;
    }
    // 返回重采样数据大小
    return resampled_data_size;
}

/**
 * 音频回调
 * @param opaque
 * @param stream
 * @param len
 */
static void audio_callback(void *opaque, uint8_t *stream, int len) {
    CainPlayer *player = (CainPlayer *) opaque;
    // 执行回调
    player->audioCallbackProcess(stream, len);
}

/**
 * 音频回调执行方法
 * @param stream
 * @param len
 */
void CainPlayer::audioCallbackProcess(uint8_t *stream, int len) {
    int audio_size, len1;

    audio_callback_time = av_gettime_relative();

    // TODO 播放速度发生变化时，这里需要设置PlaybackRate

    while (len > 0) {
        if (is->audio_buf_index >= is->audio_buf_size) {
            // 取得解码帧
            audio_size = audio_decode_frame(is);
            //  如果不存在音频帧，则输出静音
            if (audio_size < 0) {
                /* if error, just output silence */
                is->audio_buf = NULL;
                is->audio_buf_size = SDL_AUDIO_MIN_BUFFER_SIZE / is->audio_tgt.frame_size
                                     * is->audio_tgt.frame_size;
            } else {
                // TODO 显示音频波形
//                if (is->show_mode != SHOW_MODE_VIDEO) {
//                    update_sample_display(is, (int16_t *) is->audio_buf, audio_size);
//                }

                is->audio_buf_size = audio_size;
            }
            is->audio_buf_index = 0;
        }
        len1 = is->audio_buf_size - is->audio_buf_index;
        if (len1 > len)
            len1 = len;
        // 如果不处于静音模式并且声音最大
        if (!is->muted && is->audio_buf && is->audio_volume == MIX_MAXVOLUME) {
            memcpy(stream, (uint8_t *) is->audio_buf + is->audio_buf_index, len1);
        } else {
            memset(stream, 0, len1);
            // 非静音、并且音量不是最大，则需要混音
            if (!is->muted && is->audio_buf) {
                // TODO 混音
//                Cain_MixAudio(stream, (uint8_t *) is->audio_buf + is->audio_buf_index, len1,
//                             is->audio_volume);
            }
        }
        len -= len1;
        stream += len1;
        is->audio_buf_index += len1;
    }
    is->audio_write_buf_size = is->audio_buf_size - is->audio_buf_index;
    /* Let's assume the audio driver that is used by SDL has two periods. */
    if (!isnan(is->audio_clock)) {
        set_clock_at(&is->audclk, is->audio_clock - (double)(2 * is->audio_hw_buf_size + is->audio_write_buf_size) / is->audio_tgt.bytes_per_sec, is->audio_clock_serial, audio_callback_time / 1000000.0);
        sync_clock_to_slave(&is->extclk, &is->audclk);
    }
}

/**
 * 打开音频 TODO 暂未实现
 * @param opaque
 * @param wanted_channel_layout
 * @param wanted_nb_channels
 * @param wanted_sample_rate
 * @param audio_hw_params
 * @return
 */
int CainPlayer::audio_open(void *opaque, int64_t wanted_channel_layout, int wanted_nb_channels,
               int wanted_sample_rate, struct AudioParams *audio_hw_params) {

    CainAudioSpec wanted_spec, spec;
    static const int next_nb_channels[] = {0, 0, 1, 6, 2, 6, 4, 6};
    static const int next_sample_rates[] = {0, 44100, 48000};
    int next_sample_rate_idx = FF_ARRAY_ELEMS(next_sample_rates) - 1;

    if (!wanted_channel_layout || wanted_nb_channels != av_get_channel_layout_nb_channels(wanted_channel_layout)) {
        wanted_channel_layout = av_get_default_channel_layout(wanted_nb_channels);
        wanted_channel_layout &= ~AV_CH_LAYOUT_STEREO_DOWNMIX;
    }
    wanted_nb_channels = av_get_channel_layout_nb_channels(wanted_channel_layout);
    wanted_spec.channels = wanted_nb_channels;
    wanted_spec.freq = wanted_sample_rate;
    if (wanted_spec.freq <= 0 || wanted_spec.channels <= 0) {
        av_log(NULL, AV_LOG_ERROR, "Invalid sample rate or channel count!\n");
        return -1;
    }
    while (next_sample_rate_idx && next_sample_rates[next_sample_rate_idx] >= wanted_spec.freq) {
        next_sample_rate_idx--;
    }
    wanted_spec.format = AUDIO_S16SYS;
    wanted_spec.silence = 0;
    wanted_spec.samples = FFMAX(SDL_AUDIO_MIN_BUFFER_SIZE, 2 << av_log2(wanted_spec.freq / SDL_AUDIO_MAX_CALLBACKS_PER_SEC));
    wanted_spec.callback = audio_callback;
    wanted_spec.userdata = this;
    // 打开音频播放器
    while (Cain_OpenAudio(&wanted_spec, &spec) < 0) {
        // 如果请求取消，则退出循环
        if (is->abort_request) {
            return -1;
        }
        av_log(NULL, AV_LOG_WARNING, "SDL_OpenAudio (%d channels, %d Hz): %s\n",
               wanted_spec.channels, wanted_spec.freq, Cain_GetError());
        // 获取声道数
        wanted_spec.channels = next_nb_channels[FFMIN(7, wanted_spec.channels)];
        // 判断声道数量是否存在，如果不存在，则需要重新设置
        if (!wanted_spec.channels) {
            wanted_spec.freq = next_sample_rates[next_sample_rate_idx--];
            wanted_spec.channels = wanted_nb_channels;
            if (!wanted_spec.freq) {
                av_log(NULL, AV_LOG_ERROR,
                       "No more combinations to try, audio open failed\n");
                return -1;
            }
        }
        wanted_channel_layout = av_get_default_channel_layout(wanted_spec.channels);
    }
    // 判断声道格式是不是16bit的
    if (spec.format != AUDIO_S16SYS) {
        av_log(NULL, AV_LOG_ERROR,
               "SDL advised audio format %d is not supported!\n", spec.format);
        return -1;
    }
    // 声道是否跟期望值相同，不相同则需要获取声道格式(channel_layout)
    if (spec.channels != wanted_spec.channels) {
        wanted_channel_layout = av_get_default_channel_layout(spec.channels);
        if (!wanted_channel_layout) {
            av_log(NULL, AV_LOG_ERROR,
                   "SDL advised channel count %d is not supported!\n", spec.channels);
            return -1;
        }
    }
    // 设置音频硬件参数
    audio_hw_params->fmt = AV_SAMPLE_FMT_S16;
    audio_hw_params->freq = spec.freq;
    audio_hw_params->channel_layout = wanted_channel_layout;
    audio_hw_params->channels =  spec.channels;
    audio_hw_params->frame_size = av_samples_get_buffer_size(NULL, audio_hw_params->channels, 1,
                                                             audio_hw_params->fmt, 1);
    audio_hw_params->bytes_per_sec = av_samples_get_buffer_size(NULL, audio_hw_params->channels,
                                                                audio_hw_params->freq,
                                                                audio_hw_params->fmt, 1);

    if (audio_hw_params->bytes_per_sec <= 0 || audio_hw_params->frame_size <= 0) {
        av_log(NULL, AV_LOG_ERROR, "av_samples_get_buffer_size failed\n");
        return -1;
    }
    return spec.size;
}

/**
 * 音频解码线程
 * @param arg
 * @return
 */
static int audio_thread(void *arg) {
    CainPlayer *player = (CainPlayer *)arg;
    return player->audioDecode();
}

/**
 * 视频解码线程
 * @param arg
 * @return
 */
static int video_thread(void *arg) {
    CainPlayer *player = (CainPlayer *)arg;
    return player->videoDecode();
}

/**
 * 字幕解码线程
 * @param arg
 * @return
 */
static int subtitle_thread(void *arg) {
    CainPlayer *player = (CainPlayer *)arg;
    return player->subtitleDecode();
}

/**
 * 打开码流
 * @param is
 * @param stream_index
 * @return
 */
int CainPlayer::stream_component_open(VideoState *is, int stream_index) {
    AVFormatContext *ic = is->ic;
    AVCodecContext *avctx;
    AVCodec *codec;
    const char *forced_codec_name = NULL;
    AVDictionary *opts = NULL;
    AVDictionaryEntry *t = NULL;
    int sample_rate, nb_channels;
    int64_t channel_layout;
    int ret = 0;
    int stream_lowres = lowres;

    if (stream_index < 0 || stream_index >= ic->nb_streams) {
        return -1;
    }

    // 1、创建解码上下文
    avctx = avcodec_alloc_context3(NULL);
    if (!avctx)
        return AVERROR(ENOMEM);
    // 2、复制解码器信息到解码上下文
    ret = avcodec_parameters_to_context(avctx, ic->streams[stream_index]->codecpar);
    if (ret < 0) {
        goto fail;
    }
    // 时间基准
    av_codec_set_pkt_timebase(avctx, ic->streams[stream_index]->time_base);
    // 3、查找解码器
    codec = avcodec_find_decoder(avctx->codec_id);
    // 4、判断解码器类型，设置流的索引并根据类型设置解码名称
    switch(avctx->codec_type) {
        // 音频流
        case AVMEDIA_TYPE_AUDIO:
            is->last_audio_stream = stream_index;
            forced_codec_name = audio_codec_name;
            break;

        // 字幕流
        case AVMEDIA_TYPE_SUBTITLE:
            is->last_subtitle_stream = stream_index;
            forced_codec_name = subtitle_codec_name;
            break;

        // 视频流
        case AVMEDIA_TYPE_VIDEO:
            is->last_video_stream = stream_index;
            forced_codec_name = video_codec_name;
            break;
    }
    // 5、是否需要强制查找解码器，并判断解码器是否存在
    if (forced_codec_name) {
        codec = avcodec_find_decoder_by_name(forced_codec_name);
    }
    if (!codec) {
        if (forced_codec_name) {
            av_log(NULL, AV_LOG_WARNING,
                   "No codec could be found with name '%s'\n", forced_codec_name);
        } else {
            av_log(NULL, AV_LOG_WARNING,
                   "No codec could be found with id %d\n", avctx->codec_id);
        }
        ret = AVERROR(EINVAL);
        goto fail;
    }
    // 6、设置解码器的Id
    avctx->codec_id = codec->id;
    // 判断是否需要重新设置lowres的值
    if (stream_lowres > av_codec_get_max_lowres(codec)) {
        av_log(avctx, AV_LOG_WARNING, "The maximum value for lowres supported by the decoder is %d\n",
               av_codec_get_max_lowres(codec));
        stream_lowres = av_codec_get_max_lowres(codec);
    }
    // 设置是否低分辨率解码
    av_codec_set_lowres(avctx, stream_lowres);

#if FF_API_EMU_EDGE
    if (stream_lowres) {
        avctx->flags |= CODEC_FLAG_EMU_EDGE;
    }
#endif
    if (fast) {
        avctx->flags2 |= AV_CODEC_FLAG2_FAST; // 允许非规范兼容加速技巧
    }
#if FF_API_EMU_EDGE
    if (codec->capabilities & AV_CODEC_CAP_DR1) {
        avctx->flags |= CODEC_FLAG_EMU_EDGE;
    }
#endif
    // 7、设置解码参数
    opts = filter_codec_opts(codec_opts, avctx->codec_id, ic, ic->streams[stream_index], codec);

    // 设置多线程解码参数
    if (!av_dict_get(opts, "threads", NULL, 0)) {
        av_dict_set(&opts, "threads", "auto", 0);
    }

    // 是否低分辨率解码
    if (stream_lowres) {
        av_dict_set_int(&opts, "lowres", stream_lowres, 0);
    }

    // 设置音频和视频解码参数
    // 解码时，当AVCodecContext的refcounted_frames字段为0，则frame的分配与释放由ffmpeg内部自己控制
    if (avctx->codec_type == AVMEDIA_TYPE_VIDEO || avctx->codec_type == AVMEDIA_TYPE_AUDIO) {
        av_dict_set(&opts, "refcounted_frames", "1", 0);
    }

    // 8、打开解码器
    if ((ret = avcodec_open2(avctx, codec, &opts)) < 0) {
        goto fail;
    }

    if ((t = av_dict_get(opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
        av_log(NULL, AV_LOG_ERROR, "Option %s not found.\n", t->key);
        ret =  AVERROR_OPTION_NOT_FOUND;
        goto fail;
    }

    is->eof = 0;
    ic->streams[stream_index]->discard = AVDISCARD_DEFAULT;

    // 9、根据类型打开解码器
    switch (avctx->codec_type) {
        // 准备音频
        case AVMEDIA_TYPE_AUDIO:
            sample_rate    = avctx->sample_rate;
            nb_channels    = avctx->channels;
            channel_layout = avctx->channel_layout;
            /* prepare audio output */
            // 9.1.1、打开音频
            if ((ret = audio_open(this, channel_layout, nb_channels, sample_rate, &is->audio_tgt)) < 0) {
                goto fail;
            }
            // 设置音频参数
            is->audio_hw_buf_size = ret;
            is->audio_src = is->audio_tgt;
            is->audio_buf_size  = 0;
            is->audio_buf_index = 0;

            /* init averaging filter */
            is->audio_diff_avg_coef  = exp(log(0.01) / AUDIO_DIFF_AVG_NB);
            is->audio_diff_avg_count = 0;
            /* since we do not have a precise anough audio FIFO fullness,
               we correct audio sync only if larger than this threshold */
            is->audio_diff_threshold = (double)(is->audio_hw_buf_size) / is->audio_tgt.bytes_per_sec;
            is->audio_stream = stream_index;
            is->audio_st = ic->streams[stream_index];
            // 9.1.2、音频解码器初始化
            decoder_init(&is->auddec, avctx, &is->audioq, is->continue_read_thread);
            if ((is->ic->iformat->flags & (AVFMT_NOBINSEARCH | AVFMT_NOGENSEARCH | AVFMT_NO_BYTE_SEEK)) && !is->ic->iformat->read_seek) {
                is->auddec.start_pts = is->audio_st->start_time;
                is->auddec.start_pts_tb = is->audio_st->time_base;
            }
            // 9.1.3、音频解码队列和线程初始化
            if ((ret = decoder_start(&is->auddec, audio_thread, this)) < 0) {
                goto out;
            }

            Cain_PauseAudio(0);
            break;

        // 视频
        case AVMEDIA_TYPE_VIDEO:
            is->video_stream = stream_index;
            is->video_st = ic->streams[stream_index];
            // 9.2.1、视频解码器初始化
            decoder_init(&is->viddec, avctx, &is->videoq, is->continue_read_thread);
            // 9.2.2、视频解码队列和线程初始化
            if ((ret = decoder_start(&is->viddec, video_thread, this)) < 0) {
                goto out;
            }
            is->queue_attachments_req = 1;
            break;

        // 字幕
        case AVMEDIA_TYPE_SUBTITLE:
            is->subtitle_stream = stream_index;
            is->subtitle_st = ic->streams[stream_index];
            // 9.3.1、字幕解码器初始化
            decoder_init(&is->subdec, avctx, &is->subtitleq, is->continue_read_thread);
            // 9.3.2、字幕解码队列和线程初始化
            if ((ret = decoder_start(&is->subdec, subtitle_thread, this)) < 0)
                goto out;
            break;

        default:
            break;
    }
    goto out;

fail:
    avcodec_free_context(&avctx);

out:
    av_dict_free(&opts);

    return ret;
}

/**
 * 判断是否有足够的包
 * @param st
 * @param stream_id
 * @param queue
 * @return
 */
int CainPlayer::stream_has_enough_packets(AVStream *st, int stream_id, PacketQueue *queue) {
    return stream_id < 0 ||
           queue->abort_request ||
           (st->disposition & AV_DISPOSITION_ATTACHED_PIC) ||
           queue->nb_packets > MIN_FRAMES && (!queue->duration || av_q2d(st->time_base) * queue->duration > 1.0);
}

/**
 * 判断是否实时流
 * @param s
 * @return
 */
int CainPlayer::is_realtime(AVFormatContext *s) {

    if(!strcmp(s->iformat->name, "rtp") || !strcmp(s->iformat->name, "rtsp")
       || !strcmp(s->iformat->name, "sdp")) {
        return 1;
    }

    if(s->pb && (!strncmp(s->filename, "rtp:", 4) || !strncmp(s->filename, "udp:", 4))) {
        return 1;
    }
    return 0;
}

/**
 * 读线程
 * @param arg
 * @return
 */
static int read_thread(void *arg) {
    CainPlayer *player = (CainPlayer *)arg;
    return player->readAndDemuxing();
}

/**
 * 视频刷新线程
 * @param arg
 * @return
 */
static int video_refresh_thread(void *arg) {
    CainPlayer *player = (CainPlayer *) arg;
    return player->videoRefresh();
}

/**
 * 打开流，这里是prepare入口
 * @param filename
 * @param iformat
 * @return
 */
VideoState *CainPlayer::stream_open(const char *filename, AVInputFormat *iformat) {
    VideoState *is;
    is = (VideoState *) av_mallocz(sizeof(VideoState));
    if (!is)
        return NULL;
    is->filename = av_strdup(filename);
    if (!is->filename)
        goto fail;
    is->iformat = iformat;
    is->ytop = 0;
    is->xleft = 0;

    // TODO 创建SoundTouch对象，用于处理音频变声变速变调


    // 1、创建视频、字幕和音频的已解码队列
    if (frame_queue_init(&is->pictq, &is->videoq, VIDEO_PICTURE_QUEUE_SIZE, 1) < 0) {
        goto fail;
    }
    if (frame_queue_init(&is->subpq, &is->subtitleq, SUBPICTURE_QUEUE_SIZE, 0) < 0) {
        goto fail;
    }
    if (frame_queue_init(&is->sampq, &is->audioq, SAMPLE_QUEUE_SIZE, 1) < 0) {
        goto fail;
    }

    // 2、创建视频、字幕和音频的待解码队列
    if (packet_queue_init(&is->videoq) < 0 ||
        packet_queue_init(&is->audioq) < 0 ||
        packet_queue_init(&is->subtitleq) < 0) {
        goto fail;
    }
    // 创建读条件锁
    if (!(is->continue_read_thread = Cain_CreateCond())) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateCond(): %s\n", Cain_GetError());
        goto fail;
    }

    // 初始化时钟
    init_clock(&is->vidclk, &is->videoq.serial);
    init_clock(&is->audclk, &is->audioq.serial);
    init_clock(&is->extclk, &is->extclk.serial);
    is->audio_clock_serial = -1;

    // 初始化音量
    if (startup_volume < 0) {
        av_log(NULL, AV_LOG_WARNING, "-volume=%d < 0, setting to 0\n", startup_volume);
    }
    if (startup_volume > 100) {
        av_log(NULL, AV_LOG_WARNING, "-volume=%d > 100, setting to 100\n", startup_volume);
    }
    startup_volume = av_clip(startup_volume, 0, 100);
    startup_volume = av_clip(MIX_MAXVOLUME * startup_volume / 100, 0, MIX_MAXVOLUME);
    is->audio_volume = startup_volume;
    // 是否静音
    is->muted = 0;
    is->av_sync_type = av_sync_type;

    // 创建刷新线程
    CainThread videoThread;
    is->video_refresh_tid = Cain_CreateThread(&videoThread, video_refresh_thread,
                                              this, "video_refresh_thread");
    // 判断刷新线程是否创建成功
    if (!is->video_refresh_tid) {
        goto fail;
    }

    // 3、创建读线程，该线程从文件/地址源源不断地读取数据
    CainThread thread;
    is->read_tid = Cain_CreateThread(&thread, read_thread, this, "read_thread");
    if (!is->read_tid) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateThread(): %s\n", Cain_GetError());
fail:
        // 如果打开流的某一步失败，都需要关闭流，释放所有资源
        stream_close(is);
        return NULL;
    }
    return is;
}

/**
 * 解码中断回调
 * @param ctx
 * @return
 */
static int decode_interrupt_cb(void *ctx) {
    VideoState *is = (VideoState *)ctx;
    return is->abort_request;
}

/**
 * 读文件/解复用操作
 * @return
 */
int CainPlayer::readAndDemuxing(void) {
    AVFormatContext *ic = NULL;
    int err, i, ret;
    int st_index[AVMEDIA_TYPE_NB];
    AVPacket pkt1, *pkt = &pkt1;
    int64_t stream_start_time;
    int pkt_in_play_range = 0;
    AVDictionaryEntry *t;
    AVDictionary **opts;
    int orig_nb_streams;
    Mutex *wait_mutex = Cain_CreateMutex();
    int scan_all_pmts_set = 0;
    int64_t pkt_ts;
    if (!wait_mutex) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateMutex(): %s\n", Cain_GetError());
        ret = AVERROR(ENOMEM);
        goto fail;
    }

    // 初始化参数
    memset(st_index, -1, sizeof(st_index));
    is->last_video_stream = is->video_stream = -1;
    is->last_audio_stream = is->audio_stream = -1;
    is->last_subtitle_stream = is->subtitle_stream = -1;
    is->eof = 0;

    // 1、创建输入上下文
    ic = avformat_alloc_context();
    if (!ic) {
        av_log(NULL, AV_LOG_FATAL, "Could not allocate context.\n");
        ret = AVERROR(ENOMEM);
        goto fail;
    }
    // 2、设置解码中断回调方法
    ic->interrupt_callback.callback = decode_interrupt_cb;
    // 设置中断回调参数
    ic->interrupt_callback.opaque = is;
    // 获取参数
    if (!av_dict_get(format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE)) {
        av_dict_set(&format_opts, "scan_all_pmts", "1", AV_DICT_DONT_OVERWRITE);
        scan_all_pmts_set = 1;
    }
    // 3、打开文件
    err = avformat_open_input(&ic, is->filename, is->iformat, &format_opts);
    if (err < 0) {
        print_error(is->filename, err);
        ret = -1;
        goto fail;
    }
    if (scan_all_pmts_set)
        av_dict_set(&format_opts, "scan_all_pmts", NULL, AV_DICT_MATCH_CASE);

    if ((t = av_dict_get(format_opts, "", NULL, AV_DICT_IGNORE_SUFFIX))) {
        av_log(NULL, AV_LOG_ERROR, "Option %s not found.\n", t->key);
    }
    is->ic = ic;

    if (genpts) {
        ic->flags |= AVFMT_FLAG_GENPTS;
    }

    av_format_inject_global_side_data(ic);

    opts = setup_find_stream_info_opts(ic, codec_opts);
    orig_nb_streams = ic->nb_streams;

    err = avformat_find_stream_info(ic, opts);

    // 释放参数使用的对象
    for (i = 0; i < orig_nb_streams; i++) {
        av_dict_free(&opts[i]);
    }
    av_freep(&opts);

    if (err < 0) {
        av_log(NULL, AV_LOG_WARNING,
               "%s: could not find codec parameters\n", is->filename);
        ret = -1;
        goto fail;
    }

    if (ic->pb) {
        ic->pb->eof_reached = 0; // FIXME hack, ffplay maybe should not use avio_feof() to test for the end
    }

    if (seek_by_bytes < 0) {
        seek_by_bytes = !!(ic->iformat->flags & AVFMT_TS_DISCONT) && strcmp("ogg", ic->iformat->name);
    }
    is->max_frame_duration = (ic->iformat->flags & AVFMT_TS_DISCONT) ? 10.0 : 3600.0;

    /* if seeking requested, we execute it */
    // 如果开始时间不为0，则表示需要定位到实际的具体位置
    if (start_time != AV_NOPTS_VALUE) {
        int64_t timestamp;

        timestamp = start_time;
        /* add the stream start time */
        if (ic->start_time != AV_NOPTS_VALUE)
            timestamp += ic->start_time;
        // 定位文件
        ret = avformat_seek_file(ic, -1, INT64_MIN, timestamp, INT64_MAX, 0);
        if (ret < 0) {
            av_log(NULL, AV_LOG_WARNING, "%s: could not seek to position %0.3f\n",
                   is->filename, (double)timestamp / AV_TIME_BASE);
        }
    }
    //  判断是否实时流
    is->realtime = is_realtime(ic);
    // 打印信息
    if (show_status) {
        av_dump_format(ic, 0, is->filename, 0);
    }
    // 4、获取码流对应的索引
    for (i = 0; i < ic->nb_streams; i++) {
        AVStream *st = ic->streams[i];
        enum AVMediaType type = st->codecpar->codec_type;
        st->discard = AVDISCARD_ALL;
        if (type >= 0 && wanted_stream_spec[type] && st_index[type] == -1)
            if (avformat_match_stream_specifier(ic, st, wanted_stream_spec[type]) > 0)
                st_index[type] = i;
    }
    for (i = 0; i < AVMEDIA_TYPE_NB; i++) {
        if (wanted_stream_spec[i] && st_index[i] == -1) {
            av_log(NULL, AV_LOG_ERROR, "Stream specifier %s does not match any %s stream\n", wanted_stream_spec[i], av_get_media_type_string(
                    (AVMediaType) i));
            st_index[i] = INT_MAX;
        }
    }

    // 5、查找视频流
    if (!video_disable) {
        st_index[AVMEDIA_TYPE_VIDEO] =
                av_find_best_stream(ic, AVMEDIA_TYPE_VIDEO,
                                    st_index[AVMEDIA_TYPE_VIDEO], -1, NULL, 0);
    }

    // 6、查找音频流
    if (!audio_disable) {
        st_index[AVMEDIA_TYPE_AUDIO] =
                av_find_best_stream(ic, AVMEDIA_TYPE_AUDIO,
                                    st_index[AVMEDIA_TYPE_AUDIO],
                                    st_index[AVMEDIA_TYPE_VIDEO],
                                    NULL, 0);
    }

    // 7、查找字幕流
    if (!video_disable && !subtitle_disable) {
        st_index[AVMEDIA_TYPE_SUBTITLE] =
                av_find_best_stream(ic, AVMEDIA_TYPE_SUBTITLE,
                                    st_index[AVMEDIA_TYPE_SUBTITLE],
                                    (st_index[AVMEDIA_TYPE_AUDIO] >= 0 ?
                                     st_index[AVMEDIA_TYPE_AUDIO] :
                                     st_index[AVMEDIA_TYPE_VIDEO]),
                                    NULL, 0);
    }
    // 设置显示模式
    is->show_mode = show_mode;

    /* open the streams */
    // 8、打开音频流
    if (st_index[AVMEDIA_TYPE_AUDIO] >= 0) {
        stream_component_open(is, st_index[AVMEDIA_TYPE_AUDIO]);
    }

    // 9、打开视频流
    ret = -1;
    if (st_index[AVMEDIA_TYPE_VIDEO] >= 0) {
        ret = stream_component_open(is, st_index[AVMEDIA_TYPE_VIDEO]);
    }
    // 设置显示视频还是自适应滤波
    if (is->show_mode == SHOW_MODE_NONE) {
        is->show_mode = ret >= 0 ? SHOW_MODE_VIDEO : SHOW_MODE_RDFT;
    }
    // 10、打开字幕流
    if (st_index[AVMEDIA_TYPE_SUBTITLE] >= 0) {
        stream_component_open(is, st_index[AVMEDIA_TYPE_SUBTITLE]);
    }

    // 如果音频流和视频流都不存在，则退出
    if (is->video_stream < 0 && is->audio_stream < 0) {
        av_log(NULL, AV_LOG_FATAL, "Failed to open file '%s' or configure filtergraph\n",
               is->filename);
        ret = -1;
        goto fail;
    }

    if (infinite_buffer < 0 && is->realtime)
        infinite_buffer = 1;
    // 11、不断从本地或IP地址读取数据包
    for (;;) {

        // 取消读，则退出循环
        if (is->abort_request) {
            break;
        }

        // 定位请求
        if (is->seek_req) {
            int64_t seek_target = is->seek_pos;
            int64_t seek_min    = is->seek_rel > 0 ? seek_target - is->seek_rel + 2: INT64_MIN;
            int64_t seek_max    = is->seek_rel < 0 ? seek_target - is->seek_rel - 2: INT64_MAX;
// FIXME the +-2 is due to rounding being not done in the correct direction in generation
//      of the seek_pos/seek_rel variables
            // 定位文件
            ret = avformat_seek_file(is->ic, -1, seek_min, seek_target, seek_max, is->seek_flags);
            if (ret < 0) {
                av_log(NULL, AV_LOG_ERROR,
                       "%s: error while seeking\n", is->ic->filename);
            } else {
                // 音频入队
                if (is->audio_stream >= 0) {
                    packet_queue_flush(&is->audioq);
                    packet_queue_put(&is->audioq, &flush_pkt, flush_pkt);
                }
                // 字幕入队
                if (is->subtitle_stream >= 0) {
                    packet_queue_flush(&is->subtitleq);
                    packet_queue_put(&is->subtitleq, &flush_pkt, flush_pkt);
                }
                // 视频入队
                if (is->video_stream >= 0) {
                    packet_queue_flush(&is->videoq);
                    packet_queue_put(&is->videoq, &flush_pkt, flush_pkt);
                }
                // 根据定位的标志设置时钟
                if (is->seek_flags & AVSEEK_FLAG_BYTE) {
                    set_clock(&is->extclk, NAN, 0);
                } else {
                    set_clock(&is->extclk, seek_target / (double)AV_TIME_BASE, 0);
                }
            }
            is->seek_req = 0;
            is->queue_attachments_req = 1;
            is->eof = 0;

            // TODO 暂停(paused)

        }
        if (is->queue_attachments_req) {
            if (is->video_st && is->video_st->disposition & AV_DISPOSITION_ATTACHED_PIC) {
                AVPacket copy;
                if ((ret = av_copy_packet(&copy, &is->video_st->attached_pic)) < 0)
                    goto fail;
                packet_queue_put(&is->videoq, &copy, flush_pkt);
                packet_queue_put_nullpacket(&is->videoq, is->video_stream, flush_pkt);
            }
            is->queue_attachments_req = 0;
        }

        /* if the queue are full, no need to read more */
        // 待解码数据写入队列失败，并且待解码数据还有足够的包时，等待待解码队列的数据消耗掉
        if (infinite_buffer<1 &&
            // MAX_QUEUE_SIZE
            (is->audioq.size + is->videoq.size + is->subtitleq.size > MAX_QUEUE_SIZE
             //            (is->audioq.size + is->videoq.size + is->subtitleq.size > dcc.max_buffer_size
             || (stream_has_enough_packets(is->audio_st, is->audio_stream, &is->audioq) &&
                 stream_has_enough_packets(is->video_st, is->video_stream, &is->videoq) &&
                 stream_has_enough_packets(is->subtitle_st, is->subtitle_stream, &is->subtitleq)))) {
            /* wait 10 ms */
            Cain_LockMutex(wait_mutex);
            Cain_CondWaitTimeout(is->continue_read_thread, wait_mutex, 10);
            Cain_UnlockMutex(wait_mutex);
            continue;
        }

        if (!is->paused &&
            (!is->audio_st || (is->auddec.finished == is->audioq.serial && frame_queue_nb_remaining(&is->sampq) == 0)) &&
            (!is->video_st || (is->viddec.finished == is->videoq.serial && frame_queue_nb_remaining(&is->pictq) == 0))) {
            if (loop != 1 && (!loop || --loop)) {
                stream_seek(is, start_time != AV_NOPTS_VALUE ? start_time : 0, 0, 0);
            } else if (autoexit) {
                ret = AVERROR_EOF;
                goto fail;
            }
        }
        // 读取数据包
        ret = av_read_frame(ic, pkt);
        if (ret < 0) {
            // 读取结束或失败
            if ((ret == AVERROR_EOF || avio_feof(ic->pb)) && !is->eof) {
                if (is->video_stream >= 0)
                    packet_queue_put_nullpacket(&is->videoq, is->video_stream, flush_pkt);
                if (is->audio_stream >= 0)
                    packet_queue_put_nullpacket(&is->audioq, is->audio_stream, flush_pkt);
                if (is->subtitle_stream >= 0)
                    packet_queue_put_nullpacket(&is->subtitleq, is->subtitle_stream, flush_pkt);
                is->eof = 1;
            }
            // 出错则直接退出循环
            if (ic->pb && ic->pb->error) {
                break;
            }
            Cain_LockMutex(wait_mutex);
            Cain_CondWaitTimeout(is->continue_read_thread, wait_mutex, 10);
            Cain_UnlockMutex(wait_mutex);
            continue;
        } else {
            is->eof = 0;
        }
        /* check if packet is in play range specified by user, then queue, otherwise discard */
        // 检查包是否在用户指定的播放范围内，在则入队，否则丢弃
        stream_start_time = ic->streams[pkt->stream_index]->start_time;
        pkt_ts = pkt->pts == AV_NOPTS_VALUE ? pkt->dts : pkt->pts;
        pkt_in_play_range = duration == AV_NOPTS_VALUE ||
                            (pkt_ts - (stream_start_time != AV_NOPTS_VALUE ? stream_start_time : 0)) *
                            av_q2d(ic->streams[pkt->stream_index]->time_base) -
                            (double)(start_time != AV_NOPTS_VALUE ? start_time : 0) / 1000000
                            <= ((double)duration / 1000000);

        // 将解复用得到的数据包添加到对应的待解码队列中
        if (pkt->stream_index == is->audio_stream && pkt_in_play_range) {
            packet_queue_put(&is->audioq, pkt, flush_pkt);
        } else if (pkt->stream_index == is->video_stream && pkt_in_play_range
                   && !(is->video_st->disposition & AV_DISPOSITION_ATTACHED_PIC)) {
            packet_queue_put(&is->videoq, pkt, flush_pkt);
        } else if (pkt->stream_index == is->subtitle_stream && pkt_in_play_range) {
            packet_queue_put(&is->subtitleq, pkt, flush_pkt);
        } else {
            av_packet_unref(pkt);
        }
    }
    ret = 0;

fail:
    if (ic && !is->ic)
        avformat_close_input(&ic);
    if (ret != 0) {
        // TODO 处理出错

    }
    Cain_DestroyMutex(wait_mutex);
    return 0;
}

/**
 * 视频解码线程执行实体
 * @return
 */
int CainPlayer::videoDecode(void) {
    AVFrame *frame = av_frame_alloc();
    double pts;
    double duration;
    int ret;
    AVRational tb = is->video_st->time_base;
    // 猜测视频帧率
    AVRational frame_rate = av_guess_frame_rate(is->ic, is->video_st, NULL);

    if (!frame) {
        return AVERROR(ENOMEM);
    }

    for (;;) {
        // 获得视频解码帧，如果失败，则直接释放，如果没有视频帧，则继续等待
        ret = get_video_frame(is, frame);
        // 如果出错，则直接退出解码过程
        if (ret < 0) {
            break;
        }
        // 如果没有找到需要解码的帧则继续等待
        if (!ret) {
            continue;
        }
        // 计算帧的pts、duration等
        duration = (frame_rate.num && frame_rate.den
                    ? av_q2d((AVRational) {frame_rate.den, frame_rate.num}) : 0);
        // 判断帧的PTS是否存在，如果不存在则设置为NAN
        pts = (frame->pts == AV_NOPTS_VALUE) ? NAN : frame->pts * av_q2d(tb);
        // 放入到已解码队列
        ret = queue_picture(is, frame, pts, duration, av_frame_get_pkt_pos(frame),
                            is->viddec.pkt_serial);
        // 释放局部变量
        av_frame_unref(frame);
        if (ret < 0) {
            break;
        }
    }
    // 释放 AVFrame
    av_frame_free(&frame);
    return 0;
}

/*
 * 音频解码线程执行实体
 */
int CainPlayer::audioDecode(void) {
    AVFrame *frame = av_frame_alloc();
    Frame *af;
    int got_frame = 0;
    AVRational tb;
    int ret = 0;

    if (!frame) {
        return AVERROR(ENOMEM);
    }

    do {
        // 解码音频帧
        if ((got_frame = decoder_decode_frame(&is->auddec, frame, NULL)) < 0) {
            break;
        }

        if (got_frame) {
            tb = (AVRational){1, frame->sample_rate};

            // TODO 精确定位/查找(seek)

            // 检查是否帧队列是否可写入，如果不可写入，则直接释放
            if (!(af = frame_queue_peek_writable(&is->sampq))) {
                break;
            }

            // 设定帧的pts、duration等参数
            af->pts = (frame->pts == AV_NOPTS_VALUE) ? NAN : frame->pts * av_q2d(tb);
            af->pos = av_frame_get_pkt_pos(frame);
            af->serial = is->auddec.pkt_serial;
            af->duration = av_q2d((AVRational){frame->nb_samples, frame->sample_rate});

            // 将解码后的音频帧压入解码后的音频队列
            av_frame_move_ref(af->frame, frame);
            frame_queue_push(&is->sampq);
        }

    } while (ret >= 0 || ret == AVERROR(EAGAIN) || ret == AVERROR_EOF);
    // 释放
    av_frame_free(&frame);
    return ret;
}

/**
 * 字幕解码线程执行实体
 * @return
 */
int CainPlayer::subtitleDecode() {
    // TODO 解码字幕
    return 0;
}

/**
 * 视频刷新线程执行实体
 * @return
 */
int CainPlayer::videoRefresh() {
    double remaining_time = 0.0;
    // 如果不取消的话，不断刷新画面
    while (!is->abort_request) {
        if (remaining_time > 0.0) {
            av_usleep((int) (int64_t) (remaining_time * 1000000.0));
        }
        remaining_time = REFRESH_RATE;
        if (is->show_mode != SHOW_MODE_NONE && (!is->paused || is->force_refresh)) {
            video_refresh(is, &remaining_time);
        }
    }

    return 0;
}