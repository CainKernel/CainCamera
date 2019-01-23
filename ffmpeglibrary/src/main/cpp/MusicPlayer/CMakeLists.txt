
# 添加 soundtouch 动态库
set(soundtouch_dir ../SoundTouch)
add_subdirectory(${soundtouch_dir} soundtouch)

# 添加头文件路径
include_directories( source
        ${soundtouch_dir}
        ${soundtouch_dir}/include )

# 添加源文件
add_library( music_player

             SHARED

             # library
             source/AudioQueue.cpp
             source/AVAudioDecoder.cpp
             source/AVMusicPlayer.cpp
             source/PlayerCallback.cpp
             source/PlayerStatus.cpp

             # controller
             JniPlayerCallback.cpp
             PlayerController.cpp)

# 链接静态库
target_link_libraries( music_player

                       ffmpeg
                       soundtouch

                       android
                       log
                       OpenSLES )

