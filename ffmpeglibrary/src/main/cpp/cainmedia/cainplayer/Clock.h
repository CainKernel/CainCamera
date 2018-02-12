//
// Created by Administrator on 2018/2/12.
//

#ifndef CAINCAMERA_CLOCK_H
#define CAINCAMERA_CLOCK_H

#include "PlayerDefintion.h"

class Clock {
public:
    double pts;         // 时钟基准
    double pts_drift;   // 更新时钟的差值
    double last_updated;// 上一个更新时间
    double speed;       // 速度
    int serial;         // 时钟基于使用该序列的包
    int paused;         // 停止标志
    int *queue_serial;  // 只想当前数据包队列的指针，用于过时的时钟检测

private:
    void init(int *queue_serial);

public:

    Clock(int *queue_serial);

    // 获取时钟
    double getClock();
    // 设置时钟
    void setClockAt(double pts, int serial, double time);
    // 设置时钟
    void setClock(double pts, int serial);
    // 设置时钟速度
    void setClockSpeed(double speed);
    // 同步从属时钟
    void syncToSlave(Clock *slave);
    // TODO 获取同步类型

    // TODO 获取主时钟
    // TODO 检查外部时钟速度

};


#endif //CAINCAMERA_CLOCK_H
