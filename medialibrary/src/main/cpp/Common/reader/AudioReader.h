//
// Created by CainHuang on 2020-01-19.
//

#ifndef AUDIOREADER_H
#define AUDIOREADER_H

#include <string>
#include <map>
#include "MediaReader.h"
#include "demuxer/AVMediaDemuxer.h"
#include "decoder/AVAudioDecoder.h"

class AudioReader : public MediaReader {
public:
    AudioReader();

    virtual ~AudioReader();

    void setDataSource(const char *url) override;

    void setInputFormat(const char *format) override;

    void setAudioDecoder(const char *decoder);

    void setReadListener(OnDecodeListener *listener, bool autoRelease = false) override;

    void addFormatOptions(std::string key, std::string value) override;

    void addDecodeOptions(std::string key, std::string value) override;

    int openInputFile() override;

    int seekTo(float timeMs) override;

    int64_t getDuration() override;

    int decodePacket() override;

    int getSampleRate();

    int getChannels();

    AVSampleFormat getSampleFormat();

    void release();

private:
    void reset();

    int decodePacket(AVPacket *packet, OnDecodeListener *listener);

private:
    const char *mSrcPath;                               // input path
    OnDecodeListener *mDecodeListener;                    // decode listener
    bool mAutoRelease;                                  // auto release decode listener or not

    const char *mFormat;                                // AVInputFormat name
    std::map<std::string, std::string> mFormatOptions;  // format options
    std::map<std::string, std::string> mDecodeOptions;  // decode options

    const char *mAudioCodecName;                        // audio decoder name
    std::shared_ptr<AVMediaDemuxer> mMediaDemuxer;      // demuxer
    std::shared_ptr<AVAudioDecoder> mAudioDecoder;      // video decoder

    AVPacket mPacket;                                   // temp packet
    bool mAbortRequest;                                 // abort and stop request
};


#endif //AUDIOREADER_H
