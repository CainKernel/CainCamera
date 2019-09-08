//
// Created by cain on 2018/11/29.
//

#if defined(__ANDROID__)

#include <jni.h>
#include <Mutex.h>
#include <Condition.h>
#include <Errors.h>
#include <JNIHelp.h>
#include <CainMediaPlayer.h>

extern "C" {
#include <libavcodec/jni.h>
}

const char *CLASS_NAME = "com/cgfay/media/CainMediaPlayer";

// -------------------------------------------------------------------------------------------------
struct fields_t {
    jfieldID    context;
    jmethodID   post_event;
};
static fields_t fields;

static JavaVM *javaVM = NULL;

static JNIEnv *getJNIEnv() {
    JNIEnv *env;
    assert(javaVM != NULL);
    if (javaVM->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return NULL;
    }
    return env;
}

// -------------------------------------------------------------------------------------------------
class JNIMediaPlayerListener : public MediaPlayerListener {
public:
    JNIMediaPlayerListener(JNIEnv* env, jobject thiz, jobject weak_thiz);
    ~JNIMediaPlayerListener();
    void notify(int msg, int ext1, int ext2, void *obj) override;

private:
    JNIMediaPlayerListener();
    jclass mClass;
    jobject mObject;
};

JNIMediaPlayerListener::JNIMediaPlayerListener(JNIEnv *env, jobject thiz, jobject weak_thiz) {
// Hold onto the MediaPlayer class for use in calling the static method
    // that posts events to the application thread.
    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == NULL) {
        ALOGE("Can't find com/cgfay/media/CainMediaPlayer");
        jniThrowException(env, "java/lang/Exception");
        return;
    }
    mClass = (jclass)env->NewGlobalRef(clazz);

    // We use a weak reference so the MediaPlayer object can be garbage collected.
    // The reference is only used as a proxy for callbacks.
    mObject  = env->NewGlobalRef(weak_thiz);
}

JNIMediaPlayerListener::~JNIMediaPlayerListener() {
    JNIEnv *env = getJNIEnv();
    env->DeleteGlobalRef(mObject);
    env->DeleteGlobalRef(mClass);
}

void JNIMediaPlayerListener::notify(int msg, int ext1, int ext2, void *obj) {
    JNIEnv *env = getJNIEnv();

    bool status = (javaVM->AttachCurrentThread(&env, NULL) >= 0);

    // TODO obj needs changing into jobject

    env->CallStaticVoidMethod(mClass, fields.post_event, mObject,
                              msg, ext1, ext2, obj);

    if (env->ExceptionCheck()) {
        ALOGW("An exception occurred while notifying an event.");
        env->ExceptionClear();
    }

    if (status) {
        javaVM->DetachCurrentThread();
    }
}

// -------------------------------------------------------------------------------------------------

static CainMediaPlayer *getMediaPlayer(JNIEnv *env, jobject thiz) {
    CainMediaPlayer * const mp = (CainMediaPlayer *) env->GetLongField(thiz, fields.context);
    return mp;
}

static CainMediaPlayer *setMediaPlayer(JNIEnv *env, jobject thiz, long mediaPlayer) {
    CainMediaPlayer *old = (CainMediaPlayer *) env->GetLongField(thiz, fields.context);
    env->SetLongField(thiz, fields.context, mediaPlayer);
    return old;
}

// If exception is NULL and opStatus is not OK, this method sends an error
// event to the client application; otherwise, if exception is not NULL and
// opStatus is not OK, this method throws the given exception to the client
// application.
static void process_media_player_call(JNIEnv *env, jobject thiz, int opStatus,
        const char* exception, const char *message) {
    if (exception == NULL) {  // Don't throw exception. Instead, send an event.
        if (opStatus != (int) OK) {
            CainMediaPlayer* mp = getMediaPlayer(env, thiz);
            if (mp != 0) mp->notify(MEDIA_ERROR, opStatus, 0);
        }
    } else {  // Throw exception!
        if ( opStatus == (int) INVALID_OPERATION ) {
            jniThrowException(env, "java/lang/IllegalStateException");
        } else if ( opStatus == (int) PERMISSION_DENIED ) {
            jniThrowException(env, "java/lang/SecurityException");
        } else if ( opStatus != (int) OK ) {
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

void CainMediaPlayer_setDataSourceAndHeaders(JNIEnv *env, jobject thiz, jstring path_,
        jobjectArray keys, jobjectArray values) {

    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }

    if (path_ == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException");
        return;
    }

    const char *path = env->GetStringUTFChars(path_, 0);
    if (path == NULL) {
        return;
    }

    const char *restrict = strstr(path, "mms://");
    char *restrict_to = restrict ? strdup(restrict) : NULL;
    if (restrict_to != NULL) {
        strncpy(restrict_to, "mmsh://", 6);
        puts(path);
    }

    char *headers = NULL;
    if (keys && values != NULL) {
        int keysCount = env->GetArrayLength(keys);
        int valuesCount = env->GetArrayLength(values);

        if (keysCount != valuesCount) {
            ALOGE("keys and values arrays have different length");
            jniThrowException(env, "java/lang/IllegalArgumentException");
            return;
        }

        int i = 0;
        const char *rawString = NULL;
        char hdrs[2048];

        for (i = 0; i < keysCount; i++) {
            jstring key = (jstring) env->GetObjectArrayElement(keys, i);
            rawString = env->GetStringUTFChars(key, NULL);
            strcat(hdrs, rawString);
            strcat(hdrs, ": ");
            env->ReleaseStringUTFChars(key, rawString);

            jstring value = (jstring) env->GetObjectArrayElement(values, i);
            rawString = env->GetStringUTFChars(value, NULL);
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

void CainMediaPlayer_setDataSource(JNIEnv *env, jobject thiz, jstring path_) {
    CainMediaPlayer_setDataSourceAndHeaders(env, thiz, path_, NULL, NULL);
}

void CainMediaPlayer_setDataSourceFD(JNIEnv *env, jobject thiz, jobject fileDescriptor,
                                     jlong offset, jlong length) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }

    if (fileDescriptor == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException");
        return;
    }

    int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    if (offset < 0 || length < 0 || fd < 0) {
        if (offset < 0) {
            ALOGE("negative offset (%lld)", offset);
        }
        if (length < 0) {
            ALOGE("negative length (%lld)", length);
        }
        if (fd < 0) {
            ALOGE("invalid file descriptor");
        }
        jniThrowException(env, "java/lang/IllegalArgumentException");
        return;
    }

    char path[256] = "";
    int myfd = dup(fd);
    char str[20];
    sprintf(str, "pipe:%d", myfd);
    strcat(path, str);

    status_t opStatus = mp->setDataSource(path, offset, NULL);
    process_media_player_call( env, thiz, opStatus, "java/io/IOException",
            "setDataSourceFD failed.");

}

void CainMediaPlayer_init(JNIEnv *env) {

    jclass clazz = env->FindClass(CLASS_NAME);
    if (clazz == NULL) {
        return;
    }
    fields.context = env->GetFieldID(clazz, "mNativeContext", "J");
    if (fields.context == NULL) {
        return;
    }

    fields.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
                                               "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if (fields.post_event == NULL) {
        return;
    }

    env->DeleteLocalRef(clazz);
}

void CainMediaPlayer_setup(JNIEnv *env, jobject thiz, jobject mediaplayer_this) {

    CainMediaPlayer *mp = new CainMediaPlayer();
    if (mp == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }

    // 这里似乎存在问题
    // init CainMediaPlayer
    mp->init();

    // create new listener and give it to MediaPlayer
    JNIMediaPlayerListener *listener = new JNIMediaPlayerListener(env, thiz, mediaplayer_this);
    mp->setListener(listener);

    // Stow our new C++ MediaPlayer in an opaque field in the Java object.
    setMediaPlayer(env, thiz, (long)mp);
}

void CainMediaPlayer_release(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp != nullptr) {
        mp->disconnect();
        delete mp;
        setMediaPlayer(env, thiz, 0);
    }
}

void CainMediaPlayer_reset(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->reset();
}

void CainMediaPlayer_finalize(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp != NULL) {
        ALOGW("MediaPlayer finalized without being released");
    }
    CainMediaPlayer_release(env, thiz);
}

void CainMediaPlayer_setVideoSurface(JNIEnv *env, jobject thiz, jobject surface) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    ANativeWindow *window = NULL;
    if (surface != NULL) {
        window = ANativeWindow_fromSurface(env, surface);
    }
    mp->setVideoSurface(window);
}

void CainMediaPlayer_setLooping(JNIEnv *env, jobject thiz, jboolean looping) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->setLooping(looping);
}

jboolean CainMediaPlayer_isLooping(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return JNI_FALSE;
    }
    return (jboolean)(mp->isLooping() ? JNI_TRUE : JNI_FALSE);
}

void CainMediaPlayer_prepare(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }

    mp->prepare();
}

void CainMediaPlayer_prepareAsync(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->prepareAsync();
}

void CainMediaPlayer_start(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->start();
}

void CainMediaPlayer_pause(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->pause();
}

void CainMediaPlayer_resume(JNIEnv *env, jobject thiz) {

    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->resume();

}

void CainMediaPlayer_stop(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->stop();
}

void CainMediaPlayer_seekTo(JNIEnv *env, jobject thiz, jfloat timeMs) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->seekTo(timeMs);
}

void CainMediaPlayer_setMute(JNIEnv *env, jobject thiz, jboolean mute) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->setMute(mute);
}

void CainMediaPlayer_setVolume(JNIEnv *env, jobject thiz, jfloat leftVolume, jfloat rightVolume) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->setVolume(leftVolume, rightVolume);
}

void CainMediaPlayer_setRate(JNIEnv *env, jobject thiz, jfloat speed) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->setRate(speed);
}

void CainMediaPlayer_setPitch(JNIEnv *env, jobject thiz, jfloat pitch) {

    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->setPitch(pitch);
}

jlong CainMediaPlayer_getCurrentPosition(JNIEnv *env, jobject thiz) {

    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0L;
    }
    return mp->getCurrentPosition();
}

jlong CainMediaPlayer_getDuration(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0L;
    }
    return mp->getDuration();
}

jboolean CainMediaPlayer_isPlaying(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return JNI_FALSE;
    }
    return (jboolean)(mp->isPlaying() ? JNI_TRUE : JNI_FALSE);
}

jint CainMediaPlayer_getRotate(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getRotate();
}

jint CainMediaPlayer_getVideoWidth(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getVideoWidth();
}

jint CainMediaPlayer_getVideoHeight(JNIEnv *env, jobject thiz) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return 0;
    }
    return mp->getVideoHeight();
}

void CainMediaPlayer_changeFilter(JNIEnv *env, jobject thiz, int node_type, jstring filterName_) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    const char *name = env->GetStringUTFChars(filterName_, 0);
    mp->changeFilter(node_type, name);
    env->ReleaseStringUTFChars(filterName_, name);
}

void CainMediaPlayer_changeFilterById(JNIEnv *env, jobject thiz, int node_type, jint filterId) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    mp->changeFilter(node_type, filterId);
}

void CainMediaPlayer_setOption(JNIEnv *env, jobject thiz,
        int category, jstring type_, jstring option_) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    const char *type = env->GetStringUTFChars(type_, 0);
    const char *option = env->GetStringUTFChars(option_, 0);
    if (type == NULL || option == NULL) {
        return;
    }

    mp->setOption(category, type, option);

    env->ReleaseStringUTFChars(type_, type);
    env->ReleaseStringUTFChars(option_, option);
}

void CainMediaPlayer_setOptionLong(JNIEnv *env, jobject thiz,
        int category, jstring type_, jlong option_) {
    CainMediaPlayer *mp = getMediaPlayer(env, thiz);
    if (mp == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return;
    }
    const char *type = env->GetStringUTFChars(type_, 0);
    if (type == NULL) {
        return;
    }
    mp->setOption(category, type, option_);

    env->ReleaseStringUTFChars(type_, type);
}


static const JNINativeMethod gMethods[] = {
        {"_setDataSource", "(Ljava/lang/String;)V", (void *)CainMediaPlayer_setDataSource},
        {
            "_setDataSource",
            "(Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V",
            (void *)CainMediaPlayer_setDataSourceAndHeaders
        },
        {"_setDataSource", "(Ljava/io/FileDescriptor;JJ)V", (void *)CainMediaPlayer_setDataSourceFD},
        {"_setVideoSurface", "(Landroid/view/Surface;)V", (void *) CainMediaPlayer_setVideoSurface},
        {"_prepare", "()V", (void *) CainMediaPlayer_prepare},
        {"_prepareAsync", "()V", (void *) CainMediaPlayer_prepareAsync},
        {"_start", "()V", (void *) CainMediaPlayer_start},
        {"_stop", "()V", (void *) CainMediaPlayer_stop},
        {"_resume", "()V", (void *) CainMediaPlayer_resume},
        {"_getRotate", "()I", (void *) CainMediaPlayer_getRotate},
        {"_getVideoWidth", "()I", (void *) CainMediaPlayer_getVideoWidth},
        {"_getVideoHeight", "()I", (void *) CainMediaPlayer_getVideoHeight},
        {"_seekTo", "(F)V", (void *) CainMediaPlayer_seekTo},
        {"_pause", "()V", (void *) CainMediaPlayer_pause},
        {"_isPlaying", "()Z", (void *) CainMediaPlayer_isPlaying},
        {"_getCurrentPosition", "()J", (void *) CainMediaPlayer_getCurrentPosition},
        {"_getDuration", "()J", (void *) CainMediaPlayer_getDuration},
        {"_release", "()V", (void *) CainMediaPlayer_release},
        {"_reset", "()V", (void *) CainMediaPlayer_reset},
        {"_setLooping", "(Z)V", (void *) CainMediaPlayer_setLooping},
        {"_isLooping", "()Z", (void *) CainMediaPlayer_isLooping},
        {"_setVolume", "(FF)V", (void *) CainMediaPlayer_setVolume},
        {"_setMute", "(Z)V", (void *) CainMediaPlayer_setMute},
        {"_setRate", "(F)V", (void *) CainMediaPlayer_setRate},
        {"_setPitch", "(F)V", (void *) CainMediaPlayer_setPitch},
        {"native_init", "()V", (void *)CainMediaPlayer_init},
        {"native_setup", "(Ljava/lang/Object;)V", (void *) CainMediaPlayer_setup},
        {"native_finalize", "()V", (void *) CainMediaPlayer_finalize},
        {"_changeFilter", "(ILjava/lang/String;)V", (void *)CainMediaPlayer_changeFilter},
        {"_changeFilter", "(II)V", (void *)CainMediaPlayer_changeFilterById},
        {"_setOption", "(ILjava/lang/String;Ljava/lang/String;)V", (void *)CainMediaPlayer_setOption},
        {"_setOption", "(ILjava/lang/String;J)V", (void *)CainMediaPlayer_setOptionLong}
};

// 注册CainMediaPlayer的Native方法
static int register_com_cgfay_media_CainMediaPlayer(JNIEnv *env) {
    int numMethods = (sizeof(gMethods) / sizeof( (gMethods)[0]));
    jclass clazz = env->FindClass(CLASS_NAME);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", CLASS_NAME);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("Native registration unable to find class '%s'", CLASS_NAME);
        return JNI_ERR;
    }
    env->DeleteLocalRef(clazz);

    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    av_jni_set_java_vm(vm, NULL);
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if (register_com_cgfay_media_CainMediaPlayer(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

#endif  /* defined(__ANDROID__) */
