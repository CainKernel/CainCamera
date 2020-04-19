//
// Created by CainHuang on 2019/3/13.
//

#include <AndroidLog.h>
#include "GLInputABGRFilter.h"

const std::string kABGRFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        uniform sampler2D inputTexture;
        varying vec2 textureCoordinate;

        void main() {
            vec4 abgr = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = abgr;
            gl_FragColor.r = abgr.b;
            gl_FragColor.b = abgr.r;
        }
);

GLInputABGRFilter::GLInputABGRFilter() {
    for (int i = 0; i < 1; ++i) {
        inputTextureHandle[i] = 0;
        textures[i] = 0;
    }
}

GLInputABGRFilter::~GLInputABGRFilter() {

}

void GLInputABGRFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kABGRFragmentShader.c_str());
}

void GLInputABGRFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);

    if (isInitialized()) {
        // 4字节对齐
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        glUseProgram(programHandle);

        if (textures[0] == 0) {
            glGenTextures(1, textures);
            for (int i = 0; i < 1; ++i) {
                glActiveTexture(GL_TEXTURE0 + i);
                glBindTexture(GL_TEXTURE_2D, textures[i]);

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            }
        }
    }
}

GLboolean GLInputABGRFilter::uploadTexture(Texture *texture) {
    // 需要设置4字节对齐
    glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
    glUseProgram(programHandle);
    // 更新纹理数据
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textures[0]);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_RGBA,                 // 对于YUV来说，数据格式是GL_LUMINANCE亮度值，而对于BGRA来说，这个则是颜色通道值
                 texture->pitches[0] / 4, // pixels中存放的数据是BGRABGRABGRA方式排列的，这里除4是为了求出对齐后的宽度，也就是每个颜色通道的数值
                 texture->height,
                 0,
                 GL_RGBA,
                 GL_UNSIGNED_BYTE,
                 texture->pixels[0]);
    glUniform1i(inputTextureHandle[0], 0);
    return GL_TRUE;
}

GLboolean GLInputABGRFilter::renderTexture(Texture *texture, float *vertices, float *textureVertices) {
    if (!texture || !isInitialized()) {
        return GL_FALSE;
    }

    // 绑定属性值
    bindAttributes(vertices, textureVertices);
    // 绘制前处理
    onDrawBegin();
    // 绘制纹理
    onDrawFrame();
    // 绘制后处理
    onDrawAfter();
    // 解绑属性
    unbindAttributes();
    // 解绑纹理
    unbindTextures();
    // 解绑program
    glUseProgram(0);
    return GL_TRUE;
}
