//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLSATURATIONFILTER_H
#define GLSATURATIONFILTER_H

#include <filter/GLIntensityFilter.h>

class GLSaturationFilter : public GLIntensityFilter {
public:
    void initProgram() override;
};


#endif //GLSATURATIONFILTER_H
