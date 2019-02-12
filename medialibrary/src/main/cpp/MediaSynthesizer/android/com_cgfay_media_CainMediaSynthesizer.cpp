//
// Created by cain on 2019/2/12.
//

#include <jni.h>
#include <Mutex.h>
#include <Condition.h>
#include <Errors.h>
#include <cassert>
#include <CainMediaSynthesizer.h>
#include <AndroidLog.h>
#include <cstdio>
#include <cstring>

extern "C" {
#include <libavcodec/jni.h>
}

const char *CLASS_NAME = "com/cgfay/media/CainMediaSynthesizer";

// -------------------------------------------------------------------------------------------------
struct fields_t {
    jfieldID  context;
    jmethodID post_event;
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

void throwException(JNIEnv* env, const char* className, const char* msg = NULL) {
    jclass exception = env->FindClass(className);
    env->ThrowNew(exception, msg);
}


class JNIMediaSynthesizerListener : public MediaSynthesizerListener {
public:
    JNIMediaSynthesizerListener(JNIEnv *env, jobject thiz, jobject weak_thiz);

    ~JNIMediaSynthesizerListener();

    void notify(int msg, int ext1, int ext2, void *obj) override;

private:
    JNIMediaSynthesizerListener();
    jclass mClass;
    jobject mObject;
};

JNIMediaSynthesizerListener::JNIMediaSynthesizerListener(JNIEnv *env, jobject thiz,
        jobject weak_thiz) {
    // Hold onto the MediaPlayer class for use in calling the static method
    // that posts events to the application thread.
    jclass clazz = env->GetObjectClass(thiz);
    if (clazz == NULL) {
        ALOGE("Can't find com/cgfay/media/CainMediaSynthesizer");
        throwException(env, "java/lang/Exception");
        return;
    }
    mClass = (jclass)env->NewGlobalRef(clazz);

    // We use a weak reference so the MediaPlayer object can be garbage collected.
    // The reference is only used as a proxy for callbacks.
    mObject  = env->NewGlobalRef(weak_thiz);
}

JNIMediaSynthesizerListener::~JNIMediaSynthesizerListener() {
    JNIEnv *env = getJNIEnv();
    env->DeleteGlobalRef(mObject);
    env->DeleteGlobalRef(mClass);
}

void JNIMediaSynthesizerListener::notify(int msg, int ext1, int ext2, void *obj) {
    JNIEnv *env = getJNIEnv();

    bool status = (javaVM->AttachCurrentThread(&env, NULL) >= 0);

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
static CainMediaSynthesizer *getSynthesizer(JNIEnv *env, jobject thiz) {
    CainMediaSynthesizer * const synthesizer = (CainMediaSynthesizer *) env->GetLongField(thiz, fields.context);
    return synthesizer;
}

static CainMediaSynthesizer *setSynthesizer(JNIEnv *env, jobject thiz, long synthesizer) {
    CainMediaSynthesizer *old = (CainMediaSynthesizer *) env->GetLongField(thiz, fields.context);
    env->SetLongField(thiz, fields.context, synthesizer);
    return old;
}

static void process_media_synthesizer_call(JNIEnv *env, jobject thiz, int opStatus,
        const char *exception, const char *message) {

    if (exception == NULL) {  // Don't throw exception. Instead, send an event.
        if (opStatus != (int) OK) {
            CainMediaSynthesizer* synthesizer = getSynthesizer(env, thiz);
            if (synthesizer != NULL) {
                synthesizer->notify(MEDIA_ERROR, opStatus, 0);
            }
        }
    } else {  // Throw exception!
        if ( opStatus == (int) INVALID_OPERATION ) {
            throwException(env, "java/lang/IllegalStateException");
        } else if ( opStatus == (int) PERMISSION_DENIED ) {
            throwException(env, "java/lang/SecurityException");
        } else if ( opStatus != (int) OK ) {
            if (strlen(message) > 230) {
                // if the message is too long, don't bother displaying the status code
                throwException( env, exception, message);
            } else {
                char msg[256];
                // append the status code to the message
                sprintf(msg, "%s: status=0x%X", message, opStatus);
                throwException( env, exception, msg);
            }
        }
    }
}

void CainMediaSynthesizer_setDataSource(JNIEnv *env, jobject thiz, jstring path_) {

}

void CainMediaSynthesizer_setReverseDataSource(JNIEnv *env, jobject thiz, jstring path_) {

}

void CainMediaSynthesizer_setBackgroundDataSource(JNIEnv *env, jobject thiz, jstring path_) {

}

void CainMediaSynthesizer_prepare(JNIEnv *env, jobject thiz) {

}

void CainMediaSynthesizer_start(JNIEnv *env, jobject thiz) {

}

void CainMediaSynthesizer_stop(JNIEnv *env, jobject thiz) {

}

jlong CainMediaSynthesizer_getCurrentPosition(JNIEnv *env, jobject thiz) {
    return 0l;
}

jlong CainMediaSynthesizer_getDuration(JNIEnv *env, jobject thiz) {
    return 0l;
}

void CainMediaSynthesizer_setFilterType(JNIEnv *env, jobject thiz, jint filterType) {

}

void CainMediaSynthesizer_release(JNIEnv *env, jobject thiz) {

}

void CainMediaSynthesizer_reset(JNIEnv *env, jobject thiz) {

}

void CainMediaSynthesizer_setVolume(JNIEnv *env, jobject thiz, jfloat foregroundPercent,
        jfloat backgroundPercent) {

}

void CainMediaSynthesizer_setup(JNIEnv *env, jobject thiz) {

}

void CainMediaSynthesizer_init(JNIEnv *env) {

}

void CainMediaSynthesizer_finalize(JNIEnv *env, jobject thiz) {

}

void CainMediaSynthesizer_setVideoSurface(JNIEnv *env, jobject thiz, jobject surface) {

}

static const JNINativeMethod gMethods[] {
        {"_setDataSource", "(Ljava/lang/String;)V", (void *)CainMediaSynthesizer_setDataSource},
        {"_setReverseDataSource", "(Ljava/lang/String;)V", (void *)CainMediaSynthesizer_setReverseDataSource},
        {"_setBackgroundDataSource", "(Ljava/lang/String;)V", (void *)CainMediaSynthesizer_setBackgroundDataSource},
        {"_setVideoSurface", "(Landroid/view/Surface;)V", (void *) CainMediaSynthesizer_setVideoSurface},
        {"_prepare", "()V", (void *) CainMediaSynthesizer_prepare},
        {"_start", "()V", (void *) CainMediaSynthesizer_start},
        {"_stop", "()V", (void *) CainMediaSynthesizer_stop},
        {"_release", "()V", (void *) CainMediaSynthesizer_release},
        {"_reset", "()V", (void *) CainMediaSynthesizer_reset},
        {"_getCurrentPosition", "()J", (void *) CainMediaSynthesizer_getCurrentPosition},
        {"_getDuration", "()J", (void *) CainMediaSynthesizer_getDuration},
        {"_setFilterType", "(I)V", (void *) CainMediaSynthesizer_setFilterType},
        {"_setVolume", "(FF)V", (void *) CainMediaSynthesizer_setVolume},
        {"native_init", "()V", (void *) CainMediaSynthesizer_init},
        {"native_setup", "(Ljava/lang/Object;)V", (void *) CainMediaSynthesizer_setup},
        {"native_finalize", "()V", (void *) CainMediaSynthesizer_finalize},
};

// 注册CainMediaSynthesizer的Native方法
static int register_com_cgfay_media_CainMediaSynthesizer(JNIEnv *env) {
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
    if (register_com_cgfay_media_CainMediaSynthesizer(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

