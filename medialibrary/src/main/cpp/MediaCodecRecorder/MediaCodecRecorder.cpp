//
// Created by CainHuang on 2019/9/15.
//

#include "MediaCodecRecorder.h"

MediaCodecRecorder::MediaCodecRecorder() : mRecordListener(nullptr), mAbortRequest(true),
                                           mStartRequest(false), mExit(true), mRecordThread(nullptr),
                                           mYuvConvertor(nullptr), mMediaWriter(nullptr) {
    mRecordConfig = new RecordConfig();
}

MediaCodecRecorder::~MediaCodecRecorder() {
    release();
    if (mRecordConfig != nullptr) {
        delete mRecordConfig;
        mRecordConfig = nullptr;
    }
}

void MediaCodecRecorder::setOnRecordListener(OnMediaRecordListener *listener) {
    mRecordListener = listener;
}

void MediaCodecRecorder::release() {
    stopRecord();
    // 等待退出
    mMutex.lock();
    while (!mExit) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();

    if (mRecordListener != nullptr) {
        delete mRecordListener;
        mRecordListener = nullptr;
    }
    if (mFrameQueue != nullptr) {
        delete mFrameQueue;
        mFrameQueue = nullptr;
    }
    if (mRecordThread != nullptr) {
        delete mRecordThread;
        mRecordThread = nullptr;
    }
    if (mMediaWriter != nullptr) {
        mMediaWriter->release();
        delete mMediaWriter;
        mMediaWriter = nullptr;
    }
}

RecordConfig* MediaCodecRecorder::getRecordConfig() {
    return mRecordConfig;
}

int MediaCodecRecorder::prepare() {
    if (mMediaWriter != nullptr) {
        LOGE("encoder is working");
        return -1;
    }

    RecordConfig *config = mRecordConfig;
    if (config->rotateDegree % 90 != 0) {
        LOGE("invalid rotate degree: %d", config->rotateDegree);
        return -1;
    }

    AVPixelFormat pixelFormat = getPixelFormat((PixelFormat)config->pixelFormat);
    AVSampleFormat sampleFormat = getSampleFormat((SampleFormat)config->sampleFormat);
    if ((!config->enableVideo && !config->enableAudio)
        || (config->enableVideo && pixelFormat == AV_PIX_FMT_NONE)
           && (config->enableAudio && sampleFormat == AV_SAMPLE_FMT_NONE)) {
        LOGE("pixel format and sample format invalid: %d, %d", config->pixelFormat,
             config->sampleFormat);
        return -1;
    }


    int ret;

    mFrameQueue = new SafetyQueue<AVMediaData *>();

    LOGI("Record to file: %s, width: %d, height: %d", config->dstFile, config->width,
         config->height);

    // yuv转换
    int outputWidth = config->width;
    int outputHeight = config->height;

    // yuv转换器
    mYuvConvertor = new YuvConvertor();
    mYuvConvertor->setInputParams(config->width, config->height, config->pixelFormat);
    mYuvConvertor->setCrop(config->cropX, config->cropY, config->cropWidth, config->cropHeight);
    mYuvConvertor->setRotate(config->rotateDegree);
    mYuvConvertor->setScale(config->scaleWidth, config->scaleHeight);
    mYuvConvertor->setMirror(config->mirror);
    // 准备
    if (mYuvConvertor->prepare() < 0) {
        delete mYuvConvertor;
        mYuvConvertor = nullptr;
    } else {
        pixelFormat = AV_PIX_FMT_YUV420P;
        outputWidth = mYuvConvertor->getOutputWidth();
        outputHeight = mYuvConvertor->getOutputHeight();
        if (outputWidth == 0 || outputHeight == 0) {
            outputWidth = config->rotateDegree % 180 == 0 ? config->width : config->height;
            outputHeight = config->rotateDegree % 180 == 0 ? config->height : config->width;
        }
    }

    mMediaWriter = new MediaCodecWriter();
    // 设置参数
    mMediaWriter->setUseTimeStamp(true);
    mMediaWriter->setMaxBitRate(config->maxBitRate);
    mMediaWriter->setOutputPath(config->dstFile);
    mMediaWriter->setOutputVideo(outputWidth, outputHeight, config->frameRate, pixelFormat);
    mMediaWriter->setOutputAudio(config->sampleRate, config->channels, sampleFormat);

    // 准备
    ret = mMediaWriter->prepare();
    if (ret < 0) {
        release();
    }
    return 0;
}

int MediaCodecRecorder::recordFrame(AVMediaData *data) {
    if (mAbortRequest || mExit) {
        LOGE("Recoder is not recording.");
        delete data;
        return -1;
    }

    // 录制的是音频数据并且不允许音频录制，则直接删除
    if (!mRecordConfig->enableAudio && data->getType() == MediaAudio) {
        delete data;
        return -1;
    }

    // 录制的是视频数据并且不允许视频录制，直接删除
    if (!mRecordConfig->enableVideo && data->getType() == MediaVideo) {
        delete data;
        return -1;
    }

    // 将媒体数据入队
    if (mFrameQueue != nullptr) {
        mFrameQueue->push(data);
    } else {
        delete data;
    }
    return 0;
}

void MediaCodecRecorder::startRecord() {
    mMutex.lock();
    mAbortRequest = false;
    mStartRequest = true;
    mCondition.signal();
    mMutex.unlock();

    if (mRecordThread == nullptr) {
        mRecordThread = new Thread(this);
        mRecordThread->start();
        mRecordThread->detach();
    }
}

void MediaCodecRecorder::stopRecord() {
    mMutex.lock();
    mAbortRequest = true;
    mCondition.signal();
    mMutex.unlock();
    if (mRecordThread != nullptr) {
        mRecordThread->join();
        delete mRecordThread;
        mRecordThread = nullptr;
    }
}

bool MediaCodecRecorder::isRecording() {
    bool recording = false;
    mMutex.lock();
    recording = !mAbortRequest && mStartRequest && !mExit;
    mMutex.unlock();
    return recording;
}

void MediaCodecRecorder::run() {
    int ret = 0;
    int64_t start = 0;
    int64_t current = 0;
    mExit = false;

    // 录制回调监听器
    if (mRecordListener != nullptr) {
        mRecordListener->onRecordStart();
    }

    LOGD("waiting to start record");
    while (!mStartRequest) {
        if (mAbortRequest) { // 停止请求则直接退出
            break;
        } else { // 睡眠10毫秒继续
            av_usleep(10 * 1000);
        }
    }

    // 开始录制编码流程
    if (!mAbortRequest && mStartRequest) {
        LOGD("start record");
        // 正在运行，并等待frameQueue消耗完
        while (!mAbortRequest || !mFrameQueue->empty()) {
            if (!mFrameQueue->empty()) {

                // 从帧对列里面取出媒体数据
                auto data = mFrameQueue->pop();
                if (!data) {
                    continue;
                }
                if (start == 0) {
                    start = data->getPts();
                }
                if (data->getPts() >= current) {
                    current = data->getPts();
                }

                // yuv转码
                if (data->getType() == MediaVideo && mYuvConvertor != nullptr) {
                    // 将数据转换成Yuv数据，处理失败则开始处理下一帧
                    if (mYuvConvertor->convert(data) < 0) {
                        LOGE("Failed to convert video data to yuv420");
                        delete data;
                        continue;
                    }
                }

                // 编码
                ret = mMediaWriter->encodeMediaData(data);
                if (ret < 0) {
                    LOGE("Failed to encoder media data： %s", data->getName());
                } else {
                    LOGD("recording time: %f", (float)(current - start));
                    if (mRecordListener != nullptr) {
                        mRecordListener->onRecording((float)(current - start));
                    }
                }
                // 释放资源
                delete data;
            }
        }

        // 停止文件写入器
        ret = mMediaWriter->stop();
    }

    // 通知退出成功
    mExit = true;
    mCondition.signal();

    // 录制完成回调
    if (mRecordListener != nullptr) {
        mRecordListener->onRecordFinish(ret == 0, (float)(current - start));
    }
}

