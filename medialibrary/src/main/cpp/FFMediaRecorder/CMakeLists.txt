# 添加目录
set(common_dir ../Common)

# 添加头文件路径
include_directories(${common_dir} )

# 添加源文件
add_library( ffrecorder

             SHARED

             #
             ${common_dir}/writer/AVMediaMuxer.cpp
             ${common_dir}/writer/AVMediaWriter.cpp
             ${common_dir}/writer/AVAudioEncoder.cpp
             ${common_dir}/writer/AVMediaEncoder.cpp
             ${common_dir}/writer/AVVideoEncoder.cpp

             # 硬编码
             ${common_dir}/writer/NdkMediaCodecMuxer.cpp
             ${common_dir}/writer/NdkAudioEncoder.cpp
             ${common_dir}/writer/NdkMediaEncoder.cpp
             ${common_dir}/writer/NdkMediaWriter.cpp
             ${common_dir}/writer/NdkVideoEncoder.cpp

             # 通用类
             ${common_dir}/AVFormatter.cpp
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/Resampler.cpp
             ${common_dir}/YuvConvertor.cpp
             ${common_dir}/YuvData.cpp

             # 录制器
             FFMediaRecorder.cpp
             RecordParams.cpp
             com_cgfay_media_recorder_FFMediaRecorder.cpp)

# 链接so库
target_link_libraries( ffrecorder

                       ffmpeg
                       yuv

                       android
                       jnigraphics
                       mediandk
                       log )

