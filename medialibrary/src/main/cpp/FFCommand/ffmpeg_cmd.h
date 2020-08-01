//
// Created by CainHuang on 2020/7/12.
//

#ifndef CAINCAMERA_FFMPEG_CMD_H
#define CAINCAMERA_FFMPEG_CMD_H

#ifdef __cplusplus
extern "C" {
#endif

// ffmpeg命令执行进度回调
void ffmpeg_executor_progress(int progress);

#ifdef __cplusplus
}
#endif
#endif //CAINCAMERA_FFMPEG_CMD_H
