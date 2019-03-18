//
// Created by CainHuang on 2019/3/13.
//

#ifndef FILTER_H
#define FILTER_H

#include "macros.h"

#include "FrameBuffer.h"

#include "common/EglHelper.h"
#include "common/OpenGLUtils.h"
#include "common/vecmath.h"
#include "common/CoordinateUtils.h"

// 基础滤镜
#include "filter/GLGaussianBlurFilter.h"
#include "filter/GLGroupFilter.h"

// 输入滤镜
#include "filter/input/GLInputFilter.h"
#include "filter/input/GLInputABGRFilter.h"
#include "filter/input/GLInputYUV420PFilter.h"

// 美颜滤镜
#include "filter/beauty/GLBeautyFilter.h"

// 分屏特效滤镜
#include "filter/effect/GLFrameBlackWhiteThreeFilter.h"
#include "filter/effect/GLFrameTwoFilter.h"
#include "filter/effect/GLFrameThreeFilter.h"
#include "filter/effect/GLFrameFourFilter.h"
#include "filter/effect/GLFrameSixFilter.h"
#include "filter/effect/GLFrameNineFilter.h"

#endif //FILTER_H
