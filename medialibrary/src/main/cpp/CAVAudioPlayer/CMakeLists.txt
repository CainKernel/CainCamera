# 添加目录
set(common_dir ../Common)
set(native_help ../nativehelp)

# 添加头文件路径
include_directories(${common_dir} ${native_help})

# 添加源文件
add_library(audio_player

             SHARED

             # audio
             ${common_dir}/audio/AudioPlay.cpp
             ${common_dir}/audio/AudioSLPlay.cpp

             #demuxer
             ${common_dir}/demuxer/AVMediaDemuxer.cpp

             # decode
             ${common_dir}/decoder/AVAudioDecoder.cpp
             ${common_dir}/decoder/AVMediaDecoder.cpp
             ${common_dir}/decoder/DecodeAudioThread.cpp

             # common
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/MessageQueue.cpp
             ${common_dir}/sonic.cpp
             ${common_dir}/SonicAudioTranscoder.cpp

             # player
             ${common_dir}/player/AudioStreamPlayer.cpp
             ${common_dir}/player/Timestamp.cpp
             ${common_dir}/player/CAVAudioPlayer.cpp

             # jni
             ${native_help}/JNIHelp.cpp
             com_cgfay_media_CAVAudioPlayer.cpp)


target_link_libraries(audio_player

                       ffmpeg

                       android
                       OpenSLES
                       log )

