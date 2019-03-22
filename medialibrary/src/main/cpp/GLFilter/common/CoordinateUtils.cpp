//
// Created by CainHuang on 2019/3/17.
//

#include "CoordinateUtils.h"

static const  float vertices_default[] = {
        -1.0f, -1.0f,  // left,  bottom
        1.0f,  -1.0f,  // right, bottom
        -1.0f,  1.0f,  // left,  top
        1.0f,   1.0f,  // right, top
};

static const short indices_default[] = {
        0, 1, 2,
        2, 1, 3,
};

static const float texture_vertices_none[] = {
        0.0f, 0.0f, // left,  bottom
        1.0f, 0.0f, // right, bottom
        0.0f, 1.0f, // left,  top
        1.0f, 1.0f, // right, top
};

static const float texture_vertices_90[] = {
        1.0f, 0.0f, // right, bottom
        1.0f, 1.0f, // right, top
        0.0f, 0.0f, // left,  bottom
        0.0f, 1.0f, // left,  top
};

static const float texture_vertices_180[] = {
        1.0f, 1.0f, // righ,  top
        0.0f, 1.0f, // left,  top
        1.0f, 0.0f, // right, bottom
        0.0f, 0.0f, // left,  bottom
};

static const float texture_vertices_270[] = {
        0.0f, 1.0f, // left,  top
        0.0f, 0.0f, // left,  bottom
        1.0f, 1.0f, // right, top
        1.0f, 0.0f, // right, bottom
};

static const float texture_vertices_flip_vertical[] = {
        0.0f, 1.0f, // left,  top
        1.0f, 1.0f, // right, top
        0.0f, 0.0f, // left,  bottom
        1.0f, 0.0f, // right, bottom
};

static const float texture_vertices_flip_horizontal[] = {
        1.0f, 0.0f, // right, bottom
        0.0f, 0.0f, // left,  bottom
        1.0f, 1.0f, // right, top
        0.0f, 1.0f, // left,  top
};

const float *CoordinateUtils::getVertexCoordinates() {
    return vertices_default;
}

const short *CoordinateUtils::getDefaultIndices() {
    return indices_default;
}

const float *CoordinateUtils::getTextureCoordinates(const RotationMode &rotationMode) {
    switch (rotationMode) {
        case ROTATE_NONE: {
            return texture_vertices_none;
        }
        case ROTATE_90: {
            return texture_vertices_90;
        }

        case ROTATE_180: {
            return texture_vertices_180;
        }

        case ROTATE_270: {
            return texture_vertices_270;
        }

        case ROTATE_FLIP_VERTICAL: {
            return texture_vertices_flip_vertical;
        }

        case ROTATE_FLIP_HORIZONTAL: {
            return texture_vertices_flip_horizontal;
        }
    }
    return texture_vertices_none;
}
