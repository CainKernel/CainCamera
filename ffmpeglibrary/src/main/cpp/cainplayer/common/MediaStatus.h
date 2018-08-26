//
// Created by admin on 2018/4/29.
//

#ifndef CAINPLAYER_MEDIASTATUS_H
#define CAINPLAYER_MEDIASTATUS_H

#include <sys/types.h>

#define WORKER_THREAD 1

#define REGISTER_LOCK_MANAGER 2
#define OPEN_URL_FAILED 3
#define FIND_STREAMS_FAILED 4

class MediaStatus {
public:
    MediaStatus();

    virtual ~MediaStatus();

    bool isExit() const;

    void setExit(bool exit);

    bool isPause() const;

    void setPause(bool pause);

    bool isLoad() const;

    void setLoad(bool load);

    bool isSeek() const;

    void setSeek(bool seek);

    void setHardDecode(bool hardDecode);

    bool isHardDecode() const;

private:
    bool exit;      // 是否处于退出状态
    bool pause;     // 是否处于暂停状态
    bool load;      // 是否处于加载裸数据包状态
    bool seek;      // 是否处于定位状态
    bool hardDecode;// 是否处于硬解码状态
};


#endif //CAINPLAYER_MEDIASTATUS_H
