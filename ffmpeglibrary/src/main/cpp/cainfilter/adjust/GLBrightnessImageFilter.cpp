//
// Created by admin on 2018/4/4.
//

#include "GLBrightnessImageFilter.h"

static const char fragment_brightness[] = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;
        uniform lowp float brightness;

        void main()
        {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);
        }
);

GLBrightnessImageFilter::GLBrightnessImageFilter() {
    GLBrightnessImageFilter(getVertexShader(), getFragmentShader());
}

GLBrightnessImageFilter::GLBrightnessImageFilter(const char *vertexShader,
                                                 const char *fragmentShader) : GLImageFilter(
        vertexShader, fragmentShader) {
}

const char *GLBrightnessImageFilter::getFragmentShader(void) {
    return fragment_brightness;
}

void GLBrightnessImageFilter::initHandle(void) {
    GLImageFilter::initHandle();
    mBrightnessLoc = glGetUniformLocation(programHandle, "brightness");
    setBrightness(1.0);
}

void GLBrightnessImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    GLImageFilter::bindValue(texture, vertices, textureCoords);
    glUniform1f(mBrightnessLoc, brightness);
}



