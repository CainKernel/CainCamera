//
// Created by CainHuang on 2020-04-18.
//

#include <AVMediaHeader.h>
#include "GLVideoRender.h"

GLVideoRender::GLVideoRender() {
    mWindow = nullptr;
    mSurfaceWidth = 0;
    mSurfaceHeight = 0;
    eglSurface = EGL_NO_SURFACE;
    eglHelper = new EglHelper();

    mHaveEGLSurface = false;
    mHaveEGlContext = false;
    mHasSurface = false;

    mVideoTexture = (Texture *) malloc(sizeof(Texture));
    memset(mVideoTexture, 0, sizeof(Texture));
    mRenderNode = nullptr;
    nodeList = new RenderNodeList();
    nodeList->addNode(new DisplayRenderNode());     // 显示渲染结点
    memset(&filterInfo, 0, sizeof(FilterInfo));
    filterInfo.type = NODE_NONE;
    filterInfo.name = nullptr;
    filterInfo.id = -1;
    filterChange = false;

    resetVertices();
    resetTexVertices();
}

GLVideoRender::~GLVideoRender() {
    terminate(true);
}

void GLVideoRender::surfaceCreated(ANativeWindow *window) {
    if (mWindow != nullptr) {
        ANativeWindow_release(mWindow);
        mWindow = nullptr;
        mSurfaceReset = true;
    }
    mWindow = window;
    if (mWindow != nullptr) {
        mSurfaceWidth = ANativeWindow_getWidth(mWindow);
        mSurfaceHeight = ANativeWindow_getHeight(mWindow);
    }
    mHasSurface = true;
}

void GLVideoRender::surfaceChanged(int width, int height) {
    mSurfaceWidth = width;
    mSurfaceHeight = height;
}

void GLVideoRender::terminate(bool releaseContext) {
    if (eglSurface != EGL_NO_SURFACE) {
        eglHelper->destroySurface(eglSurface);
        eglSurface = EGL_NO_SURFACE;
        mHaveEGLSurface = false;
    }
    if (eglHelper->getEglContext() != EGL_NO_CONTEXT && releaseContext) {
        if (mRenderNode) {
            mRenderNode->destroy();
            delete mRenderNode;
        }
        eglHelper->release();
        mHaveEGlContext = false;
    }
    // 释放Surface
    if (releaseContext && mWindow != nullptr) {
        ANativeWindow_release(mWindow);
        mWindow = nullptr;
    }
}

/**
 * 设置时间戳
 */
void GLVideoRender::setTimeStamp(double timeStamp) {
    if (nodeList) {
        nodeList->setTimeStamp(timeStamp);
    }
}

/**
 * 初始化纹理
 */
void GLVideoRender::initTexture(int width, int height, int rotate) {
    // 创建EGLContext
    if (!mHaveEGlContext) {
        mHaveEGlContext = eglHelper->init(FLAG_TRY_GLES3);
        LOGD("mHaveEGlContext = %d", mHaveEGlContext);
    }

    if (!mHaveEGlContext) {
        return;
    }

    // 重新设置Surface，兼容SurfaceHolder处理
    if (mHasSurface && mSurfaceReset) {
        terminate(false);
        mSurfaceReset = false;
    }

    // 创建/释放EGLSurface
    if (eglSurface == EGL_NO_SURFACE && mWindow != nullptr) {
        if (mHasSurface && !mHaveEGLSurface) {
            eglSurface = eglHelper->createSurface(mWindow);
            if (eglSurface != EGL_NO_SURFACE) {
                mHaveEGLSurface = true;
                LOGD("mHaveEGLSurface = %d", mHaveEGLSurface);
            }
        }
    } else if (eglSurface != EGL_NO_SURFACE && mHaveEGLSurface) {
        // 处于SurfaceDestroyed状态，释放EGLSurface
        if (!mHasSurface) {
            terminate(false);
        }
    }

    // 计算帧的宽高，如果不相等，则需要重新计算缓冲区的大小
    if (mWindow != nullptr) {
        mSurfaceWidth = ANativeWindow_getWidth(mWindow);
        mSurfaceHeight = ANativeWindow_getHeight(mWindow);
    }
    if (mWindow != nullptr && mSurfaceWidth != 0 && mSurfaceHeight != 0) {
        // 宽高比例不一致时，需要调整缓冲区的大小，这里是以宽度为基准
        if ((mSurfaceWidth / mSurfaceHeight) != (width / height)) {
            mSurfaceHeight = mSurfaceWidth * height / width;
            int windowFormat = ANativeWindow_getFormat(mWindow);
            ANativeWindow_setBuffersGeometry(mWindow, mSurfaceWidth, mSurfaceHeight, windowFormat);
        }
    }
    mVideoTexture->rotate = rotate;
    mVideoTexture->frameWidth = width;
    mVideoTexture->frameHeight = height;
    mVideoTexture->height = height;
    mVideoTexture->format = FMT_YUV420P;
    mVideoTexture->blendMode = BLEND_NONE;
    mVideoTexture->direction = FLIP_NONE;
    eglHelper->makeCurrent(eglSurface);
    if (mRenderNode == nullptr) {
        mRenderNode = new InputRenderNode();
        if (mRenderNode != nullptr) {
            mRenderNode->initFilter(mVideoTexture);
            FrameBuffer *frameBuffer = new FrameBuffer(width, height);
            frameBuffer->init();
            mRenderNode->setFrameBuffer(frameBuffer);
        }
    }
    if (filterChange) {
        nodeList->changeFilter(filterInfo.type, FilterManager::getInstance()->getFilter(&filterInfo));
        filterChange = false;
    }
    nodeList->init();
    nodeList->setTextureSize(width, height);
}

/**
 * 更新上载yuv纹理数据
 * @param yData
 * @param yPitch
 * @param uData
 * @param uPitch
 * @param vData
 * @param vPitch
 * @return
 */
int GLVideoRender::uploadData(uint8_t *yData, int yPitch, uint8_t *uData, int uPitch, uint8_t *vData,
                          int vPitch) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mVideoTexture->pitches[0] = yPitch;
    mVideoTexture->pitches[1] = uPitch;
    mVideoTexture->pitches[2] = vPitch;
    mVideoTexture->pixels[0] = yData;
    mVideoTexture->pixels[1] = uData;
    mVideoTexture->pixels[2] = vData;
    if (mRenderNode != nullptr && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        mRenderNode->uploadTexture(mVideoTexture);
    }
    // 设置像素实际的宽度，即linesize的值
    mVideoTexture->width = yPitch;
    return 0;
}

/**
 * 渲染一帧数据
 * @return
 */
int GLVideoRender::renderFrame() {
    if (!mHaveEGlContext) {
        return -1;
    }
    mVideoTexture->direction = FLIP_NONE;
    if (mRenderNode != nullptr && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        int texture = mRenderNode->drawFrameBuffer(mVideoTexture);
        if (mSurfaceWidth != 0 && mSurfaceHeight != 0) {
            nodeList->setDisplaySize(mSurfaceWidth, mSurfaceHeight);
        }
        nodeList->drawFrame(texture, vertices, textureVertices);
        eglHelper->swapBuffers(eglSurface);
    }
    return 0;
}

/**
 * 切换滤镜
 * @param type
 * @param filterName
 */
void GLVideoRender::changeFilter(RenderNodeType type, const char *filterName) {
    if (filterInfo.type == type && strcmp(filterName, filterInfo.name)) {
        return;
    }
    filterInfo.type = type;
    filterInfo.name = av_strdup(filterName);
    filterInfo.id = -1;
    filterChange = true;
}

/**
 * 切换滤镜
 * @param type
 * @param id
 */
void GLVideoRender::changeFilter(RenderNodeType type, const int id) {
    if (filterInfo.type == type && filterInfo.id == id) {
        return;
    }
    filterInfo.type = type;
    if (filterInfo.name) {
        av_freep(&filterInfo.name);
        filterInfo.name = nullptr;
    }
    filterInfo.name = nullptr;
    filterInfo.id = id;
    filterChange = true;
}

void GLVideoRender::resetVertices() {
    const float *verticesCoord = CoordinateUtils::getVertexCoordinates();
    for (int i = 0; i < 8; ++i) {
        vertices[i] = verticesCoord[i];
    }
}

void GLVideoRender::resetTexVertices() {
    const float *vertices = CoordinateUtils::getTextureCoordinates(ROTATE_NONE);
    for (int i = 0; i < 8; ++i) {
        textureVertices[i] = vertices[i];
    }
}
