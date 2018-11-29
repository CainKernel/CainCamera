//
// Created by cain on 2018/11/27.
//

#include <jni.h>
#include <AndroidLog.h>
#include "source/MediaMetadataRetriever.h"

MediaMetadataRetriever *metadataRetriever = NULL;

/**
 * 创建新的jstring对象
 * @param env
 * @param data
 * @return
 */
static jstring newUTFString(JNIEnv *env, const char *data) {
    jstring str = NULL;

    int size = strlen(data);

    jbyteArray array = NULL;
    array = env->NewByteArray(size);
    if (!array) {
        ALOGE("convertString: OutOfMemoryError is thrown.");
    } else {
        jbyte* bytes = env->GetByteArrayElements(array, NULL);
        if (bytes != NULL) {
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
void throwException(JNIEnv* env, const char* className, const char* msg) {
    jclass exception = env->FindClass(className);
    env->ThrowNew(exception, msg);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeSetup(JNIEnv *env,
                                                                          jobject instance) {

    metadataRetriever = new MediaMetadataRetriever();

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeRelease(JNIEnv *env,
                                                                            jobject instance) {

    if (metadataRetriever != NULL) {
        metadataRetriever->release();
        delete metadataRetriever;
        metadataRetriever = NULL;
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeSetDataSource(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jstring path_) {
    if (metadataRetriever != NULL) {
        const char *path = env->GetStringUTFChars(path_, 0);

        metadataRetriever->setDataSource(path);

        env->ReleaseStringUTFChars(path_, path);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeGetMetadata__Ljava_lang_String_2(
        JNIEnv *env, jobject instance, jstring key_) {

    if (!key_) {
        throwException(env, "java/lang/IllegalArgumentException", "Null pointer");
        return NULL;
    }
    const char *key = env->GetStringUTFChars(key_, 0);
    if (!key) {
        return NULL;
    }

    const char *value = metadataRetriever->getMetadata(key);
    if (!value) {
        return NULL;
    }

    env->ReleaseStringUTFChars(key_, key);

    return newUTFString(env, value);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeGetMetadata__Ljava_lang_String_2I(
        JNIEnv *env, jobject instance, jstring key_, jint chapter) {

    if (!key_) {
        throwException(env, "java/lang/IllegalArgumentException", "Null pointer");
        return NULL;
    }
    const char *key = env->GetStringUTFChars(key_, 0);
    if (!key) {
        return NULL;
    }

    const char *value = metadataRetriever->getMetadata(key, chapter);
    if (!value) {
        return NULL;
    }

    env->ReleaseStringUTFChars(key_, key);

    return newUTFString(env, value);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeGetMetadata__(JNIEnv *env,
                                                                                   jobject instance) {
    if (metadataRetriever != NULL) {

        AVDictionary *metadata = NULL;
        int ret = metadataRetriever->getMetadata(&metadata);
        if (ret == 0) {
            jclass hashMapClass = env->FindClass("java/util/HashMap");
            jmethodID hashMap_init = env->GetMethodID(hashMapClass, "<init>", "()V");
            // 创建一个HashMap对象
            jobject hashMap = env->NewObject(hashMapClass, hashMap_init);
            jmethodID hashMap_put = env->GetMethodID(hashMapClass, "put",
                                                     "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            // 将metadata的参数复制到HashMap中
            for (int i = 0; i < metadata->count; ++i) {
                jstring key = newUTFString(env, metadata->elements[i].key);
                jstring value = newUTFString(env, metadata->elements[i].value);
                env->CallObjectMethod(hashMap, hashMap_put, key, value);
            }

            if (metadata) {
                av_dict_free(&metadata);
            }

            return hashMap;
        }
    }
    return NULL;
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeGetFrame__J(JNIEnv *env,
                                                                                jobject instance,
                                                                                jlong timeUs) {

    if (metadataRetriever != NULL) {
        AVPacket packet;
        av_init_packet(&packet);
        jbyteArray array = NULL;
        if (metadataRetriever->getFrame(timeUs, &packet) == 0) {
            int size = packet.size;
            uint8_t *data = packet.data;
            array = env->NewByteArray(size);

            jbyte *bytes = env->GetByteArrayElements(array, NULL);
            if (bytes != NULL) {
                memcpy(bytes, data, size);
                env->ReleaseByteArrayElements(array, bytes, 0);
            }
        }
        av_packet_unref(&packet);
        return array;
    }

    return NULL;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeGetFrame__JII(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong timeUs,
                                                                                  jint width,
                                                                                  jint height) {

    if (metadataRetriever != NULL) {
        AVPacket packet;
        av_init_packet(&packet);
        jbyteArray array = NULL;
        if (metadataRetriever->getFrame(timeUs, &packet, width, height) == 0) {
            int size = packet.size;
            uint8_t *data = packet.data;
            array = env->NewByteArray(size);

            jbyte *bytes = env->GetByteArrayElements(array, NULL);
            if (bytes != NULL) {
                memcpy(bytes, data, size);
                env->ReleaseByteArrayElements(array, bytes, 0);
            }
        }
        av_packet_unref(&packet);
        return array;
    }

    return NULL;

}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_cgfay_ffmpeglibrary_Metadata_AVMediaMetadataRetriever_nativeGetCoverPicture(JNIEnv *env,
                                                                                     jobject instance) {

    if (metadataRetriever != NULL) {
        AVPacket packet;
        av_init_packet(&packet);
        jbyteArray array = NULL;
        if (metadataRetriever->getCoverPicture(&packet) == 0) {
            int size = packet.size;
            uint8_t *data = packet.data;
            array = env->NewByteArray(size);

            jbyte *bytes = env->GetByteArrayElements(array, NULL);
            if (bytes != NULL) {
                memcpy(bytes, data, size);
                env->ReleaseByteArrayElements(array, bytes, 0);
            }
        }
        av_packet_unref(&packet);
        return array;
    }
    return NULL;
}
