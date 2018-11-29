//
// Created by cain on 2018/11/25.
//

#ifndef CAINCAMERA_JNIPLAYERCALLBACK_H
#define CAINCAMERA_JNIPLAYERCALLBACK_H


#include <jni.h>
#include "source/PlayerCallback.h"

class JniPlayerCallback : public PlayerCallback {
public:
    JniPlayerCallback(_JavaVM *javaVM, JNIEnv *env, jobject *obj);

    virtual ~JniPlayerCallback();

    void onPrepared() override;


    void onCurrentInfo(double current, double total) override;

    void onError(int code, char *msg) override;

    void onComplete() override;

    void onGetPCM(uint8_t *pcmData, size_t size) override;

    void onVolumeDB(int db) override;

private:
    _JavaVM *javaVM = NULL;
    JNIEnv *jniEnv = NULL;
    jobject jobj;

    jmethodID jmid_prepared;
    jmethodID jmid_currentInfo;
    jmethodID jmid_error;
    jmethodID jmid_complete;
    jmethodID jmid_getPCM;
    jmethodID jmid_volumedb;
};


#endif //CAINCAMERA_JNIPLAYERCALLBACK_H
