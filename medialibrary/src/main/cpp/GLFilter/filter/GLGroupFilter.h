//
// Created by CainHuang on 2019/3/17.
//

#ifndef GLGROUPFILTER_H
#define GLGROUPFILTER_H

#include <vector>
#include "GLFilter.h"

/**
 * 组滤镜基类
 */
class GLGroupFilter : public GLFilter {
public:
    GLGroupFilter();

    virtual ~GLGroupFilter();

    void initProgram() override;

    void destroyProgram() override;

    void drawTexture(GLuint texture, float *vertices, float *textureVertices, bool viewPortUpdate = false) override;

    void drawTexture(FrameBuffer *frameBuffer, GLuint texture, float *vertices,
                     float *textureVertices) override;

    // 设置组滤镜的子滤镜使用的FBO宽高缩放值
    void setFrameBufferScale(float scale);

    void setTextureSize(int width, int height) override;

    void setDisplaySize(int width, int height) override;

    void setTimeStamp(double timeStamp) override;

    void setIntensity(float intensity) override;

protected:
    // 初始化program，对于组滤镜来说，默认限制外部使用。主要是高斯模糊这种多个通道的滤镜在用
    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    // 添加滤镜
    void addFilter(GLFilter *filter);

    // 是否包含某个滤镜
    bool containFilter(GLFilter *filter) const;

    // 移除所有滤镜
    void removeAllFilters();

    // 滤镜列表
    std::vector<GLFilter *> filterList;
    // fbo列表，备注，FBO缓冲区的数量
    std::vector<FrameBuffer *> frameBufferList;

    float fboSizeScale;
};


#endif //GLGROUPFILTER_H
