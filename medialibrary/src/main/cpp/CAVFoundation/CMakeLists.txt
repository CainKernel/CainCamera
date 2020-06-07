# 添加目录
set(common_dir ../Common)

# 添加头文件路径
include_directories(${common_dir})

# 添加源文件
add_library( cavfoundation

        SHARED

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

        android/com_cgfay_cavfoundation_CAVUriAsset.cpp
        )

# 链接so库
target_link_libraries( cavfoundation

        ffmpeg
        yuv

        android
        jnigraphics
        log )

