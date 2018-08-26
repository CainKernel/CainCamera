//
// Created by admin on 2018/4/4.
//

#include "GLContrastImageFilter.h"

static const char fragment_contrast[] = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;
        uniform lowp float contrast;

        void main()
        {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
        }
);

GLContrastImageFilter::GLContrastImageFilter() {
    GLContrastImageFilter(getVertexShader(), getFragmentShader());
}

GLContrastImageFilter::GLContrastImageFilter(const char *vertexShader, const char *fragmentShader)
        : GLImageFilter(vertexShader, fragmentShader) {
}

const char *GLContrastImageFilter::getFragmentShader(void) {
    return fragment_contrast;
}

void GLContrastImageFilter::initHandle(void) {
    GLImageFilter::initHandle();
    mContrastLoc = glGetUniformLocation(programHandle, "contrast");
    setContrast(1.0);
}

void GLContrastImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    GLImageFilter::bindValue(texture, vertices, textureCoords);
    glUniform1f(mContrastLoc, contrast);
}
