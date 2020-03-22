//
// Created by CainHuang on 2020-02-24.
//

#if defined(__ANDROID__)

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

extern "C" {
#include <libavcodec/jni.h>
}

#include "FFMediaPlayer.h"

static JavaVM *javaVm = nullptr;

static JNIEnv *getJNIEnv() {
    JNIEnv *env;
    assert(javaVm != nullptr);
    if (javaVm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return nullptr;
    }
    return env;
}

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVm = vm;
    JNIEnv *env;
    av_jni_set_java_vm(vm, NULL);
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

//-------------------------------------- JNI回调监听器 ----------------------------------------------
class JNIOnPlayListener : public OnPlayListener {
public:
    JNIOnPlayListener(JavaVM *vm, JNIEnv *env, jobject listener);

    virtual ~JNIOnPlayListener();

    void onPrepared() override;

    void onPlaying(float pts) override;

    void onSeekComplete() override;

    void onCompletion() override;

    void onError(int errorCode, const char *msg) override;

private:
    JNIOnPlayListener() = delete;

    JavaVM *javaVM;
    jobject mJniListener;
    jmethodID jmid_onPrepared;
    jmethodID jmid_onPlaying;
    jmethodID jmid_onSeekComplete;
    jmethodID jmid_onCompletion;
    jmethodID jmid_onError;
};

JNIOnPlayListener::JNIOnPlayListener(JavaVM *vm, JNIEnv *env, jobject listener) {
    this->javaVM = vm;
    if (listener != nullptr) {
        mJniListener = env->NewGlobalRef(listener);
    } else {
        mJniListener = nullptr;
    }

    jclass javaClass = nullptr;
    if (listener != nullptr) {
        javaClass = env->GetObjectClass(listener);
    }
    if (javaClass != nullptr) {
        jmid_onPrepared = env->GetMethodID(javaClass, "onPrepared", "()V");
        jmid_onPlaying = env->GetMethodID(javaClass, "onPlaying", "(F)V");
        jmid_onSeekComplete = env->GetMethodID(javaClass, "onSeekComplete", "()V");
        jmid_onCompletion = env->GetMethodID(javaClass, "onCompletion", "()V");
        jmid_onError = env->GetMethodID(javaClass, "onError", "(ILjava/lang/String;)V");
    } else {
        jmid_onPrepared = nullptr;
        jmid_onPlaying = nullptr;
        jmid_onSeekComplete = nullptr;
        jmid_onCompletion = nullptr;
        jmid_onError = nullptr;
    }
}

JNIOnPlayListener::~JNIOnPlayListener() {
    if (mJniListener != nullptr) {
        JNIEnv *env = getJNIEnv();
        env->DeleteGlobalRef(mJniListener);
        mJniListener = nullptr;
    }
}

void JNIOnPlayListener::onPrepared() {
    if (jmid_onPrepared != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onPrepared);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnPlayListener::onPlaying(float pts) {
    if (jmid_onPlaying != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onPlaying, pts);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnPlayListener::onSeekComplete() {
    if (jmid_onSeekComplete != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onSeekComplete);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnPlayListener::onCompletion() {
    if (jmid_onCompletion != nullptr) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, nullptr) != JNI_OK) {
            return;
        }
        jniEnv->CallVoidMethod(mJniListener, jmid_onCompletion);
        javaVM->DetachCurrentThread();
    }
}

void JNIOnPlayListener::onError(int errorCode, const char *msg) {
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
        jniEnv->CallVoidMethod(mJniListener, jmid_onError, errorCode, jmsg);
        javaVM->DetachCurrentThread();
    }
}

//--------------------------------------------------------------------------------------------------

extern "C" JNIEXPORT jlong JNICALL
Java_com_cgfay_media_VideoPlayer_nativeInit(JNIEnv *env, jobject thiz) {
    auto player = new FFMediaPlayer();
    player->init();
    return (jlong)player;
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        delete player;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setOnPlayListener(JNIEnv *env, jobject thiz, jlong handle, jobject listener) {
    auto player = (FFMediaPlayer *)handle;
    if (player != nullptr) {
        auto playListener = std::make_shared<JNIOnPlayListener>(javaVm, env, listener);
        player->setVideoPlayListener(playListener);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setDataSource(JNIEnv *env, jobject thiz, jlong handle, jstring path_) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        auto path = env->GetStringUTFChars(path_, nullptr);
        player->setDataSource(path);
        env->ReleaseStringUTFChars(path_, path);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setAudioDecoder(JNIEnv *env, jobject thiz, jlong handle, jstring decoder_) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        auto decoder = env->GetStringUTFChars(decoder_, nullptr);
        player->setAudioDecoder(decoder);
        env->ReleaseStringUTFChars(decoder_, decoder);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setVideoDecoder(JNIEnv *env, jobject thiz, jlong handle, jstring decoder_) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        auto decoder = env->GetStringUTFChars(decoder_, nullptr);
        player->setVideoDecoder(decoder);
        env->ReleaseStringUTFChars(decoder_, decoder);
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setVideoSurface(JNIEnv *env, jobject thiz, jlong handle, jobject surface) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        if (surface != nullptr) {
            auto window = ANativeWindow_fromSurface(env, surface);
            player->setVideoSurface(window);
        } else {
            player->setVideoSurface(nullptr);
        }

    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setSpeed(JNIEnv *env, jobject thiz, jlong handle, jfloat speed) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->setSpeed(speed);
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setLooping(JNIEnv *env, jobject thiz, jlong handle, jboolean looping) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->setLooping(looping);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setRange(JNIEnv *env, jobject thiz, jlong handle, jfloat start, jfloat end) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->setRange(start, end);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setVolume(JNIEnv *env, jobject thiz, jlong handle, jfloat leftVolume, jfloat rightVolume) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->setVolume(leftVolume, rightVolume);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_prepare(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->prepare();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_start(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->start();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_pause(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->pause();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_stop(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->stop();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_setDecodeOnPause(JNIEnv *env, jobject thiz, jlong handle, jboolean decodeOnPause) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->setDecodeOnPause(decodeOnPause);
    }
}


extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_VideoPlayer_seekTo(JNIEnv *env, jobject thiz, jlong handle, jfloat timeMs) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        player->seekTo(timeMs);
    }
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_cgfay_media_VideoPlayer_getDuration(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        return player->getDuration();
    }
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_cgfay_media_VideoPlayer_getVideoWidth(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        return player->getVideoWidth();
    }
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_cgfay_media_VideoPlayer_getVideoHeight(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        return player->getVideoHeight();
    }
    return 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cgfay_media_VideoPlayer_isLooping(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        return (jboolean)player->isLooping();
    }
    return 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_cgfay_media_VideoPlayer_isPlaying(JNIEnv *env, jobject thiz, jlong handle) {
    auto player = (FFMediaPlayer *) handle;
    if (player != nullptr) {
        return (jboolean)player->isPlaying();
    }
    return 0;
}

#endif /* defined(__ANDROID__) */