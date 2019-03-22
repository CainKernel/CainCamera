//
// Created by CainHuang on 2019/3/21.
//

#ifndef GL64LOOKUPTABLEFILTER_H
#define GL64LOOKUPTABLEFILTER_H


#include "GLIntensityFilter.h"

class GL64LookupTableFilter : public GLIntensityFilter {
public:
    GL64LookupTableFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setLutTexture(int lutTexture);

protected:
    void bindTexture(GLuint texture) override;

private:
    GLuint lutTexture;
};


#endif //GL64LOOKUPTABLEFILTER_H
