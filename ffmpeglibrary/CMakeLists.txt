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
             src/main/cpp/ffmpeg_command/ffmpeg_cmd.c
             src/main/cpp/ffmpeg_command/ffmpeg_cmd_wrapper.c )

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


#--------------------------------- librecorder.so 编译 start ---------------------------------------

# 添加头文件
include_directories(src/main/cpp/common
                    src/main/cpp/ffmpeg/include
                    src/main/cpp/cainrecorder )

# 添加自身的jni库
add_library( recorder

             SHARED

             src/main/cpp/cainrecorder/recorder/CainEncoder.cpp
             src/main/cpp/cainrecorder/recorder/CainRecorder.cpp
             src/main/cpp/cainrecorder/RecorderController.cpp )

# 查找Android存在的库
find_library( log-lib

              log )

# 链接库文件
target_link_libraries(
                       recorder

                       # ffmpeg库
                       ffmpeg

                       ${log-lib} )

#---------------------------------- librecorder.so 编译 end ----------------------------------------


# 查找Android存在的库
find_library( OpenSLES-lib OpenSLES )
find_library( EGL-lib EGL )
find_library( GLESv2-lib GLESv2 )

#------------------------------------- common 加载 start -------------------------------------------
# 添加头文件
include_directories( src/main/cpp/common )

# 添加源文件
add_library( common

             STATIC

             # common
             src/main/cpp/common/Handler.cpp
             src/main/cpp/common/Looper.cpp
             src/main/cpp/common/MessageQueue.cpp
             src/main/cpp/common/Mutex.cpp
             src/main/cpp/common/Thread.cpp )

# 链接静态库
target_link_libraries( common

                       -landroid
                       ${log-lib} )
#-------------------------------------- common 加载 end --------------------------------------------

#----------------------------------- cainfilter 加载 start -----------------------------------------
# 添加头文件
include_directories(src/main/cpp/common
                    src/main/cpp/caingles
                    src/main/cpp/cainfilter )

# 添加源文件
add_library( cainfilter

             STATIC

             # cainfilter
             src/main/cpp/cainfilter/GLImageFilter.cpp
             src/main/cpp/cainfilter/GLImageInputFilter.cpp

             src/main/cpp/cainfilter/adjust/GLBrightnessImageFilter.cpp
             src/main/cpp/cainfilter/adjust/GLContrastImageFilter.cpp
             src/main/cpp/cainfilter/adjust/GLExposureImageFilter.cpp
             src/main/cpp/cainfilter/adjust/GLHueImageFilter.cpp
             src/main/cpp/cainfilter/adjust/GLSaturationImageFilter.cpp
             src/main/cpp/cainfilter/adjust/GLSharpnessImageFilter.cpp

             src/main/cpp/cainfilter/input/YUV420PImageInputFilter.cpp

             # caingles
             src/main/cpp/caingles/EglCore.cpp
             src/main/cpp/caingles/EglSurfaceBase.cpp
             src/main/cpp/caingles/GlShaders.cpp
             src/main/cpp/caingles/GlUtils.cpp
             src/main/cpp/caingles/OffscreenSurface.cpp
             src/main/cpp/caingles/WindowSurface.cpp )

# 链接静态库
target_link_libraries( cainfilter

                       common

                       # Android自带的库
                       -ljnigraphics
                       -landroid
                       ${log-lib}
                       ${EGL-lib}
                       ${GLESv2-lib} )

#------------------------------------ cainfilter 加载 end ------------------------------------------


#---------------------------------- cainsoundtouch 加载 end ----------------------------------------
# 添加头文件
include_directories( src/main/cpp/soundtouch/include
                     src/main/cpp/soundtouch )
# 添加源文件
add_library( cainsoundtouch

             STATIC

             # library
             src/main/cpp/soundtouch/SoundTouch/AAFilter.cpp
             src/main/cpp/soundtouch/SoundTouch/BPMDetect.cpp
             src/main/cpp/soundtouch/SoundTouch/cpu_detect_x86.cpp
             src/main/cpp/soundtouch/SoundTouch/FIFOSampleBuffer.cpp
             src/main/cpp/soundtouch/SoundTouch/FIRFilter.cpp
             src/main/cpp/soundtouch/SoundTouch/InterpolateCubic.cpp
             src/main/cpp/soundtouch/SoundTouch/InterpolateLinear.cpp
             src/main/cpp/soundtouch/SoundTouch/InterpolateShannon.cpp
             src/main/cpp/soundtouch/SoundTouch/mmx_optimized.cpp
             src/main/cpp/soundtouch/SoundTouch/PeakFinder.cpp
             src/main/cpp/soundtouch/SoundTouch/RateTransposer.cpp
             src/main/cpp/soundtouch/SoundTouch/SoundTouch.cpp
             src/main/cpp/soundtouch/SoundTouch/sse_optimized.cpp
             src/main/cpp/soundtouch/SoundTouch/TDStretch.cpp

             # wrapper
             src/main/cpp/SoundTouch/SoundTouchWrapper.cpp )

# 链接静态库
target_link_libraries( cainsoundtouch

                       common

                       -landroid
                       ${log-lib} )

#---------------------------------- cainsoundtouch 加载 end ----------------------------------------


#--------------------------------- libcainplayer.so 编译 start -------------------------------------
# 添加头文件
include_directories( src/main/cpp/common
                     src/main/cpp/ffmpeg/include )

# 添加源文件
add_library( cainplayer

             SHARED

             # common
             src/main/cpp/cainplayer/common/MediaJniCall.cpp
             src/main/cpp/cainplayer/common/MediaQueue.cpp
             src/main/cpp/cainplayer/common/MediaStatus.cpp
             src/main/cpp/cainplayer/common/MediaStream.cpp

             # decoder
             src/main/cpp/cainplayer/decoder/AVAudioDecoder.cpp
             src/main/cpp/cainplayer/decoder/AVDecoder.cpp
             src/main/cpp/cainplayer/decoder/AVVideoDecoder.cpp

             # output
             src/main/cpp/cainplayer/output/AVAudioOutput.cpp
             src/main/cpp/cainplayer/output/AVVideoOutput.cpp
             src/main/cpp/cainplayer/output/VideoOutputLooper.cpp

             # player
             src/main/cpp/cainplayer/player/AVMediaPlayer.cpp
             src/main/cpp/cainplayer/player/MediaPlayerHandler.cpp

             # synchronize
             src/main/cpp/cainplayer/synchronizer/AVSynchronizer.cpp

             src/main/cpp/cainplayer/PlayerController.cpp )

target_link_libraries( cainplayer

                       # 第三方库
                       ffmpeg
                       cainfilter
                       cainsoundtouch
                       common

                       # Android自带的库
                       -ljnigraphics
                       -landroid
                       ${log-lib}
                       ${OpenSLES-lib}
                       ${EGL-lib}
                       ${GLESv2-lib} )

#---------------------------------- libcainplayer.so 编译 end --------------------------------------