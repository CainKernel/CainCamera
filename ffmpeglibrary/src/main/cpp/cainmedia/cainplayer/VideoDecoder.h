//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_VIDEODECODER_H
#define CAINCAMERA_VIDEODECODER_H


#include <android/native_window.h>
#include "BaseDecoder.h"

// 视频解码器
class VideoDecoder : public BaseDecoder {
public:
    VideoDecoder();
    // 视频解码
    void decodeFrame() override;

    // 设置AntiveWindow
    void setNativeWindow(ANativeWindow *nativeWindow);


private:

};


#endif //CAINCAMERA_VIDEODECODER_H
