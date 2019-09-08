//
// Created by CainHuang on 2019/2/17.
//

#ifndef CAINVIDEOEDITOR_H
#define CAINVIDEOEDITOR_H

#include "VideoCutEditor.h"
#include "AudioCutEditor.h"

/**
 * 视频编辑器
 */
class CainVideoEditor {
public:
    CainVideoEditor();

    virtual ~CainVideoEditor();

    // 释放资源
    void release();

    // 视频倍速剪辑
    void videoSpeedCut(const char *srcPath, const char *dstPath, long start, long duration,
                      float speed, EditListener *listener);

    // 音频倍速剪辑
    void audioSpeedCut(const char *srcPath, const char *dstPath, long start, long duration,
                      float speed, EditListener *listener);

    // 视频逆序
    void videoReverse(const char *srcPath, const char *dstPath, EditListener *listener);

private:
    VideoCutEditor *mVideoCutEditor;
    AudioCutEditor *mAudioCutEditor;
};

#endif //CAINVIDEOEDITOR_H
