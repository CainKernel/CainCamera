//
// Created by CainHuang on 2019/6/7.
//
#if defined(__ANDROID__)

#include <unistd.h>
#include <jni.h>
#include <AndroidLog.h>
#include <string.h>

extern "C" {
#include "ffmpeg.h"
#include <libavcodec/jni.h>
};
static const char * const RETRIEVER_CLASS_NAME = "com/cgfay/media/FFmpegUtils";

static int
FFmpegUtils_execute(JNIEnv *env, jobject thiz, jobjectArray command) {
    int argc = env->GetArrayLength(command);
    char *argv[argc];
    for (int i = 0; i < argc; ++i) {
        jstring commandStr = (jstring) env->GetObjectArrayElement(command, i);
        argv[i] = (char*) env->GetStringUTFChars(commandStr, 0);
    }
    int result = runCommand(argc, argv);
    return result;
}

static JNINativeMethod nativeMethods[] = {
        {"_execute", "([Ljava/lang/String;)I", (void *)FFmpegUtils_execute},
};

// 注册FFmpegUtils的Native方法
static int register_com_cgfay_media_FFmpegUtils(JNIEnv *env) {
    int numMethods = (sizeof(nativeMethods) / sizeof( (nativeMethods)[0]));
    jclass clazz = env->FindClass(RETRIEVER_CLASS_NAME);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", RETRIEVER_CLASS_NAME);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, nativeMethods, numMethods) < 0) {
        ALOGE("Native registration unable to find class '%s'", RETRIEVER_CLASS_NAME);
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
    if (register_com_cgfay_media_FFmpegUtils(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

#endif  /* defined(__ANDROID__) */