//
// Created by CainHuang on 2020-02-27.
//

#include "../AVMediaHeader.h"

/**
 * 读帧监听器
 */
class OnDecodeListener {
public:
    virtual ~OnDecodeListener() = default;

    // 解码开始
    virtual void onDecodeStart(AVMediaType type) = 0;

    // 解码结束
    virtual void onDecodeFinish(AVMediaType type) = 0;

    // 定位结束
    virtual void onSeekComplete(AVMediaType type, float seekTime) = 0;

    // seek出错回调
    virtual void onSeekError(AVMediaType type, int ret) = 0;
};
