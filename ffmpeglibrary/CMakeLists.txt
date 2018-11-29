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


# 备注，这里可以拆分成多个子CmakeLists的方式来编译
# 但是拆分成多个CmakeLists编译，只要是用到ffmpeg的，原先的libffmpeg.so会被拆分成libavcodec.so等多个库，重复放到libs目录下
# 因此，拆分时为了防止加入重复的库，需要用子CmakeLists，并将libavcodec.a等静态库重新合成一个libffmpeg.so
# 拆封步骤后续再做优化

# 加载头文件
include_directories( src/main/cpp/common
                     src/main/cpp/include
                     src/main/cpp/SoundTouch/include )

# 添加ffmpeg库
add_library( ffmpeg
             SHARED
             IMPORTED )
set_target_properties( ffmpeg
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/libffmpeg.so )

# 添加recorder库
add_library( media_recorder

             SHARED

             # library
             src/main/cpp/MediaRecorder/source/CainEncoder.cpp
             src/main/cpp/MediaRecorder/source/CainRecorder.cpp

             # controller
             src/main/cpp/MediaRecorder/RecorderController.cpp )

target_link_libraries( media_recorder

                       ffmpeg

                       log )

# 添加soundtouch库，由于soundtouch没有用libffmpeg.so，因此，这里可以用单独的CmakeLists来编译
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/SoundTouch)

# 备注：后续再做拆分
# 添加musicPlayer库
# ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/Metadata)

# 添加metadata库
add_library( metadata_retriever

             SHARED

             # library
             src/main/cpp/Metadata/source/MediaMetadataRetriever.cpp
             src/main/cpp/Metadata/source/Metadata.cpp

             # controller
             src/main/cpp/Metadata/MetadataController.cpp )

target_link_libraries( metadata_retriever

                       ffmpeg

                       android
                       jnigraphics
                       log )

# 备注：后续再做拆分
# 添加musicPlayer库
# ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/MusicPlayer)

# 添加musicPlayer库
add_library( music_player

             SHARED

             # library
             src/main/cpp/MusicPlayer/source/AudioQueue.cpp
             src/main/cpp/MusicPlayer/source/AVAudioDecoder.cpp
             src/main/cpp/MusicPlayer/source/AVMusicPlayer.cpp
             src/main/cpp/MusicPlayer/source/PlayerCallback.cpp
             src/main/cpp/MusicPlayer/source/PlayerStatus.cpp

             # controller
             src/main/cpp/MusicPlayer/JniPlayerCallback.cpp
             src/main/cpp/MusicPlayer/PlayerController.cpp)

target_link_libraries( music_player

                       ffmpeg
                       soundtouch

                       android
                       log
                       OpenSLES )