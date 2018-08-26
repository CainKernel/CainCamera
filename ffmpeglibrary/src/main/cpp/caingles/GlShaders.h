//
// Created by Administrator on 2018/2/7.
//

#ifndef CAINCAMERA_GLSHADERS_H
#define CAINCAMERA_GLSHADERS_H
#ifdef __cplusplus
extern "C" {
#endif

// 转成字符串
#define SHADER_STRING(s) #s

typedef enum {
    VERTEX_DEFAULT,
    VERTEX_REVERSE,
    FRAGMENT_SOLID,
    FRAGMENT_ABGR,
    FRAGMENT_ARGB,
    FRAGMENT_RGBA,
    FRAGMENT_BGR,
    FRAGMENT_RGB,
    FRAGMENT_I420,
    FRAGMENT_NV12,
    FRAGMENT_NV21
} ShaderType;

// 获取shader
const char *GlShader_GetShader(ShaderType type);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_GLSHADERS_H
