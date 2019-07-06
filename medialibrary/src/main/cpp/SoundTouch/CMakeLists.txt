# 设置cmake最低版本
cmake_minimum_required(VERSION 3.4.1)

# 设置GCC编译器的编译选项
if(CMAKE_COMPILER_IS_GNUCC)
  set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wmissing-declarations -Wmissing-prototypes")
endif(CMAKE_COMPILER_IS_GNUCC)

# 设置clang编译器的编译选项
if(CMAKE_COMPILER_IS_CLANG)
  set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wmissing-declarations -Wmissing-prototypes -Wdocumentation -Wunreachable-code")
endif(CMAKE_COMPILER_IS_CLANG)

# 添加头文件
#include_directories( include )

# 添加源文件
add_library( soundtouch

             SHARED

             # library
             source/SoundTouch/AAFilter.cpp
             source/SoundTouch/BPMDetect.cpp
             source/SoundTouch/cpu_detect_x86.cpp
             source/SoundTouch/FIFOSampleBuffer.cpp
             source/SoundTouch/FIRFilter.cpp
             source/SoundTouch/InterpolateCubic.cpp
             source/SoundTouch/InterpolateLinear.cpp
             source/SoundTouch/InterpolateShannon.cpp
             source/SoundTouch/mmx_optimized.cpp
             source/SoundTouch/PeakFinder.cpp
             source/SoundTouch/RateTransposer.cpp
             source/SoundTouch/SoundTouch.cpp
             source/SoundTouch/sse_optimized.cpp
             source/SoundTouch/TDStretch.cpp

             # wrapper
             SoundTouchWrapper.cpp

             # controller
             android/com_cgfay_media_SoundTouch.cpp)

target_include_directories( soundtouch

                            PRIVATE

                            include
                            source/SoundTouch )
# 链接静态库
target_link_libraries( soundtouch

                       android
                       log )

