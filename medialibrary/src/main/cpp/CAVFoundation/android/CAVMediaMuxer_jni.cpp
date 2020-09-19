//
// Created by CainHuang on 2020/9/2.
//

#if defined(__ANDROID__)

#include "CAVMediaMuxer_jni.h"
#include <Mutex.h>
#include <codec/CAVMediaMuxer.h>
#include <JNIHelp.h>

struct muxer_field_t {
    jfieldID context;
    jmethodID setVideoTrack;
    jmethodID setAudioTrack;
};
static muxer_field_t field;
static Mutex sLock;

static const char * const MUXER_CLASS_NAME = "com/cgfay/cavfoundation/codec/CAVMediaMuxer";

static CAVMediaMuxer *getMediaMuxer(JNIEnv *env, jobject thiz) {
    return (CAVMediaMuxer *)env->GetLongField(thiz, field.context);
}

static void
setMediaMuxer(JNIEnv *env, jobject thiz, long muxer) {
    env->SetLongField(thiz, field.context, muxer);
}

static void
setAudioTrack(JNIEnv *env, jobject thiz, int track) {
    env->CallVoidMethod(thiz, field.setAudioTrack, track);
}

static void
setVideoTrack(JNIEnv *env, jobject thiz, int track) {
    env->CallVoidMethod(thiz, field.setVideoTrack, track);
}

static void
CAVMediaMuxer_setOutputPath(JNIEnv *env, jobject thiz, jstring path_) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env,  "java/io/IOException", "No MediaMuxer available");
        return;
    }

    if (!path_) {
        jniThrowException(env,  "java/io/IOException", "null path");
        return;
    }

    const char *path = env->GetStringUTFChars(path_, nullptr);
    muxer->setOutputPath(path);
    env->ReleaseStringUTFChars(path_, path);
}

static void
CAVMediaMuxer_prepare(JNIEnv *env, jobject thiz) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return;
    }
    int result = muxer->prepare();
    if (result < 0) {
        jniThrowException(env, "java/io/IOException", "Failed to prepare MediaMuxer");
        return;
    }
    setVideoTrack(env, thiz, muxer->getVideoTrack());
    setAudioTrack(env, thiz, muxer->getAudioTrack());
}

static jboolean
CAVMediaMuxer_start(JNIEnv *env, jobject thiz) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return JNI_FALSE;
    }
    muxer->start();
    return static_cast<jboolean>(muxer->isStarted() ? JNI_TRUE : JNI_FALSE);
}

static void
CAVMediaMuxer_stop(JNIEnv *env, jobject thiz) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return;
    }
    muxer->stop();
}

static void
CAVMediaMuxer_release(JNIEnv *env, jobject thiz) {
    Mutex::Autolock lock(sLock);
    auto muxer = getMediaMuxer(env, thiz);
    delete muxer;
    setMediaMuxer(env, thiz, 0);
}

static void
CAVMediaMuxer_setVideoParam(JNIEnv *env, jobject thiz, jint width, jint height, jstring mime_,
                            jint frame_rate, jint bit_rate, jint profile, jint level) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return;
    }
    if (!mime_) {
        jniThrowException(env, "java/io/IOException", "null mime");
        return;
    }
    const char *mime = env->GetStringUTFChars(mime_, nullptr);

    CAVVideoInfo videoInfo;
    videoInfo.width = width;
    videoInfo.height = height;
    videoInfo.rotate = 0;
    videoInfo.frame_rate = frame_rate;
    videoInfo.bit_rate = bit_rate;
    videoInfo.profile = profile;
    videoInfo.level = level;
    AVCodecID codecId = AV_CODEC_ID_NONE;
    if (!strcmp("video/avc", mime)) {
        codecId = AV_CODEC_ID_H264;
    } else if (!strcmp("video/hevc", mime)) {
        codecId = AV_CODEC_ID_HEVC;
    }
    if (codecId == AV_CODEC_ID_NONE) {
        return;
    }
    videoInfo.codec_id = codecId;
    muxer->setVideoInfo(videoInfo);
    env->ReleaseStringUTFChars(mime_, mime);
}

static void
CAVMediaMuxer_setAudioParam(JNIEnv *env, jobject thiz, jint sample_rate, jint channel_count,
                            jint bit_rate, jint audio_format) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return;
    }
    CAVAudioInfo audioInfo;
    audioInfo.sample_rate = sample_rate;
    audioInfo.channels = channel_count;
    audioInfo.bit_rate = bit_rate;
    // only support ENCODING_PCM_16BIT
    audioInfo.audio_format = AV_SAMPLE_FMT_S16;
    // only support aac
    audioInfo.codec_id = AV_CODEC_ID_AAC;
    muxer->setAudioInfo(audioInfo);
}

static int
CAVMediaMuxer_writeExtraData(JNIEnv *env, jobject thiz, jint track_index,
                             jbyteArray extra_data_, jint size) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return -1;
    }
    uint8_t *extra_data = (uint8_t *) malloc((size_t) size);
    jbyte *data = env->GetByteArrayElements(extra_data_, nullptr);
    memcpy(extra_data, data, (size_t)size);
    int ret = muxer->writeExtraData(track_index, extra_data, (size_t)size);
    av_freep(&extra_data);
    env->ReleaseByteArrayElements(extra_data_, data, 0);
    return ret;
}

int64_t lastPts;

static int
CAVMediaMuxer_writeFrame(JNIEnv *env, jobject thiz, jint track_index, jbyteArray encode_data_,
                         jint size, jlong pts, jboolean key_frame) {
    auto muxer = getMediaMuxer(env, thiz);
    if (!muxer) {
        jniThrowException(env, "java/io/IOException", "No MediaMuxer available");
        return -1;
    }

    // 将编码后的AAC/H264数据复制到AVPacket中，然后写入Muxer中
    uint8_t *encode_data = (uint8_t *) malloc((size_t)size);
    memset(encode_data, 0, (size_t)size);
    jbyte *data = env->GetByteArrayElements(encode_data_, nullptr);
    memcpy(encode_data, data, (size_t)size);
    env->ReleaseByteArrayElements(encode_data_, data, 0);

    // copy encode data to avpacket
    AVPacket pkt;
    av_init_packet(&pkt);
    AVPacket *packet = &pkt;
    packet->stream_index = track_index;
    packet->data = encode_data;
    packet->size = size;
    packet->duration = 0;
    packet->pos = -1;
    // if encode data is video key frame, need to set packet flag.
    // key frame flag for audio encode data is always false.
    if (key_frame) {
        packet->flags |= AV_PKT_FLAG_KEY;
    }
    // rescale packet ts, pts is from BufferInfo.presentationTimeUs, it's AVRational is {1, 1000000}
    AVRational rational = (AVRational){1, 1000000};
    muxer->rescalePacketTs(packet, track_index, pts, rational);
    packet->dts = packet->pts;

    // 相同pts
    if (lastPts != 0 && packet->pts == lastPts) {

    }

    LOGD("packet pts: %ld", packet->pts);
    int ret = muxer->writeFrame(packet);
    av_packet_unref(&pkt);
    return ret;
}

static void
CAVMediaMuxer_native_finalize(JNIEnv *env, jobject thiz) {
    CAVMediaMuxer_release(env, thiz);
}

static void
CAVMediaMuxer_native_setup(JNIEnv *env, jobject thiz) {
    auto muxer = new CAVMediaMuxer();
    if (!muxer) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }
    setMediaMuxer(env, thiz, (long)muxer);
}

static void
CAVMediaMuxer_native_init(JNIEnv *env, jclass clazz) {
    if (clazz == nullptr) {
        return;
    }
    field.context = env->GetFieldID(clazz, "mNativeContext", "J");
    field.setAudioTrack = env->GetMethodID(clazz, "setAudioTrack", "(I)V");
    field.setVideoTrack = env->GetMethodID(clazz, "setVideoTrack", "(I)V");
}

static JNINativeMethod nativeMethods[] = {
        {"native_init", "()V", (void *)CAVMediaMuxer_native_init},
        {"native_setup", "()V", (void *)CAVMediaMuxer_native_setup},
        {"native_finalize", "()V", (void *)CAVMediaMuxer_native_finalize},
        {"_setOutputPath", "(Ljava/lang/String;)V", (void *) CAVMediaMuxer_setOutputPath},
        {"_prepareMuxer", "()V", (void *)CAVMediaMuxer_prepare},
        {"_start", "()Z", (void *) CAVMediaMuxer_start},
        {"_stop", "()V", (void *) CAVMediaMuxer_stop},
        {"_release", "()V", (void *) CAVMediaMuxer_release},
        {"_setVideoParam", "(IILjava/lang/String;IIII)V", (void *)CAVMediaMuxer_setVideoParam},
        {"_setAudioParam", "(IIII)V", (void *)CAVMediaMuxer_setAudioParam},
        {"_writeExtraData", "(I[BI)I", (void *)CAVMediaMuxer_writeExtraData},
        {"_writeFrame", "(I[BIJZ)I", (void *)CAVMediaMuxer_writeFrame}
};

/**
 * 注册Native方法
 */
int register_CAVMediaMuxer(JNIEnv *env) {
    int numMethods = sizeof(nativeMethods) / sizeof(nativeMethods[0]);
    jclass clazz = env->FindClass(MUXER_CLASS_NAME);
    if (clazz == nullptr) {
        LOGE("Native registration unable to find class '%s'", MUXER_CLASS_NAME);
        return JNI_ERR;
    }
    if (env->RegisterNatives(clazz, nativeMethods, numMethods) < 0) {
        LOGE("Native registration unable to register class '%s'", MUXER_CLASS_NAME);
        env->DeleteLocalRef(clazz);
        return JNI_ERR;
    }
    env->DeleteLocalRef(clazz);
    return JNI_OK;
}

#endif /* defined(__ANDROID__) */
