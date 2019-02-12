# 添加 soundtouch 动态库
set(soundtouch_dir ../SoundTouch)

# 添加头文件路径
include_directories( source
        android
        ${soundtouch_dir} )

# 添加源文件
add_library( media_synthesizer

             SHARED

             # library
             source/MediaSynthesizer.cpp

             android/CainMediaSynthesizer.cpp
             android/com_cgfay_media_CainMediaSynthesizer.cpp )

# 链接静态库
target_link_libraries( media_synthesizer

                       ffmpeg
                       soundtouch

                       -landroid
                       -llog)

