//
// Created by cain on 2018/1/20.
//

#include <malloc.h>
#include "RtmpPush.h"

extern "C" {
/**
 * 初始化视频编码器
 * @param url
 * @param width
 * @param height
 * @param bitrate
 * @return
 */
int RtmpPusher::initVideo(const char *url, int width, int height, int bitrate) {

    mediaSize = width * height;
    mediaWidth = width;
    mediaHeight = height;
    bitRate = bitrate;

    // 设置x264编码参数
    x264_param_t param;
    x264_param_default_preset(&param, "ultrafast", "zerolatency");

    param.i_csp = X264_CSP_I420;
    param.i_width = width;
    param.i_height = height;
    param.rc.i_bitrate = bitrate / 1000;
    // 码率控制，CQP(恒定质量)，CRF(恒定码率)，ABR(平均码率)
    param.rc.i_rc_method = X264_RC_ABR;
    // 设置了i_vbv_max_bitrate必须设置此参数，码率控制区大小,单位kbps
    param.rc.i_vbv_buffer_size = bitrate / 1000;
    // 瞬时最大码率
    param.rc.i_vbv_max_bitrate = bitrate / 1000 * 1.2;
    param.i_keyint_max = 25 * 2;
    param.i_fps_num = 25;
    param.i_fps_den = 1;
    param.i_threads = 1;
    param.i_timebase_den = param.i_fps_num;
    param.i_timebase_num = param.i_fps_den;
    // 该参数设置是让每个关键帧(I帧)都附带sps/pps。
    param.b_repeat_headers = 1;
    // 设置编码质量
    x264_param_apply_profile(&param, "baseline");

    videoEncoder = x264_encoder_open(&param);
    pic_in = (x264_picture_t *) malloc(sizeof(x264_picture_t));
    pic_out = (x264_picture_t *) malloc(sizeof(x264_picture_t));
    x264_picture_alloc(pic_in, X264_CSP_I420, width, height);
    x264_picture_init(pic_out);

    return initRtmp((char *) url);
}

/**
 * 初始化RTMP
 * @param url
 * @return
 */
int RtmpPusher::initRtmp(char *url) {
    // 1、初始化RTMP
    rtmpPusher = RTMP_Alloc();
    RTMP_Init(rtmpPusher);
    rtmpPusher->Link.timeout = 5;
    rtmpPusher->Link.flashVer = RTMP_DefaultFlashVer;

    // 2、设置url地址
    if (!RTMP_SetupURL(rtmpPusher, (char *) url)) {
        RTMP_Close(rtmpPusher);
        RTMP_Free(rtmpPusher);
        ALOGI("RTMP_SetupURL fail");
        return -1;
    }
    RTMP_EnableWrite(rtmpPusher);
    // 3、建立连接
    if (!RTMP_Connect(rtmpPusher, NULL)) {
        RTMP_Close(rtmpPusher);
        RTMP_Free(rtmpPusher);
        ALOGI("RTMP_Connect fail");
        return -1;
    }
    // 4、连接流
    if (!RTMP_ConnectStream(rtmpPusher, 0)) {
        RTMP_Close(rtmpPusher);
        RTMP_Free(rtmpPusher);
        ALOGI("RTMP_ConnectStream fail");
        return -1;
    }

    // 创建队列
    create_queue();
    startTime = (long) RTMP_GetTime();

    pushing = 1;
    mutex = PTHREAD_MUTEX_INITIALIZER;
    cond = PTHREAD_COND_INITIALIZER;
    pthread_create(&publisher_tid, NULL, RtmpPusher::rtmpPushThread, this);
    return 0;
}

/**
 * 初始化音频编码器
 * @param sampleRate
 * @param channel
 * @return
 */
int RtmpPusher::initAudio(int sampleRate, int channel) {
    if (audioEncoder) {
        return 0;
    }
    // 初始化AAC编码器
    audioEncoder = faacEncOpen((unsigned long) sampleRate, (unsigned int) channel,
                               &inputSamples, &maxOutputBytes);
    if (!audioEncoder) {
        ALOGI("faacEncOpen failed!");
        return -1;
    }

    // 设置AAC编码器参数
    faacEncConfigurationPtr config = faacEncGetCurrentConfiguration(audioEncoder);
    config->mpegVersion = MPEG4;
    config->allowMidside = 1;
    config->aacObjectType = LOW;
    // 是否包含ADTS头
    config->outputFormat = 0;
    // 时域噪音控制,大概就是消爆音
    config->useTns = 1;
    config->useLfe = 0;
    config->inputFormat = FAAC_INPUT_16BIT;
    config->quantqual = 100;
    config->bandWidth = 0;
    config->shortctl = SHORTCTL_NORMAL;
    if (!faacEncSetConfiguration(audioEncoder,config)) {
        ALOGI("faacEncSetConfiguration failed!");
        return -1;
    }

    return initAudioHeader();
}

/**
 * 初始化音频头部信息
 * @return
 */
int RtmpPusher::initAudioHeader() {

    // 获取解码器信息
    unsigned char* data;
    unsigned long len;
    faacEncGetDecoderSpecificInfo(audioEncoder, &data, &len);
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    if (!packet) {
        return -1;
    }
    RTMPPacket_Reset(packet);
    if (!RTMPPacket_Alloc(packet,len + 2)) {
        RTMPPacket_Free(packet);
        return -1;
    }

    char *body = packet->m_body;
    body[0] = (char) 0xaf;
    body[1] = 0x00;
    memcpy(&body[2], data, len);

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = len + 2;
    packet->m_nChannel = 0x04;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nTimeStamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;

    rtmpPacketPush(packet);
    return 0;
}
/**
 * 音频推流
 * @param data
 */
void RtmpPusher::aacEncode(char *data) {
    // 1、判断是否已经连接
    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("aacEncode RTMP_IsConnected ? false");
        return;
    }
    if (!pushing || requestStop) {
        return;
    }
    // 2、编码
    unsigned char* buffer = (unsigned char *) malloc(maxOutputBytes * sizeof(unsigned char*));
    int length = faacEncEncode(audioEncoder, (int32_t *) data, inputSamples,
                                 buffer, maxOutputBytes);
    if (length <= 0) {
        if(buffer){
            free(buffer);
        }
        return;
    }

    // 3、创建RTMPPacket
    int body_size = length + 2;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    if(!packet){
        return;
    }
    RTMPPacket_Reset(packet);
    if(!RTMPPacket_Alloc(packet, (uint32_t) body_size)){
        RTMPPacket_Free(packet);
        return;
    }
    // 设置RTMP参数
    char *body = packet->m_body;
    body[0] = (char) 0xaf;
    body[1] = 0x01;
    memcpy(&body[2], buffer, (size_t) length);
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = (uint32_t) body_size;
    packet->m_nChannel = 0x04;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;

    // 添加入队
    rtmpPacketPush(packet);
}

/**
 * 后置摄像头推流
 * @param data
 */
void RtmpPusher::avcEncodeWithBackCamera(char *data) {
    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("avcEncode RTMP_IsConnected ? false");
        return;
    }
    if (!pushing || requestStop) {
        return;
    }

    int width = mediaHeight;
    int height = mediaWidth;
    int uv_height = height >> 1;
    char *yuv = new char[mediaSize * 3/2];
    int k = 0;
    //y
    for (int i = 0; i < width; i++) {
        for (int j = height - 1; j >= 0; j--) {
            yuv[k++] = data[width * j + i];
        }
    }
    //u v
    for (int j = 0; j < width; j += 2) {
        for (int i = uv_height - 1; i >= 0; i--) {
            yuv[k++] = data[mediaSize + width * i + j];
            yuv[k++] = data[mediaSize + width * i + j + 1];
        }
    }

    avcEncode(yuv, 1);
}

/**
 * 前置摄像头推流
 * @param data
 */
void RtmpPusher::avcEncodeWithFrontCamera(char *data) {
    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("avcEncode RTMP_IsConnected ? false");
        return;
    }
    if (!pushing || requestStop) {
        return;
    }
    int width = mediaHeight;
    int height = mediaWidth;
    int uv_height = height >> 1;
    char *yuv = new char[mediaSize*3/2];
    int k = 0;

    //y
    for (int i = 0; i < width; i++) {
        int n_pos = width - 1 - i;
        for (int j = 0; j < height; j++) {
            yuv[k++] = data[n_pos];
            n_pos+=width;
        }
    }
    //u v
    for (int i = 0; i < width; i += 2) {
        int nPos = mediaSize + width - 1;
        for (int j = 0; j < uv_height; j++) {
            yuv[k] = data[nPos - i - 1];
            yuv[k + 1] = data[nPos - i];
            k += 2;
            nPos += width;
        }
    }

    avcEncode(yuv, 1);
}
/**
 * 手机横屏推流
 * @param data
 */
void RtmpPusher::avcEncodeLandscape(char *data) {
    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("avcEncode RTMP_IsConnected ? false");
        return;
    }
    if (!pushing || requestStop) {
        return;
    }
    avcEncode(data, 0);
}

/**
 * 推流视频
 * @param data
 * @param clear
 */
void RtmpPusher::avcEncode(char *data, int clear) {
    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("avcEncode RTMP_IsConnected ? false");
        return;
    }
    if (!pushing || requestStop) {
        return;
    }
    // 将nv21转成I420
    memcpy(pic_in->img.plane[0], data, (size_t) mediaSize);
    char* u = (char *) pic_in->img.plane[1];
    char* v = (char *) pic_in->img.plane[2];
    for (int j = 0; j < mediaSize / 4; j++) {
        *(u + j) = *(data + mediaSize + j * 2 + 1);
        *(v + j) = *(data + mediaSize + j * 2);
    }
    // 编码
    int num = -1;
    x264_nal_t *nal = NULL;
    if (x264_encoder_encode(videoEncoder, &nal, &num, pic_in, pic_out) < 0) {
        ALOGI("x264_encoder_encode failed!");
        return;
    }
    // 设置帧参数
    pic_in->i_pts++;
    int sps_len = 0, pps_len = 0;
    char *sps = NULL;
    char *pps = NULL;
    for (int i = 0; i < num; i++) {
        if (nal[i].i_type == NAL_SPS) { // SPS帧
            sps_len = nal[i].i_payload - 4;
            sps = (char *) malloc((size_t) (sps_len + 1));
            memcpy(sps, nal[i].p_payload + 4, (size_t) sps_len);
        } else if (nal[i].i_type == NAL_PPS) {  // PPS帧
            pps_len = nal[i].i_payload - 4;
            pps = (char *) malloc((size_t) (pps_len + 1));
            memcpy(pps, nal[i].p_payload + 4, (size_t) pps_len);
            writeSpsPpsFrame(pps, sps, pps_len, sps_len);
            free(sps);
            free(pps);
        } else {    // 关键帧 或 普通帧
            pushVideoFrame((char *) nal[i].p_payload, nal[i].i_payload);
        }
    }

    // 清除数据
    if (clear) {
        free(data);
    }

}

/**
 * 写入sps 和 pps头部信息
 * @param pps
 * @param sps
 * @param pps_len
 * @param sps_len
 */
void RtmpPusher::writeSpsPpsFrame(char *pps, char *sps, int pps_len, int sps_len) {
    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("writeSpsPpsFrame RTMP_IsConnected ? false");
        return;
    }
    if (!pushing || requestStop) {
        return;
    }
    int body_size = 13 + sps_len + 3 + pps_len;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    if (!packet) {
        return;
    }
    RTMPPacket_Reset(packet);
    if (!RTMPPacket_Alloc(packet, (uint32_t) body_size)) {
        RTMPPacket_Free(packet);
        return;
    }
    char *body = packet->m_body;
    int k = 0;
    body[k++] = 0x17;
    body[k++] = 0x00;
    body[k++] = 0x00;
    body[k++] = 0x00;
    body[k++] = 0x00;

    body[k++] = 0x01;
    body[k++] = sps[1];
    body[k++] = sps[2];
    body[k++] = sps[3];
    body[k++] = (char) 0xff;

    // sps信息
    body[k++] = (char) 0xe1;
    body[k++] = (char) ((sps_len >> 8) & 0xff);
    body[k++] = (char) (sps_len & 0xff);
    memcpy(&body[k], sps, (size_t) sps_len);
    k += sps_len;

    //pps
    body[k++] = 0x01;
    body[k++] = (char) ((pps_len >> 8) & 0xff);
    body[k++] = (char) (pps_len & 0xff);
    memcpy(&body[k], pps, (size_t) pps_len);
    k += pps_len;

    // 设置参数
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = (uint32_t) body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;

    //添加到队列中
    rtmpPacketPush(packet);
}

/**
 * 添加视频帧
 * @param buf
 * @param len
 */
void RtmpPusher::pushVideoFrame(char *buf, int len) {

    if (!rtmpPusher || !RTMP_IsConnected(rtmpPusher)) {
        ALOGI("pushVideoFrame RTMP_IsConnected ? false");
        return;
    }

    if (!pushing || requestStop) {
        return;
    }
    //sps 与 pps 的帧界定符都是 00 00 00 01，而普通帧可能是 00 00 00 01 也有可能 00 00 01
    /*去掉帧界定符*/
    if (buf[2] == 0x00) {   // 00 00 00 01
        buf += 4;
        len -= 4;
    } else if (buf[2] == 0x01) { // 00 00 01
        buf += 3;
        len -= 3;
    }
    int body_size = len + 9;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    if (!packet) {
        return;
    }
    RTMPPacket_Reset(packet);
    if (!RTMPPacket_Alloc(packet, (uint32_t) body_size)) {
        return;
    }
    char *body = packet->m_body;
    int k = 0;
    int type = buf[0] & 0x1f;
    if (type == NAL_SLICE_IDR) {
        body[k++] = 0x17;
    } else {
        body[k++] = 0x27;
    }
    body[k++] = 0x01;
    body[k++] = 0x00;
    body[k++] = 0x00;
    body[k++] = 0x00;

    body[k++] = (char) ((len >> 24) & 0xff);
    body[k++] = (char) ((len >> 16) & 0xff);
    body[k++] = (char) ((len >> 8) & 0xff);
    body[k++] = (char) (len & 0xff);

    memcpy(&body[k++], buf, (size_t) len);
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = (uint32_t) body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;

    //添加到队列
    rtmpPacketPush(packet);
}

/**
 * 添加RTMPPacket数据包到队列
 * @param packet
 */
void RtmpPusher::rtmpPacketPush(RTMPPacket *packet) {
    pthread_mutex_lock(&mutex);
    queue_put_tail(packet);
    pthread_cond_signal(&cond);
    pthread_mutex_unlock(&mutex);
}

/**
 * 推流线程
 * @param args
 * @return
 */
void *RtmpPusher::rtmpPushThread(void *args) {
    RtmpPusher *pusher = (RtmpPusher *) args;

    while (pusher->isPushing()) {
        pthread_mutex_lock(&pusher->mutex);
        pthread_cond_wait(&pusher->cond, &pusher->mutex);

        // 请求停止，则立马跳出循环
        if (pusher->isStop()) {
            break;
        }

        // 如果不处于pushing状态，则继续等待
        if (!pusher->isPushing()) {
            continue;
        }

        // 获得一个RTMP包
        RTMPPacket *packet = (RTMPPacket *) queue_get_head();
        if (packet) {
            queue_remove();
        }
        pthread_mutex_unlock(&pusher->mutex);
        // 发送RTMP包，推流操作
        if (packet) {
            if (RTMP_SendPacket(pusher->rtmpPusher, packet, TRUE)) {
                ALOGI("RTMP_SendPacket success!");
            } else {
                ALOGI("RTMP_SendPacket failed!");
            }
            RTMPPacket_Free(packet);
        }
        // 丢包操作
        if (queue_size() > 50) {
            for (int i = 0; i < 25; i++) {
                queue_remove();
            }
        }
    }
    // 如果请求停止操作，则释放RTMP等资源，以防内存泄漏
    if (pusher->requestStop) {
        RtmpPusher::nativeStop(pusher);
    }
    delete pusher;
    ALOGI("RTMP_SendPacket stop!");
    return NULL;
}

/**
 * 停止推流
 */
void RtmpPusher::stop() {
    pushing = 0;
    requestStop = 1;
}

/**
 * 释放资源
 */
void RtmpPusher::nativeStop(RtmpPusher *pusher) {
    destroy_queue();
    if (pusher->getAudioEncoder()) {
        faacEncClose(pusher->getAudioEncoder());
        pusher->setAudioEncoder(NULL);
    }
    if (pusher->getPictureIn()) {
        free(pusher->getPictureIn());
        pusher->setPictureIn(NULL);
    }

    if (pusher->getPictureOut()) {
        free(pusher->getPictureOut());
        pusher->setPictureOut(NULL);
    }

    if (pusher->getVideoEncoder()) {
        x264_encoder_close(pusher->getVideoEncoder());
        pusher->setVideoEncoder(NULL);
    }

    if (pusher->getRtmpPusher() && RTMP_IsConnected(pusher->getRtmpPusher())) {
        RTMP_Close(pusher->getRtmpPusher());
        RTMP_Free(pusher->getRtmpPusher());
        pusher->setRtmpPusher(NULL);
    }
}

/**
 * 获取视频编码器
 * @return
 */
x264_t* RtmpPusher::getVideoEncoder() {
    return videoEncoder;
}

/**
 * 获取输入的picture
 * @return
 */
x264_picture_t* RtmpPusher::getPictureIn() {
    return pic_in;
}

/**
 * 获取输出的picture
 * @return
 */
x264_picture_t* RtmpPusher::getPictureOut() {
    return pic_out;
}

/**
 * 获取推流器
 * @return
 */
RTMP* RtmpPusher::getRtmpPusher() {
    return rtmpPusher;
}

/**
 * 获取音频编码器
 * @return
 */
faacEncHandle RtmpPusher::getAudioEncoder() {
    return audioEncoder;
}

/**
 * 设置视频编码器
 * @param encoder
 */
void RtmpPusher::setVideoEncoder(x264_t *encoder) {
    videoEncoder = encoder;
}

/**
 * 设置输入的picture
 * @param picture
 */
void RtmpPusher::setPictureIn(x264_picture_t *picture) {
    pic_in = picture;
}

/**
 * 设置输出的picture
 * @param picture
 */
void RtmpPusher::setPictureOut(x264_picture_t *picture) {
    pic_out = picture;
}

/**
 * 设置RTMP对象
 * @param pusher
 */
void RtmpPusher::setRtmpPusher(RTMP *pusher) {
    rtmpPusher = pusher;
}

/**
 * 设置音频编码器
 * @param encoder
 */
void RtmpPusher::setAudioEncoder(faacEncHandle encoder) {
    audioEncoder = encoder;
}

/**
 * 是否处于停止状态
 * @return
 */
int RtmpPusher::isStop() {
    return requestStop;
}

/**
 * 是否处于推流状态
 * @return
 */
int RtmpPusher::isPushing() {
    return pushing;
}

}