//
// Created by CainHuang on 2020/5/30.
//
#if defined(__ANDROID__)

#include <Errors.h>
#include <jni.h>
#include <unistd.h>
#include <string.h>
#include "../CAVFoundation.h"
#include "../CAVUriAsset.h"

struct asset_fields_t {
    jfieldID context;
    jfieldID trackCount;
    jmethodID putTrack;
    jmethodID putVideoSize;
    jmethodID putDuration;
    jmethodID putRotation;
};
static asset_fields_t fields;
static Mutex sLock;
static const char * const ASSET_CLASS_NAME = "com/cgfay/cavfoundation/CAVUriAsset";

/**
 * 创建新的jstring对象
 * @param env
 * @param data
 * @return
 */
static jstring newUTFString(JNIEnv *env, const char *data) {
    jstring str = nullptr;

    int size = strlen(data);

    jbyteArray array = nullptr;
    array = env->NewByteArray(size);
    if (!array) {
        LOGE("convertString: OutOfMemoryError is thrown.");
    } else {
        jbyte* bytes = env->GetByteArrayElements(array, nullptr);
        if (bytes != nullptr) {
            memcpy(bytes, data, size);
            env->ReleaseByteArrayElements(array, bytes, 0);

            jclass clazz = env->FindClass("java/lang/String");
            jmethodID pMethodID = env->GetMethodID(clazz, "<init>", "([BLjava/lang/String;)V");
            jstring utf = env->NewStringUTF("UTF-8");
            str = (jstring) env->NewObject(clazz, pMethodID, array, utf);

            env->DeleteLocalRef(utf);
        }
    }
    env->DeleteLocalRef(array);

    return str;
}

/**
 * 抛出异常
 * @param env
 * @param className
 * @param msg
 */
static void
throwException(JNIEnv* env, const char* className, const char* msg) {
    jclass exception = env->FindClass(className);
    env->ThrowNew(exception, msg);
}

static int
getFDFromFileDescriptor(JNIEnv * env, jobject fileDescriptor) {
    jint fd = -1;
    jclass fdClass = env->FindClass("java/io/FileDescriptor");

    if (fdClass != nullptr) {
        jfieldID fdClassDescriptorFieldID = env->GetFieldID(fdClass, "descriptor", "I");
        if (fdClassDescriptorFieldID != nullptr && fileDescriptor != nullptr) {
            fd = env->GetIntField(fileDescriptor, fdClassDescriptorFieldID);
        }
    }

    return fd;
}

static void
process_media_asset_call(JNIEnv *env, status_t opStatus, const char *exception,
                             const char *message) {
    if (opStatus == (status_t) INVALID_OPERATION) {
        throwException(env, "java/lang/IllegalStateException", nullptr);
    } else if (opStatus != (status_t) OK) {
        if (strlen(message) > 230) {
            throwException(env, exception, message);
        } else {
            char msg[256];
            sprintf(msg, "%s: status = 0x%X", message, opStatus);
            throwException(env, exception, msg);
        }
    }
}

static CAVUriAsset *getAsset(JNIEnv *env, jobject thiz) {
    auto asset = (CAVUriAsset *) env->GetLongField(thiz, fields.context);
    return asset;
}

static void setAsset(JNIEnv *env, jobject thiz, long asset) {
    env->SetLongField(thiz, fields.context, asset);
}

/**
 * 设置轨道数量
 * @param trackCount 轨道数量
 */
static void setTrackCount(JNIEnv *env, jobject thiz, int trackCount) {
    env->SetIntField(thiz, fields.trackCount, trackCount);
}

/**
 * 绑定轨道索引index和轨道trackID
 */
static void putTrack(JNIEnv *env, jobject thiz, int index, int trackID) {
    env->CallVoidMethod(thiz, fields.putTrack, index, trackID);
}

/**
 * 设置视频分辨率
 */
static void putVideoSize(JNIEnv *env, jobject thiz, int width, int height) {
    env->CallVoidMethod(thiz, fields.putVideoSize, width, height);
}

/**
 * 设置时长
 */
static void putDuration(JNIEnv *env, jobject thiz, long value, int timescale) {
    env->CallVoidMethod(thiz, fields.putDuration, value, timescale);
}

/**
 * 设置旋转角度
 */
static void putRotation(JNIEnv *env, jobject thiz, int rotation) {
    env->CallVoidMethod(thiz, fields.putRotation, rotation);
}

/**
 * 初始化媒体参数
 * @param env
 * @param thiz
 * @param asset
 */
static void initAssetData(JNIEnv *env, jobject thiz, CAVUriAsset *asset) {
    // 设置轨道数量
    int count = asset->getTrackCount();
    setTrackCount(env, thiz, count);
    // 绑定轨道ID
    for (int i = 0; i < count; ++i) {
        putTrack(env, thiz, i, asset->getTrackID(i));
    }
    putVideoSize(env, thiz, asset->getWidth(), asset->getHeight());
    putRotation(env, thiz, asset->getRotation());
    AVTime time = asset->getDuration();
    time = AVTimeConvertScale(time, DEFAULT_TIME_SCALE);
    putDuration(env, thiz, time.value, time.timescale);
}

static void
CAVUriAsset_setDataSourceAndHeaders(JNIEnv *env, jobject thiz,
                                          jstring path_, jobjectArray keys, jobjectArray values) {
    auto asset = getAsset(env, thiz);
    if (asset == nullptr) {
        throwException(env, "java/io/IOException", "No Extractor available");
        return;
    }
    if (!path_) {
        throwException(env, "java/io/IOException", "null path");
        return;
    }
    const char *path = env->GetStringUTFChars(path_, nullptr);
    if (!path) {
        return;
    }
    if (strncmp("mem://", path, 6) == 0) {
        throwException(env, "java/io/IOException", "Invalid pathname");
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
            throwException(env, "java/lang/IllegalArgumentException", nullptr);
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

    status_t opStatus = asset->setDataSource(path, 0, headers);
    process_media_asset_call(env, opStatus, "java/io/IOException", "setDataSource failed.");
    env->ReleaseStringUTFChars(path_, path);

//    initAssetData(env, thiz, asset);
}

static void
CAVUriAsset_setDataSource(JNIEnv *env, jobject thiz, jstring path_) {
    CAVUriAsset_setDataSourceAndHeaders(env, thiz, path_, nullptr, nullptr);
}

static void
CAVUriAsset_setDataSourceFD(JNIEnv *env, jobject thiz,
                                  jobject fileDescriptor, jlong offset, jlong length) {
    auto asset = getAsset(env, thiz);
    if (asset == nullptr) {
        throwException(env, "java/io/IOException", "No Extractor available");
        return;
    }
    if (!fileDescriptor) {
        throwException(env, "java/lang/IllegalStateException", "fileDescriptor is null");
        return;
    }
    int fd = getFDFromFileDescriptor(env, fileDescriptor);
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
        throwException(env, "java/lang/IllegalArgumentException", nullptr);
        return;
    }

    char path[256] = "";
    int myfd = dup(fd);
    char str[20];
    sprintf(str, "pipe:%d", myfd);
    strcat(path, str);

    status_t opStatus = asset->setDataSource(path, offset, nullptr);
    process_media_asset_call(env, opStatus, "java/io/IOException", "setDataSourceFD failed.");

//    initAssetData(env, thiz, asset);
}

static int
CAVUriAsset_getTrackType(JNIEnv *env, jobject thiz, int index) {
    auto asset = getAsset(env, thiz);
    if (asset != nullptr) {
        return asset->getTrackType(index);
    }
    return -1;
}

static void
CAVUriAsset_native_setup(JNIEnv *env, jobject thiz) {
    auto asset = new CAVUriAsset();
    if (asset == nullptr) {
        throwException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    setAsset(env, thiz, (long)asset);
}

static void
CAVUriAsset_release(JNIEnv *env, jobject thiz) {
    Mutex::Autolock lock(sLock);
    auto asset = getAsset(env, thiz);
    delete asset;
    setAsset(env, thiz, 0);
}

static void
CAVUriAsset_native_finalize(JNIEnv *env, jobject thiz) {
    LOGV("native_finalize");
    CAVUriAsset_release(env, thiz);
}

static void
CAVUriAsset_native_init(JNIEnv *env, jclass clazz) {
    if (clazz == nullptr) {
        return;
    }
    fields.context = env->GetFieldID(clazz, "mNativeContext", "J");
    fields.trackCount = env->GetFieldID(clazz, "mTrackCount", "I");
    fields.putTrack = env->GetMethodID(clazz, "putTrack", "(II)V");
    fields.putVideoSize = env->GetMethodID(clazz, "putVideoSize", "(II)V");
    fields.putDuration = env->GetMethodID(clazz, "putDuration", "(JI)V");
    fields.putRotation = env->GetMethodID(clazz, "putRotation", "(I)V");
}

static JNINativeMethod nativeMethods[] = {
        {"setDataSource", "(Ljava/lang/String;)V", (void *)CAVUriAsset_setDataSource},
        {
         "_setDataSource",
                          "(Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V",
                                                   (void *)CAVUriAsset_setDataSourceAndHeaders
        },
        {"_setDataSource",   "(Ljava/io/FileDescriptor;JJ)V", (void *)CAVUriAsset_setDataSourceFD},
        {"_getTrackType", "(I)I", (void *)CAVUriAsset_getTrackType},
        {"native_setup", "()V", (void *)CAVUriAsset_native_setup},
        {"native_init", "()V", (void *)CAVUriAsset_native_init},
        {"native_finalize", "()V", (void *)CAVUriAsset_native_finalize},
};

/**
 * 注册native方法
 * @param env
 * @return
 */
int register_com_cgfay_cavfoundation_CAVUriAsset(JNIEnv *env) {
    int numMethods = (sizeof(nativeMethods) / sizeof( (nativeMethods)[0]));
    jclass clazz = env->FindClass(ASSET_CLASS_NAME);
    if (clazz == nullptr) {
        LOGE("Native registration unable to find class '%s'", ASSET_CLASS_NAME);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, nativeMethods, numMethods) < 0) {
        LOGE("Native registration unable to find class '%s'", ASSET_CLASS_NAME);
        return JNI_ERR;
    }
    env->DeleteLocalRef(clazz);

    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    if (register_com_cgfay_cavfoundation_CAVUriAsset(env) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_4;
}

#endif  /* defined(__ANDROID__) */