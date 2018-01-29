//
// Created by Administrator on 2018/1/29.
//

#include "cainsdl_audio.h"


// 计算音频速度
void SDL_CalculateAudioSpec(SDL_AudioSpec * spec) {
    switch (spec->format) {
        case AUDIO_U8:
            spec->silence = 0x80;
            break;
        default:
            spec->silence = 0x00;
            break;
    }
    spec->size = (uint32_t) SDL_AUDIO_BITSIZE(spec->format) / 8;
    spec->size *= spec->channels;
    spec->size *= spec->samples;
}

// 混音
void SDL_MixAudio(uint8_t*       dst,
                  const uint8_t* src,
                  uint32_t       len,
                  int          volume) {

}