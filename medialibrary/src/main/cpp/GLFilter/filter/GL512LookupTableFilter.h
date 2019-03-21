//
// Created by CainHuang on 2019/3/21.
//

#ifndef GL512LOOKUPTABLEFILTER_H
#define GL512LOOKUPTABLEFILTER_H


#include "GLFilter.h"

class GL512LookupTableFilter : public GLFilter {
public:
    GL512LookupTableFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

protected:
    void bindTexture(GLuint texture) override;

    void onDrawBegin() override;

private:
    int intensityHandle;
    GLuint lutTexture;
};


#endif //GL512LOOKUPTABLEFILTER_H
