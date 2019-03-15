//
// Created by CainHuang on 2019/3/13.
//

#include "FrameBuffer.h"

TextureAttributes FrameBuffer::defaultTextureAttributes = {
        .minFilter = GL_LINEAR,
        .magFilter = GL_LINEAR,
        .wrapS = GL_CLAMP_TO_EDGE,
        .wrapT = GL_CLAMP_TO_EDGE,
        .internalFormat = GL_RGBA,
        .format = GL_RGBA,
        .type = GL_UNSIGNED_BYTE
};

FrameBuffer::FrameBuffer(int width, int height, const TextureAttributes textureAttributes)
                         : texture(-1), framebuffer(-1) {
    this->width = width;
    this->height = height;
    this->textureAttributes = textureAttributes;
    initialized = false;
}

FrameBuffer::~FrameBuffer() {

}

void FrameBuffer::init() {
    createFrameBuffer();
}

void FrameBuffer::destroy() {
    destroyFrameBuffer();
}

void FrameBuffer::bindBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
    glViewport(0, 0, width, height);
}

void FrameBuffer::unbindBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}

void FrameBuffer::createTexture() {
    glGenTextures(1, &texture);
    glBindTexture(GL_TEXTURE_2D, texture);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, textureAttributes.minFilter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, textureAttributes.magFilter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, textureAttributes.wrapS);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, textureAttributes.wrapT);
}

void FrameBuffer::createFrameBuffer() {
    glGenFramebuffers(1, &framebuffer);
    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);

    createTexture();
    glTexImage2D(GL_TEXTURE_2D, 0, textureAttributes.internalFormat, width, height, 0, textureAttributes.format, textureAttributes.type, 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);

    glBindTexture(GL_TEXTURE_2D, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    initialized = true;
}

void FrameBuffer::destroyFrameBuffer() {

}
