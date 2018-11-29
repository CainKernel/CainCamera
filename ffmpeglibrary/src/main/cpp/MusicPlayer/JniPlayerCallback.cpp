//
// Created by cain on 2018/11/25.
//

#include "JniPlayerCallback.h"
#include "AndroidLog.h"

JniPlayerCallback::JniPlayerCallback(_JavaVM *javaVM, JNIEnv *env, jobject *obj) {
    this->javaVM = javaVM;
    this->jniEnv = env;
    this->jobj = *obj;
    this->jobj = env->NewGlobalRef(jobj);

    jclass  clazz = jniEnv->GetObjectClass(jobj);
    if (clazz) {
        jmid_complete    = env->GetMethodID(clazz, "onCompletion", "()V");
        jmid_error       = env->GetMethodID(clazz, "onError", "(ILjava/lang/String;)V");
        jmid_prepared    = env->GetMethodID(clazz, "onPrepared", "()V");
        jmid_currentInfo = env->GetMethodID(clazz, "onCurrentInfo", "(II)V");
        jmid_getPCM      = env->GetMethodID(clazz, "onGetPCM", "([BI)V");
        jmid_volumedb    = env->GetMethodID(clazz, "onVolumeDB", "(I)V");
    }
}

JniPlayerCallback::~JniPlayerCallback() {

}

void JniPlayerCallback::onPrepared() {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        return;
    }
    jniEnv->CallVoidMethod(jobj, jmid_prepared);
    javaVM->DetachCurrentThread();
}

void JniPlayerCallback::onCurrentInfo(double current, double total) {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        return;
    }
    ALOGD("current = %f, mDuration = %f", current, total);
    jniEnv->CallVoidMethod(jobj, jmid_currentInfo, (int)current, (int)total);
    javaVM->DetachCurrentThread();
}

void JniPlayerCallback::onError(int code, char *msg) {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {

        return;
    }
    jstring jmsg = jniEnv->NewStringUTF(msg);
    jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
    jniEnv->DeleteLocalRef(jmsg);
    javaVM->DetachCurrentThread();
}

void JniPlayerCallback::onComplete() {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        return;
    }
    jniEnv->CallVoidMethod(jobj, jmid_complete);
    javaVM->DetachCurrentThread();
}

void JniPlayerCallback::onGetPCM(uint8_t *pcmData, size_t size) {
    jbyteArray data = jniEnv->NewByteArray(size);
    jniEnv->SetByteArrayRegion(data, 0, size, (jbyte*)pcmData);
    jniEnv->CallVoidMethod(jobj, jmid_getPCM, data, size);
    jniEnv->DeleteLocalRef(data);
}

void JniPlayerCallback::onVolumeDB(int db) {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        return;
    }
    jniEnv->CallVoidMethod(jobj, jmid_volumedb, db);
    javaVM->DetachCurrentThread();
}
