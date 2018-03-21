//
// Created by Administrator on 2018/3/21.
//

#include "CainPlayer.h"

static AVPacket flush_pkt;

/**
 * 构造器
 */
CainPlayer::CainPlayer() {
    mAudioDecoder = new AudioDecoder();
    mVideoDecoder = new VideoDecoder();
    mDemuxer = new Demuxer(mVideoDecoder, mAudioDecoder);
    fileName = NULL;
}

/**
 * 析构器
 */
CainPlayer::~CainPlayer() {
    release();
}

/**
 * 设置数据源
 * @param path
 */
void CainPlayer::setDataSource(const char *path) {
    fileName = av_strdup(path);
}

/**
 * 设置Surface
 * @param env
 * @param surface
 */
void CainPlayer::setSurface(JNIEnv *env, jobject surface) {
    if (mWindow) {
        ANativeWindow_release(mWindow);
    }
    mWindow = ANativeWindow_fromSurface(env, surface);
}

/**
 * 获取当前位置
 */
int CainPlayer::getCurrentPosition() {
    return 0;
}


/**
 * 获取时长
 * @return
 */
int CainPlayer::getDuration() {
    return 0;
}

/**
 * 是否循环播放
 * @return
 */
bool CainPlayer::isLooping() {
    return false;
}

/**
 * 是否正在播放
 * @return
 */
bool CainPlayer::isPlaying() {
    return false;
}

/**
 * 是否处于停止状态
 * @return
 */
bool CainPlayer::isStopped() {
    return false;
}

/**
 * 暂停
 */
void CainPlayer::pause() {

}


/**
 * 开始
 */
void CainPlayer::start() {

}

/**
 * 停止
 */
void CainPlayer::stop() {

}

/**
 * 异步装载流媒体
 */
void CainPlayer::prepare() {

}

/**
 * 重置所有状态
 */
void CainPlayer::reset() {

}

/**
 * 释放资源
 */
void CainPlayer::release() {
    stop();
}

/**
 * 指定播放区域
 * @param msec
 */
void CainPlayer::seekTo(int msec) {

}



/**
 * 设置是否循环播放
 * @param loop
 */
void CainPlayer::setLooping(bool loop) {

}

/**
 * 设置是否倒放
 * @param reverse
 */
void CainPlayer::setReverse(bool reverse) {

}

/**
 * 设置是否播放声音
 * @param play
 */
void CainPlayer::setPlayAudio(bool play) {

}

/**
 * 设置播放速度
 * @param playbackRate
 */
void CainPlayer::setPlaybackRate(float playbackRate) {

}

