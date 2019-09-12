//
// Created by CainHuang on 2019/2/26.
//

#include "VideoCutEditor.h"

VideoCutEditor::VideoCutEditor(const char *srcUrl, const char *dstUrl)
                : MediaEditor(), mStart(0), mDuration(0), mSpeed(1.0) {
    this->srcUrl = av_strdup(srcUrl);
    this->dstUrl = av_strdup(dstUrl);
}

VideoCutEditor::~VideoCutEditor() {
    if (srcUrl != nullptr) {
        av_freep(&srcUrl);
        srcUrl = nullptr;
    }
    if (dstUrl != nullptr) {
        av_freep(&dstUrl);
        dstUrl = nullptr;
    }
}

void VideoCutEditor::setDuration(long start, long duration) {
    this->mStart = start;
    this->mDuration = duration;
}

void VideoCutEditor::setSpeed(float speed) {
    this->mSpeed = speed;
}

int VideoCutEditor::process() {
    AVOutputFormat *ofmt = NULL;
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket packet;
    int ret;
    int stream_index = 0;
    int *stream_mapping = NULL;
    int stream_mapping_size = 0;
    int seekFlag = 0;
    int audio_index = -1;
    int video_index = -1;

    av_register_all();

    // 打开输入文件
    if ((ret = openInputFile(srcUrl, &ifmt_ctx)) < 0) {
        LOGE("Could not open input file %s", srcUrl);
        return ret;
    }

    // 打开输出文件
    if ((ret = initOutput(dstUrl, &ofmt_ctx)) < 0) {
        LOGE("Could not create output context");
        return ret;
    }

    // 设置stream_mapping
    stream_mapping_size = ifmt_ctx->nb_streams;
    stream_mapping = (int*)av_mallocz_array((size_t)stream_mapping_size, sizeof(*stream_mapping));
    if (!stream_mapping){
        ret = AVERROR(ENOMEM);
        LOGE("Error while set stream_mapping");
        return ret;
    }

    ofmt = ofmt_ctx->oformat;

    // 查找媒体流索引
    for (int i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
        AVCodecParameters *in_codecpar = in_stream->codecpar;
        if (in_codecpar->codec_type != AVMEDIA_TYPE_AUDIO &&
            in_codecpar->codec_type != AVMEDIA_TYPE_VIDEO) {
            stream_mapping[i] = -1;
            continue;
        }
        stream_mapping[i] = stream_index++;

        // 查找音频和视频媒体流索引
        if (in_codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_index = i;
        } else if (in_codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_index = i;
        }

        if (!out_stream) {
            LOGE("Failed to create output stream");
            ret = AVERROR_UNKNOWN;
            return ret;
        }
        if ((ret = avcodec_parameters_copy(out_stream->codecpar, in_stream->codecpar)) < 0) {
            LOGE("Failed to copy codec parameters");
            return ret;
        }
        out_stream->codecpar->codec_tag = 0;
    }

    // 检查输出文件是否正确配置完成
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&ofmt_ctx->pb, dstUrl, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Could not open output file %s", dstUrl);
            return ret;
        }
    }

    // 写入文件头
    ret = avformat_write_header(ofmt_ctx, NULL);
    if (ret < 0) {
        LOGE("Error occurred while write header");
        return ret;
    }

    // 用于记录首帧的dts和pts，因为并不是所有的媒体流数据包都跟裁剪的时间一致。
    int64_t *dts_start_from = (int64_t *) malloc(sizeof(int64_t) * ifmt_ctx->nb_streams);
    memset(dts_start_from, 0, sizeof(int64_t) * ifmt_ctx->nb_streams);
    int64_t *pts_start_from = (int64_t *) malloc(sizeof(int64_t) * ifmt_ctx->nb_streams);
    memset(pts_start_from, 0, sizeof(int64_t) * ifmt_ctx->nb_streams);

    // 定位到起始位置，av_seek_frame 有可能会造成比较大的误差，这里要用avformat_seek_file来处理
    seekFlag &= ~AVSEEK_FLAG_BYTE;
    ret = avformat_seek_file(ifmt_ctx, -1, INT64_MIN, (int64_t) (mStart / 1000 * AV_TIME_BASE), INT64_MAX, seekFlag);
    if (ret < 0) {
        LOGE("\tError seek to the start");
        return ret;
    }

    // 开始写入视频信息
    // TODO 音频转码以及倍速处理后续再做
    while (1) {
        AVStream *in_stream, *out_stream;

        ret = av_read_frame(ifmt_ctx, &packet);
        if (ret < 0) {
            break;
        }


        in_stream = ifmt_ctx->streams[packet.stream_index];
        if (packet.stream_index >= stream_mapping_size ||
            stream_mapping[packet.stream_index] < 0) {
            av_packet_unref(&packet);
            continue;
        }

        packet.stream_index= stream_mapping[packet.stream_index];

        out_stream = ofmt_ctx->streams[packet.stream_index];

        av_dict_copy(&(out_stream->metadata), in_stream->metadata, AV_DICT_IGNORE_SUFFIX);

        //  计算是否超过时长
        if (packet.stream_index == audio_index || packet.stream_index == video_index) {
            if (av_q2d(in_stream->time_base) * packet.pts > (mStart + mDuration) / 1000) {
                av_packet_unref(&packet);
                break;
            }
        }

        // 记录首次dts
        if (dts_start_from[packet.stream_index] == 0) {
            dts_start_from[packet.stream_index] = packet.dts;
        }

        // 记录首次pts
        if (pts_start_from[packet.stream_index] == 0) {
            pts_start_from[packet.stream_index] = packet.pts;
        }

        // 解决av_write_frame() 时dts > pts导致的 Invalid Argument
        if (dts_start_from[packet.stream_index] < pts_start_from[packet.stream_index]){
            pts_start_from[packet.stream_index] = dts_start_from[packet.stream_index];
        }

        // 重新计算dts和pts
        packet.pts = av_rescale_q_rnd(packet.pts - pts_start_from[packet.stream_index],
                                      in_stream->time_base, out_stream->time_base,
                                      AV_ROUND_INF );
        packet.dts = av_rescale_q_rnd(packet.dts - dts_start_from[packet.stream_index],
                                      in_stream->time_base, out_stream->time_base,
                                      AV_ROUND_ZERO);
        if (packet.pts < 0) {
            packet.pts = 0;
        }
        if (packet.dts < 0) {
            packet.dts = 0;
        }
        packet.duration = av_rescale_q((int64_t) packet.duration, in_stream->time_base,
                                       out_stream->time_base);
        packet.pos = -1;

        // 计算裁剪进度，优先使用视频索引
        int index = video_index >= 0 ? video_index : audio_index;
        if (index == packet.stream_index) {
            double timeStamp = packet.pts * av_q2d(out_stream->time_base);
            double percent = timeStamp * 1000 / mDuration;
            if (percent < 0) {
                percent = 0;
            } else if (percent > 1.0) {
                percent = 1.0;
            }
            if (mEditListener != nullptr) {
                mEditListener->onProcessing((int)(percent * 100));
            } else {
                LOGD("process percent: %d", (int)(percent * 100));
            }
        }

        // 将数据包写入复用器
        if ((ret = av_interleaved_write_frame(ofmt_ctx, &packet)) < 0) {
            break;
        }

        av_packet_unref(&packet);
    }
    free(dts_start_from);
    free(pts_start_from);

    // 写入stream尾部信息
    av_write_trailer(ofmt_ctx);

    // 关闭输入文件
    avformat_close_input(&ifmt_ctx);

    // 关闭文件
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE)) {
        avio_closep(&ofmt_ctx->pb);
    }

    // 释放复用器上下文
    avformat_free_context(ofmt_ctx);

    // 成功还是失败回调
    if (ret < 0) {
        if (mEditListener != nullptr) {
            mEditListener->onFailed(av_err2str(ret));
        }
    } else {
        if (mEditListener != nullptr) {
            mEditListener->onSuccess();
        }
    }

    return 0;
}
