//
// Created by cain on 2018/11/25.
//

#ifndef CAINCAMERA_PLAYERSTATUS_H
#define CAINCAMERA_PLAYERSTATUS_H


class PlayerStatus {
public:
    PlayerStatus();

    virtual ~PlayerStatus();

    bool isExit() const;

    void setExit(bool exit);

    bool isSeek() const;

    void setSeek(bool seek);

    bool isPlaying() const;

    void setPlaying(bool playing);

private:
    bool exit = false;
    bool seek = false;
    bool playing = false;
};


#endif //CAINCAMERA_PLAYERSTATUS_H
