//
// Created by CainHuang on 2020/1/12.
//

#ifndef AVMEDIACODECREADER_H
#define AVMEDIACODECREADER_H

#if defined(__ANDROID__)

#include "MediaReader.h"
#include "decoder/MediaCodecVideoDecoder.h"
#include "decoder/AVAudioDecoder.h"

class AVMediaCodecReader : public MediaReader {
public:
    AVMediaCodecReader();

    virtual ~AVMediaCodecReader();

    void setDataSource(const char *url) override;

    void setInputFormat(const char *format) override;

    void addFormatOptions(std::string key, std::string value) override;

    void addDecodeOptions(std::string key, std::string value) override;

    void setReadListener(OnDecodeListener *listener, bool autoRelease) override;

    int openInputFile() override;

    int seekTo(float timeMs) override;

    int64_t getDuration() override;

    int decodePacket() override;

private:
    void reset();

    void release();

    int decodePacket(AVPacket *packet, OnDecodeListener *listener);

private:
    const char *mSrcPath;               // input path
    OnDecodeListener *mReadListener;    // decode listener
    bool mAutoRelease;                  // auto release decode listener or not

    const char *mFormat;                // AVInputFormat name
    std::map<std::string, std::string> mFormatOptions;  // format options
    std::map<std::string, std::string> mDecodeOptions;  // decode options

    std::shared_ptr<AVMediaDemuxer> mMediaDemuxer;          // demuxer
    std::shared_ptr<AVAudioDecoder> mAudioDecoder;          // audio decoder
    std::shared_ptr<MediaCodecVideoDecoder> mVideoDecoder;  // video decoder

    AVPacket mPacket;   // decoder temp packet

    bool mAbortRequest; // abort and stop request
};

#endif /* defined(__ANDROID__) */

#endif //AVMEDIACODECREADER_H
