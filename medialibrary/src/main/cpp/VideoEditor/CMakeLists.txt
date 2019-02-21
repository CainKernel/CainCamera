# 添加头文件路径
include_directories( source )

# 添加源文件
add_library( video_editor

             SHARED

             # library
             source/cmdutils.c
             source/ffmpeg.c
             source/ffmpeg_filter.c
             source/ffmpeg_opt.c

             android/CainShortVideoEditor.cpp
             android/com_cgfay_media_CainShortVideoEditor.cpp)

# 链接静态库
target_link_libraries( video_editor

                       ffmpeg

                       android
                       jnigraphics
                       log )

