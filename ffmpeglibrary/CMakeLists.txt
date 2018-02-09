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

# 设置路径
set(distribution_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

#---------------------------------- libffmpeg.so 加载 start ----------------------------------------
# 加载ffmpeg库
add_library( ffmpeg
             SHARED
             IMPORTED )
set_target_properties( ffmpeg
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/libffmpeg.so )
#----------------------------------- libffmpeg.so 加载 end -----------------------------------------

#-------------------------------- libffmpeg_cmd.so 编译 start --------------------------------------
# 编译 libffmpeg_cmd.so 部分
# 加载头文件
# 这里需要添加ffmpeg库的路径，用来编译生成ffmpeg_cmd.so
# 这里如果不引入头文件，会出现libavresample/avresample.h等头文件找不到的情况
# 因为原来就没有引入libavresample库
include_directories(${CMAKE_SOURCE_DIR}/../sources/ffmpeg
                    src/main/cpp/ffmpeg
                    src/main/cpp/ffmpeg/include )

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
#-------------------------------- libffmpeg_cmd.so 编译 end ----------------------------------------

#--------------------------------- libencoder.so 编译 start ----------------------------------------

# 添加头文件
include_directories(src/main/cpp/common
                    src/main/cpp/ffmpeg/include
                    src/main/cpp/encoder/include )

# 添加自身的jni库
add_library( encoder

             SHARED

             src/main/cpp/encoder/cain_recorder.cpp
             src/main/cpp/encoder/ffmpeg_encoder.cpp
             src/main/cpp/encoder/cain_encoder_jni.cpp )

# 查找Android存在的库
find_library( log-lib

              log )

# 链接库文件
target_link_libraries(
                       encoder

                       # ffmpeg库
                       ffmpeg

                       ${log-lib} )

#---------------------------------- libencoder.so 编译 end -----------------------------------------

#-------------------------------- libcainplayer.so 编译 start --------------------------------------
#添加头文件
include_directories(src/main/cpp/common
                    src/main/cpp/ffmpeg/include
                    src/main/cpp/cainmedia/cainsdl
                    src/main/cpp/cainmedia/cainplayer )

#添加自身的jni文件
add_library( cainplayer

             SHARED

             src/main/cpp/cainmedia/cainplayer/CainPlayer.cpp
             src/main/cpp/cainmedia/cainplayer/CainThread.cpp
             src/main/cpp/cainmedia/cainplayer/CainAudio.cpp
             src/main/cpp/cainmedia/cainplayer/CainClock.cpp
             src/main/cpp/cainmedia/cainplayer/CainCmdUtils.cpp
             src/main/cpp/cainmedia/cainplayer/CainFrameQueue.cpp
             src/main/cpp/cainmedia/cainplayer/CainGLUtils.cpp
             src/main/cpp/cainmedia/cainplayer/CainMutex.cpp
             src/main/cpp/cainmedia/cainplayer/CainPacketQueue.cpp
             src/main/cpp/cainmedia/cainplayer/CainShaders.cpp
             src/main/cpp/cainmedia/cainplayer/CainVideoRender.cpp
             )

# 查找Android存在的库
find_library( OpenSLES-lib OpenSLES )
find_library( EGL-lib EGL )
find_library( GLESv2-lib GLESv2 )

# 链接库文件
target_link_libraries(
                       cainplayer

                       # 第三方库
                       ffmpeg

                       # Android 自带的库
                       ${log-lib}
                       ${OpenSLES-lib}
                       ${EGL-lib}
                       ${GLESv2-lib} )
#--------------------------------- libcainplayer.so 编译 end ---------------------------------------