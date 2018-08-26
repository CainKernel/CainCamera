//
// Created by admin on 2018/4/4.
//

#include "GLSaturationImageFilter.h"

static const char fragment_satutation[] = SHADER_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform sampler2D inputTexture;
        uniform lowp float inputLevel;
        const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);
        void main() {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
            lowp vec3 greyScaleColor = vec3(luminance);
            gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, inputLevel), textureColor.w);
        }
);

GLSatutationImageFilter::GLSatutationImageFilter() {
    GLSatutationImageFilter(getVertexShader(), getFragmentShader());
}

GLSatutationImageFilter::GLSatutationImageFilter(const char *vertexShader,
                                                 const char *fragmentShader) : GLImageFilter(
        vertexShader, fragmentShader) {}

const char *GLSatutationImageFilter::getFragmentShader(void) {
    return fragment_satutation;
}

void GLSatutationImageFilter::initHandle(void) {
    GLImageFilter::initHandle();
    mSaturationLoc = glGetUniformLocation(programHandle, "inputLevel");
    setSaturation(1.0);
}

void GLSatutationImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    GLImageFilter::bindValue(texture, vertices, textureCoords);
    glUniform1f(mSaturationLoc, saturation);
}
