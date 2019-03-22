//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLHUEFILTER_H
#define GLHUEFILTER_H

#include <filter/GLIntensityFilter.h>

/**
 * 色调调节滤镜
 */
class GLHueFilter : public GLIntensityFilter {
public:
    void initProgram() override;
};


#endif //GLHUEFILTER_H
