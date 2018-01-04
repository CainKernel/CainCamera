//
// Created by Administrator on 2018/1/3.
//

#include"jni.h"
#include "cain_recorder.h"

using namespace std;


extern "C" {

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
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_initMediaRecorder
        (JNIEnv *env, jclass obj, jstring videoPath_, jint previewWidth, jint previewHeight,
         jint videoWidth, jint videoHeight, jint frameRate, jint bitRate, jboolean enableAudio,
         jint audioBitRate, jint audioSampleRate);

/**
 * 开始录制
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_startRecord(JNIEnv *env, jclass obj);

/**
 * 发送YUV数据进行编码
 * @param env
 * @param obj
 * @param yuvArray
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_sendYUVFrame
        (JNIEnv *env, jclass obj, jbyteArray yuvArray);

/**
 * 发送PCM数据进行编码
 * @param env
 * @param obj
 * @param pcmArray
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_sendPCMFrame
        (JNIEnv *env, jclass obj, jbyteArray pcmArray, jint len);

/**
 * 发送停止命令
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_stopRecord(JNIEnv *env, jclass obj);

/**
 * 释放资源
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_nativeRelease(JNIEnv *env, jclass obj);

} // extern "C"


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
 * @param audioBitRate  音频比特率
 * @param audioSampleRate  音频采样频率
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_initMediaRecorder
        (JNIEnv *env, jclass obj, jstring videoPath_, jint previewWidth, jint previewHeight,
         jint videoWidth, jint videoHeight, jint frameRate, jint bitRate, jboolean enableAudio,
         jint audioBitRate, jint audioSampleRate) {

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
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_startRecord(JNIEnv *env, jclass obj) {
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
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_sendYUVFrame
        (JNIEnv *env, jclass obj, jbyteArray yuvData) {
    if (!recorder) {
        return 0;
    }
    // 获取数据
    jbyte *elements = env->GetByteArrayElements(yuvData, 0);
    // 发送大编码队列
    recorder->sendFrame((uint8_t *)elements, FRAME_YUV, /*unused*/-1);
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
JNIEXPORT jint
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_sendPCMFrame
        (JNIEnv *env, jclass obj, jbyteArray pcmData, jint len) {
    if (!recorder) {
        return 0;
    }
    jbyte *pcm = env->GetByteArrayElements(pcmData, 0);
    recorder->sendFrame((uint8_t *)pcm, FRAME_PCM, len);
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
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_stopRecord(JNIEnv *env, jclass obj) {
    if (!recorder) {
        return;
    }
    recorder->stopRecord();
}

/**
 * 释放资源
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_caincamera_jni_FFmpegHandler_nativeRelease(JNIEnv *env, jclass obj) {
    if(!recorder) {
        return;
    }
    recorder->closeRecorder();
    delete(recorder);
}
