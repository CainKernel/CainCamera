//
// Created by CainHuang on 2019/3/13.
//

#ifndef FILTER_H
#define FILTER_H

#include "macros.h"

#include "common/EglHelper.h"
#include "common/OpenGLUtils.h"
#include "common/vecmath.h"

// 输入滤镜
#include "filter/GLInputFilter.h"
#include "filter/GLInputABGRFilter.h"
#include "filter/GLInputYUV420PFilter.h"

// 特效滤镜
#include "filter/GLFrameTwoFilter.h"

#endif //FILTER_H
