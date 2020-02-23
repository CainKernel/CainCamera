# 根据API版本判断使用哪个版本的OpenGLES
if (${ANDROID_PLATFORM_LEVEL} LESS 12)
    message(FATAL_ERROR "OpenGL 2 is not supported before API level 11 (currently using ${ANDROID_PLATFORM_LEVEL}).")
    return()
elseif (${ANDROID_PLATFORM_LEVEL} LESS 18)
    add_definitions("-DDYNAMIC_ES3")
    set(GLES-lib GLESv2)
else ()
    set(GLES-lib GLESv3)
endif (${ANDROID_PLATFORM_LEVEL} LESS 11)

# 添加GLFilter库
set(filter_dir ../CainFilter)

# 添加目录
set(common_dir ../Common)

# 添加头文件路径
include_directories(${common_dir} ${filter_dir})

# 添加源文件
add_library( transcoder

             SHARED

             # reader
             ${common_dir}/reader/AVMediaReader.cpp
             ${common_dir}/reader/AVMediaCodecReader.cpp
             # demuxer
             ${common_dir}/demuxer/AVMediaDemuxer.cpp
             # decoder
             ${common_dir}/decoder/AVAudioDecoder.cpp
             ${common_dir}/decoder/AVMediaDecoder.cpp
             ${common_dir}/decoder/AVVideoDecoder.cpp
             ${common_dir}/decoder/MediaCodecVideoDecoder.cpp

             # encoder
             ${common_dir}/encoder/AVAudioEncoder.cpp
             ${common_dir}/encoder/AVMediaEncoder.cpp
             ${common_dir}/encoder/AVVideoEncoder.cpp
             ${common_dir}/encoder/NdkAudioEncoder.cpp
             ${common_dir}/encoder/NdkMediaEncoder.cpp
             ${common_dir}/encoder/NdkVideoEncoder.cpp

             # muxer
             ${common_dir}/muxer/AVMediaMuxer.cpp
             ${common_dir}/muxer/NdkMediaCodecMuxer.cpp

             # media writer
             ${common_dir}/writer/AVMediaWriter.cpp
             ${common_dir}/writer/NdkMediaWriter.cpp
             ${common_dir}/writer/NdkAudioWriter.cpp
             ${common_dir}/writer/NdkVideoWriter.cpp

             # common
             ${common_dir}/AVFormatter.cpp
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/Handler.cpp
             ${common_dir}/MessageQueue.cpp
             ${common_dir}/Resampler.cpp
             ${common_dir}/YuvConvertor.cpp
             ${common_dir}/YuvData.cpp

             # 转码器
             MediaFrameProvider.cpp
             MediaTranscoder.cpp
             TranscodeParams.cpp
             com_cgfay_media_transcoder_MediaTranscoder.cpp)

# 链接so库
target_link_libraries( transcoder

                       ffmpeg
                       yuv
                       cainfilter

                       android
                       jnigraphics
                       mediandk
                       EGL
                       ${GLES-lib}
                       log )

