//
// Created by cain on 2019/2/12.
//

#ifndef CAINMEDIASYNTHESIZER_H
#define CAINMEDIASYNTHESIZER_H

#include <cstdio>
#include <Thread.h>
#include <android/native_window.h>

enum media_event_type {
    MEDIA_NOP                   = 0, // interface test message
    MEDIA_PREPARED              = 1,
    MEDIA_SYNTHESIZE_COMPLETE   = 2,
    MEDIA_PROCESSING            = 3,
    MEDIA_ERROR                 = 100,
};


class MediaSynthesizerListener {
public:
    virtual void notify(int msg, int ext1, int ext2, void *obj) {}
};


class CainMediaSynthesizer : public Runnable {
public:
    CainMediaSynthesizer();

    virtual ~CainMediaSynthesizer();

    void init();

    void disconnect();

    void setDataSource(const char *ulr);

    void setVideoSurface(ANativeWindow *native_window);

    void setListener(MediaSynthesizerListener *listener);

    void prepare();

    void start();

    void stop();

    long getCurrentPosition();

    long getDuration();

    void reset();

    void notify(int msg, int ext1, int ext2, void *obj = NULL, int len = 0);

protected:
    void run() override;

private:
    void postEvent(int what, int arg1, int arg2, void *obj = NULL);
};


#endif //CAINMEDIASYNTHESIZER_H
