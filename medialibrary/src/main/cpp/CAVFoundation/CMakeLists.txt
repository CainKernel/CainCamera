# 添加目录
set(common_dir ../Common)

set(native_help_dir ../nativehelp)

# 添加头文件路径
include_directories(${common_dir} ${native_help_dir} ./)

# 添加源文件
add_library( cavfoundation

        SHARED

        codec/CAVMediaMuxer.cpp

        coregraphics/Geometry.cpp

        coremedia/AVTime.cpp
        coremedia/AVTimeRange.cpp

        AVAsset.cpp
        AVAssetTrack.cpp
        AVAssetTrackSegment.cpp
        CAVUriAsset.cpp

        AVComposition.cpp
        AVCompositionTrack.cpp
        AVCompositionTrackSegment.cpp

        # android
        ${native_help_dir}/JNIHelp.cpp
        android/CAVUriAsset_jni.cpp
        android/CAVMediaMuxer_jni.cpp
        )

# 链接so库
target_link_libraries( cavfoundation

        ffmpeg
        yuv

        android
        jnigraphics
        log )

