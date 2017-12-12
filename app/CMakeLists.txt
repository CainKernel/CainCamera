# 设置cmake最低版本
cmake_minimum_required(VERSION 3.4.1)

# 设置路径
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../libs)

# 加载头文件
include_directories(src/main/cpp/ffmpeg
                    src/main/cpp/ffmpeg/include )

# 加载ffmpeg库
add_library( ffmpeg
             SHARED
             IMPORTED )
set_target_properties( ffmpeg
                       PROPERTIES IMPORTED_LOCATION
                       ../../../../libs/armeabi-v7a/libffmpeg.so )

# 添加自身的jni库
add_library( ffmpeg_cmd

             SHARED

             src/main/cpp/ffmpeg/cmdutils.c
             src/main/cpp/ffmpeg/ffmpeg.c
             src/main/cpp/ffmpeg/ffmpeg_opt.c
             src/main/cpp/ffmpeg/ffmpeg_filter.c
             src/main/cpp/ffmpeg_cmd/ffmpeg_cmd.c
             src/main/cpp/ffmpeg_cmd/ffmpeg_cmd_wrapper.c )

# 查找Android存在的库
find_library( log-lib

              log )

set(CMAKE_EXE_LINKER_FLAGS "-lz -ldl")

# 链接库文件
target_link_libraries(
                       ffmpeg_cmd

                       # ffmpeg库
                       ffmpeg

                       ${log-lib} )