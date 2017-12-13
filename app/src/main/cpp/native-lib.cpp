//
// Created by Administrator on 2017/12/13.
//
#include <jni.h>
#include <string>


extern "C" {

#include <libavcodec/avcodec.h>



JNIEXPORT jstring
JNICALL
Java_com_cgfay_ffmpegsample_MainActivity_stringFromFFmpeg(
        JNIEnv *env,
        jobject /* this */) {
    char info[10000] = { 0 };
    sprintf(info, "%s\n", avcodec_configuration());
    return env->NewStringUTF(info);
}

}
