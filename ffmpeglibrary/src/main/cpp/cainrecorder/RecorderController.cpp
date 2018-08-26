//
// Created by cain.huang on 2018/1/3.
//

#include"jni.h"
#include "recorder/CainRecorder.h"

using namespace std;

// 录制器
CainRecorder *recorder;

/**
 * 初始化编码器
 * @param env
 * @param obj
 * @param videoPath_    视频路径
 * @param previewWidth  预览宽度
 * @param previewHeight 预览高度
 * @param videoWidth    录制视频宽度
 * @param videoHeight   录制视频高度
 * @param frameRate     帧率
 * @param bitRate       视频比特率
 * @param enableAudio   允许音频编码
 * @param audioBitRate  音频比特率
 * @param audioSampleRate  音频采样频率
 * @return
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_recorder_CainRecorder_initMediaRecorder(JNIEnv *env, jobject instance,
                                                                     jstring videoPath_,
                                                                     jint previewWidth,
                                                                     jint previewHeight,
                                                                     jint videoWidth,
                                                                     jint videoHeight,
                                                                     jint frameRate, jint bitRate,
                                                                     jboolean enableAudio,
                                                                     jint audioBitRate,
                                                                     jint audioSampleRate) {
    // 配置参数
    const char * videoPath = env->GetStringUTFChars(videoPath_, 0);
    EncoderParams *params = (EncoderParams *)malloc(sizeof(EncoderParams));
    params->mediaPath = videoPath;
    params->previewWidth = previewWidth;
    params->previewHeight = previewHeight;
    params->videoWidth = videoWidth;
    params->videoHeight = videoHeight;
    params->frameRate = frameRate;
    params->bitRate = bitRate;
    // 是否允许音频编码
    if (enableAudio) {
        params->enableAudio = 1;
    } else {
        params->enableAudio = 0;
    }
    params->audioBitRate = audioBitRate;
    params->audioSampleRate = audioSampleRate;
    // 初始化录制器
    recorder = new CainRecorder(params);
    return recorder->initRecorder();
}

/**
 * 开始录制
 * @param env
 * @param obj
 * @return
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_recorder_CainRecorder_startRecord(JNIEnv *env, jobject instance) {
    if (!recorder) {
        return;
    }
    recorder->startRecord();
}

/**
 * 发送YUV数据进行编码
 * @param env
 * @param obj
 * @param yuvData
 * @return
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_recorder_CainRecorder_encodeYUVFrame(JNIEnv *env, jobject instance, jbyteArray yuvData) {
    if (!recorder) {
        return 0;
    }
    // 获取数据
    jbyte *elements = env->GetByteArrayElements(yuvData, 0);
    // 发送大编码队列
    recorder->avcEncode(elements);
    // 释放资源
    env->ReleaseByteArrayElements(yuvData, elements, 0);
    return 0;
}

/**
 * 发送PCM数据进行编码
 * @param env
 * @param obj
 * @param pcmData
 * @return
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_ffmpeglibrary_recorder_CainRecorder_encodePCMFrame(JNIEnv *env, jobject instance, jbyteArray pcmData, jint len) {
    if (!recorder) {
        return 0;
    }
    jbyte *pcm = env->GetByteArrayElements(pcmData, 0);
    // 音频编码
    recorder->aacEncode(pcm);
    // 释放资源
    env->ReleaseByteArrayElements(pcmData, pcm, 0);
    return 0;
}

/**
 * 发送停止命令
 * @param env
 * @param obj
 * @return
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_recorder_CainRecorder_stopRecord(JNIEnv *env, jobject instance) {
    if (!recorder) {
        return;
    }
    recorder->recordEndian();
}

/**
 * 释放资源
 * @param env
 * @param obj
 * @return
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_cgfay_ffmpeglibrary_recorder_CainRecorder_nativeRelease(JNIEnv *env, jobject instance) {
    if(!recorder) {
        return;
    }
    recorder->release();
    delete(recorder);
}