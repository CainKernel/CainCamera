//
// Created by CainHuang on 2019/3/26.
//

#ifndef GLEFFECTSCALEFILTER_H
#define GLEFFECTSCALEFILTER_H


#include <filter/GLFilter.h>

class GLEffectScaleFilter : public GLFilter {
public:
    GLEffectScaleFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setTimeStamp(double timeStamp) override;

protected:
    void onDrawBegin() override;

private:
    int scaleHandle;
    bool plus;
    float scale;
    float offset;
};


#endif //GLEFFECTSCALEFILTER_H
