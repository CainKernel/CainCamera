//
// Created by CainHuang on 2019/6/7.
//
#if defined(__ANDROID__)

#include <unistd.h>
#include <jni.h>
#include <string.h>

extern "C" {
#include "ffmpeg.h"
#include <libavcodec/jni.h>
};

#include <android/log.h>
#include <cassert>

#define JNI_TAG "FFCommand"
#define ALOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, JNI_TAG, format, ##__VA_ARGS__)
#define ALOGI(format, ...) __android_log_print(ANDROID_LOG_INFO,  JNI_TAG, format, ##__VA_ARGS__)
#define ALOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, JNI_TAG, format, ##__VA_ARGS__)
#define ALOGW(format, ...) __android_log_print(ANDROID_LOG_WARN,  JNI_TAG, format, ##__VA_ARGS__)
#define ALOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE,  JNI_TAG, format, ##__VA_ARGS__)

#include "ffmpeg_cmd.h"

static const char * const CAVCOMMAND = "com/cgfay/media/CAVCommand";

static JavaVM *javaVM = nullptr;
static jclass mClazz = nullptr;

static JNIEnv *getJNIEnv() {
    JNIEnv *env;
    assert(javaVM != nullptr);
    if (javaVM->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return nullptr;
    }
    return env;
}

void ffmpeg_executor_progress(int progress) {
    ALOGD("executing: %f", progress);
    JNIEnv *env = getJNIEnv();
    if (env == nullptr) {
        ALOGE("env == null");
        return;
    }
    // 判断是否异步回调
//    bool status = (javaVM->AttachCurrentThread(&env, nullptr) >= 0);

    if (mClazz == nullptr) {
        ALOGE("class = null");
        return;
    }
    jmethodID onProgress = env->GetStaticMethodID(mClazz, "onProgress", "(I)V");
    if (onProgress == nullptr) {
        ALOGE("onProgress == null");
        return;
    }
    env->CallStaticVoidMethod(mClazz, onProgress, progress);

    // 判断是否需要解绑线程
//    if (status) {
//        javaVM->DetachCurrentThread();
//    }
}


static int
CAVCommand_execute(JNIEnv *env, jclass clazz, jobjectArray command) {
    ALOGD("executing start: ");
    env->GetJavaVM(&javaVM);
    mClazz = static_cast<jclass>(env->NewGlobalRef(clazz));
    int argc = env->GetArrayLength(command);
    char *argv[argc];
    for (int i = 0; i < argc; ++i) {
        jstring commandStr = (jstring) env->GetObjectArrayElement(command, i);
        argv[i] = (char*) env->GetStringUTFChars(commandStr, 0);
    }
    int result = runCommand(argc, argv);
    ALOGD("executing end");
    return result;
}

static JNINativeMethod nativeMethods[] = {
        {"_execute", "([Ljava/lang/String;)I", (void *)CAVCommand_execute},
};

// 注册CAVCommand的Native方法
static int register_com_cgfay_media_CAVCommand(JNIEnv *env) {
    int numMethods = (sizeof(nativeMethods) / sizeof( (nativeMethods)[0]));
    jclass clazz = env->FindClass(CAVCOMMAND);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", CAVCOMMAND);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, nativeMethods, numMethods) < 0) {
        ALOGE("Native registration unable to find class '%s'", CAVCOMMAND);
        return JNI_ERR;
    }
    env->DeleteLocalRef(clazz);

    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    av_jni_set_java_vm(vm, NULL);
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if (register_com_cgfay_media_CAVCommand(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

#endif  /* defined(__ANDROID__) */