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
#include <JNIHelp.h>
#include <player/AVMediaPlayer.h>

extern "C" {
#include <libavcodec/jni.h>
}

#include "player/AVMediaPlayer.h"

const char *VIDEO_PLAYER = "com/cgfay/media/VideoPlayer";

struct media_player_t {
    jfieldID  context;
    jmethodID post_event;
};

static media_player_t media_player;
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
    // Hold onto the VideoPlayer class for use in calling the static method
    // that posts events to the application thread.
    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == nullptr) {
        LOGE("Can't find com/cgfay/media/VideoPlayer");
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
    env->CallStaticVoidMethod(mClass, media_player.post_event, mObject,
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
 * 获取媒体播放器对象
 */
static AVMediaPlayer *getVideoPlayer(JNIEnv *env, jobject thiz) {
    Mutex::Autolock l(sLock);
    auto const player = (AVMediaPlayer *) env->GetLongField(thiz, media_player.context);
    return player;
}

/**
 * 绑定媒体播放器对象
 */
static AVMediaPlayer *setVideoPlayer(JNIEnv *env, jobject thiz, long player) {
    Mutex::Autolock l(sLock);
    AVMediaPlayer *old = (AVMediaPlayer *) env->GetLongField(thiz, media_player.context);
    env->SetLongField(thiz, media_player.context, player);
    return old;
}

/**
 * If exception is nullptr and opStatus is not OK, this method sends an error
 * event to the client application; otherwise, if exception is not nullptr and
 * opStatus is not OK, this method throws the given exception to the client
 * application.
 */
static void process_media_player_call(JNIEnv *env, jobject thiz, status_t opStatus, const char *exception, const char *message) {
    if (exception == nullptr) {  // Don't throw exception. Instead, send an event.
        if (opStatus != (status_t) OK) {
            auto mp = getVideoPlayer(env, thiz);
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
com_cgfay_media_VideoPlayer_init(JNIEnv *env) {
    jclass clazz = env->FindClass(VIDEO_PLAYER);
    if (clazz == nullptr) {
        return;
    }
    media_player.context = env->GetFieldID(clazz, "mNativeContext", "J");
    if (media_player.context == nullptr) {
        return;
    }
    media_player.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
                                                     "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if (media_player.post_event == nullptr) {
        return;
    }

    env->DeleteLocalRef(clazz);
}

/**
 * 初始化对象
 */
static void
com_cgfay_media_VideoPlayer_native_setup(JNIEnv *env, jobject thiz, jobject musicplayer_this) {
    auto mp = new AVMediaPlayer();
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    mp->init();

    // 设置监听器
    auto listener = std::make_shared<JNIOnPlayListener>(env, thiz, musicplayer_this);
    mp->setOnPlayingListener(listener);

    setVideoPlayer(env, thiz, (long)mp);
}

/**
 * 释放资源
 * @param env
 * @param thiz
 */
static void
com_cgfay_media_VideoPlayer_release(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp != nullptr) {
        delete mp;
        setVideoPlayer(env, thiz, 0);
    }
}

/**
 * 重置
 */
static void
com_cgfay_media_VideoPlayer_reset(JNIEnv *env, jobject thiz) {

}

/**
 * finalize
 */
static void
com_cgfay_media_VideoPlayer_native_finalize(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr ) {
        LOGW("VideoPlayer finalized without being released");
    }
    com_cgfay_media_VideoPlayer_release(env, thiz);
}

/**
 * 设置播放路径
 */
static void
com_cgfay_media_VideoPlayer_setDataSourceAndHeaders(JNIEnv *env, jobject thiz, jstring path_,
        jobjectArray keys, jobjectArray values) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    if (path_ == nullptr) {
        jniThrowException(env, "java/lang/IllegalArgumentException");
        return;
    }

    const char *path = env->GetStringUTFChars(path_, 0);
    if (path == nullptr) {
        return;
    }

    const char *restrict = strstr(path, "mms://");
    char *restrict_to = restrict ? strdup(restrict) : nullptr;
    if (restrict_to != nullptr) {
        strncpy(restrict_to, "mmsh://", 6);
        puts(path);
    }

    char *headers = nullptr;
    if (keys && values != nullptr) {
        int keysCount = env->GetArrayLength(keys);
        int valuesCount = env->GetArrayLength(values);

        if (keysCount != valuesCount) {
            LOGE("keys and values arrays have different length");
            jniThrowException(env, "java/lang/IllegalArgumentException");
            return;
        }

        int i = 0;
        const char *rawString = nullptr;
        char hdrs[2048];

        for (i = 0; i < keysCount; i++) {
            jstring key = (jstring) env->GetObjectArrayElement(keys, i);
            rawString = env->GetStringUTFChars(key, nullptr);
            strcat(hdrs, rawString);
            strcat(hdrs, ": ");
            env->ReleaseStringUTFChars(key, rawString);

            jstring value = (jstring) env->GetObjectArrayElement(values, i);
            rawString = env->GetStringUTFChars(value, nullptr);
            strcat(hdrs, rawString);
            strcat(hdrs, "\r\n");
            env->ReleaseStringUTFChars(value, rawString);
        }

        headers = &hdrs[0];
    }

    status_t opStatus = mp->setDataSource(path, 0, headers);
    process_media_player_call(env, thiz, opStatus, "java/io/IOException",
                              "setDataSource failed." );

    env->ReleaseStringUTFChars(path_, path);
}

/**
 * 设置播放路径
 */
static void
com_cgfay_media_VideoPlayer_setDataSource(JNIEnv *env, jobject thiz, jstring path_) {
    com_cgfay_media_VideoPlayer_setDataSourceAndHeaders(env, thiz, path_, nullptr, nullptr);
}

/**
 * 设置播放路径
 */
static void
com_cgfay_media_VideoPlayer_setDataSourceFD(JNIEnv *env, jobject thiz, jobject fileDescriptor,
        jlong offset, jlong length) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    if (fileDescriptor == nullptr) {
        jniThrowException(env, "java/lang/IllegalArgumentException");
        return;
    }

    int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (offset < 0 || length < 0 || fd < 0) {
        if (offset < 0) {
            LOGE("negative offset (%lld)", offset);
        }
        if (length < 0) {
            LOGE("negative length (%lld)", length);
        }
        if (fd < 0) {
            LOGE("invalid file descriptor");
        }
        jniThrowException(env, "java/lang/IllegalArgumentException");
        return;
    }

    char path[256] = "";
    int myfd = dup(fd);
    char str[20];
    sprintf(str, "pipe:%d", myfd);
    strcat(path, str);

    status_t opStatus = mp->setDataSource(path, offset, nullptr);
    process_media_player_call( env, thiz, opStatus, "java/io/IOException",
                               "setDataSourceFD failed.");
}

/**
 * 设置音频解码器
 */
static void
com_cgfay_media_VideoPlayer_setAudioDecoder(JNIEnv *env, jobject thiz, jstring decoder_) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    const char *audioDecoder = env->GetStringUTFChars(decoder_, nullptr);
    status_t opStatus = mp->setAudioDecoder(audioDecoder);
    env->ReleaseStringUTFChars(decoder_, audioDecoder);
    process_media_player_call(env, thiz, opStatus, "java/io/IOException", "setAudioDecoder failed.");
}

/**
 * 设置视频解码器
 */
static void
com_cgfay_media_VideoPlayer_setVideoDecoder(JNIEnv *env, jobject thiz, jstring decoder_) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    const char *videoDecoder = env->GetStringUTFChars(decoder_, nullptr);
    status_t opStatus = mp->setVideoDecoder(videoDecoder);
    env->ReleaseStringUTFChars(decoder_, videoDecoder);
    process_media_player_call(env, thiz, opStatus, "java/io/IOException", "setAudioDecoder failed.");
}

/**
 * 设置surface
 */
static void
com_cgfay_media_VideoPlayer_setVideoSurface(JNIEnv *env, jobject thiz, jobject surface) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    ANativeWindow *window = nullptr;
    if (surface != nullptr) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    process_media_player_call(env, thiz, mp->setVideoSurface(window), nullptr, nullptr);
}

/**
 * 设置播放速度
 */
static void
com_cgfay_media_VideoPlayer_setSpeed(JNIEnv *env, jobject thiz, jfloat speed) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->setSpeed(speed), nullptr, nullptr);
}

/**
 * 设置是否循环播放
 */
static void
com_cgfay_media_VideoPlayer_setLooping(JNIEnv *env, jobject thiz, jboolean looping) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->setLooping(looping), nullptr, nullptr);
}

/**
 * 设置播放区间
 */
static void
com_cgfay_media_VideoPlayer_setRange(JNIEnv *env, jobject thiz, jfloat start, jfloat end) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->setRange(start, end), nullptr, nullptr);
}

/**
 * 设置播放音量
 */
static void
com_cgfay_media_VideoPlayer_setVolume(JNIEnv *env, jobject thiz, jfloat leftVolume, jfloat rightVolume) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->setVolume(leftVolume, rightVolume), nullptr, nullptr);
}

/**
 * 设置是否静音
 */
static void
com_cgfay_media_VideoPlayer_setMute(JNIEnv *env, jobject thiz, jboolean mute) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->setMute(mute), nullptr, nullptr);
}

/**
 * 准备播放器
 */
static void
com_cgfay_media_VideoPlayer_prepare(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->prepare(), nullptr, nullptr);
}

/**
 * 开始播放
 */
static void
com_cgfay_media_VideoPlayer_start(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->start(), nullptr, nullptr);
}

/**
 * 暂停播放
 */
static void
com_cgfay_media_VideoPlayer_pause(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->pause(), nullptr, nullptr);
}

/**
 * 继续播放
 */
static void
com_cgfay_media_VideoPlayer_resume(JNIEnv *env, jobject thiz) {
    com_cgfay_media_VideoPlayer_start(env, thiz);
}

/**
 * 停止播放
 */
static void
com_cgfay_media_VideoPlayer_stop(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->stop(), nullptr, nullptr);
}

/**
 * 设置是否允许暂停状态下解码
 */
static void
com_cgfay_media_VideoPlayer_setDecodeOnPause(JNIEnv *env, jobject thiz, jboolean decodeOnPause) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->setDecodeOnPause(decodeOnPause), nullptr, nullptr);
}

/**
 * 跳转
 */
static void
com_cgfay_media_VideoPlayer_seekTo(JNIEnv *env, jobject thiz, jfloat timeMs) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    process_media_player_call(env, thiz, mp->seekTo(timeMs), nullptr, nullptr);
}

/**
 * 获取当前时钟
 * @param env
 * @param thiz
 * @return
 */
static jlong
com_cgfay_media_VideoPlayer_getCurrentPosition(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getCurrentPosition();
}

/**
 * 获取时长
 */
static jlong
com_cgfay_media_VideoPlayer_getDuration(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getDuration();
}

/**
 * 获取旋转角度
 */
static jint
com_cgfay_media_VideoPlayer_getRotate(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getRotate();
}

/**
 * 获取视频宽度
 */
static jint
com_cgfay_media_VideoPlayer_getVideoWidth(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getVideoWidth();
}

/**
 * 获取视频高度
 */
static jint
com_cgfay_media_VideoPlayer_getVideoHeight(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getVideoHeight();
}

/**
 * 是否循环播放
 */
static jboolean
com_cgfay_media_VideoPlayer_isLooping(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
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
com_cgfay_media_VideoPlayer_isPlaying(JNIEnv *env, jobject thiz) {
    auto mp = getVideoPlayer(env, thiz);
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return JNI_FALSE;
    }
    const jboolean is_playing = mp->isPlaying();
    LOGV("isPlaying: %d", is_playing);
    return is_playing;
}

//--------------------------------------------------------------------------------------------------

static const JNINativeMethod gMethods[] = {
        {"native_init", "()V", (void *)com_cgfay_media_VideoPlayer_init},
        {"native_setup", "(Ljava/lang/Object;)V", (void *) com_cgfay_media_VideoPlayer_native_setup},
        {"native_finalize", "()V", (void *) com_cgfay_media_VideoPlayer_native_finalize},
        {"_release", "()V", (void *) com_cgfay_media_VideoPlayer_release},
        {"_reset", "()V", (void *) com_cgfay_media_VideoPlayer_reset},
        {"_setDataSource", "(Ljava/lang/String;)V", (void *)com_cgfay_media_VideoPlayer_setDataSource},
        {
         "_setDataSource",
                           "(Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V",
                                                    (void *)com_cgfay_media_VideoPlayer_setDataSourceAndHeaders
        },
        {"_setDataSource", "(Ljava/io/FileDescriptor;JJ)V", (void *)com_cgfay_media_VideoPlayer_setDataSourceFD},
        {"_setAudioDecoder", "(Ljava/lang/String;)V", (void *)com_cgfay_media_VideoPlayer_setAudioDecoder},
        {"_setVideoDecoder", "(Ljava/lang/String;)V", (void *)com_cgfay_media_VideoPlayer_setVideoDecoder},
        {"_setVideoSurface", "(Landroid/view/Surface;)V", (void *) com_cgfay_media_VideoPlayer_setVideoSurface},
        {"_setSpeed", "(F)V", (void *) com_cgfay_media_VideoPlayer_setSpeed},
        {"_setLooping", "(Z)V", (void *) com_cgfay_media_VideoPlayer_setLooping},
        {"_setRange", "(FF)V", (void *) com_cgfay_media_VideoPlayer_setRange},
        {"_setVolume", "(FF)V", (void *) com_cgfay_media_VideoPlayer_setVolume},
        {"_setMute", "(Z)V", (void *) com_cgfay_media_VideoPlayer_setMute},
        {"_prepare", "()V", (void *) com_cgfay_media_VideoPlayer_prepare},
        {"_start", "()V", (void *) com_cgfay_media_VideoPlayer_start},
        {"_pause", "()V", (void *) com_cgfay_media_VideoPlayer_pause},
        {"_resume", "()V", (void *) com_cgfay_media_VideoPlayer_resume},
        {"_stop", "()V", (void *) com_cgfay_media_VideoPlayer_stop},
        {"_setDecodeOnPause", "(Z)V", (void *) com_cgfay_media_VideoPlayer_setDecodeOnPause},
        {"_seekTo", "(F)V", (void *) com_cgfay_media_VideoPlayer_seekTo},
        {"_getCurrentPosition", "()J", (void *) com_cgfay_media_VideoPlayer_getCurrentPosition},
        {"_getDuration", "()J", (void *) com_cgfay_media_VideoPlayer_getDuration},
        {"_getRotate", "()I", (void *) com_cgfay_media_VideoPlayer_getRotate},
        {"_getVideoWidth", "()I", (void *) com_cgfay_media_VideoPlayer_getVideoWidth},
        {"_getVideoHeight", "()I", (void *) com_cgfay_media_VideoPlayer_getVideoHeight},
        {"_isLooping", "()Z", (void *) com_cgfay_media_VideoPlayer_isLooping},
        {"_isPlaying", "()Z", (void *) com_cgfay_media_VideoPlayer_isPlaying}
};

/**
 * 动态注册VideoPlayer的Native方法
 */
static int register_com_cgfay_media_VideoPlayer(JNIEnv *env) {
    int numMethods = (sizeof(gMethods) / sizeof( (gMethods)[0]));
    jclass clazz = env->FindClass(VIDEO_PLAYER);
    if (clazz == nullptr) {
        LOGE("Native registration unable to find class '%s'", VIDEO_PLAYER);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("Native registration unable to find class '%s'", VIDEO_PLAYER);
        return JNI_ERR;
    }
    env->DeleteLocalRef(clazz);
    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    JNIEnv *env;
    av_jni_set_java_vm(vm, nullptr);
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if (register_com_cgfay_media_VideoPlayer(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}
#endif /* defined(__ANDROID__) */