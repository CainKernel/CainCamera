//
// Created by CainHuang on 2019/3/22.
//

#include "GLImageAdjustFilter.h"
#include "GLBrightnessFilter.h"
#include "GLContrastFilter.h"
#include "GLExposureFilter.h"
#include "GLHueFilter.h"
#include "GLSaturationFilter.h"
#include "GLSharpenFilter.h"

GLImageAdjustFilter::GLImageAdjustFilter() {
    addFilter(new GLBrightnessFilter());
    addFilter(new GLContrastFilter());
    addFilter(new GLExposureFilter());
    addFilter(new GLHueFilter());
    addFilter(new GLSaturationFilter());
    addFilter(new GLSharpenFilter());
}

void GLImageAdjustFilter::setAdjustIntensity(const float *adjust) {
    for (int i = 0; i < 6; ++i) {
        filterList[i]->setIntensity(adjust[i]);
    }
}
