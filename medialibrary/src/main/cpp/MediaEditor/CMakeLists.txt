
# 添加目录
set(soundtouch_dir ../SoundTouch)
set(common_dir ../Common)

# 添加头文件路径
include_directories( android source ${soundtouch_dir} )

# 添加源文件
add_library( media_editor

             SHARED

             # 通用类
#             ${common_dir}/reader/MediaCodecReader.cpp
             ${common_dir}/reader/AVMediaReader.cpp
#             ${common_dir}/writer/MediaCodecWriter.cpp
             ${common_dir}/writer/AVMediaWriter.cpp
             ${common_dir}/AVFormatter.cpp
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/Resampler.cpp
             ${common_dir}/YuvConvertor.cpp
             ${common_dir}/YuvData.cpp

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

