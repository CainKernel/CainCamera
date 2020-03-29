//
// Created by CainHuang on 2020-03-21.
//

#ifndef ONPLAYLISTENER_H
#define ONPLAYLISTENER_H

/**
 * 播放监听器
 */
class OnPlayListener {
public:
    virtual ~OnPlayListener() = default;

    virtual void notify(int msg, int arg1, int arg2, void *obj) = 0;
};

#endif //ONPLAYLISTENER_H
