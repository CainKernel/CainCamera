//
// Created by CainHuang on 2019/2/17.
//

#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <Mutex.h>
#include <AndroidLog.h>
#include <assert.h>

extern "C" {
#include <libavcodec/jni.h>
};

#include "CainShortVideoEditor.h"

struct editor_field_t {
    jfieldID    context;
    jmethodID   post_event;
};
static editor_field_t fields;
static Mutex sLock;
static const char * const EDITOR_CLASS_NAME = "com/cgfay/media/CainShortVideoEditor";

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
void jniThrowException(JNIEnv *env, const char* className, const char* msg = NULL) {
    jclass exception = env->FindClass(className);
    if (exception != NULL) {
        env->ThrowNew(exception, msg);
    }
}

class JNIShortVideoEditorListener : public ShortVideoEditorListener {

public:
    JNIShortVideoEditorListener(JNIEnv *env, jobject thiz, jobject weak_thiz);

    ~JNIShortVideoEditorListener();

    void notify(int msg, int ext1, int ext2, void *obj) override;

private:
    JNIShortVideoEditorListener();
    jclass mClass;
    jobject mObject;
};

JNIShortVideoEditorListener::JNIShortVideoEditorListener(JNIEnv *env, jobject thiz,
                                                         jobject weak_thiz) {
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

JNIShortVideoEditorListener::~JNIShortVideoEditorListener() {
    JNIEnv *env = getJNIEnv();
    env->DeleteGlobalRef(mObject);
    env->DeleteGlobalRef(mClass);
}

void JNIShortVideoEditorListener::notify(int msg, int ext1, int ext2, void *obj) {
    JNIEnv *env = getJNIEnv();

    bool status = (javaVM->AttachCurrentThread(&env, NULL) >= 0);

    // TODO obj needs changing into jobject

    env->CallStaticVoidMethod(mClass, fields.post_event, mObject, msg, ext1, ext2, obj);

    if (status) {
        javaVM->DetachCurrentThread();
    }
}

// -------------------------------------------------------------------------------------------------
static void
process_media_retriever_call(JNIEnv *env, status_t opStatus, const char * exception, const char *message) {
    if (opStatus == (status_t) INVALID_OPERATION) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
    } else if (opStatus != (status_t) OK) {
        if (strlen(message) > 230) {
            jniThrowException(env, exception, message);
        } else {
            char msg[256];
            sprintf(msg, "%s: status = 0x%X", message, opStatus);
            jniThrowException(env, exception, msg);
        }
    }
}

static CainShortVideoEditor *getVideoEditor(JNIEnv *env, jobject thiz) {
    CainShortVideoEditor *editor = (CainShortVideoEditor *) env->GetLongField(thiz, fields.context);
    return editor;
}

static void setVideoEditor(JNIEnv *env, jobject thiz, long editor) {
    CainShortVideoEditor *old = (CainShortVideoEditor *)env->GetLongField(thiz, fields.context);
    env->SetLongField(thiz, fields.context, editor);
}

// -------------------------------------------------------------------------------------------------
static int
CainShortVideoEditor_execute(JNIEnv *env, jobject thiz, jobjectArray command) {
    CainShortVideoEditor *editor = getVideoEditor(env, thiz);
    if (editor == NULL) {
        jniThrowException(env, "java/lang/IllegalStateException");
        return -1;
    }
    int argc = env->GetArrayLength(command);
    char *argv[argc];
    for (int i = 0; i < argc; ++i) {
        jstring commandStr = (jstring) env->GetObjectArrayElement(command, i);
        argv[i] = (char*) env->GetStringUTFChars(commandStr, 0);
    }
    int result = editor->execute(argc, argv);
    process_media_retriever_call(env, result, "java/lang/IllegalStateException", "command execute failed.");
    return result;
}

static void
CainShortVideoEditor_init(JNIEnv *env) {
    jclass clazz = env->FindClass(EDITOR_CLASS_NAME);
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

static void
CainShortVideoEditor_setup(JNIEnv *env, jobject thiz, jobject editor_thiz) {
    CainShortVideoEditor *editor = new CainShortVideoEditor();
    if (editor == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    // 初始化编辑器
    editor->init();

    // create new listener and give it to CainShortVideoEditor
    JNIShortVideoEditorListener *listener = new JNIShortVideoEditorListener(env, thiz, editor_thiz);
    editor->setListener(listener);

    // Stow our new C++ ShortVideoEditor in an opaque field in the Java object.
    setVideoEditor(env, thiz, (long) editor);
}

static void
CainShortVideoEditor_release(JNIEnv *env, jobject thiz) {
    Mutex::Autolock lock(sLock);
    CainShortVideoEditor *editor = getVideoEditor(env, thiz);
    if (editor != NULL) {
        editor->disconnect();
        delete editor;
    }
    setVideoEditor(env, thiz, 0);
}

static void
CainShortVideoEditor_finalize(JNIEnv *env, jobject thiz) {
    CainShortVideoEditor *editor = getVideoEditor(env, thiz);
    if (editor != NULL) {
        ALOGW("ShortVideoEditor finalized without being release");
    }
    CainShortVideoEditor_release(env, thiz);
}

// -------------------------------------------------------------------------------------------------

static JNINativeMethod nativeMethods[] = {
        {"execute", "([Ljava/lang/String;)I", (void *)CainShortVideoEditor_execute},
        {"_release", "()V", (void *)CainShortVideoEditor_release},
        {"native_setup", "(Ljava/lang/Object;)V", (void *)CainShortVideoEditor_setup},
        {"native_init", "()V", (void *)CainShortVideoEditor_init},
        {"native_finalize", "()V", (void *)CainShortVideoEditor_finalize},
};

// 注册CainShortVideoEditor的Native方法
static int register_com_cgfay_media_CainShortVideoEditor(JNIEnv *env) {
    int numMethods = (sizeof(nativeMethods) / sizeof( (nativeMethods)[0]));
    jclass clazz = env->FindClass(EDITOR_CLASS_NAME);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", EDITOR_CLASS_NAME);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, nativeMethods, numMethods) < 0) {
        ALOGE("Native registration unable to find class '%s'", EDITOR_CLASS_NAME);
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
    if (register_com_cgfay_media_CainShortVideoEditor(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}





