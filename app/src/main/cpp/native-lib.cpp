#include <jni.h>
#include <string>

extern "C" {

#include <libavformat/avformat.h>

/**
 * avcodec配置
 * @param env
 * @return
 */
JNIEXPORT jstring JNICALL
Java_com_cgfay_caincamera_eglnative_EGLNativeHelper_configurationFromFFmpeg(
        JNIEnv *env,
        jobject /* this */) {
    char info[10000] = { 0 };
    sprintf(info, "%s\n", avcodec_configuration());
    return env->NewStringUTF(info);
}


}
