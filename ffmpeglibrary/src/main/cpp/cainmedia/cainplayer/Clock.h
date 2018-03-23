//
// Created by Administrator on 2018/3/23.
//

#ifndef CAINCAMERA_CLOCK_H
#define CAINCAMERA_CLOCK_H

#define AV_NOSYNC_THRESHOLD 10.0

class Clock {
public:
    Clock();
    virtual ~Clock();

    double getClock();
    void setClock(double pts, double time);
    void setClock(double pts);
    void setSpeed(double speed);
    void syncToSlave(Clock *slave);

private:
    double pts;             // 显示时间戳，时钟基准
    double pts_drift;       // 更新时钟的差值
    double last_updated;    // 上一次更新的时间
    double speed;           // 速度
    bool mAbortRequest;     // 停止标志
};


#endif //CAINCAMERA_CLOCK_H
