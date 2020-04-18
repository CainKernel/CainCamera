# 添加头文件路径
include_directories( source )
# 添加源文件
add_library( cav_command

        SHARED

        # library
        cmdutils.c
        ffmpeg.c
        ffmpeg_filter.c
        ffmpeg_opt.c
        com_cgfay_media_CAVCommand.cpp)

# 链接静态库
target_link_libraries( cav_command

                       ffmpeg

                       android
                       jnigraphics
                       log )

