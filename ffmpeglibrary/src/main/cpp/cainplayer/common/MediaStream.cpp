//
// Created by admin on 2018/4/29.
//

#include "MediaStream.h"

MediaStream::MediaStream(int id, AVRational base) {
    streamIndex = id;
    timeBase = base;
}

MediaStream::MediaStream(int id, AVRational base, int f) {
    streamIndex = id;
    timeBase = base;
    fps = f;
}

int MediaStream::getStreamIndex() const {
    return streamIndex;
}

void MediaStream::setStreamIndex(int index) {
    MediaStream::streamIndex = index;
}

const AVRational &MediaStream::getTimeBase() const {
    return timeBase;
}

void MediaStream::setTimeBase(const AVRational &time_base) {
    MediaStream::timeBase = time_base;
}

int MediaStream::getFps() const {
    return fps;
}

void MediaStream::setFps(int fps) {
    MediaStream::fps = fps;
}
