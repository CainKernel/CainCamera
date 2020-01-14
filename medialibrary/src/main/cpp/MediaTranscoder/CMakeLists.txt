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

             # demuxer
             ${common_dir}/reader/AVMediaReader.cpp
             ${common_dir}/reader/AVMediaCodecReader.cpp
             # soft decoder
             ${common_dir}/reader/AVMediaDemuxer.cpp
             ${common_dir}/reader/AVAudioDecoder.cpp
             ${common_dir}/reader/AVMediaDecoder.cpp
             ${common_dir}/reader/AVVideoDecoder.cpp
             # hard decoder
             ${common_dir}/reader/MediaCodecVideoDecoder.cpp

             # soft encoder
             ${common_dir}/writer/AVAudioEncoder.cpp
             ${common_dir}/writer/AVMediaEncoder.cpp
             ${common_dir}/writer/AVVideoEncoder.cpp
             ${common_dir}/writer/AVMediaMuxer.cpp
             ${common_dir}/writer/AVMediaWriter.cpp
             # hard encoder
             ${common_dir}/writer/NdkMediaCodecMuxer.cpp
             ${common_dir}/writer/NdkAudioEncoder.cpp
             ${common_dir}/writer/NdkMediaEncoder.cpp
             ${common_dir}/writer/NdkVideoEncoder.cpp
             ${common_dir}/writer/NdkMediaWriter.cpp
             # audio/video single track writer
             ${common_dir}/writer/NdkAudioWriter.cpp
             ${common_dir}/writer/NdkVideoWriter.cpp

             # player
             ${common_dir}/player/AudioPlayer.cpp
             ${common_dir}/player/AudioSLPlayer.cpp

             # common
             ${common_dir}/AVFormatter.cpp
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
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
                       OpenSLES
                       EGL
                       ${GLES-lib}
                       log )

