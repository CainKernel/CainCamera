//
// Created by cain on 2018/11/25.
//

#include <jni.h>
#include "source/AVMusicPlayer.h"
#include "JniPlayerCallback.h"

_JavaVM *javaVM = NULL;
AVMusicPlayer *musicPlayer = NULL;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVM = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetup(JNIEnv *env, jobject instance) {

    assert(musicPlayer == NULL);
    JniPlayerCallback *callback = new JniPlayerCallback(javaVM, env, &instance);
    musicPlayer = new AVMusicPlayer();
    musicPlayer->setPlayerCallback(callback);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeRelease(JNIEnv *env, jobject instance) {

    if (musicPlayer != NULL) {
        musicPlayer->release();
        delete(musicPlayer);
        musicPlayer = NULL;
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetLooping(JNIEnv *env,
                                                                        jobject instance,
                                                                        jboolean looping) {

    if (musicPlayer != NULL) {
        musicPlayer->setLooping(looping);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativePrepare(JNIEnv *env, jobject instance, jstring source_) {
    const char *source = env->GetStringUTFChars(source_, 0);
    if (musicPlayer != NULL) {
        musicPlayer->setDataSource(source);
        musicPlayer->prepare();
    }
    env->ReleaseStringUTFChars(source_, source);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeStart(JNIEnv *env, jobject instance) {
    if (musicPlayer != NULL) {
        musicPlayer->start();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativePause(JNIEnv *env, jobject instance) {

    if (musicPlayer != NULL) {
        musicPlayer->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeResume(JNIEnv *env, jobject instance) {

    if (musicPlayer != NULL) {
        musicPlayer->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeStop(JNIEnv *env, jobject instance) {

    if (musicPlayer != NULL) {
        musicPlayer->stop();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSeek(JNIEnv *env, jobject instance,
                                                                  jint seconds) {

    if (musicPlayer != NULL) {
        musicPlayer->seek(seconds);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetVolume(JNIEnv *env,
                                                                       jobject instance,
                                                                       jint percent) {

    if (musicPlayer != NULL) {
        musicPlayer->setVolume(percent);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetChannelType(JNIEnv *env,
                                                                             jobject instance,
                                                                             jint channelType) {
    if (musicPlayer != NULL) {
        musicPlayer->setChannelType(channelType);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetSpeed(JNIEnv *env, jobject instance,
                                                                      jfloat speed) {

    if (musicPlayer != NULL) {
        musicPlayer->setSpeed(speed);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetPitch(JNIEnv *env, jobject instance,
                                                                      jfloat pitch) {

    if (musicPlayer != NULL) {
        musicPlayer->setPitch(pitch);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetTempo(JNIEnv *env, jobject instance,
                                                                      jfloat tempo) {

    if (musicPlayer != NULL) {
        musicPlayer->setTempo(tempo);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetSpeedChange(JNIEnv *env,
                                                                            jobject instance,
                                                                            jdouble speedChange) {

    if (musicPlayer != NULL) {
        musicPlayer->setSpeedChange(speedChange);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetTempoChange(JNIEnv *env,
                                                                            jobject instance,
                                                                            jdouble tempoChange) {

    if (musicPlayer != NULL) {
        musicPlayer->setTempoChange(tempoChange);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetPitchOctaves(JNIEnv *env,
                                                                             jobject instance,
                                                                             jdouble pitchOctaves) {

    if (musicPlayer != NULL) {
        musicPlayer->setPitchOctaves(pitchOctaves);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeSetPitchSemiTones(JNIEnv *env,
                                                                               jobject instance,
                                                                               jdouble semiTones) {

    if (musicPlayer != NULL) {
        musicPlayer->setPitchSemiTones(semiTones);
    }

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeGetSampleRate(JNIEnv *env,
                                                                           jobject instance) {

    if (musicPlayer != NULL) {
        return musicPlayer->getSampleRate();
    }
    return 0;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativeGetDuration(JNIEnv *env,
                                                                         jobject instance) {

    if (musicPlayer != NULL) {
        return musicPlayer->getDuration();
    }
    return 0;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_cgfay_ffmpeglibrary_MusicPlayer_AVMusicPlayer_nativePlaying(JNIEnv *env,
                                                                     jobject instance) {

    if (musicPlayer != NULL) {
        return (jboolean)musicPlayer->isPlaying();
    }
    return 0;
}
