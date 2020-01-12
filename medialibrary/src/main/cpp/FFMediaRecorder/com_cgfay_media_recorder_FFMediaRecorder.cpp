//
// Created by CainHuang on 2019/8/17.
//
#if defined(__ANDROID__)

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <Mutex.h>
#include <assert.h>
#include "FFMediaRecorder.h"
#include "AVMediaHeader.h"

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
class JNIOnRecordListener : public OnRecordListener {
public:
    JNIOnRecordListener(JavaVM *vm, JNIEnv *env, jobject listener);

    virtual ~JNIOnRecordListener();

    void onRecordStart() override;

    void onRecording(float duration) override;

    void onRecordFinish(bool success, float duration) override;

    void onRecordError(const char *msg) override;

private:
    JNIOnRecordListener();

    JavaVM *javaVM;
    jobject mJniListener;               // java接口创建的全局对象
    jmethodID jmid_onRecordStart;       // 录制开始回调
    jmethodID jmid_onRecording;         // 正在录制回调
    jmethodID jmid_onRecordFinish;      // 录制完成回调
    jmethodID jmid_onRecordError;       // 录制出错回调
};

JNIOnRecordListener::JNIOnRecordListener(JavaVM *vm, JNIEnv *env, jobject listener) {
    this->javaVM = vm;
    if (listener != nullptr) {
        mJniListener = env->NewGlobalRef(listener);
    } else {
        mJniListener = nullptr;
    }
    jclass javaClass = env->GetObjectClass(listener);
    if (javaClass != nullptr) {
        jmid_onRecordStart = env->GetMethodID(javaClass, "onRecordStart", "()V");
        jmid_onRecording = env->GetMethodID(javaClass, "onRecording", "(F)V");
        jmid_onRecordFinish = env->GetMethodID(javaClass, "onRecordFinish", "(ZF)V");
        jmid_onRecordError = env->GetMethodID(javaClass, "onRecordError", "(Ljava/lang/String;)V");
    } else {
        jmid_onRecordStart = nullptr;
        jmid_onRecording = nullptr;
        jmid_onRecordFinish = nullptr;
        jmid_onRecordError = nullptr;
    }
}

JNIOnRecordListener::~JNIOnRecordListener() {
    if (mJniListener != nullptr) {
        JNIEnv *env = getJNIEnv();
        env->DeleteGlobalRef(mJniListener);
        mJniListener = nullptr;
    }
}

void JNIOnRecordListener::onRecordStart() {
    LOGD("onRecordStart");
    if (jmid_onRecordStart != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onRecordStart);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnRecordListener::onRecording(float duration) {
    LOGD("JNIOnRecordListener onRecording: %f", duration);
    if (jmid_onRecording != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onRecording, duration);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnRecordListener::onRecordFinish(bool success, float duration) {
    LOGD("onRecordFinish: %d", success);
    if (jmid_onRecordFinish != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onRecordFinish, success, duration);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnRecordListener::onRecordError(const char *msg) {
    LOGD("onRecordError: %s", msg);
    if (jmid_onRecordError != nullptr) {
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
        jniEnv->CallVoidMethod(mJniListener, jmid_onRecordError, jmsg);
        javaVM->DetachCurrentThread();
    }
}

//--------------------------------------------------------------------------------------------------

/**
 * 初始化一个录制器对象
 */
extern "C" JNIEXPORT jlong JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_nativeInit(JNIEnv *env, jobject thiz) {
    FFMediaRecorder *recorder = new FFMediaRecorder();
    return (jlong) recorder;
}

/**
 * 释放录制器对象
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    FFMediaRecorder *recorder = (FFMediaRecorder *)handle;
    if (recorder != nullptr) {
        recorder->release();
        LOGD("release FFMediaRecorder");
        delete recorder;
    }
}

/**
 * 设置录制监听器
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setRecordListener(JNIEnv *env, jobject thiz, jlong handle, jobject listener) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        JNIOnRecordListener *recordListener = new JNIOnRecordListener(javaVM, env, listener);
        recorder->setOnRecordListener(recordListener);
    }
}

/**
 * 设置录制输出路径
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setOutput(JNIEnv *env, jobject thiz, jlong handle, jstring dstPath_) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        const char *dstPath = env->GetStringUTFChars(dstPath_, nullptr);
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setOutput(dstPath);
        env->ReleaseStringUTFChars(dstPath_, dstPath);
    }
}

/**
 * 设置音频编码器名称
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setAudioEncoder(JNIEnv *env, jobject thiz, jlong handle, jstring encoder_) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        const char *encoder = env->GetStringUTFChars(encoder_, nullptr);
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setAudioEncoder(encoder);
        env->ReleaseStringUTFChars(encoder_, encoder);
    }
}

/**
 * 设置视频编码器名称
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setVideoEncoder(JNIEnv *env, jobject thiz, jlong handle, jstring encoder_) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        const char *encoder = env->GetStringUTFChars(encoder_, nullptr);
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setVideoEncoder(encoder);
        env->ReleaseStringUTFChars(encoder_, encoder);
    }
}

/**
 * 设置音频AVFilter
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setAudioFilter(JNIEnv *env, jobject thiz, jlong handle, jstring filter_) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        const char *filter = env->GetStringUTFChars(filter_, nullptr);
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setAudioFilter(filter);
        env->ReleaseStringUTFChars(filter_, filter);
    }
}

/**
 * 设置视频AVFilter
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setVideoFilter(JNIEnv *env, jobject thiz, jlong handle, jstring filter_) {

    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        const char *filter = env->GetStringUTFChars(filter_, nullptr);
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setVideoFilter(filter);
        env->ReleaseStringUTFChars(filter_, filter);
    }

}

/**
 * 设置录制视频旋转角度
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setVideoRotate(JNIEnv *env, jobject thiz, jlong handle, jint rotate) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setRotate(rotate);
    }
}

/**
 * 设置视频录制参数
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setVideoParams(JNIEnv *env, jobject thiz, jlong handle,
        jint width, jint height, jint frameRate, jint pixelFormat, jlong maxBitRate, jint quality) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setVideoParams(width, height, frameRate, pixelFormat, maxBitRate, quality);
    }
}

/**
 * 设置音频录制参数
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_setAudioParams(JNIEnv *env, jobject thiz, jlong handle,
        jint sampleRate, jint sampleFormat, jint channels) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        RecordParams *recordParams = recorder->getRecordParams();
        recordParams->setAudioParams(sampleRate, sampleFormat, channels);
    }
}

/**
 * 录制一帧视频帧
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_recordVideoFrame(JNIEnv *env, jobject thiz, jlong handle,
        jbyteArray data_, jint length, jint width, jint height, jint pixelFormat) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr && recorder->isRecording()) {
        uint8_t *yuvData = (uint8_t *) malloc((size_t) length);
        if (yuvData == nullptr) {
            LOGE("Could not allocate memory");
            return -1;
        }
        jbyte *data = env->GetByteArrayElements(data_, nullptr);
        memcpy(yuvData, data, (size_t) length);
        env->ReleaseByteArrayElements(data_, data, 0);

        auto mediaData = new AVMediaData();
        mediaData->setVideo(yuvData, length, width, height, pixelFormat);
        mediaData->setPts(getCurrentTimeMs());
        return recorder->recordFrame(mediaData);
    }
    return -1;
}

/**
 * 录制一帧音频帧
 */
extern "C" JNIEXPORT jint JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_recordAudioFrame(JNIEnv *env, jobject thiz, jlong handle,
        jbyteArray data_, jint length) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr && recorder->isRecording()) {
        uint8_t *pcmData = (uint8_t *) malloc((size_t) length);
        if (pcmData == nullptr) {
            LOGE("Could not allocate memory");
            return -1;
        }
        jbyte *data = env->GetByteArrayElements(data_, nullptr);
        memcpy(pcmData, data, (size_t) length);
        env->ReleaseByteArrayElements(data_, data, 0);

        auto mediaData = new AVMediaData();
        mediaData->setAudio(pcmData, length);
        mediaData->setPts(getCurrentTimeMs());
        return recorder->recordFrame(mediaData);
    }
    return -1;
}

/**
 * 开始录制
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_startRecord(JNIEnv *env, jobject thiz, jlong handle) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        int ret = recorder->prepare();
        if (ret < 0) {
            LOGE("Failed to prepare recorder");
        } else {
            recorder->startRecord();
        }
    }
}

/**
 * 停止录制
 */
extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_recorder_FFMediaRecorder_stopRecord(JNIEnv *env, jobject thiz, jlong handle) {
    FFMediaRecorder *recorder = (FFMediaRecorder *) handle;
    if (recorder != nullptr) {
        recorder->stopRecord();
    }
}

#endif  /* defined(__ANDROID__) */