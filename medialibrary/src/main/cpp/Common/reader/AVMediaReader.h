//
// Created by CainHuang on 2019/8/12.
//

#ifndef AVMEDIAREADER_H
#define AVMEDIAREADER_H

#include <string>
#include <map>
#include "MediaReader.h"
#include "AVMediaDemuxer.h"
#include "AVAudioDecoder.h"
#include "AVVideoDecoder.h"

/**
 * 媒体读取器
 */
class AVMediaReader : public MediaReader {
public:
    AVMediaReader();

    virtual ~AVMediaReader();

    // 设置数据源
    void setDataSource(const char *url) override;

    // 指定解码格式，对应命令行的 -f 的参数
    void setInputFormat(const char *format);

    // 指定音频解码器名称
    void setAudioDecoder(const char *decoder);

    // 指定视频解码器名称
    void setVideoDecoder(const char *decoder);

    // 添加格式参数
    void addFormatOptions(std::string key, std::string value);

    // 添加解码参数
    void addDecodeOptions(std::string key, std::string value);

    // 设置媒体读取监听器
    void setReadListener(OnDecodeListener *listener, bool autoRelease) override;

    // 打开输入文件
    int openInputFile();

    // 定位
    int seekTo(float timeMs) override;

    // 解码数据包
    int decodePacket() override;

    // 获取时长
    int64_t getDuration() override;

private:
    // 重置所有参数
    void reset();

    // 释放所有参数
    void release();

    // 解码数据包
    int decodePacket(AVPacket *packet, OnDecodeListener *listener);

private:
    const char *mSrcPath;                               // input path
    OnDecodeListener *mReadListener;                    // decode listener
    bool mAutoRelease;                                  // auto release decode listener or not

    const char *mFormat;                                // AVInputFormat name
    std::map<std::string, std::string> mFormatOptions;  // format options
    std::map<std::string, std::string> mDecodeOptions;  // decode options

    const char *mVideoCodecName;                        // video decoder name
    const char *mAudioCodecName;                        // audio decoder name

    std::shared_ptr<AVMediaDemuxer> mMediaDemuxer;      // demuxer
    std::shared_ptr<AVAudioDecoder> mAudioDecoder;      // video decoder
    std::shared_ptr<AVVideoDecoder> mVideoDecoder;      // audio decoder

    AVPacket mPacket;                                   // decoder temp packet

    bool mAbortRequest;                                 // abort and stop request

};


#endif //AVMEDIAREADER_H
