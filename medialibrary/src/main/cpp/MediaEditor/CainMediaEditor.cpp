//
// Created by CainHuang on 2019/2/17.
//

#include "CainMediaEditor.h"

CainVideoEditor::CainVideoEditor() : mVideoCutEditor(nullptr), mAudioCutEditor(nullptr) {

}

CainVideoEditor::~CainVideoEditor() {
    release();
}

/**
 * 释放资源
 */
void CainVideoEditor::release() {

    if (mVideoCutEditor != nullptr) {
        mVideoCutEditor->stop();
        delete mVideoCutEditor;
        mVideoCutEditor = nullptr;
    }

    if (mAudioCutEditor != nullptr) {
        mAudioCutEditor->stop();
        delete mAudioCutEditor;
        mAudioCutEditor = nullptr;
    }

}

/**
 * 视频倍速剪辑
 * @param srcPath
 * @param dstPath
 * @param start
 * @param duration
 * @param speed
 */
void CainVideoEditor::videoSpeedCut(const char *srcPath, const char *dstPath, long start,
                                    long duration, float speed, EditListener *listener) {
    if (mVideoCutEditor != nullptr) {
        mVideoCutEditor->stop();
        delete mVideoCutEditor;
        mVideoCutEditor = nullptr;
    }
    mVideoCutEditor = new VideoCutEditor(srcPath, dstPath);
    mVideoCutEditor->setDuration(start, duration);
    mVideoCutEditor->setSpeed(speed);
    mVideoCutEditor->setListener(listener);
    mVideoCutEditor->start();
}

/**
 * 音频倍速剪辑
 * @param srcPath
 * @param dstPath
 * @param start
 * @param duration
 * @param speed
 */
void CainVideoEditor::audioSpeedCut(const char *srcPath, const char *dstPath, long start,
                                   long duration, float speed, EditListener *listener) {
    if (mAudioCutEditor != nullptr) {
        mAudioCutEditor->stop();
        delete mAudioCutEditor;
        mAudioCutEditor = nullptr;
    }
    mAudioCutEditor = new AudioCutEditor(srcPath, dstPath);
    mAudioCutEditor->setDuration(start, duration);
    mAudioCutEditor->setSpeed(speed);
    mAudioCutEditor->setListener(listener);
    mAudioCutEditor->start();
}

/**
 * 视频逆序
 * @param srcPath
 * @param dstPath
 * @param listener
 */
void CainVideoEditor::videoReverse(const char *srcPath, const char *dstPath,
                                   EditListener *listener) {

}