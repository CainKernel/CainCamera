//
// Created by admin on 2018/4/4.
//

#ifndef CAINPLAYER_GLBRIGHTNESSIMAGEFILTER_H
#define CAINPLAYER_GLBRIGHTNESSIMAGEFILTER_H


#include "../GLImageFilter.h"

class GLBrightnessImageFilter : public GLImageFilter {
public:
    GLBrightnessImageFilter();

    GLBrightnessImageFilter(const char *vertexShader, const char *fragmentShader);

    inline void setBrightness(float brightness) {
        this->brightness = brightness;
    }

protected:
    virtual void initHandle(void);

    virtual void bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords);

    virtual const char *getFragmentShader(void);

private:
    int mBrightnessLoc;
    float brightness;
};


#endif //CAINPLAYER_GLBRIGHTNESSIMAGEFILTER_H
