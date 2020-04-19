//
// Created by CainHuang on 2020-03-29.
//

#include "PixelBuffer.h"

PixelBuffer::PixelBuffer() : mWidth(0), mHeight(0), mIndex(0) {

}

PixelBuffer::~PixelBuffer() {

}

/**
 * 初始化
 */
void PixelBuffer::init(int width, int height) {
    if (mWidth == width && mHeight == height) {
        return;
    }
    mWidth = width;
    mHeight = height;
    int dataSize = getDataSize();
    if (dataSize > 0) {
        glGenBuffers(2, mPboIds);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, mPboIds[0]);
        glBufferData(GL_PIXEL_PACK_BUFFER, dataSize, 0, GL_STREAM_READ);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, mPboIds[1]);
        glBufferData(GL_PIXEL_PACK_BUFFER, dataSize, 0, GL_STREAM_READ);
        glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    }
}

/**
 * 释放资源
 */
void PixelBuffer::release() {
    if (mPboIds[0]) {
        glDeleteBuffers(2, mPboIds);
    }
}

/**
 * 提取纹理数据
 * @return
 */
GLubyte *PixelBuffer::getPixelBuffer() {
    if (mWidth <= 0 || mHeight <= 0) {
        return nullptr;
    }
    GLubyte *pBuffer = nullptr;
    mIndex = mIndex % 2;
    // 绑定PBO
    glBindBuffer(GL_PIXEL_PACK_BUFFER, mPboIds[mIndex]);
    // 读取像素
    glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);

    // 从glMapBufferRange里面读取像素
    mIndex = (mIndex + 1) % 2;
    // 绑定到另外一个PBO中
    glBindBuffer(GL_PIXEL_PACK_BUFFER, mPboIds[mIndex]);
    pBuffer = static_cast<GLubyte *> (glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0, getDataSize(), GL_MAP_READ_BIT));
    if (pBuffer != nullptr) {
        glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
    }
    // 解绑PBO
    glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
    return pBuffer;
}

/**
 * 上传像素数据
 */
void PixelBuffer::pushPixelBuffer(void *data, int texture) {
    // 没有数据、没有绑定的纹理，直接退出
    if (!data || texture < 0 || mWidth <= 0 || mHeight <= 0) {
        return;
    }
    mIndex = mIndex % 2;
    // 绑定纹理
    glBindTexture(GL_TEXTURE_2D, texture);
    // 绑定PBO
    glBindBuffer(GL_PIXEL_UNPACK_BUFFER, mPboIds[mIndex]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, nullptr);

    // 获取另一个PBO
    mIndex = (mIndex + 1) % 2;
    glBindBuffer(GL_PIXEL_UNPACK_BUFFER, mPboIds[mIndex]);
    glBufferData(GL_PIXEL_UNPACK_BUFFER, getDataSize(), nullptr, GL_STREAM_DRAW);
    GLubyte *buffer = static_cast<GLubyte *>(glMapBufferRange(GL_PIXEL_UNPACK_BUFFER, 0, getDataSize(),
            GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT));
    if (buffer) {
        memcpy(buffer, data, (size_t)getDataSize());
        glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
    }
    // 解绑PBO
    glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
}

/**
 * 获取缓冲区大小
 * @return
 */
int PixelBuffer::getDataSize() {
    return mWidth * mHeight * 4;
}
