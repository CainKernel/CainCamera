//
// Created by admin on 2018/4/4.
//

#ifndef CAINPLAYER_GLSHARPNESSIMAGEFILTER_H
#define CAINPLAYER_GLSHARPNESSIMAGEFILTER_H

#include "../GLImageFilter.h"

class GLSharpnessImageFilter : public GLImageFilter {
public:
    GLSharpnessImageFilter();

    GLSharpnessImageFilter(const char *vertexShader, const char *fragmentShader);

    inline void setSharpness(float sharpness) {
        this->sharpness = sharpness;
    }

protected:
    virtual void bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords);

public:
    virtual void onInputSizeChanged(int width, int height);

protected:
    virtual void initHandle(void);

    virtual const char *getVertexShader(void);

    virtual const char *getFragmentShader(void);

private:
    int mSharpnessLoc;
    int mImageWidthLoc;
    int mImageHeightLoc;
    float sharpness;
    float widthFactor;
    float heightFactor;

};


#endif //CAINPLAYER_GLSHARPNESSIMAGEFILTER_H
