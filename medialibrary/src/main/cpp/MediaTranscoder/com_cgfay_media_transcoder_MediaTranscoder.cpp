//
// Created by CainHuang on 2020/1/4.
//

#if defined(__ANDROID__)

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <Mutex.h>
#include <assert.h>
#include "AVMediaHeader.h"
#include "OnTranscodeListener.h"
#include "MediaTranscoder.h"
#include "TranscodeParams.h"

extern "C" {
#include <libavcodec/jni.h>
};

static JavaVM *javaVM = nullptr;

static JNIEnv *getJNIEnv() {
    JNIEnv *env;
    assert(javaVM != nullptr);
    if (javaVM->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return nullptr;
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
class JNIOnTranscodeListener : public OnTranscodeListener {
public:
    JNIOnTranscodeListener(JavaVM *vm, JNIEnv *env, jobject listener);

    virtual ~JNIOnTranscodeListener();

    // 转码开始
    void onTranscodeStart() override ;

    // 正在转码
    void onTranscoding(float duration) override;

    // 转码完成
    void onTranscodeFinish(bool success, float duration) override;

    // 转码出错
    void onTranscodeError(const char *msg) override;

private:
    JNIOnTranscodeListener();

    JavaVM *javaVM;
    jobject mJniListener;               // java接口创建的全局对象
    jmethodID jmid_onTranscodeStart;    // 转码开始回调
    jmethodID jmid_onTranscoding;       // 正在转码回调
    jmethodID jmid_onTranscodeFinish;   // 转码完成回调
    jmethodID jmid_onTranscodeError;    // 转码出错回调
};

JNIOnTranscodeListener::JNIOnTranscodeListener(JavaVM *vm, JNIEnv *env, jobject listener) {
    this->javaVM = vm;
    if (listener != nullptr) {
        mJniListener = env->NewGlobalRef(listener);
    } else {
        mJniListener = nullptr;
    }
    jclass javaClass = env->GetObjectClass(listener);
    if (javaClass != nullptr) {
        jmid_onTranscodeStart = env->GetMethodID(javaClass, "onTranscodeStart", "()V");
        jmid_onTranscoding = env->GetMethodID(javaClass, "onTranscoding", "(F)V");
        jmid_onTranscodeFinish = env->GetMethodID(javaClass, "onTranscodeFinish", "(ZF)V");
        jmid_onTranscodeError = env->GetMethodID(javaClass, "onTranscodeError", "(Ljava/lang/String;)V");
    } else {
        jmid_onTranscodeStart = nullptr;
        jmid_onTranscoding = nullptr;
        jmid_onTranscodeFinish = nullptr;
        jmid_onTranscodeError = nullptr;
    }
}

JNIOnTranscodeListener::~JNIOnTranscodeListener() {
    if (mJniListener != nullptr) {
        JNIEnv *env = getJNIEnv();
        env->DeleteGlobalRef(mJniListener);
        mJniListener = nullptr;
    }
}

void JNIOnTranscodeListener::onTranscodeStart() {
    if (jmid_onTranscodeStart != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onTranscodeStart);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnTranscodeListener::onTranscoding(float duration) {
    if (jmid_onTranscoding != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onTranscoding, duration);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnTranscodeListener::onTranscodeFinish(bool success, float duration) {
    if (jmid_onTranscodeFinish != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onTranscodeFinish, success, duration);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnTranscodeListener::onTranscodeError(const char *msg) {
    if (jmid_onTranscodeError != nullptr) {
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
        jniEnv->CallVoidMethod(mJniListener, jmid_onTranscodeError, jmsg);
        javaVM->DetachCurrentThread();
    }
}

//--------------------------------------------------------------------------------------------------
/**
 * 初始化一个转码器
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_nativeInit(JNIEnv *env, jobject thiz) {
    MediaTranscoder *transcoder = new MediaTranscoder();
    return (jlong) transcoder;
}

/**
 * 释放转码器
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        transcoder->release();
        delete transcoder;
    }
}

/**
 * 设置转码监听器
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_setTranscodeListener(JNIEnv *env, jobject thiz, jlong handle, jobject listener) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        JNIOnTranscodeListener *transcodeListener = new JNIOnTranscodeListener(javaVM, env, listener);
        transcoder->setOnTranscodeListener(transcodeListener);
    }
}

/**
 * 设置输出路径
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_setOutputPath(JNIEnv *env, jobject thiz, jlong handle, jstring dstPath_) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        const char *dstPath = env->GetStringUTFChars(dstPath_, nullptr);
        TranscodeParams *params = transcoder->getParams();
        params->setOutput(dstPath);
        env->ReleaseStringUTFChars(dstPath_, dstPath);
    }
}

/**
 * 设置旋转角度
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_setVideoRotate(JNIEnv *env, jobject thiz, jlong handle, jint rotate) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        TranscodeParams *params = transcoder->getParams();
        params->setRotate(rotate);
    }
}

/**
 * 设置视频参数
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_setVideoParams(JNIEnv *env, jobject thiz, jlong handle,
        jint width, jint height, jint frameRate, jint pixelFormat, jlong maxBitRate, jint quality) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        TranscodeParams *params = transcoder->getParams();
        params->setVideoParams(width, height, frameRate, pixelFormat, maxBitRate, quality);
    }
}

/**
 * 设置音频参数
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_setAudioParams(JNIEnv *env, jobject thiz, jlong handle,
        jint sampleRate, jint sampleFormat, jint channels) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        TranscodeParams *params = transcoder->getParams();
        params->setAudioParams(sampleRate, sampleFormat, channels);
    }
}

/**
 * 设置是否使用硬解硬编
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_setUseHardCodec(JNIEnv *env, jobject thiz, jlong handle, jboolean useHardCodec) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        TranscodeParams *params = transcoder->getParams();
        params->setUseHardCodec(useHardCodec);
    }
}

/**
 * 开始转码
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_startTranscode(JNIEnv *env, jobject thiz, jlong handle) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        transcoder->startTranscode();
    }
}

/**
 * 停止转码
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_transcoder_MediaTranscoder_stopTranscode(JNIEnv *env, jobject thiz, jlong handle) {
    MediaTranscoder *transcoder = (MediaTranscoder *) handle;
    if (transcoder != nullptr) {
        transcoder->stopTranscode();
    }
}

#endif /* defined(__ANDROID__) */