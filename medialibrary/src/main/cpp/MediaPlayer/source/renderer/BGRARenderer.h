//
// Created by cain on 2019/1/16.
//

#ifndef CAINPLAYER_BGRARENDER_H
#define CAINPLAYER_BGRARENDER_H


#include "Renderer.h"

class BGRARenderer : public Renderer {
public:
    BGRARenderer();

    virtual ~BGRARenderer();

    void reset() override;

    int onInit(Texture *texture) override;

    GLboolean uploadTexture(Texture *texture) override;

    GLboolean renderTexture(Texture *texture) override;

private:
    void resetVertices();

    void resetTexVertices();

    void cropTexVertices(Texture *texture);
private:
    int mInited; // 是否已经初始化
};


#endif //CAINPLAYER_BGRARENDER_H
