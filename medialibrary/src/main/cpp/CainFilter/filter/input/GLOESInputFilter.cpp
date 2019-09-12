//
// Created by CainHuang on 2019/3/21.
//

#include "GLOESInputFilter.h"

const std::string kOESInputVertexShader = SHADER_TO_STRING(
        precision mediump float;
        uniform mat4 transformMatrix;
        attribute highp vec4 aPosition;
        attribute highp vec2 aTextureCoord;
        varying vec2 textureCoordinate;
        void main() {
            gl_Position  = aPosition;
            textureCoordinate = (transformMatrix * aTextureCoord).xy;
        }
);

const std::string kOESInputFragmentShader =
        "#extension GL_OES_EGL_image_external : require\n"
        SHADER_TO_STRING(
                precision mediump float;
                varying vec2 textureCoordinate;
                uniform samplerExternalOES inputTexture;
                void main() {
                    gl_FragColor = texture2D(inputTexture, textureCoordinate);
                }
        );

GLOESInputFilter::GLOESInputFilter() {
    transformMatrixHandle = -1;
    transformMatrix = Matrix4::identity();
}

void GLOESInputFilter::initProgram() {
    GLFilter::initProgram(kOESInputVertexShader.c_str(), kOESInputFragmentShader.c_str());
}

void GLOESInputFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        transformMatrixHandle = glGetUniformLocation(programHandle, "transformMatrix");
    }
}

void GLOESInputFilter::updateTransformMatrix(const float *matrix) {
    transformMatrix.put(matrix);
}

void GLOESInputFilter::bindAttributes(const float *vertices, const float *textureVertices) {
    GLFilter::bindAttributes(vertices, textureVertices);
    if (isInitialized()) {
        glUniformMatrix4fv(transformMatrixHandle, 1, GL_FALSE, transformMatrix.ptr());
    }
}

GLenum GLOESInputFilter::getTextureType() {
    return GL_TEXTURE_EXTERNAL_OES;
}
