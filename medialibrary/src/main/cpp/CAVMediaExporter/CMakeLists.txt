# 添加目录
set(filter_dir ../CainFilter)
set(common_dir ../Common)
set(native_help ../nativehelp)

# 添加头文件路径
include_directories(${filter_dir} ${common_dir} ${native_help})

# 添加源文件
add_library( media_exporter

             SHARED

             #demuxer
             ${common_dir}/demuxer/AVMediaDemuxer.cpp

             # decode
             ${common_dir}/decoder/AVAudioDecoder.cpp
             ${common_dir}/decoder/AVMediaDecoder.cpp
             ${common_dir}/decoder/AVVideoDecoder.cpp

             # decode thread
             ${common_dir}/decoder/DecodeAudioThread.cpp
             ${common_dir}/decoder/DecodeVideoThread.cpp

             # encode
             ${common_dir}/encoder/AVAudioEncoder.cpp
             ${common_dir}/encoder/AVMediaEncoder.cpp
             ${common_dir}/encoder/AVVideoEncoder.cpp
             ${common_dir}/encoder/NdkAudioEncoder.cpp
             ${common_dir}/encoder/NdkMediaEncoder.cpp
             ${common_dir}/encoder/NdkVideoEncoder.cpp

             # mux
             ${common_dir}/muxer/AVMediaMuxer.cpp

             # encode thread
             ${common_dir}/encoder/EncodeAudioThread.cpp
             ${common_dir}/encoder/EncodeVideoThread.cpp

             # filter
             ${common_dir}/filter/AmixFilter.cpp
             ${common_dir}/filter/VolumeFilter.cpp

             # common
             ${common_dir}/AVFormatter.cpp
             ${common_dir}/AVFrameFilter.cpp
             ${common_dir}/AVMediaData.cpp
             ${common_dir}/MessageQueue.cpp
             ${common_dir}/Resampler.cpp
             ${common_dir}/sonic.cpp
             ${common_dir}/SonicAudioTranscoder.cpp
             ${common_dir}/YuvConvertor.cpp
             ${common_dir}/YuvData.cpp

             # 媒体导出器
             ${common_dir}/exporter/AudioExporter.cpp
             ${common_dir}/exporter/CAVMediaExporter.cpp
             ${common_dir}/exporter/VideoExporter.cpp

             # 媒体编辑器
             ${native_help}/JNIHelp.cpp
             com_cgfay_media_CAVMediaExporter.cpp)


target_link_libraries( media_exporter

                       ffmpeg
                       yuv
                       cainfilter

                       android
                       jnigraphics
                       mediandk
                       EGL
                       ${GLES-lib}
                       log )

