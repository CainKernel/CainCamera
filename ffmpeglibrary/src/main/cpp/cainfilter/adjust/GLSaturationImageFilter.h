//
// Created by admin on 2018/4/4.
//

#ifndef CAINPLAYER_GLSATURATIONIMAGEFILTER_H
#define CAINPLAYER_GLSATURATIONIMAGEFILTER_H


#include "../GLImageFilter.h"

class GLSatutationImageFilter : public GLImageFilter {
public:
    GLSatutationImageFilter();

    GLSatutationImageFilter(const char *vertexShader, const char *fragmentShader);

    inline void setSaturation(float saturation) {
        this->saturation = saturation;
    }

protected:
    virtual const char *getFragmentShader(void);

    virtual void bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords);

    virtual void initHandle(void);

private:
    int mSaturationLoc;
    float saturation;
};


#endif //CAINPLAYER_GLSATURATIONIMAGEFILTER_H
