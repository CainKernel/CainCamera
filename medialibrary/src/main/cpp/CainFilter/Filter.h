//
// Created by CainHuang on 2019/3/13.
//

#ifndef FILTER_H
#define FILTER_H

#include "macros.h"

#include "FrameBuffer.h"
#include "FilterManager.h"

#include "base/EglHelper.h"
#include "base/OpenGLUtils.h"
#include "base/vecmath.h"
#include "base/CoordinateUtils.h"

// 基础滤镜
#include "filter/GLGaussianBlurFilter.h"
#include "filter/GLGroupFilter.h"

// 输入滤镜
#include "filter/input/GLInputFilter.h"
#include "filter/input/GLInputABGRFilter.h"
#include "filter/input/GLInputYUV420PFilter.h"
#include "filter/input/GLOESInputFilter.h"

// 美颜滤镜
#include "filter/beauty/GLBeautyFilter.h"

// 滤镜特效
#include "filter/effect/GLEffectGlitterWhiteFilter.h"
#include "filter/effect/GLEffectIllusionFilter.h"
#include "filter/effect/GLEffectScaleFilter.h"
#include "filter/effect/GLEffectShakeFilter.h"
#include "filter/effect/GLEffectSoulStuffFilter.h"
// 分屏特效滤镜
#include "filter/effect/GLFrameBlackWhiteThreeFilter.h"
#include "filter/effect/GLFrameBlurFilter.h"
#include "filter/effect/GLFrameTwoFilter.h"
#include "filter/effect/GLFrameThreeFilter.h"
#include "filter/effect/GLFrameFourFilter.h"
#include "filter/effect/GLFrameSixFilter.h"
#include "filter/effect/GLFrameNineFilter.h"

#endif //FILTER_H
