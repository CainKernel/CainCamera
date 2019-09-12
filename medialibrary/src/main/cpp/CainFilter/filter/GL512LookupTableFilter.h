//
// Created by CainHuang on 2019/3/21.
//

#ifndef GL512LOOKUPTABLEFILTER_H
#define GL512LOOKUPTABLEFILTER_H


#include "GLIntensityFilter.h"

class GL512LookupTableFilter : public GLIntensityFilter {
public:
    GL512LookupTableFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setLutTexture(int lutTexture);

protected:
    void bindTexture(GLuint texture) override;

private:
    GLuint lutTexture;
};


#endif //GL512LOOKUPTABLEFILTER_H
