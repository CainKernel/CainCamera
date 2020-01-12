//
// Created by CainHuang on 2020/1/4.
//

#ifndef ONTRANSCODELISTENER_H
#define ONTRANSCODELISTENER_H

class OnTranscodeListener {
public:
    // 转码开始
    virtual void onTranscodeStart() = 0;

    // 正在转码
    virtual void onTranscoding(float duration) = 0;

    // 转码完成
    virtual void onTranscodeFinish(bool success, float duration) = 0;

    // 转码出错
    virtual void onTranscodeError(const char *msg) = 0;
};

#endif //ONTRANSCODELISTENER_H
