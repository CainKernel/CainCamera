//
// Created by cain on 2018/12/30.
//

#include "GLESDevice.h"

GLESDevice::GLESDevice() {
    mWindow = NULL;
    mSurfaceWidth = 0;
    mSurfaceHeight = 0;
    eglSurface = EGL_NO_SURFACE;
    eglHelper = new EglHelper();

    mHaveEGLSurface = false;
    mHaveEGlContext = false;
    mHasSurface = false;

    mVideoTexture = (Texture *) malloc(sizeof(Texture));
    memset(mVideoTexture, 0, sizeof(Texture));
    mRenderNode = NULL;
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

GLESDevice::~GLESDevice() {
    mMutex.lock();
    terminate();
    mMutex.unlock();
}

void GLESDevice::surfaceCreated(ANativeWindow *window) {
    mMutex.lock();
    if (mWindow != NULL) {
        ANativeWindow_release(mWindow);
        mWindow = NULL;
        mSurfaceReset = true;
    }
    mWindow = window;
    if (mWindow != NULL) {
        mSurfaceWidth = ANativeWindow_getWidth(mWindow);
        mSurfaceHeight = ANativeWindow_getHeight(mWindow);
    }
    mHasSurface = true;
    mMutex.unlock();
}

void GLESDevice::terminate() {
    terminate(true);
}

void GLESDevice::terminate(bool releaseContext) {
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
}

void GLESDevice::setTimeStamp(double timeStamp) {
    mMutex.lock();
    if (nodeList) {
        nodeList->setTimeStamp(timeStamp);
    }
    mMutex.unlock();
}

void GLESDevice::onInitTexture(int width, int height, TextureFormat format, BlendMode blendMode,
                               int rotate) {
    mMutex.lock();

    // 创建EGLContext
    if (!mHaveEGlContext) {
        mHaveEGlContext = eglHelper->init(FLAG_TRY_GLES3);
        ALOGD("mHaveEGlContext = %d", mHaveEGlContext);
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
    if (eglSurface == EGL_NO_SURFACE && mWindow != NULL) {
        if (mHasSurface && !mHaveEGLSurface) {
            eglSurface = eglHelper->createSurface(mWindow);
            if (eglSurface != EGL_NO_SURFACE) {
                mHaveEGLSurface = true;
                ALOGD("mHaveEGLSurface = %d", mHaveEGLSurface);
            }
        }
    } else if (eglSurface != EGL_NO_SURFACE && mHaveEGLSurface) {
        // 处于SurfaceDestroyed状态，释放EGLSurface
        if (!mHasSurface) {
            terminate(false);
        }
    }

    // 计算帧的宽高，如果不相等，则需要重新计算缓冲区的大小
    if (mWindow != NULL && mSurfaceWidth != 0 && mSurfaceHeight != 0) {
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
    mVideoTexture->format = format;
    mVideoTexture->blendMode = blendMode;
    mVideoTexture->direction = FLIP_NONE;
    eglHelper->makeCurrent(eglSurface);
    if (mRenderNode == NULL) {
        mRenderNode = new InputRenderNode();
        if (mRenderNode != NULL) {
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
    mMutex.unlock();
}

int GLESDevice::onUpdateYUV(uint8_t *yData, int yPitch, uint8_t *uData, int uPitch, uint8_t *vData,
                            int vPitch) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mMutex.lock();
    mVideoTexture->pitches[0] = yPitch;
    mVideoTexture->pitches[1] = uPitch;
    mVideoTexture->pitches[2] = vPitch;
    mVideoTexture->pixels[0] = yData;
    mVideoTexture->pixels[1] = uData;
    mVideoTexture->pixels[2] = vData;
    if (mRenderNode != NULL && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        mRenderNode->uploadTexture(mVideoTexture);
    }
    // 设置像素实际的宽度，即linesize的值
    mVideoTexture->width = yPitch;
    mMutex.unlock();
    return 0;
}

int GLESDevice::onUpdateARGB(uint8_t *rgba, int pitch) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mMutex.lock();
    mVideoTexture->pitches[0] = pitch;
    mVideoTexture->pixels[0] = rgba;
    if (mRenderNode != NULL && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        mRenderNode->uploadTexture(mVideoTexture);
    }
    // 设置像素实际的宽度，即linesize的值
    mVideoTexture->width = pitch / 4;
    mMutex.unlock();
    return 0;
}

int GLESDevice::onRequestRender(bool flip) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mMutex.lock();
    mVideoTexture->direction = flip ? FLIP_VERTICAL : FLIP_NONE;
    if (mRenderNode != NULL && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        int texture = mRenderNode->drawFrameBuffer(mVideoTexture);
        if (mSurfaceWidth != 0 && mSurfaceHeight != 0) {
            nodeList->setDisplaySize(mSurfaceWidth, mSurfaceHeight);
        }
        nodeList->drawFrame(texture, vertices, textureVertices);
        eglHelper->swapBuffers(eglSurface);
    }
    mMutex.unlock();
    return 0;
}


void GLESDevice::changeFilter(RenderNodeType type, const char *filterName) {
    mMutex.lock();
    filterInfo.type = type;
    filterInfo.name = av_strdup(filterName);
    filterInfo.id = -1;
    filterChange = true;
    mMutex.unlock();
}

void GLESDevice::changeFilter(RenderNodeType type, const int id) {
    mMutex.lock();
    filterInfo.type = type;
    if (filterInfo.name) {
        av_freep(&filterInfo.name);
        filterInfo.name = nullptr;
    }
    filterInfo.name = nullptr;
    filterInfo.id = id;
    filterChange = true;
    mMutex.unlock();
}

void GLESDevice::resetVertices() {
    const float *verticesCoord = CoordinateUtils::getVertexCoordinates();
    for (int i = 0; i < 8; ++i) {
        vertices[i] = verticesCoord[i];
    }
}

void GLESDevice::resetTexVertices() {
    const float *vertices = CoordinateUtils::getTextureCoordinates(ROTATE_NONE);
    for (int i = 0; i < 8; ++i) {
        textureVertices[i] = vertices[i];
    }
}
