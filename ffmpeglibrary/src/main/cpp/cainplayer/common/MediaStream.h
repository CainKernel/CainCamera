//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_MEDIACHANNEL_H
#define CAINPLAYER_MEDIACHANNEL_H

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/rational.h>

#ifdef __cplusplus
};
#endif

class MediaStream {
public:
    MediaStream(int id, AVRational base);

    MediaStream(int id, AVRational base, int fps);

    int getStreamIndex() const;

    void setStreamIndex(int index);

    const AVRational &getTimeBase() const;

    void setTimeBase(const AVRational &time_base);

    int getFps() const;

    void setFps(int fps);

private:
    int streamIndex = -1;
    AVRational timeBase;
    int fps;
};


#endif //CAINPLAYER_MEDIACHANNEL_H
