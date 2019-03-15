//
// Created by CainHuang on 2019/3/13.
//

#ifndef CAINCAMERA_FRAMEBUFFER_H
#define CAINCAMERA_FRAMEBUFFER_H

#if defined(__ANDROID__)
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

/**
 * 纹理Attribute参数
 */
typedef struct {
    GLenum minFilter;
    GLenum magFilter;
    GLenum wrapS;
    GLenum wrapT;
    GLenum format;
    GLenum internalFormat;
    GLenum type;
} TextureAttributes;

/**
 * FBO缓冲区
 */
class FrameBuffer {
public:
    FrameBuffer(int width, int height,
            const TextureAttributes textureAttributes = defaultTextureAttributes);

    virtual ~FrameBuffer();

    void init();

    void destroy();

    void bindBuffer();

    void unbindBuffer();

    GLuint getTexture() const {
        return texture;
    }

    GLuint getFrameBuffer() const {
        return framebuffer;
    }

    int getWidth() const {
        return width;
    }

    int getHeight() const {
        return height;
    }

    const TextureAttributes& getTextureAttributes() const {
        return textureAttributes;
    }

    bool isInitialized() const {
        return initialized;
    }

    static TextureAttributes defaultTextureAttributes;

private:
    int width, height;
    bool initialized;
    GLuint texture;
    GLuint framebuffer;
    TextureAttributes textureAttributes;

private:
    void createTexture();

    void createFrameBuffer();

    void destroyFrameBuffer();
};


#endif //CAINCAMERA_FRAMEBUFFER_H
