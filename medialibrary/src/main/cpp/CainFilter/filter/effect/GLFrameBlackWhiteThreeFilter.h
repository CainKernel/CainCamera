//
// Created by CainHuang on 2019/3/16.
//

#ifndef GLFRAMEBLACKWHITETHREEFILTER_H
#define GLFRAMEBLACKWHITETHREEFILTER_H

#include "filter/GLFilter.h"

/**
 * 仿抖音黑白三屏特效
 */
class GLFrameBlackWhiteThreeFilter : public GLFilter {
public:
    GLFrameBlackWhiteThreeFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setScale(float scale);

protected:
    void onDrawBegin() override;

private:
    int scaleHandle;
    float scale;
};


#endif //GLFRAMEBLACKWHITETHREEFILTER_H
