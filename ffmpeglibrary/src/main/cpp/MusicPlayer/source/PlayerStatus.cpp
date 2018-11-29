//
// Created by cain on 2018/11/25.
//

#include "PlayerStatus.h"

PlayerStatus::PlayerStatus() {

}

PlayerStatus::~PlayerStatus() {

}

bool PlayerStatus::isExit() const {
    return exit;
}

void PlayerStatus::setExit(bool exit) {
    PlayerStatus::exit = exit;
}

bool PlayerStatus::isSeek() const {
    return seek;
}

void PlayerStatus::setSeek(bool seek) {
    PlayerStatus::seek = seek;
}

bool PlayerStatus::isPlaying() const {
    return playing && !isExit();
}

void PlayerStatus::setPlaying(bool playing) {
    PlayerStatus::playing = playing;
}


