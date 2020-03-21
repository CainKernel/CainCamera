# 添加目录
set(common_dir ../Common)

# 添加头文件路径
include_directories(${common_dir})

# 添加源文件
add_library( videoplayer

             SHARED

             #demuxer
             ${common_dir}/demuxer/AVMediaDemuxer.cpp

             # decode
             ${common_dir}/decoder/AVAudioDecoder.cpp
             ${common_dir}/decoder/AVMediaDecoder.cpp
             ${common_dir}/decoder/AVVideoDecoder.cpp
        
             ${common_dir}/decoder/DecodeAudioThread.cpp
             ${common_dir}/decoder/DecodeVideoThread.cpp

             # common
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/MessageQueue.cpp
             ${common_dir}/Resampler.cpp
             ${common_dir}/sonic.cpp
             ${common_dir}/SonicAudioTranscoder.cpp

             # player
             ${common_dir}/player/AudioStreamPlayer.cpp
             ${common_dir}/player/AVMediaPlayer.cpp
             ${common_dir}/player/Timestamp.cpp
             ${common_dir}/player/VideoStreamPlayer.cpp

             # audio
             ${common_dir}/audio/AudioPlay.cpp
             ${common_dir}/audio/AudioSLPlay.cpp

             # video
             ${common_dir}/video/AVideoPlay.cpp

             # 播放器
             FFMediaPlayer.cpp
             com_cgfay_media_VideoPlayer.cpp)


target_link_libraries( videoplayer

                       ffmpeg
                       yuv

                       android
                       jnigraphics
                       mediandk
                       EGL
                       ${GLES-lib}
                       OpenSLES
                       log )

