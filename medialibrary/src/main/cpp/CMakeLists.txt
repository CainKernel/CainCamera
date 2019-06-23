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

# 添加include目录路径
include_directories( include )

# 添加ffmpeg库
add_library( ffmpeg SHARED IMPORTED )
set_target_properties( ffmpeg
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libffmpeg.so )

# 添加yuv库
add_library(yuv SHARED IMPORTED )
set_target_properties( yuv
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libyuv.so )

# FFmpeg命令行库
add_subdirectory(${CMAKE_SOURCE_DIR}/FFCommand)

# Metadata库
add_subdirectory(${CMAKE_SOURCE_DIR}/Metadata)

# SoundTouch库
add_subdirectory(${CMAKE_SOURCE_DIR}/SoundTouch)

# GLFilter库
add_subdirectory(${CMAKE_SOURCE_DIR}/CainFilter)

# 媒体播放器
add_subdirectory(${CMAKE_SOURCE_DIR}/MediaPlayer)

# 媒体合成器
add_subdirectory(${CMAKE_SOURCE_DIR}/VideoEditor)
