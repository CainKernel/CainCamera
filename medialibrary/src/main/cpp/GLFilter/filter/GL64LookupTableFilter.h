//
// Created by CainHuang on 2019/3/21.
//

#ifndef GL64LOOKUPTABLEFILTER_H
#define GL64LOOKUPTABLEFILTER_H


#include "GLFilter.h"

class GL64LookupTableFilter : public GLFilter {
public:
    GL64LookupTableFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

protected:
    void bindTexture(GLuint texture) override;

    void onDrawBegin() override;

private:
    int intensityHandle;
    GLuint lutTexture;
};


#endif //GL64LOOKUPTABLEFILTER_H
