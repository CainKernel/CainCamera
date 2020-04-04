//
// Created by CainHuang on 2019/8/17.
//

#include "FFMediaRecorder.h"


FFMediaRecorder::FFMediaRecorder() : mRecordListener(nullptr), mAbortRequest(true),
                                     mStartRequest(false), mExit(true), mRecordThread(nullptr),
                                     mYuvConvertor(nullptr), mFrameFilter(nullptr),
                                     mFrameQueue(nullptr), mMediaWriter(nullptr),
                                     mUseHardCodec(false) {
    av_register_all();
    avfilter_register_all();
    mRecordParams = new RecordParams();
}

FFMediaRecorder::~FFMediaRecorder() {
    release();
    if (mRecordParams != nullptr) {
        delete mRecordParams;
        mRecordParams = nullptr;
    }
}

/**
 * 设置录制监听器
 * @param listener
 */
void FFMediaRecorder::setOnRecordListener(OnRecordListener *listener) {
    mRecordListener = listener;
}

/**
 * 释放资源
 */
void FFMediaRecorder::release() {
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
    if (mFrameFilter != nullptr) {
        mFrameFilter->release();
        delete mFrameFilter;
        mFrameFilter = nullptr;
    }
    if (mMediaWriter != nullptr) {
        mMediaWriter->release();
        mMediaWriter.reset();
        mMediaWriter = nullptr;
    }
}

RecordParams* FFMediaRecorder::getRecordParams() {
    return mRecordParams;
}

/**
 * 初始化录制器
 * @param params
 * @return
 */
int FFMediaRecorder::prepare() {
    if (mMediaWriter != nullptr) {
        LOGE("encoder is working");
        return -1;
    }

    RecordParams *params = mRecordParams;
    if (params->rotateDegree % 90 != 0) {
        LOGE("invalid rotate degree: %d", params->rotateDegree);
        return -1;
    }

    AVPixelFormat pixelFormat = getPixelFormat((PixelFormat)params->pixelFormat);
    AVSampleFormat sampleFormat = getSampleFormat((SampleFormat)params->sampleFormat);
    if ((!params->enableVideo && !params->enableAudio)
        || (params->enableVideo && pixelFormat == AV_PIX_FMT_NONE)
            && (params->enableAudio && sampleFormat == AV_SAMPLE_FMT_NONE)) {
        LOGE("pixel format and sample format invalid: %d, %d", params->pixelFormat,
             params->sampleFormat);
        return -1;
    }

    int ret;

    mFrameQueue = new SafetyQueue<AVMediaData *>();

    LOGI("Record to file: %s, width: %d, height: %d", params->dstFile, params->width,
         params->height);

    // yuv转换
    int outputWidth = params->width;
    int outputHeight = params->height;

    // yuv转换器
    mYuvConvertor = new YuvConvertor();
    mYuvConvertor->setInputParams(params->width, params->height, params->pixelFormat);
    mYuvConvertor->setCrop(params->cropX, params->cropY, params->cropWidth, params->cropHeight);
    mYuvConvertor->setRotate(params->rotateDegree);
    mYuvConvertor->setScale(params->scaleWidth, params->scaleHeight);
    mYuvConvertor->setMirror(params->mirror);
    // 准备
    if (mYuvConvertor->prepare() < 0) {
        delete mYuvConvertor;
        mYuvConvertor = nullptr;
    } else {
        pixelFormat = AV_PIX_FMT_YUV420P;
        outputWidth = mYuvConvertor->getOutputWidth();
        outputHeight = mYuvConvertor->getOutputHeight();
        if (outputWidth == 0 || outputHeight == 0) {
            outputWidth = params->rotateDegree % 180 == 0 ? params->width : params->height;
            outputHeight = params->rotateDegree % 180 == 0 ? params->height : params->width;
        }
    }

    // filter过滤
    if ((params->videoFilter && strcmp(params->videoFilter, "null") != 0)
        || (params->audioFilter && strcmp(params->audioFilter, "anull") != 0)) {
        mFrameFilter = new AVFrameFilter();
        mFrameFilter->setVideoInput(outputWidth, outputHeight, pixelFormat, params->frameRate, params->videoFilter);
        mFrameFilter->setVideoOutput(AV_PIX_FMT_YUV420P);
        mFrameFilter->setAudioInput(params->sampleRate, params->channels, sampleFormat, params->audioFilter);
        ret = mFrameFilter->initFilter();
        if (ret < 0) {
            delete mFrameFilter;
            mFrameFilter = nullptr;
        } else {
            pixelFormat = AV_PIX_FMT_YUV420P;
        }
    }

    // 创建媒体写入器
    if (mUseHardCodec) {
        mMediaWriter = std::make_shared<NdkMediaWriter>();
    } else {
        mMediaWriter = std::make_shared<AVMediaWriter>();
    }
    // 设置参数
    mMediaWriter->setUseTimeStamp(true);
    // 软编码时设置编码额外的编码参数和编码器名称
    if (!mUseHardCodec) {
        mMediaWriter->addEncodeOptions("preset", "ultrafast");
        mMediaWriter->setQuality(params->quality > 0 ? params->quality : 23);
        // 指定编码器名称
        if (params->videoEncoder != nullptr) {
            mMediaWriter->setVideoEncoderName(params->videoEncoder);
        }
        if (params->audioEncoder != nullptr) {
            mMediaWriter->setAudioEncoderName(params->audioEncoder);
        }
    }
    mMediaWriter->setMaxBitRate(params->maxBitRate);
    mMediaWriter->setOutputPath(params->dstFile);
    mMediaWriter->setOutputVideo(outputWidth, outputHeight, params->frameRate, pixelFormat);
    mMediaWriter->setOutputAudio(params->sampleRate, params->channels, sampleFormat);

    // 准备
    ret = mMediaWriter->prepare();
    if (ret < 0) {
        release();
    }
    return ret;
}

/**
 * 录制一帧数据
 * @param data
 * @return
 */
int FFMediaRecorder::recordFrame(AVMediaData *data) {
    if (mAbortRequest || mExit) {
        LOGE("Recoder is not recording.");
        delete data;
        return -1;
    }

    // 录制的是音频数据并且不允许音频录制，则直接删除
    if (!mRecordParams->enableAudio && data->getType() == MediaAudio) {
        delete data;
        return -1;
    }

    // 录制的是视频数据并且不允许视频录制，直接删除
    if (!mRecordParams->enableVideo && data->getType() == MediaVideo) {
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

/**
 * 开始录制
 */
void FFMediaRecorder::startRecord() {
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

/**
 * 停止录制
 */
void FFMediaRecorder::stopRecord() {
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

/**
 * 判断是否正在录制
 * @return
 */
bool FFMediaRecorder::isRecording() {
    bool recording = false;
    mMutex.lock();
    recording = !mAbortRequest && mStartRequest && !mExit;
    mMutex.unlock();
    return recording;
}

void FFMediaRecorder::run() {
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

                // 过滤
                if (mFrameFilter != nullptr) {
                    ret = mFrameFilter->filterData(data);
                    if (ret < 0) {
                        LOGE("Failed to filter media data: %s", data->getName());
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

        // 清空队列
        while(!mFrameQueue->empty()) {
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

            // 过滤
            if (mFrameFilter != nullptr) {
                ret = mFrameFilter->filterData(data);
                if (ret < 0) {
                    LOGE("Failed to filter media data: %s", data->getName());
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

        // 停止文件写入器
        ret = mMediaWriter->stop();
    }

    LOGD("FFMediaRecorder exiting...");
    mMediaWriter->release();

    // 录制完成回调
    if (mRecordListener != nullptr) {
        mRecordListener->onRecordFinish(ret == 0, (float)(current - start));
    }

    // 通知退出成功
    mExit = true;
    mCondition.signal();
}