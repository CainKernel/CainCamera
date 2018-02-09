//
// Created by Administrator on 2018/2/9.
//

#ifndef CAINCAMERA_CAINAUDIO_H
#define CAINCAMERA_CAINAUDIO_H

#include "CainEndian.h"
#include <cstdint>

// 音频格式
typedef uint16_t AudioFormat;

#define Cain_AUDIO_MASK_BITSIZE       (0xFF)
#define Cain_AUDIO_MASK_DATATYPE      (1<<8)
#define Cain_AUDIO_MASK_ENDIAN        (1<<12)
#define Cain_AUDIO_MASK_SIGNED        (1<<15)
#define Cain_AUDIO_BITSIZE(x)         (x & Cain_AUDIO_MASK_BITSIZE)
#define Cain_AUDIO_ISFLOAT(x)         (x & Cain_AUDIO_MASK_DATATYPE)
#define Cain_AUDIO_ISBIGENDIAN(x)     (x & Cain_AUDIO_MASK_ENDIAN)
#define Cain_AUDIO_ISSIGNED(x)        (x & Cain_AUDIO_MASK_SIGNED)
#define Cain_AUDIO_ISINT(x)           (!Cain_AUDIO_ISFLOAT(x))
#define Cain_AUDIO_ISLITTLEENDIAN(x)  (!Cain_AUDIO_ISBIGENDIAN(x))
#define Cain_AUDIO_ISUNSIGNED(x)      (!Cain_AUDIO_ISSIGNED(x))

#define AUDIO_INVALID   0x0000
#define AUDIO_U8        0x0008  /**< Unsigned 8-bit samples */
#define AUDIO_S8        0x8008  /**< Signed 8-bit samples */
#define AUDIO_U16LSB    0x0010  /**< Unsigned 16-bit samples */
#define AUDIO_S16LSB    0x8010  /**< Signed 16-bit samples */
#define AUDIO_U16MSB    0x1010  /**< As above, but big-endian byte order */
#define AUDIO_S16MSB    0x9010  /**< As above, but big-endian byte order */
#define AUDIO_U16       AUDIO_U16LSB
#define AUDIO_S16       AUDIO_S16LSB

#define AUDIO_S32LSB    0x8020  /**< 32-bit integer samples */
#define AUDIO_S32MSB    0x9020  /**< As above, but big-endian byte order */
#define AUDIO_S32       AUDIO_S32LSB

#define AUDIO_F32LSB    0x8120  /**< 32-bit floating point samples */
#define AUDIO_F32MSB    0x9120  /**< As above, but big-endian byte order */
#define AUDIO_F32       AUDIO_F32LSB

#if CAIN_BYTEORDER == CAIN_LIL_ENDIAN
#define AUDIO_U16SYS    AUDIO_U16LSB
#define AUDIO_S16SYS    AUDIO_S16LSB
#define AUDIO_S32SYS    AUDIO_S32LSB
#define AUDIO_F32SYS    AUDIO_F32LSB
#else
#define AUDIO_U16SYS    AUDIO_U16MSB
#define AUDIO_S16SYS    AUDIO_S16MSB
#define AUDIO_S32SYS    AUDIO_S32MSB
#define AUDIO_F32SYS    AUDIO_F32MSB
#endif

#define MIX_MAXVOLUME (128)

// 音频回调函数
typedef void (*CainAudioCallback) (void *userdata, uint8_t * stream, int len);

// 音频参数结构体
typedef struct CainAudioSpec
{
    int freq;                     /**< DSP frequency -- samples per second */
    AudioFormat format;           /**< Audio data format */
    uint8_t channels;             /**< Number of channels: 1 mono, 2 stereo */
    uint8_t silence;              /**< Audio buffer silence value (calculated) */
    uint16_t samples;             /**< Audio buffer size in samples (power of 2) */
    uint16_t padding;             /**< NOT USED. Necessary for some compile environments */
    uint32_t size;                /**< Audio buffer size in bytes (calculated) */
    CainAudioCallback callback;
    void *userdata;
} CainAudioSpec;

// 打开音频 TODO 包装起来
int Cain_OpenAudio(const CainAudioSpec *desired, CainAudioSpec *obtained);
// 停止音频播放
void Cain_PauseAudio(int pause_on);
// 刷出剩余音频
void Cain_FlushAudio();
// 关闭音频
void Cain_CloseAusio();

#endif //CAINCAMERA_CAINAUDIO_H
