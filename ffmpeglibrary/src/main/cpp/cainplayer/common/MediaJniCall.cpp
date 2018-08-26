//
// Created by admin on 2018/4/29.
//

#include "MediaJniCall.h"
#include "MediaStatus.h"

MediaJniCall::MediaJniCall(_JavaVM *jvm, JNIEnv *env, jobject *obj) {
    javaVM        = jvm;
    jniEnv        = env;
    jobj          = *obj;
    jobj          = env->NewGlobalRef(jobj);
    jclass jclazz = jniEnv->GetObjectClass(jobj);
    if (jclazz) {
        mOnCompletion     = jniEnv->GetMethodID(jclazz, "onCompletion", "()V");
        mOnError          = jniEnv->GetMethodID(jclazz, "onError", "(ILjava/lang/String;)V");
        mOnLoad           = jniEnv->GetMethodID(jclazz, "onLoad", "(Z)V");
        mOnPrepared       = jniEnv->GetMethodID(jclazz, "onPrepared", "()V");
        mTimeInfo         = jniEnv->GetMethodID(jclazz, "onTimeInfo", "(II)V");
        mOnSeekCompletion = jniEnv->GetMethodID(jclazz, "onSeekCompletion", "(I)V");
    }
}

MediaJniCall::~MediaJniCall() {

}

/**
 * 释放资源
 */
void MediaJniCall::release() {
    if (javaVM != NULL) {
        javaVM = NULL;
    }
    if (jniEnv != NULL) {
        jniEnv = NULL;
    }
}

/**
 * 播放完成回调
 * @param type
 */
void MediaJniCall::onCompletion(int type) {
    if (type == WORKER_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jobj, mOnCompletion);
    } else {
        jniEnv->CallVoidMethod(jobj, mOnCompletion);
    }
}

/**
 * 出错回调
 * @param type
 * @param code
 * @param msg
 */
void MediaJniCall::onError(int type, int code, const char *msg) {
    if (type == WORKER_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, mOnError, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        javaVM->DetachCurrentThread();
    } else {
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, mOnError, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
    }
}

/**
 * 加载回调
 * @param type
 * @param load
 */
void MediaJniCall::onLoad(int type, bool load) {
    if (type == WORKER_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jobj, mOnLoad, load);
        javaVM->DetachCurrentThread();
    } else {
        jniEnv->CallVoidMethod(jobj, mOnLoad, load);
    }
}

/**
 * 准备回调
 * @param type
 */
void MediaJniCall::onPrepared(int type) {
    if (type == WORKER_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jobj, mOnPrepared);
        javaVM->DetachCurrentThread();
    } else {
        jniEnv->CallVoidMethod(jobj, mOnPrepared);
    }
}

/**
 * 播放时间信息回调
 * @param type
 * @param current
 * @param duration
 */
void MediaJniCall::onTimeInfo(int type, int current, int duration) {
    if (type == WORKER_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jobj, mTimeInfo, current, duration);
        javaVM->DetachCurrentThread();
    } else {
        jniEnv->CallVoidMethod(jobj, mTimeInfo, current, duration);
    }
}

/**
 * 定位完成
 * @param type
 * @param current
 */
void MediaJniCall::onSeekCompletion(int type, int current) {
    if (type == WORKER_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(jobj, mOnSeekCompletion, current);
    } else {
        jniEnv->CallVoidMethod(jobj, mOnSeekCompletion, current);
    }
}