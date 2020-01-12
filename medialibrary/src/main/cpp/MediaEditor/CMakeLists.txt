
# 添加目录
set(soundtouch_dir ../SoundTouch)
set(common_dir ../Common)

# 添加头文件路径
include_directories( android source ${soundtouch_dir} )

# 添加源文件
add_library( media_editor

             SHARED
             # 编辑器
             source/AudioCutEditor.cpp
             source/MediaEditor.cpp
             source/VideoCutEditor.cpp

             # android wrapper
             CainMediaEditor.cpp
             com_cgfay_media_CainMediaEditor.cpp)

# 链接静态库
target_link_libraries( media_editor

                       ffmpeg
                       soundtouch
                       yuv

                       android
                       jnigraphics
                       log
                       OpenSLES )

