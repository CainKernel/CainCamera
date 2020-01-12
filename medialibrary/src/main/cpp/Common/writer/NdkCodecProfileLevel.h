//
// Created by CainHuang on 2020-01-02.
//

#ifndef MEDIACODECPROFILELEVEL_H
#define MEDIACODECPROFILELEVEL_H

#if defined(__ANDROID__)

// from OMX_VIDEO_AVCPROFILETYPE
#define AVCProfileBaseline 0x01
#define AVCProfileMain     0x02
#define AVCProfileExtended 0x04
#define AVCProfileHigh     0x08
#define AVCProfileHigh10   0x10
#define AVCProfileHigh422  0x20
#define AVCProfileHigh444  0x40

// from OMX_VIDEO_AVCLEVELTYPE
#define AVCLevel1       0x01
#define AVCLevel1b      0x02
#define AVCLevel11      0x04
#define AVCLevel12      0x08
#define AVCLevel13      0x10
#define AVCLevel2       0x20
#define AVCLevel21      0x40
#define AVCLevel22      0x80
#define AVCLevel3       0x100
#define AVCLevel31      0x200
#define AVCLevel32      0x400
#define AVCLevel4       0x800
#define AVCLevel41      0x1000
#define AVCLevel42      0x2000
#define AVCLevel5       0x4000
#define AVCLevel51      0x8000
#define AVCLevel52      0x10000

// from OMX_VIDEO_H263PROFILETYPE
#define H263ProfileBaseline             0x01
#define H263ProfileH320Coding           0x02
#define H263ProfileBackwardCompatible   0x04
#define H263ProfileISWV2                0x08
#define H263ProfileISWV3                0x10
#define H263ProfileHighCompression      0x20
#define H263ProfileInternet             0x40
#define H263ProfileInterlace            0x80
#define H263ProfileHighLatency          0x100

// from OMX_VIDEO_H263LEVELTYPE
#define H263Level10      0x01
#define H263Level20      0x02
#define H263Level30      0x04
#define H263Level40      0x08
#define H263Level45      0x10
#define H263Level50      0x20
#define H263Level60      0x40
#define H263Level70      0x80

// from OMX_VIDEO_MPEG4PROFILETYPE
#define MPEG4ProfileSimple              0x01
#define MPEG4ProfileSimpleScalable      0x02
#define MPEG4ProfileCore                0x04
#define MPEG4ProfileMain                0x08
#define MPEG4ProfileNbit                0x10
#define MPEG4ProfileScalableTexture     0x20
#define MPEG4ProfileSimpleFace          0x40
#define MPEG4ProfileSimpleFBA           0x80
#define MPEG4ProfileBasicAnimated       0x100
#define MPEG4ProfileHybrid              0x200
#define MPEG4ProfileAdvancedRealTime    0x400
#define MPEG4ProfileCoreScalable        0x800
#define MPEG4ProfileAdvancedCoding      0x1000
#define MPEG4ProfileAdvancedCore        0x2000
#define MPEG4ProfileAdvancedScalable    0x4000
#define MPEG4ProfileAdvancedSimple      0x8000

// from OMX_VIDEO_MPEG4LEVELTYPE
#define MPEG4Level0      0x01
#define MPEG4Level0b     0x02
#define MPEG4Level1      0x04
#define MPEG4Level2      0x08
#define MPEG4Level3      0x10
#define MPEG4Level4      0x20
#define MPEG4Level4a     0x40
#define MPEG4Level5      0x80

// from OMX_AUDIO_AACPROFILETYPE
#define AACObjectMain       1
#define AACObjectLC         2
#define AACObjectSSR        3
#define AACObjectLTP        4
#define AACObjectHE         5
#define AACObjectScalable   6
#define AACObjectERLC       17
#define AACObjectLD         23
#define AACObjectHE_PS      29
#define AACObjectELD        39

// from OMX_VIDEO_VP8LEVELTYPE
#define VP8Level_Version0 0x01
#define VP8Level_Version1 0x02
#define VP8Level_Version2 0x04
#define VP8Level_Version3 0x08

// from OMX_VIDEO_VP8PROFILETYPE
#define VP8ProfileMain 0x01

// from OMX_VIDEO_HEVCPROFILETYPE
#define HEVCProfileMain   0x01
#define HEVCProfileMain10 0x02

// from OMX_VIDEO_HEVCLEVELTYPE
#define HEVCMainTierLevel1  0x1
#define HEVCHighTierLevel1  0x2
#define HEVCMainTierLevel2  0x4
#define HEVCHighTierLevel2  0x8
#define HEVCMainTierLevel21 0x10
#define HEVCHighTierLevel21 0x20
#define HEVCMainTierLevel3  0x40
#define HEVCHighTierLevel3  0x80
#define HEVCMainTierLevel31 0x100
#define HEVCHighTierLevel31 0x200
#define HEVCMainTierLevel4  0x400
#define HEVCHighTierLevel4  0x800
#define HEVCMainTierLevel41 0x1000
#define HEVCHighTierLevel41 0x2000
#define HEVCMainTierLevel5  0x4000
#define HEVCHighTierLevel5  0x8000
#define HEVCMainTierLevel51 0x10000
#define HEVCHighTierLevel51 0x20000
#define HEVCMainTierLevel52 0x40000
#define HEVCHighTierLevel52 0x80000
#define HEVCMainTierLevel6  0x100000
#define HEVCHighTierLevel6  0x200000
#define HEVCMainTierLevel61 0x400000
#define HEVCHighTierLevel61 0x800000
#define HEVCMainTierLevel62 0x1000000
#define HEVCHighTierLevel62 0x2000000

#ifndef COLOR_FormatYUV420Planar
#define COLOR_FormatYUV420Planar 19
#endif

#ifndef COLOR_FormatYUV420SemiPlanar
#define COLOR_FormatYUV420SemiPlanar 21
#endif

#ifndef COLOR_FormatSurface
#define COLOR_FormatSurface 0x7F000789
#endif

/** MediaCodec key frame flag*/
#ifndef BUFFER_FLAG_KEY_FRAME
#define BUFFER_FLAG_KEY_FRAME 1
#endif

/** Constant quality mode */
#define BITRATE_MODE_CQ 0
/** Variable bitrate mode */
#define BITRATE_MODE_VBR 1
/** Constant bitrate mode */
#define BITRATE_MODE_CBR 2

#ifndef VIDEO_MIME_AVC
#define VIDEO_MIME_AVC "video/avc"
#endif

#ifndef VIDEO_MIME_HEVC
#define VIDEO_MIME_HEVC "video/hevc"
#endif

#ifndef AUDIO_MIME_TYPE
#define AUDIO_MIME_TYPE "audio/mp4a-latm"
#endif

#ifndef VIDEO_ENCODE_TIMEOUT
#define VIDEO_ENCODE_TIMEOUT 10000
#endif

#ifndef AUDIO_BUFFER_SIZE
#define AUDIO_BUFFER_SIZE 8192
#endif

#ifndef AUDIO_BIT_RATE
#define AUDIO_BIT_RATE 128000
#endif

#endif /*defined(__ANDROID__)*/

#endif //MEDIACODECPROFILELEVEL_H
