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
include_directories( include
                     source )
# 添加源文件
add_library( soundtouch

             SHARED

             # library
             source/AAFilter.cpp
             source/BPMDetect.cpp
             source/cpu_detect_x86.cpp
             source/FIFOSampleBuffer.cpp
             source/FIRFilter.cpp
             source/InterpolateCubic.cpp
             source/InterpolateLinear.cpp
             source/InterpolateShannon.cpp
             source/mmx_optimized.cpp
             source/PeakFinder.cpp
             source/RateTransposer.cpp
             source/SoundTouch.cpp
             source/sse_optimized.cpp
             source/TDStretch.cpp

             # wrapper
             SoundTouchWrapper.cpp )

# 链接静态库
target_link_libraries( soundtouch

                       android
                       log )

