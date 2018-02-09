//
// Created by cain on 2018/2/8.
//

#ifndef CAINCAMERA_CAINPLAYER_H
#define CAINCAMERA_CAINPLAYER_H

#include <assert.h>

#include "CainPlayerDefinition.h"


class CainPlayer {
private:
    // 视频状态
    VideoState *is;

    // 参数
    AVDictionary *format_opts;
    AVDictionary *codec_opts;
    AVDictionary *sws_dict;
    AVDictionary *swr_opts;

    AVPacket flush_pkt;                             // 刷新包
    char *input_filename;		                    // 输入文件名
    int audio_disable;					            // 是否禁止播放声音
    int video_disable;					            // 是否禁止播放视频
    int subtitle_disable;				            // 是否禁止播放字幕
    char* wanted_stream_spec[AVMEDIA_TYPE_NB] = {0};
    int seek_by_bytes = -1;				            // 以字节方式查找
    int display_disable;					        // 显示禁止
    int borderless;					                //
    int startup_volume = 100;		                // 初始音量
    int show_status = 1;					        // 显示状态
    int av_sync_type = AV_SYNC_AUDIO_MASTER;	    // 时钟同步类型
    int64_t start_time = AV_NOPTS_VALUE;			// 开始时间
    int64_t duration = AV_NOPTS_VALUE;			    // 间隔
    int fast = 0;								    // 快速解码标志
    int genpts = 0;							        //
    int lowres = 0;							        // 低分辨率解码标志
    int decoder_reorder_pts = -1;	                // 解码器重新排列时间戳
    int autoexit;								    // 否自动退出
    int loop = 1;								    // 循环
    int framedrop = -1;					            // 舍弃帧
    int infinite_buffer = -1;				        // 无限缓冲区
    enum ShowMode show_mode = SHOW_MODE_NONE;       // 显示类型
    const char *audio_codec_name;	                // 音频解码器名称
    const char *subtitle_codec_name;	            // 字幕解码器名称
    const char *video_codec_name;	                // 视频解码器名称
    double rdftspeed = 0.02;						// 自适应滤波器的速度
    int autorotate = 1;						        // 是否自动旋转
    int64_t audio_callback_time;			        // 音频回调时间

    // 解码器初始化
    void decoder_init(Decoder *d, AVCodecContext *avctx,
            PacketQueue *queue, Cond *empty_queue_cond);
    // 解码方法
    int decoder_decode_frame(Decoder *d, AVFrame *frame, AVSubtitle *sub);
    // 销毁解码器
    void decoder_destroy(Decoder *d);
    // 取消解码
    void decoder_abort(Decoder *d, FrameQueue *fq);
    // 显示画面
    void video_image_display(VideoState *is);
    // 关闭组件的流
    void stream_component_close(VideoState *is, int stream_index);
    // 关闭流
    void stream_close(VideoState *is);
    // 查找流
    void stream_seek(VideoState *is, int64_t pos, int64_t rel, int seek_by_bytes);
    // 暂停/播放视频流
    void stream_toggle_pause(VideoState *is);
    // 暂停播放
    void toggle_pause(VideoState *is);
    // 是否静音
    void toggle_mute(VideoState *is);
    // 更新音频
    void update_volume(VideoState *is, int sign, double step);
    // 下一帧
    void step_to_next_frame(VideoState *is);
    // 计算延时
    double compute_target_delay(double delay, VideoState *is);
    // 计算显示时长
    double vp_duration(VideoState *is, Frame *vp, Frame *nextvp);
    // 更新视频的pts
    void update_video_pts(VideoState *is, double pts, int64_t pos, int serial);
    // 刷新视频帧
    void video_refresh(void *opaque, double *remaining_time);
    // 将已经解码帧压入队列
    int queue_picture(VideoState *is, AVFrame *src_frame, double pts, double duration,
                      int64_t pos, int serial);
    // 获取视频帧
    int get_video_frame(VideoState *is, AVFrame *frame);
    // 解码器开始
    int decoder_start(Decoder *d, int (*fn)(void *), void *arg);
    // 同步音频
    int synchronize_audio(VideoState *is, int nb_samples);
    // 从解码后的音频缓存队列中读取一帧，并做重采样处理(转码、变声、变速等操作)
    int audio_decode_frame(VideoState *is);
    // 打开音频
    int audio_open(void *opaque, int64_t wanted_channel_layout, int wanted_nb_channels,
                   int wanted_sample_rate, struct AudioParams *audio_hw_params);
    // 打开码流
    int stream_component_open(VideoState *is, int stream_index);
    // 判断是否有足够的包
    int stream_has_enough_packets(AVStream *st, int stream_id, PacketQueue *queue);
    // 判断是否实时流
    int is_realtime(AVFormatContext *s);
    // 打开流，这里是prepare入口
    VideoState *stream_open(const char *filename, AVInputFormat *iformat);

public:
    CainPlayer();
    ~CainPlayer();
    // 设置数据源
    void SetDataSource(const char *path);
    // 获取当前进度
    int GetCurrentPosition(void);
    // 返回音频Session Id
    int GetAudioSessionId(void);
    // 得到媒体时长
    int GetDuration(void);
    // 是否循环播放
    bool IsLooping(void);
    // 是否正在播放
    bool IsPlaying(void);
    // 暂停
    void Pause(void);
    // 停止音频
    void PauseAudio(void);
    // 开始
    void Start(void);
    // 停止
    void Stop(void);
    // 异步装载流媒体
    void PrepareAsync(void);
    // 重置播放器
    void Reset(void);
    // 回收资源
    void Release(void);
    // 指定播放位置
    void SeekTo(int msec);
    // 指定播放区域
    void SeekToRegion(int lmsec, int rmsec);
    // 设置是否单曲循环
    void SetLooping(bool loop);
    // 设置是否倒放
    void SetReverse(bool reverse);
    // 设置是否播放声音
    void SetPlayAudio(bool play);
    // 输入大小发生改变
    void ChangedSize(int width, int height);
    // 读文件(解复用)线程执行实体
    int readAndDemuxing(void);
    // 音频解码线程执行实体
    int audioDecode(void);
    // 视频解码线程执行实体
    int videoDecode(void);
    // 字幕解码线程执行实体
    int subtitleDecode(void);
    // 刷线画面
    int videoRefresh(void);
    // 音频回调执行
    void audioCallbackProcess(uint8_t *stream, int len);

};


#endif //CAINCAMERA_CAINPLAYER_H
