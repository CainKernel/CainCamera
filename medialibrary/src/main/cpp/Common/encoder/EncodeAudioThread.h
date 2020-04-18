//
// Created by CainHuang on 2020-04-04.
//

#ifndef ENCODEAUDIOTHREAD_H
#define ENCODEAUDIOTHREAD_H

#include <Thread.h>

/**
 * 音频编码线程
 */
class EncodeAudioThread : Runnable {
public:
    EncodeAudioThread();

    virtual ~EncodeAudioThread();

    void start();

    void stop();

};


#endif //ENCODEAUDIOTHREAD_H
