//
// Created by admin on 2018/4/29.
//


#include "MediaStatus.h"
#include <pthread.h>

MediaStatus::MediaStatus() {
    exit = false;
    pause = false;
    load = true;
    seek = false;
    hardDecode = false;
}

MediaStatus::~MediaStatus() {

}

bool MediaStatus::isExit() const {
    return exit;
}

void MediaStatus::setExit(bool exit) {
    this->exit = exit;
}

bool MediaStatus::isPause() const {
    return pause;
}

void MediaStatus::setPause(bool pause) {
    this->pause = pause;
}

bool MediaStatus::isLoad() const {
    return load;
}

void MediaStatus::setLoad(bool load) {
    this->load = load;
}

bool MediaStatus::isSeek() const {
    return seek;
}

void MediaStatus::setSeek(bool seek) {
    this->seek = seek;
}

bool MediaStatus::isHardDecode() const {
    return hardDecode;
}

void MediaStatus::setHardDecode(bool hardDecode) {
    this->hardDecode = hardDecode;
}
