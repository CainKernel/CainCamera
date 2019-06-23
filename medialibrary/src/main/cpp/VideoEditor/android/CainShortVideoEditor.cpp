//
// Created by CainHuang on 2019/2/17.
//

#include <editor_log.h>
#include <VideoCutEditor.h>
#include <AudioCutEditor.h>
#include <GifMakeEditor.h>
#include "CainShortVideoEditor.h"

CainShortVideoEditor::CainShortVideoEditor() {
    mListener = nullptr;
    abortRequest = true;
}

CainShortVideoEditor::~CainShortVideoEditor() {
    disconnect();
}

void CainShortVideoEditor::init() {

}

void CainShortVideoEditor::disconnect() {

}

void CainShortVideoEditor::setListener(ShortVideoEditorListener *listener) {
    if (mListener) {
        delete mListener;
    }
    mListener = listener;
}

int CainShortVideoEditor::videoCut(const char *srcPath, const char *dstPath, long start, long duration,float speed) {
    LOGD("video cut start");
    VideoCutEditor editor = VideoCutEditor(srcPath, dstPath);
    editor.setDuration(start, duration);
    editor.setSpeed(speed);
    return editor.process();
}

int CainShortVideoEditor::audioCut(const char *srcPath, const char *dstPath, long start,
                                   long duration) {
    LOGD("video cut start");
    AudioCutEditor editor = AudioCutEditor(srcPath, dstPath);
    editor.setDuration(start, duration);
    return editor.process();
}

int CainShortVideoEditor::videoConvertGif(const char *srcPath, const char *dstPath, long start,
                                          long duration) {
    GifMakeEditor editor = GifMakeEditor(srcPath, dstPath);
    editor.setDuration(start, duration);
    return editor.process();
}
