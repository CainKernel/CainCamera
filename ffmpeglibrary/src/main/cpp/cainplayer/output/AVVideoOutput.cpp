//
// Created by cain on 2018/4/30.
//


#include "AVVideoOutput.h"

AVVideoOutput::AVVideoOutput(AVDecoder *decoder) {
    mDecoder = decoder;
    mEglCore = NULL;
    mWindowSurface = NULL;
    mInputFilter = NULL;
    mFrameRGBA = av_frame_alloc();
    img_convert_ctx = NULL;
    mBuffer = NULL;
    mFormat = -1;
}

AVVideoOutput::~AVVideoOutput() {
    mDecoder = NULL;


    if (mFrameRGBA != NULL) {
        av_frame_unref(mFrameRGBA);
        av_frame_free(&mFrameRGBA);
        av_free(mFrameRGBA);
        mFrameRGBA = NULL;
    }

    if (img_convert_ctx != NULL) {
        sws_freeContext(img_convert_ctx);
        img_convert_ctx = NULL;
    }

    if (mBuffer != NULL) {
        av_free(mBuffer);
        mBuffer = NULL;
    }
}

void AVVideoOutput::surfaceCreated(ANativeWindow *window) {
    ALOGD("surfaceCreated");
    if (mEglCore == NULL) {
        mEglCore = new EglCore(NULL, FLAG_RECORDABLE);
    }
    if (mWindowSurface == NULL) {
        mWindowSurface = new WindowSurface(mEglCore, window, false);
    }
    assert(mWindowSurface != NULL && mEglCore != NULL);
    mWindowSurface->makeCurrent();
}

void AVVideoOutput::surfaceChanged(int width, int height) {
    if (mWindowSurface != NULL) {
        mWindowSurface->makeCurrent();

        if (mInputFilter != NULL) {
            mInputFilter->onSurfaceChanged(width, height);
        }
    }
}

void AVVideoOutput::surfaceDestroyed() {
    if (mWindowSurface != NULL) {
        mWindowSurface->makeCurrent();
    }

    if (mInputFilter != NULL) {
        mInputFilter->release();
        delete(mInputFilter);
        mInputFilter = NULL;
    }

    if (mWindowSurface != NULL) {
        mWindowSurface->release();
        delete mWindowSurface;
        mWindowSurface = NULL;
    }

    if (mEglCore != NULL) {
        mEglCore->release();
        delete mEglCore;
        mEglCore = NULL;
    }
}

/**
 * 显示视频画面，需要在setFrameData之后调用
 */
void AVVideoOutput::displayVideo(AVFrame *frame) {
    if (mWindowSurface == NULL) {
        av_frame_free(&frame);
        av_free(frame);
        return;
    }

    // 切换到当前上下文
    mWindowSurface->makeCurrent();

    // 重新创建Texture
    if (reallocTexture(frame) < 0) {
        av_frame_free(&frame);
        av_free(frame);
        return;
    }

    // 渲染视频帧
    renderFrame(frame);

    // 交换显示到前台
    mWindowSurface->swapBuffers();

    // 释放资源，防止内存泄漏
    av_frame_free(&frame);
    av_free(frame);
}

/**
 * 重新创建Texture
 * @return
 */
int AVVideoOutput::reallocTexture(AVFrame *frame) {
    if (frame == NULL || frame->format == AV_PIX_FMT_NONE) {
        return -1;
    }
    switch (frame->format) {
        case AV_PIX_FMT_YUV420P:
        case AV_PIX_FMT_YUVJ420P: {
            if (mFormat != frame->format) {
                mFormat = frame->format;

                if (mInputFilter != NULL) {
                    mInputFilter->release();
                    delete(mInputFilter);
                }
                ALOGD("YUV420P created");
                mInputFilter = new YUV420PImageInputFilter();
                mInputFilter->initHandle();
                mInputFilter->initTexture();
                mInputFilter->onInputSizeChanged(frame->width, frame->height);
                mInputFilter->onSurfaceChanged(mWindowSurface->getWidth(), mWindowSurface->getHeight());
            }
            break;
        }

        case AV_PIX_FMT_RGBA: {
            if (mFormat != frame->format) {
                mFormat = frame->format;

                if (mInputFilter != NULL) {
                    mInputFilter->release();
                    delete(mInputFilter);
                }
                ALOGD("RGBA Created");
//                mInputFilter = new YUV420PImageInputFilter();
//                mInputFilter->initHandle();
//                mInputFilter->initTexture();
            }
            break;
        }

        default: {

            if (mFormat != frame->format) {
                mFormat = frame->format;

                if (mInputFilter != NULL) {
                    mInputFilter->release();
                    delete(mInputFilter);
                }
                ALOGD("default RGBA Created");
//                mInputFilter = new YUV420PImageInputFilter();
//                mInputFilter->initHandle();
//                mInputFilter->initTexture();

                if (mDecoder == NULL) {
                    return -1;
                }
                int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA,
                                                        mDecoder->getCodecContext()->width,
                                                        mDecoder->getCodecContext()->height, 1);
                // 销毁之前的缓冲区
                if (mBuffer != NULL) {
                    av_free(mBuffer);
                    mBuffer = NULL;
                }

                // 重新创建缓冲区
                mBuffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
                av_image_fill_arrays(mFrameRGBA->data, mFrameRGBA->linesize, mBuffer,
                                     AV_PIX_FMT_RGBA, mDecoder->getCodecContext()->width,
                                     mDecoder->getCodecContext()->height, 1);
            }

            // 创建转码上下文
            if (img_convert_ctx == NULL) {
                img_convert_ctx = sws_getCachedContext(img_convert_ctx,
                                                       frame->width, frame->height,
                                                       (AVPixelFormat) frame->format,
                                                       frame->width, frame->height,
                                                       AV_PIX_FMT_BGRA, SWS_BICUBIC,
                                                       NULL, NULL, NULL);
            }

            // 将视频帧转码成RGBA格式
            sws_scale(img_convert_ctx, (const uint8_t * const *) frame->data, frame->linesize,
                      0, frame->height, mFrameRGBA->data, mFrameRGBA->linesize);

            break;
        }
    }
    return 0;
}

/**
 * 渲染视频帧
 */
void AVVideoOutput::renderFrame(AVFrame *frame) {
    // 渲染画面
    switch (frame->format) {
        // 两者都是YUV420P，颜色空间范围不太一样，YUV420P[16, 235]，YUVJ420P[0, 255]，偏色几乎可以忽略不计
        // 小米手机自带相机拍摄出来的视频，格式是YUVJ420P而不是YUV420P的
        case AV_PIX_FMT_YUV420P:
        case AV_PIX_FMT_YUVJ420P: {
            ALOGD("renderFrame AV_PIX_FMT_YUV420P");
            mInputFilter->drawFrame(frame);
            break;
        }

        case AV_PIX_FMT_RGBA: {
            ALOGD("renderFrame AV_PIX_FMT_RGBA");
            mInputFilter->drawFrame(frame);
            break;
        }

        default: {
            if (mFrameRGBA != NULL) {
                ALOGD("renderFrame AV_PIX_FMT_RGBA mFrameRGBA");
                mInputFilter->drawFrame(mFrameRGBA);
            }
            break;
        }
    }

}