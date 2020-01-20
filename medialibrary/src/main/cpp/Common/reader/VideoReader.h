//
// Created by CainHuang on 2020-01-19.
//

#ifndef VIDEOREADER_H
#define VIDEOREADER_H

#include <string>
#include <map>
#include "MediaReader.h"
#include "AVMediaDemuxer.h"
#include "AVVideoDecoder.h"

class VideoReader : public MediaReader {
public:
    VideoReader();

    virtual ~VideoReader();

    void setDataSource(const char *url) override;

    void setInputFormat(const char *format) override;

    void setVideoDecoder(const char *decoder);

    void setReadListener(OnDecodeListener *listener, bool autoRelease) override;

    void addFormatOptions(std::string key, std::string value) override;

    void addDecodeOptions(std::string key, std::string value) override;

    int openInputFile() override;

    int seekTo(float timeMs) override;

    int64_t getDuration() override;

    int decodePacket() override;

private:
    void reset();

    void release();

    int decodePacket(AVPacket *packet, OnDecodeListener *listener);

private:
    const char *mSrcPath;                               // input path
    OnDecodeListener *mReadListener;                    // decode listener
    bool mAutoRelease;                                  // auto release decode listener or not

    const char *mFormat;                                // AVInputFormat name
    std::map<std::string, std::string> mFormatOptions;  // format options
    std::map<std::string, std::string> mDecodeOptions;  // decode options

    const char *mVideoCodecName;                        // audio decoder name
    std::shared_ptr<AVMediaDemuxer> mMediaDemuxer;      // demuxer
    std::shared_ptr<AVVideoDecoder> mVideoDecoder;  // audio decoder

    AVPacket mPacket;                                   // temp packet
    bool mAbortRequest;                                 // abort and stop request
};


#endif //VIDEOREADER_H
