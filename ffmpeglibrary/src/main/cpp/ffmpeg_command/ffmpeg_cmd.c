#include <setjmp.h>
#include <android/log.h>

#include "ffmpeg_cmd.h"
#include "ffmpeg.h"
#ifdef __cplusplus
extern "C" {
#endif

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, "FFMPEG", __VA_ARGS__)

jmp_buf jmp_exit;

int run_cmd(int argc, char** argv)
{
    int res = 0;
    if(res = setjmp(jmp_exit))
    {
        LOGD("res=%d", res);
        return res;
    }

    res = run(argc, argv);
    LOGD("res_run=%d", res);
    return res;
}

#ifdef __cplusplus
}
#endif