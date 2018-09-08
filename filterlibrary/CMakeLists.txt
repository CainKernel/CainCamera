
cmake_minimum_required(VERSION 3.4.1)

add_library(
             nativefilter

             SHARED

             # common
             src/main/cpp/common/Point.cpp
             # utils
             src/main/cpp/nativefilter/FilterUtils.cpp
             src/main/cpp/nativefilter/BlendFilter.cpp
             # ndk filter
             src/main/cpp/nativefilter/BlackWhiteFilter.cpp
             src/main/cpp/nativefilter/BrightContrastFilter.cpp
             src/main/cpp/nativefilter/ColorQuantizeFilter.cpp
             src/main/cpp/nativefilter/GaussianBlurFilter.cpp
             src/main/cpp/nativefilter/HistogramEqualFilter.cpp
             src/main/cpp/nativefilter/ImageFilter.cpp
             src/main/cpp/nativefilter/InvertFilter.cpp
             src/main/cpp/nativefilter/LookupTableFilter.cpp
             src/main/cpp/nativefilter/MosaicFilter.cpp
             src/main/cpp/nativefilter/ShiftFilter.cpp
             src/main/cpp/nativefilter/StackBlurFilter.cpp
             src/main/cpp/nativefilter/VignetteFilter.cpp

             # jni interface
             src/main/cpp/ImageFilterController.cpp )


find_library( log-lib log )

target_link_libraries(
                       nativefilter

                       -ljnigraphics
                       -landroid
                       ${log-lib} )