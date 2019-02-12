//
// Created by cain on 2019/1/14.
//

#ifndef RENDERSHADERS_H
#define RENDERSHADERS_H

// 将s转成字符串
#define SHADER_STRING(s) #s

const char *GetDefaultVertexShader();

const char *GetFragmentShader_BGRA();

const char *GetFragmentShader_YUV420P();


#endif //RENDERSHADERS_H
