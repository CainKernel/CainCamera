//
// Created by admin on 2018/4/4.
//

#include "GLExposureImageFilter.h"

static const char fragment_exposure[] = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;
        uniform highp float exposure;

        void main()
        {
            highp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);
        }
);

GLExposureImageFilter::GLExposureImageFilter() {
    GLExposureImageFilter(getVertexShader(), getFragmentShader());

}

GLExposureImageFilter::GLExposureImageFilter(const char *vertexShader, const char *fragmentShader)
        : GLImageFilter(vertexShader, fragmentShader) {

}

const char *GLExposureImageFilter::getFragmentShader(void) {
    return fragment_exposure;
}

void GLExposureImageFilter::initHandle(void) {
    GLImageFilter::initHandle();
    mExposureLoc = glGetUniformLocation(programHandle, "exposure");
    setExposure(0);

}

void GLExposureImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    GLImageFilter::bindValue(texture, vertices, textureCoords);
}


