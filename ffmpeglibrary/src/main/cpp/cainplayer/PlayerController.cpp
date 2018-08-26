#include <jni.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <Thread.h>
#include "player/AVMediaPlayer.h"
#include "player/MediaPlayerHandler.h"

_JavaVM *javaVM = NULL;
AVMediaPlayer *mediaPlayer = NULL;
MediaJniCall *mediaJniCall = NULL;
MediaPlayerHandler *mediaPlayerHandler = NULL;
ANativeWindow *nativeWindow = NULL;
MessageQueue *playerQueue = NULL;
Thread *playerThread = NULL;

/**
 * 播放器消息线程回调，用于处理消息
 * @param context
 * @return
 */
int playerMessageThreadCallback(void *context) {
    MessageQueue *queue = (MessageQueue *) context;
    bool isExit = false;
    while (!isExit) {
        Message *msg = NULL;
        if (queue->dequeueMessage(&msg, true) > 0) {
            if (msg->execute() == MESSAGE_QUEUE_QUIT_FLAG) {
                isExit = true;
            }
        }
        delete(msg);
        msg = NULL;
    }
    return 0;
}

/**
 * Handler中播放器已经释放的回调，用于销毁播放器
 */
void playerRelease() {

    // 销毁播放器对象
    if (mediaPlayer) {
        delete (mediaPlayer);
        mediaPlayer = NULL;
    }

    // 销毁Jni回调
    if (mediaJniCall) {
        delete(mediaJniCall);
        mediaJniCall = NULL;
    }

    // 释放Surface
    if (nativeWindow) {
        ANativeWindow_release(nativeWindow);
        nativeWindow = NULL;
    }
}


extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVM = vm;
    JNIEnv* env;

    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeSetup(JNIEnv *env, jobject instance) {

    // 创建消息队列
    if (playerQueue == NULL) {
        playerQueue = new MessageQueue("Player Message Queue");
    }

    // 创建线程
    ThreadCreate(playerMessageThreadCallback, playerQueue, "Player Message Thread");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeRelease(JNIEnv *env, jobject instance) {
    // 发送退出消息
    if (mediaPlayerHandler) {
        mediaPlayerHandler->postMessage(new Message(MESSAGE_QUEUE_QUIT_FLAG));
    }

    // 等待销毁线程
    ThreadDestroy(playerThread);

    // 销毁消息队列
    if (playerQueue) {
        playerQueue->abort();
        delete(playerQueue);
        playerQueue = NULL;
    }

    // 销毁Handler
    delete(mediaPlayerHandler);
    mediaPlayerHandler = NULL;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeSetSurface(JNIEnv *env, jobject instance,
                                                             jobject surface) {
    if (nativeWindow) {
        ANativeWindow_release(nativeWindow);
        nativeWindow = NULL;
    }
    nativeWindow = ANativeWindow_fromSurface(env, surface);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativePrepare(JNIEnv *env, jobject instance,
                                                          jstring dataSource_) {
    const char *dataSource = env->GetStringUTFChars(dataSource_, 0);

    // 创建回调
    if (mediaJniCall == NULL) {
        mediaJniCall = new MediaJniCall(javaVM, env, &instance);
    }

    // 创建播放器
    if (mediaPlayer == NULL) {
        mediaPlayer = new AVMediaPlayer(mediaJniCall, dataSource);
    }

    // 创建Handler回调，在播放下一项目时，可以不用重新创建Handler
    if (mediaPlayerHandler == NULL) {
        mediaPlayerHandler = new MediaPlayerHandler(mediaPlayer, playerQueue);
    } else {
        mediaPlayerHandler->setMediaPlayer(mediaPlayer);
        mediaPlayerHandler->setPlayerReleaseCallback(playerRelease);
    }

    // 判断ANativeWindow是否存在
    if (nativeWindow != NULL) {
        mediaPlayer->setSurface(nativeWindow);
    }

    // 准备
    mediaPlayerHandler->prepare();

    env->ReleaseStringUTFChars(dataSource_, dataSource);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeStart(JNIEnv *env, jobject instance) {

    if (mediaPlayerHandler) {
        mediaPlayerHandler->start();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeStop(JNIEnv *env, jobject instance) {

    if (mediaPlayerHandler) {
        mediaPlayerHandler->stop();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativePause(JNIEnv *env, jobject instance) {

    if (mediaPlayerHandler) {
        mediaPlayerHandler->pause();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeResume(JNIEnv *env, jobject instance) {

    if (mediaPlayerHandler) {
        mediaPlayerHandler->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeSeek(JNIEnv *env, jobject instance,
                                                       jint seconds) {


    if (mediaPlayerHandler) {
        mediaPlayerHandler->seek(seconds);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeSetAudioChannel(JNIEnv *env, jobject instance,
                                                                  jint index) {

    if (mediaPlayerHandler) {
        mediaPlayerHandler->setAudioStream(index);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeGetDuration(JNIEnv *env, jobject instance) {

    if (mediaPlayer) {
        mediaPlayer->getDuration();
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeGetAudioChannels(JNIEnv *env, jobject instance) {

    if (mediaPlayer) {
        mediaPlayer->getAudioStreams();
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeGetVideoWidth(JNIEnv *env, jobject instance) {

    if (mediaPlayer) {
        mediaPlayer->getVideoWidth();
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_player_CainPlayer_nativeGetVideoHeight(JNIEnv *env, jobject instance) {

    if (mediaPlayer) {
        mediaPlayer->getVideoHeight();
    }

    return 0;
}