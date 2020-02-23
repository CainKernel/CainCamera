# 添加目录
set(common_dir ../Common)

# 添加头文件路径
include_directories(${common_dir})

# 添加源文件
add_library( musicplayer

             SHARED

             # audio
             ${common_dir}/audio/AudioPlayer.cpp
             ${common_dir}/audio/AudioSLPlayer.cpp

             #demuxer
             ${common_dir}/demuxer/AVMediaDemuxer.cpp

             # decode
             ${common_dir}/decoder/AudioDecodeThread.cpp
             ${common_dir}/decoder/AVAudioDecoder.cpp
             ${common_dir}/decoder/AVMediaDecoder.cpp

             # reader
             ${common_dir}/reader/AudioReader.cpp

             # common
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/Handler.cpp
             ${common_dir}/MessageQueue.cpp
             ${common_dir}/Resampler.cpp
             ${common_dir}/sonic.cpp
             ${common_dir}/SonicAudioTranscoder.cpp

             # 转码器
             MusicPlayer.cpp
             com_cgfay_media_MusicPlayer.cpp)


target_link_libraries( musicplayer

                       ffmpeg

                       android
                       OpenSLES
                       log )

