
cmake_minimum_required(VERSION 3.4.1)

add_library(
             nativefilter

             SHARED

             # common
             common/Point.cpp
             # utils
             nativefilter/FilterUtils.cpp
             nativefilter/BlendFilter.cpp
             # ndk filter
             nativefilter/BlackWhiteFilter.cpp
             nativefilter/BrightContrastFilter.cpp
             nativefilter/ColorQuantizeFilter.cpp
             nativefilter/GaussianBlurFilter.cpp
             nativefilter/HistogramEqualFilter.cpp
             nativefilter/ImageFilter.cpp
             nativefilter/InvertFilter.cpp
             nativefilter/LookupTableFilter.cpp
             nativefilter/MosaicFilter.cpp
             nativefilter/ShiftFilter.cpp
             nativefilter/StackBlurFilter.cpp
             nativefilter/VignetteFilter.cpp

             # jni interface
             ImageFilterController.cpp )


find_library( log-lib log )

target_link_libraries(
                       nativefilter

                       -ljnigraphics
                       -landroid
                       ${log-lib} )