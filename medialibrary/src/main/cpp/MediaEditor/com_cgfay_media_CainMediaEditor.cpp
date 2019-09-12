//
// Created by CainHuang on 2019/2/17.
//
#if defined(__ANDROID__)

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <Mutex.h>
#include <assert.h>

extern "C" {
#include <libavcodec/jni.h>
};

#include "CainMediaEditor.h"

static JavaVM *javaVM = NULL;

static JNIEnv *getJNIEnv() {
    JNIEnv *env;
    assert(javaVM != NULL);
    if (javaVM->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return NULL;
    }
    return env;
}

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    av_jni_set_java_vm(vm, nullptr);
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

//-------------------------------------- JNI回调监听器 ----------------------------------------------
/**
 * 编辑监听器
 */
class JNIEditListener : public EditListener {
public:
    JNIEditListener(JavaVM *vm, JNIEnv *env, jobject listener);

    virtual ~JNIEditListener();

    // 正在处理
    void onProcessing(int percent) override;

    // 处理失败
    void onFailed(const char *msg) override;

    // 处理成功
    void onSuccess() override;

private:
    JNIEditListener();

    JavaVM *javaVM;
    jobject mJniListener;           // java接口创建的全局对象
    jmethodID jmid_onProcessing;    // 正在处理中
    jmethodID jmid_onSuccess;       // 处理成功
    jmethodID jmid_onError;         // 处理失败
};

JNIEditListener::JNIEditListener(JavaVM *vm, JNIEnv *env, jobject listener) {
    this->javaVM = vm;
    if (listener != nullptr) {
        mJniListener = env->NewGlobalRef(listener);
    } else {
        mJniListener = nullptr;
    }
    jclass javaClass = env->GetObjectClass(listener);
    if (javaClass != nullptr) {
        jmid_onProcessing = env->GetMethodID(javaClass, "onProcessing", "(I)V");
        jmid_onSuccess = env->GetMethodID(javaClass, "onSuccess", "()V");
        jmid_onError = env->GetMethodID(javaClass, "onError", "(Ljava/lang/String;)V");
    } else {
        jmid_onProcessing = nullptr;
        jmid_onSuccess = nullptr;
        jmid_onError = nullptr;
    }
}

JNIEditListener::~JNIEditListener() {
    if (mJniListener != nullptr) {
        JNIEnv *env = getJNIEnv();
        env->DeleteGlobalRef(mJniListener);
        mJniListener = nullptr;
    }
}

void JNIEditListener::onProcessing(int percent) {
    LOGD("processing = %d", percent);
    if (jmid_onProcessing != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onProcessing, percent);
        javaVM->DetachCurrentThread();
    }
}

void JNIEditListener::onSuccess() {
    LOGD("onSuccess");
    if (jmid_onSuccess != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onSuccess);
        javaVM->DetachCurrentThread();
    }
}

void JNIEditListener::onFailed(const char *msg) {
    LOGD("onFailed: %s", msg);
    if (jmid_onError != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jstring jmsg = nullptr;
        if (msg != nullptr) {
            jmsg = jniEnv->NewStringUTF(msg);
        } else {
            jmsg = jniEnv->NewStringUTF("");
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onError, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        javaVM->DetachCurrentThread();
    }
}

//--------------------------------------------------------------------------------------------------

/**
 * 初始化一个编辑器对象
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_cgfay_media_CainMediaEditor_nativeInit(JNIEnv *env, jobject thiz) {
    CainVideoEditor *videoEditor = new CainVideoEditor();
    return (jlong) videoEditor;
}

/**
 * 释放编辑器对象
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_CainMediaEditor_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    CainVideoEditor *videoEditor = (CainVideoEditor *) handle;
    if (videoEditor != nullptr) {
        delete videoEditor;
        videoEditor = nullptr;
    }
}

/**
 * 视频倍速裁剪处理
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_CainMediaEditor_videoCut(JNIEnv *env, jobject thiz, jlong handle,
        jstring srcPath_, jstring dstPath_, jfloat start, jfloat duration, jfloat speed,
        jobject listener) {

    CainVideoEditor *videoEditor = (CainVideoEditor *) handle;
    if (videoEditor != nullptr) {
        const char *srcPath = env->GetStringUTFChars(srcPath_, nullptr);
        const char *dstPath = env->GetStringUTFChars(dstPath_, nullptr);
        if (listener != nullptr) {
            JNIEditListener *editListener = new JNIEditListener(javaVM, env, listener);
            videoEditor->videoSpeedCut(srcPath, dstPath, start, duration, speed, editListener);
        } else {
            videoEditor->videoSpeedCut(srcPath, dstPath, start, duration, speed, nullptr);
        }
        env->ReleaseStringUTFChars(dstPath_, dstPath);
        env->ReleaseStringUTFChars(srcPath_, srcPath);
    }

}

/**
 * 音频倍速裁剪处理
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_CainMediaEditor_audioCut(JNIEnv *env, jobject thiz, jlong handle,
        jstring srcPath_,jstring dstPath_, jfloat start, jfloat duration, jfloat speed,
        jobject listener) {

    CainVideoEditor *videoEditor = (CainVideoEditor *) handle;
    if (videoEditor != nullptr) {
        const char *srcPath = env->GetStringUTFChars(srcPath_, nullptr);
        const char *dstPath = env->GetStringUTFChars(dstPath_, nullptr);
        if (listener != nullptr) {
            JNIEditListener *editListener = new JNIEditListener(javaVM, env, listener);
            videoEditor->audioSpeedCut(srcPath, dstPath, start, duration, speed, editListener);
        } else {
            videoEditor->audioSpeedCut(srcPath, dstPath, start, duration, speed, nullptr);
        }
        env->ReleaseStringUTFChars(dstPath_, dstPath);
        env->ReleaseStringUTFChars(srcPath_, srcPath);
    }

}

/**
 * 视频逆序处理
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_CainMediaEditor_videoReverse(JNIEnv *env, jobject thiz, jlong handle,
        jstring srcPath_, jstring dstPath_, jobject listener) {

    CainVideoEditor *videoEditor = (CainVideoEditor *) handle;
    if (videoEditor != nullptr) {
        const char *srcPath = env->GetStringUTFChars(srcPath_, nullptr);
        const char *dstPath = env->GetStringUTFChars(dstPath_, nullptr);
        if (listener != nullptr) {
            JNIEditListener *editListener = new JNIEditListener(javaVM, env, listener);
            videoEditor->videoReverse(srcPath, dstPath, editListener);
        } else {
            videoEditor->videoReverse(srcPath, dstPath, nullptr);
        }
        env->ReleaseStringUTFChars(dstPath_, dstPath);
        env->ReleaseStringUTFChars(srcPath_, srcPath);
    }
}

#endif  /* defined(__ANDROID__) */