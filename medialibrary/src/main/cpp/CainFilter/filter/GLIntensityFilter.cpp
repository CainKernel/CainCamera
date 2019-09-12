//
// Created by CainHuang on 2019/3/22.
//

#include "GLIntensityFilter.h"

GLIntensityFilter::GLIntensityFilter() : intensityHandle(1.0f) {

}

void GLIntensityFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        intensityHandle = glGetUniformLocation(programHandle, "intensity");
    }
}

void GLIntensityFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(intensityHandle, intensity);
    }
}

