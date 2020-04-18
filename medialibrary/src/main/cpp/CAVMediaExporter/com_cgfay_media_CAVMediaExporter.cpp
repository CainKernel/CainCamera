//
// Created by CainHuang on 2020-04-04.
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
#include <Mutex.h>
#include <exporter/CAVMediaExporter.h>
#include <AVMediaHeader.h>

extern "C" {
#include <libavcodec/jni.h>
}

const char *MEDIA_EXPORTER = "com/cgfay/media/CAVMediaExporter";

struct media_editor_t {
    jfieldID  context;
    jmethodID post_event;
};

static media_editor_t media_editor;
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

//--------------------------------------------------------------------------------------------------

/**
 * 获取媒体编辑合成器
 */
static CAVMediaExporter *getCAVMediaExporter(JNIEnv *env, jobject thiz) {
    Mutex::Autolock l(sLock);
    auto const composer = (CAVMediaExporter *) env->GetLongField(thiz, media_editor.context);
    return composer;
}

/**
 * 绑定媒体编辑合成器
 */
static CAVMediaExporter *setCAVMediaExporter(JNIEnv *env, jobject thiz, long composer) {
    Mutex::Autolock l(sLock);
    auto old = (CAVMediaExporter *) env->GetLongField(thiz, media_editor.context);
    env->SetLongField(thiz, media_editor.context, composer);
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
            auto mp = getCAVMediaExporter(env, thiz);
            if (mp != 0) {
//                mp->notify(EXPORT_ERROR, opStatus, 0);
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
 * @param env
 */
static void
com_cgfay_media_CAVMediaExporter_init(JNIEnv *env) {
    jclass clazz = env->FindClass(MEDIA_EXPORTER);
    if (clazz == nullptr) {
        return;
    }
    media_editor.context = env->GetFieldID(clazz, "mNativeContext", "J");
    if (media_editor.context == nullptr) {
        return;
    }
    media_editor.post_event = env->GetStaticMethodID(clazz, "postEventFromNative",
            "(Ljava/lang/Object;IIILjava/lang/Object;)V");
    if (media_editor.post_event == nullptr) {
        return;
    }
    env->DeleteLocalRef(clazz);
}

/**
 * 初始化对象
 */
static void
com_cgfay_media_CAVMediaExporter_native_setup(JNIEnv *env, jobject thiz, jobject composer_this) {
    auto mp = new CAVMediaExporter();
    if (mp == nullptr) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    mp->init();

    // todo 设置监听器

    setCAVMediaExporter(env, thiz, (long)mp);
}

/**
 * 释放资源
 */
static void
com_cgfay_media_CAVMediaExporter_release(JNIEnv *env, jobject thiz) {
    auto mp = getCAVMediaExporter(env, thiz);
    if (mp != nullptr) {
        delete mp;
        setCAVMediaExporter(env, thiz, 0);
    }
}

/**
 * finalize
 */
static void
com_cgfay_media_CAVMediaExporter_native_finalize(JNIEnv *env, jobject thiz) {
    auto mp = getCAVMediaExporter(env, thiz);
    if (mp == nullptr) {
        LOGW("CAVMediaExporter finalized without being released");
    }
    com_cgfay_media_CAVMediaExporter_release(env, thiz);
}

/**
 * 导出媒体
 */
static void
com_cgfay_media_CAVMediaExporter_export(JNIEnv *env, jobject thiz) {

}

/**
 * 取消导出
 */
static void
com_cgfay_media_CAVMediaExporter_cancel(JNIEnv *env, jobject thiz) {

}

/**
 * 是否正在导出
 */
static jboolean
com_cgfay_media_CAVMediaExporter_isExporting(JNIEnv *env, jobject thiz) {

    return JNI_FALSE;
}

static const JNINativeMethod gMethods[] = {
        {"native_init", "()V", (void *)com_cgfay_media_CAVMediaExporter_init},
        {"native_setup", "(Ljava/lang/Object;)V", (void *)com_cgfay_media_CAVMediaExporter_native_setup},
        {"native_finalize", "()V", (void *)com_cgfay_media_CAVMediaExporter_native_finalize},
        {"_release", "()V", (void *)com_cgfay_media_CAVMediaExporter_release},
        {"_export", "()V", (void *)com_cgfay_media_CAVMediaExporter_export},
        {"_cancel", "()V", (void *)com_cgfay_media_CAVMediaExporter_cancel},
        {"_isExporting", "()Z", (void *)com_cgfay_media_CAVMediaExporter_isExporting},
};

static int register_com_cgfay_media_CAVMediaExporter(JNIEnv *env) {
    int numMethods = (sizeof(gMethods) / sizeof( (gMethods)[0]));
    jclass clazz = env->FindClass(MEDIA_EXPORTER);
    if (clazz == nullptr) {
        LOGE("Native registration unable to find class '%s'", MEDIA_EXPORTER);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("Native registration unable to find class '%s'", MEDIA_EXPORTER);
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
    if (register_com_cgfay_media_CAVMediaExporter(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

#endif /* defined(__ANDROID__) */