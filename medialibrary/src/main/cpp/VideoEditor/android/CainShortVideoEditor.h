//
// Created by CainHuang on 2019/2/17.
//

#ifndef CAINSHORTVIDEOEDITOR_H
#define CAINSHORTVIDEOEDITOR_H

#include <Thread.h>
#include <Mutex.h>
#include <Condition.h>


class ShortVideoEditorListener {
public:
    virtual void notify(int msg, int ext1, int ext2, void *obj) {}
};


class CainShortVideoEditor {
public:
    CainShortVideoEditor();

    virtual ~CainShortVideoEditor();

    void init();

    void disconnect();

    void setListener(ShortVideoEditorListener *listener);

    // 视频剪辑
    int videoCut(const char *srcPath, const char *dstPath, long start, long duration, float speed);

    // 音频剪辑
    int audioCut(const char *srcPath, const char *dstPath, long start, long duration);

    // 视频转gif
    int videoConvertGif(const char *srcPath, const char *dstPath, long start, long duration);

private:
    bool abortRequest;
    ShortVideoEditorListener *mListener;
};

#endif //CAINSHORTVIDEOEDITOR_H
