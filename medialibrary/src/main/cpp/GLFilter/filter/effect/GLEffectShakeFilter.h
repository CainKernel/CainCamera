//
// Created by CainHuang on 2019/3/26.
//

#ifndef GLEFFECTSHAKEFILTER_H
#define GLEFFECTSHAKEFILTER_H


#include <filter/GLFilter.h>

class GLEffectShakeFilter : public GLFilter {
public:
    GLEffectShakeFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setTimeStamp(double timeStamp) override;

protected:
    void onDrawBegin() override;

private:
    int scaleHandle;
    float scale;
    float offset;
};


#endif //GLEFFECTSHAKEFILTER_H
