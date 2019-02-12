//
// Created by cain on 2019/1/14.
//

#ifndef CAINPLAYER_YUV420PRENDERER_H
#define CAINPLAYER_YUV420PRENDERER_H


#include "Renderer.h"

class YUV420PRenderer : public Renderer {
public:
    YUV420PRenderer();

    virtual ~YUV420PRenderer();

    void reset() override;

    int onInit(Texture *texture) override;

    GLboolean uploadTexture(Texture *texture) override;

    GLboolean renderTexture(Texture *texture) override;

private:
    void resetVertices();

    void resetTexVertices();

private:
    int mInited; // 是否已经初始化
};


#endif //CAINPLAYER_YUV420PRENDERER_H
