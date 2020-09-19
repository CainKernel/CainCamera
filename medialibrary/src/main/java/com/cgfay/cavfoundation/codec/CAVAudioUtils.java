package com.cgfay.cavfoundation.codec;

import android.media.MediaCodecInfo;

/**
 * 音频数据处理工具
 */
final class CAVAudioUtils {

    /**
     * 添加ADTS头
     *
     * @param packet
     * @param size
     */
    public static void addADTSToPacket(byte[] packet, int size, CAVAudioInfo param) {
        int profile = transProfileToADTS(param.getProfile()); // AAC LC
        int freqIdx = transSampleRateToADTS(param.getSampleRate()); // 44.1KHz
        int channelConfiguration = transChannelCountToADTS(param.getChannelCount()); // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (channelConfiguration >> 2));
        packet[3] = (byte) (((channelConfiguration & 3) << 6) + (size >> 11));
        packet[4] = (byte) ((size & 0x7FF) >> 3);
        packet[5] = (byte) (((size & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    /**
     * 将profile转换成ADTS
     * @param profile pofile类型
     * @return  ADTS的profile数值
     */
    private static int transProfileToADTS(int profile) {
        int result = 2;
        switch (profile) {
            case MediaCodecInfo.CodecProfileLevel.AACObjectMain: {
                result = 0;
                break;
            }
            case MediaCodecInfo.CodecProfileLevel.AACObjectLC: {
                result = 1;
                break;
            }
            case MediaCodecInfo.CodecProfileLevel.AACObjectSSR: {
                result = 2;
                break;
            }
        }
        return result;
    }

    /**
     * 将采样率转换为adts系数
     */
    private static int transSampleRateToADTS(int sampleRate) {
        int result = 4;
        switch (sampleRate) {
            case 96000: {
                result = 0;
                break;
            }
            case 88200: {
                result = 1;
                break;
            }
            case 64000: {
                result = 2;
                break;
            }
            case 48000: {
                result = 3;
                break;
            }
            case 44100: {
                result = 4;
                break;
            }
            case 32000: {
                result = 5;
                break;
            }
            case 24000: {
                result = 6;
                break;
            }
            case 22050: {
                result = 7;
                break;
            }
            case 16000: {
                result = 8;
                break;
            }
            case 12000: {
                result = 9;
                break;
            }
            case 11025: {
                result = 10;
                break;
            }
            case 8000: {
                result = 11;
                break;
            }
        }
        return result;
    }

    /**
     * 将声道数转换为adts系数
     */
    private static int transChannelCountToADTS(int channelCount) {
        int channel = 2;
        if (channelCount >= 0 && channelCount <= 7) {
            channel = channelCount;
        }
        return channel;
    }
}
