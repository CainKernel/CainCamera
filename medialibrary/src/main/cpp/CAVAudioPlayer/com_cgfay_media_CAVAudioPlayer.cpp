//
// Created by CainHuang on 2020-01-30.
//

#if defined(__ANDROID__)

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "player/CAVAudioPlayer.h"
#include <JNIHelp.h>

// 音乐播放器类名
const char *AUDIO_PLAYER = "com/cgfay/media/CAVAudioPlayer";

struct audio_player_t {
    jfieldID  context;
    jmethodID post_event;
};

static audio_player_t audio_player;
static Mutex sLock;

static JavaVM *javaVM = nullptr;

static JNIEnv *getJNIEnv() {
    JNIEnv *env;
    assert(javaVM != nullptr);
    if (javaVM->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return nullptr;
    }
    return env;
}

//-------------------------------------- JNI回调监听器 ----------------------------------------------
class JNIOnPlayListener : public OnPlayListener {
public:
    JNIOnPlayListener(JNIEnv* env, jobject thiz, jobject weak_thiz);

    virtual ~JNIOnPlayListener();

    void notify(int msg, int arg1, int arg2, void *obj) override;

private:
    JNIOnPlayListener() = delete;
    jclass mClass;
    jobject mObject;
};

JNIOnPlayListener::JNIOnPlayListener(JNIEnv* env, jobject thiz, jobject weak_thiz) {
    // Hold onto the CAVAudioPlayer class for use in calling the static method
    // that posts events to the application thread.
    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == nullptr) {
        LOGE("Can't find com/cgfay/media/CAVAudioPlayer");
        jniThrowException(env, "java/lang/Exception");
        return;
    }
    mClass = (jclass)env->NewGlobalRef(clazz);

    // We use a weak reference so the MediaPlayer object can be garbage collected.
    // The reference is only used as a proxy for callbacks.
    mObject  = env->NewGlobalRef(weak_thiz);
}

JNIOnPlayListener::~JNIOnPlayListener() {
    JNIEnv *env = getJNIEnv();
    env->DeleteGlobalRef(mObject);
    env->DeleteGlobalRef(mClass);
    LOGD("JNIOnPlayListener::destructor()");
}

void JNIOnPlayListener::notify(int msg, int arg1, int arg2, void *obj) {
    JNIEnv *env = getJNIEnv();

    bool status = (javaVM->AttachCurrentThread(&env, nullptr) >= 0);

    // TODO obj needs changing into jobject
    env->CallStaticVoidMethod(mClass, audio_player.post_event, mObject,
                              msg, arg1, arg2, obj);

    if (env->ExceptionCheck()) {
        LOGW("An exception occurred while notifying an event.");
        env->ExceptionClear();
    }

    if (status) {
        javaVM->DetachCurrentThread();
    }
}

//--------------------------------------------------------------------------------------------------
/**
 * 获取音乐播放器对象
 */
static CAVAudioPlayer *getCAVAudioPlayer(JNIEnv *env, jobject thiz) {
    Mutex::Autolock l(sLock);
    auto const player = (CAVAudioPlayer *) env->GetLongField(thiz, audio_player.context);
    return player;
}

/**
 * 绑定音乐播放器对象
 */
static CAVAudioPlayer *setCAVAudioPlayer(JNIEnv *env, jobject thiz, long player) {
    Mutex::Autolock l(sLock);
    CAVAudioPlayer *old = (CAVAudioPlayer *) env->GetLongField(thiz, audio_player.context);
    env->SetLongField(thiz, audio_player.context, player);
    return old;
}

/**
 * If exception is nullptr and opStatus is not OK, this method sends an error
 * event to the client application; otherwise, if exception is not nullptr and
 * opStatus is not OK, this method throws the given exception to the client
 * application.
 */
static void process_music_player_call(JNIEnv *env, jobject thiz, status_t opStatus, const char *exception, const char *message) {
    if (exception == nullptr) {  // Don't throw exception. Instead, send an event.
        if (opStatus != (status_t) OK) {
            auto mp = getCAVAudioPlayer(env, thiz);
            if (mp != 0) {
                mp->notify(MEDIA_ERROR, opStatus, 0);
            }
        }
    } else {  // Throw exception!
        if ( opStatus == (status_t) INVALID_OPERATION ) {
            jniThrowException(env, "java/lang/IllegalStateException", nullptr);
        } else if ( opStatus == (status_t) BAD_VALUE ) {
            jniThrowException(env, "java/lang/IllegalArgumentException", nullptr);
        } else if ( opStatus == (status_t) PERMISSION_DENIED ) {
            jniThrowException(env, "java/lang/SecurityException", nullptr);
        } else if ( opStatus != (status_t) OK ) {
            if (strlen(message) > 230) {
                // if the message is too long, don't bother displaying the status code
                jniThrowException( env, exception, message);
            } else {
                char msg[256];
                // append the status code to the message
                sprintf(msg, "%s: status=0x%X", message, opStatus);
                jniThrowException( env, exception, msg);
            }
        }
    }
}

/**
 * 初始化类
 */
static void
com_cgfay_media_CAVAudioPlayer_init(JNIEnv *env) {
    jclass clazz = env->FindClass(AUDIO_PLAYER);
    if (clazz == nullptr) {
        return;
    }
    audio_player.context = env->GetFieldID(clazz, "mNativeContext", "J");
    if (audio_player.context == nullptr) {
        return;
    }
    audio_player.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
                                               "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if (audio_player.post_event == nullptr) {
        return;
    }

    env->DeleteLocalRef(clazz);
}

/**
 * 初始化对象
 */
static void
com_cgfay_media_CAVAudioPlayer_native_setup(JNIEnv *env, jobject thiz, jobject musicplayer_this) {
    auto mp = new CAVAudioPlayer();
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    mp->init();

    // 设置监听器
    auto listener = std::make_shared<JNIOnPlayListener>(env, thiz, musicplayer_this);
    mp->setOnPlayingListener(listener);

    setCAVAudioPlayer(env, thiz, (long)mp);
}

/**
 * 释放资源
 * @param env
 * @param thiz
 */
static void
com_cgfay_media_CAVAudioPlayer_release(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp != nullptr) {
        delete mp;
        setCAVAudioPlayer(env, thiz, 0);
    }
}

/**
 * finalize
 */
static void
com_cgfay_media_CAVAudioPlayer_native_finalize(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr ) {
        LOGW("CAVAudioPlayer finalized without being released");
    }
    com_cgfay_media_CAVAudioPlayer_release(env, thiz);
}

/**
 * 设置播放路径
 */
static void
com_cgfay_media_CAVAudioPlayer_setDataSource(JNIEnv *env, jobject thiz, jstring path_) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    const char *path = env->GetStringUTFChars(path_, nullptr);
    status_t opStatus = mp->setDataSource(path);
    env->ReleaseStringUTFChars(path_, path);
    process_music_player_call(env, thiz, opStatus, "java/io/IOException", "setDataSource failed.");
}

/**
 * 设置播放速度
 */
static void
com_cgfay_media_CAVAudioPlayer_setSpeed(JNIEnv *env, jobject thiz, jfloat speed) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->setSpeed(speed), nullptr, nullptr);
}

/**
 * 设置是否循环播放
 */
static void
com_cgfay_media_CAVAudioPlayer_setLooping(JNIEnv *env, jobject thiz, jboolean looping) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->setLooping(looping), nullptr, nullptr);
}

/**
 * 设置播放区间
 */
static void
com_cgfay_media_CAVAudioPlayer_setRange(JNIEnv *env, jobject thiz, jfloat start, jfloat end) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->setRange(start, end), nullptr, nullptr);
}

/**
 * 设置播放音量
 */
static void
com_cgfay_media_CAVAudioPlayer_setVolume(JNIEnv *env, jobject thiz, jfloat leftVolume, jfloat rightVolume) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->setVolume(leftVolume, rightVolume), nullptr, nullptr);
}

/**
 * 准备播放器
 */
static void
com_cgfay_media_CAVAudioPlayer_prepare(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->prepare(), nullptr, nullptr);
}

/**
 * 开始播放
 */
static void
com_cgfay_media_CAVAudioPlayer_start(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->start(), nullptr, nullptr);
}

/**
 * 暂停播放
 */
static void
com_cgfay_media_CAVAudioPlayer_pause(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->pause(), nullptr, nullptr);
}

/**
 * 停止播放
 */
static void
com_cgfay_media_CAVAudioPlayer_stop(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->stop(), nullptr, nullptr);
}

/**
 * 跳转
 */
static void
com_cgfay_media_CAVAudioPlayer_seekTo(JNIEnv *env, jobject thiz, jfloat timeMs) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_music_player_call(env, thiz, mp->seekTo(timeMs), nullptr, nullptr);
}

/**
 * 获取时长
 */
static jfloat
com_cgfay_media_CAVAudioPlayer_getDuration(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getDuration();
}

/**
 * 是否循环播放
 */
static jboolean
com_cgfay_media_CAVAudioPlayer_isLooping(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return JNI_FALSE;
    }
    return mp->isLooping() ? JNI_TRUE : JNI_FALSE;
}

/**
 * 是否正在播放
 * @param env
 * @param thiz
 * @return
 */
static jboolean
com_cgfay_media_CAVAudioPlayer_isPlaying(JNIEnv *env, jobject thiz) {
    auto mp = getCAVAudioPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return JNI_FALSE;
    }
    const jboolean is_playing = mp->isPlaying();
    LOGV("isPlaying: %d", is_playing);
    return is_playing;
}

static const JNINativeMethod gMethods[] = {
        {"native_init", "()V", (void *)com_cgfay_media_CAVAudioPlayer_init},
        {"native_setup", "(Ljava/lang/Object;)V", (void *)com_cgfay_media_CAVAudioPlayer_native_setup},
        {"native_finalize", "()V", (void *)com_cgfay_media_CAVAudioPlayer_native_finalize},
        {"_release", "()V", (void *)com_cgfay_media_CAVAudioPlayer_release},
        {"_setDataSource", "(Ljava/lang/String;)V", (void *)com_cgfay_media_CAVAudioPlayer_setDataSource},
        {"_setSpeed", "(F)V", (void *)com_cgfay_media_CAVAudioPlayer_setSpeed},
        {"_setLooping", "(Z)V", (void *)com_cgfay_media_CAVAudioPlayer_setLooping},
        {"_setRange", "(FF)V", (void *)com_cgfay_media_CAVAudioPlayer_setRange},
        {"_setVolume", "(FF)V", (void *)com_cgfay_media_CAVAudioPlayer_setVolume},
        {"_prepare", "()V", (void *)com_cgfay_media_CAVAudioPlayer_prepare},
        {"_start", "()V", (void *)com_cgfay_media_CAVAudioPlayer_start},
        {"_pause", "()V", (void *)com_cgfay_media_CAVAudioPlayer_pause},
        {"_stop", "()V", (void *)com_cgfay_media_CAVAudioPlayer_stop},
        {"_seekTo", "(F)V", (void *)com_cgfay_media_CAVAudioPlayer_seekTo},
        {"_getDuration", "()F", (void *)com_cgfay_media_CAVAudioPlayer_getDuration},
        {"_isLooping", "()Z", (void *)com_cgfay_media_CAVAudioPlayer_isLooping},
        {"_isPlaying", "()Z", (void *)com_cgfay_media_CAVAudioPlayer_isPlaying},
};

/**
 * 动态注册CAVAudioPlayer的Native方法
 */
static int register_com_cgfay_media_CAVAudioPlayer(JNIEnv *env) {
    int numMethods = (sizeof(gMethods) / sizeof( (gMethods)[0]));
    jclass clazz = env->FindClass(AUDIO_PLAYER);
    if (clazz == nullptr) {
        LOGE("Native registration unable to find class '%s'", AUDIO_PLAYER);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("Native registration unable to find class '%s'", AUDIO_PLAYER);
        return JNI_ERR;
    }
    env->DeleteLocalRef(clazz);
    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if (register_com_cgfay_media_CAVAudioPlayer(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

#endif /* defined(__ANDROID__) */