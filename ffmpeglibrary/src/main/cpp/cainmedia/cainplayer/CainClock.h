//
// Created by Administrator on 2018/2/8.
//

#ifndef CAINCAMERA_CLOCK_H
#define CAINCAMERA_CLOCK_H

#ifdef __cplusplus
extern "C" {
#endif

#include "CainPlayerDefinition.h"

// 获取时钟
double get_clock(Clock *c);
// 设置时钟
void set_clock_at(Clock *c, double pts, int serial, double time);
// 设置时钟
void set_clock(Clock *c, double pts, int serial);
// 设置时钟速度
void set_clock_speed(Clock *c, double speed);
// 初始化时钟
void init_clock(Clock *c, int *queue_serial);
// 同步从属时钟
void sync_clock_to_slave(Clock *c, Clock *slave);
// 获取同步类型
int get_master_sync_type(VideoState *is);
// 获取主时钟
double get_master_clock(VideoState *is);
// 检查外部时钟速度
void check_external_clock_speed(VideoState *is);

#ifdef __cplusplus
}
#endif
#endif //CAINCAMERA_CLOCK_H
