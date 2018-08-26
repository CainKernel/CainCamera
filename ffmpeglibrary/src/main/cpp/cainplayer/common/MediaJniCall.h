//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_MEDIAJNICALL_H
#define CAINPLAYER_MEDIAJNICALL_H

#include <jni.h>
#include <stdint.h>


class MediaJniCall {

public:
    MediaJniCall(_JavaVM *jvm, JNIEnv *env, jobject *jobj);

    virtual ~MediaJniCall();

    // 出错
    void onError(int type, int code, const char *msg);

    // 加载
    void onLoad(int type, bool load);

    // 准备完成
    void onPrepared(int type);

    // 回调信息
    void onTimeInfo(int type, int current, int duration);

    // 完成
    void onCompletion(int type);

    // seek完成回调
    void onSeekCompletion(int type, int current);

    // 释放资源
    void release();


private:
    _JavaVM *javaVM;
    JNIEnv *jniEnv;
    jobject jobj;
    jmethodID mOnError;             // 出错回调方法
    jmethodID mOnLoad;              // 加载回调方法
    jmethodID mOnPrepared;          // 准备完成回调方法
    jmethodID mTimeInfo;            // 播放信息回调方法
    jmethodID mOnCompletion;        // 播放完成回调方法
    jmethodID mOnSeekCompletion;    // seek完成回调方法
};


#endif //CAINPLAYER_MEDIAJNICALL_H
