//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLDROSTEFILTER_H
#define GLDROSTEFILTER_H


#include "GLFilter.h"

/**
 * 德罗斯特滤镜
 */
class GLDrosteFilter : public GLFilter {

public:
    GLDrosteFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

protected:
    void onDrawBegin() override;

private:
    int repeatHandle;
    int repeat;
};


#endif //GLDROSTEFILTER_H
