//
// Created by CainHuang on 2019/6/30.
//

#include <jni.h>
#include <SoundTouch.h>
using namespace soundtouch;

extern "C" JNIEXPORT jlong JNICALL
Java_com_cgfay_media_SoundTouch_nativeInit(JNIEnv *env, jclass thiz) {
    SoundTouch *soundTouch = new SoundTouch();
    return (jlong)soundTouch;
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_nativeRelease(JNIEnv *env, jobject thiz, jlong handle) {
    SoundTouch *ptr = (SoundTouch *) handle;
    delete ptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setRate(JNIEnv *env, jobject thiz, jlong handle, jdouble rate) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setRate(rate);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setTempo(JNIEnv *env, jobject thiz, jlong handle, jfloat tempo) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setTempo(tempo);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setRateChange(JNIEnv *env, jobject thiz, jlong handle, jdouble rate) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setRateChange(rate);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setTempoChange(JNIEnv *env, jobject thiz, jlong handle, jfloat tempo) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setTempoChange(tempo);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setPitch(JNIEnv *env, jobject thiz, jlong handle, jfloat pitch) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setPitch(pitch);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setPitchOctaves(JNIEnv *env, jobject thiz, jlong handle, jfloat pitch) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setPitchOctaves(pitch);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setPitchSemiTones(JNIEnv *env, jobject thiz, jlong handle, jfloat pitch) {
    SoundTouch *ptr = (SoundTouch *)handle;
    if (ptr != nullptr) {
        ptr->setPitchSemiTones(pitch);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setChannels(JNIEnv *env, jobject thiz, jlong handle, jint channels) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setChannels(channels);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_setSampleRate(JNIEnv *env, jobject thiz, jlong handle, jint srate) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->setSampleRate(srate);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_flush(JNIEnv *env, jobject thiz, jlong handle) {
    SoundTouch *ptr = (SoundTouch *) handle;
    if (ptr != nullptr) {
        ptr->flush();
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_cgfay_media_SoundTouch_putSamples(JNIEnv *env, jobject thiz, jlong handle, jbyteArray input, jint offset, jint length) {
    SoundTouch *ptr = (SoundTouch *)handle;
    if (ptr != nullptr) {
        jbyte *data;
        data = env->GetByteArrayElements(input, JNI_FALSE);
        ptr->putSamples((SAMPLETYPE *)data, (uint)length/2);
        env->ReleaseByteArrayElements(input, data, 0);
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_cgfay_media_SoundTouch_receiveSamples(JNIEnv *env, jobject thiz, jlong handle, jbyteArray output, jint length) {
    int receiveSamples = 0;
    uint maxSamples = (uint)length/2;
    SoundTouch *ptr = (SoundTouch *)handle;
    if (ptr != nullptr) {
        jbyte *data;
        data = env->GetByteArrayElements(output, JNI_FALSE);
        receiveSamples = ptr->receiveSamples((SAMPLETYPE *)data, maxSamples);
        env->ReleaseByteArrayElements(output, data, 0);
    }
    return receiveSamples;
}

