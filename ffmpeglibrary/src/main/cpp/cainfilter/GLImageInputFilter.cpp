//
// Created by cain on 2018/5/6.
//

#include "GLImageInputFilter.h"

GLImageInputFilter::GLImageInputFilter() {}

GLImageInputFilter::~GLImageInputFilter() {

}

int GLImageInputFilter::initHandle(void) {
    return 0;
}

void GLImageInputFilter::initTexture() {

}

void GLImageInputFilter::onInputSizeChanged(int width, int height) {

}

void GLImageInputFilter::onSurfaceChanged(int width, int height) {

}

bool GLImageInputFilter::drawFrame(AVFrame *yuvFrame) {
    return false;
}

int GLImageInputFilter::drawFrameBuffer(AVFrame *yuvFrame) {
    return -1;
}

void GLImageInputFilter::initFrameBuffer(int width, int height) {

}

void GLImageInputFilter::destroyFrameBuffer() {

}

void GLImageInputFilter::release(void) {

}

void GLImageInputFilter::initCoordinates() {

}