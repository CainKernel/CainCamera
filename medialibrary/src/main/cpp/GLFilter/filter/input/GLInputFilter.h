//
// Created by CainHuang on 2019/3/13.
//

#ifndef GLINPUTFILTER_H
#define GLINPUTFILTER_H

#include "filter/GLFilter.h"

#include <cstdint>

#define GLES_MAX_PLANE 3

/**
 * 纹理图像格式
 */
typedef enum {
    FMT_NONE = -1,
    FMT_YUV420P,
    FMT_ARGB
} TextureFormat;

/**
 * 设置翻转模式
 */
typedef enum {
    FLIP_NONE = 0x00,
    FLIP_HORIZONTAL = 0x01,
    FLIP_VERTICAL = 0x02
} FlipDirection;

/**
 * 设置混合模式
 */
typedef enum {
    BLEND_NONE = 0x00,
    BLEND_NORMAL = 0x01,
    BLEND_ADD = 0x02,
    BLEND_MODULATE = 0x04,
} BlendMode;

#define NUM_DATA_POINTERS 3
/**
 * 纹理结构体，用于记录纹理宽高、混合模式、YUV还是RGBA格式数据等
 */
typedef struct Texture {
    int width;  // 纹理宽度，即linesize的宽度
    int height; // 纹理高度, 帧高度
    int frameWidth;  // 帧宽度
    int frameHeight; // 帧高度
    BlendMode blendMode; // 混合模式，主要是方便后续添加字幕渲染之类的。字幕是绘制到图像上的，需要开启混合模式。

    FlipDirection direction;    // 翻转格式

    TextureFormat format;                   // 纹理图像格式
    uint16_t pitches[NUM_DATA_POINTERS]; // 宽对齐
    uint8_t *pixels[NUM_DATA_POINTERS]; // 像素数据

} Texture;

/**
 * 图像数据输入滤镜基类
 */
class GLInputFilter : public GLFilter {
public:
    GLInputFilter();

    virtual ~GLInputFilter();

    virtual GLboolean uploadTexture(Texture *texture);

    virtual GLboolean renderTexture(Texture *texture, float *vertices, float *textureVertices);

protected:
    void drawTexture(GLuint texture, float *vertices, float *textureVertices,
                     bool viewPortUpdate = false) override;

    void drawTexture(FrameBuffer *frameBuffer, GLuint texture, float *vertices,
                     float *textureVertices) override;

protected:
    GLuint textureHandle[GLES_MAX_PLANE];   // textureHandle句柄
    GLuint textures[GLES_MAX_PLANE];        // 纹理id
};


#endif //GLINPUTFILTER_H
