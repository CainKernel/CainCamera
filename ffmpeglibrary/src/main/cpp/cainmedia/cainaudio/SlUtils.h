//
// Created by cain on 2018/2/11.
//

#ifndef CAINCAMERA_SLUTILS_H
#define CAINCAMERA_SLUTILS_H
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

/**
 * 将SLresult转成字符串
 * @param result
 * @return
 */
static const char *resultToString(SLresult result) {
    const char* str = 0;

    switch (result) {
        case SL_RESULT_SUCCESS:
            str = "Success";
            break;

        case SL_RESULT_PRECONDITIONS_VIOLATED:
            str = "Preconditions violated";
            break;

        case SL_RESULT_PARAMETER_INVALID:
            str = "Parameter invalid";
            break;

        case SL_RESULT_MEMORY_FAILURE:
            str = "Memory failure";
            break;

        case SL_RESULT_RESOURCE_ERROR:
            str = "Resource error";
            break;

        case SL_RESULT_RESOURCE_LOST:
            str = "Resource lost";
            break;

        case SL_RESULT_IO_ERROR:
            str = "IO error";
            break;

        case SL_RESULT_BUFFER_INSUFFICIENT:
            str = "Buffer insufficient";
            break;

        case SL_RESULT_CONTENT_CORRUPTED:
            str = "Success";
            break;

        case SL_RESULT_CONTENT_UNSUPPORTED:
            str = "Content unsupported";
            break;

        case SL_RESULT_CONTENT_NOT_FOUND:
            str = "Content not found";
            break;

        case SL_RESULT_PERMISSION_DENIED:
            str = "Permission denied";
            break;

        case SL_RESULT_FEATURE_UNSUPPORTED:
            str = "Feature unsupported";
            break;

        case SL_RESULT_INTERNAL_ERROR:
            str = "Internal error";
            break;

        case SL_RESULT_UNKNOWN_ERROR:
            str = "Unknown error";
            break;

        case SL_RESULT_OPERATION_ABORTED:
            str = "Operation aborted";
            break;

        case SL_RESULT_CONTROL_LOST:
            str = "Control lost";
            break;

        default:
            str = "Unknown code";
    }

    return str;
}

/**
 * 根据channel获取对应的flag
 * @param channels
 * @return
 */
static int getChannelMask(int channels) {
    int channelMask = SL_SPEAKER_FRONT_CENTER;
    switch (channels) {
        case 1:
            channelMask = SL_SPEAKER_FRONT_CENTER;
            break;
        case 2:
            channelMask = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
            break;
    };
    return channelMask;
}

/**
 * 获取采样频率
 * @param sampleRate
 * @return
 */
static int getSLSampleRate(int sampleRate) {
    int samplesPerSec = SL_SAMPLINGRATE_44_1;
    switch (sampleRate) {
        case 8000:
            samplesPerSec = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            samplesPerSec = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            samplesPerSec = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            samplesPerSec = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            samplesPerSec = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            samplesPerSec = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            samplesPerSec = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            samplesPerSec = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            samplesPerSec = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            samplesPerSec = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            samplesPerSec = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            samplesPerSec = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            samplesPerSec = SL_SAMPLINGRATE_192;
            break;
        default:
            samplesPerSec = SL_SAMPLINGRATE_44_1;
    }
    return samplesPerSec;
}

/**
 * 获取出错信息
 * @param result
 * @return
 */
static const char *getSLErrorString(SLresult result) {
    switch (result) {
        case SL_RESULT_PRECONDITIONS_VIOLATED:
            return "Preconditions violated";
        case SL_RESULT_PARAMETER_INVALID:
            return "Invalid parameter";
        case SL_RESULT_MEMORY_FAILURE:
            return "Memory failure";
        case SL_RESULT_RESOURCE_ERROR:
            return "Resource error";
        case SL_RESULT_RESOURCE_LOST:
            return "Resource lost";
        case SL_RESULT_IO_ERROR:
            return "IO error";
        case SL_RESULT_BUFFER_INSUFFICIENT:
            return "Insufficient buffer";
        case SL_RESULT_CONTENT_CORRUPTED:
            return "Content corrupted";
        case SL_RESULT_CONTENT_UNSUPPORTED:
            return "Content unsupported";
        case SL_RESULT_CONTENT_NOT_FOUND:
            return "Content not found";
        case SL_RESULT_PERMISSION_DENIED:
            return "Permission denied";
        case SL_RESULT_FEATURE_UNSUPPORTED:
            return "Feature unsupported";
        case SL_RESULT_INTERNAL_ERROR:
            return "Internal error";
        case SL_RESULT_UNKNOWN_ERROR:
            return "Unknown error";
        case SL_RESULT_OPERATION_ABORTED:
            return "Operation aborted";
        case SL_RESULT_CONTROL_LOST:
            return "Control lost";
    }
    return "Unknown OpenSL error";
}

#endif //CAINCAMERA_SLUTILS_H
