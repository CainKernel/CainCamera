//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLSHARPENFILTER_H
#define GLSHARPENFILTER_H

#include <filter/GLIntensityFilter.h>

class GLSharpenFilter : public GLIntensityFilter {
public:
    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

protected:
    void onDrawBegin() override;

private:
    int widthFactorHandle;
    int heightFactorHandle;
};


#endif //GLSHARPENFILTER_H
