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

# 添加soundtouch库
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/SoundTouch)

# 添加recorder库
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/MediaRecorder)

# 添加metadata_retriever库
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/Metadata)

# 添加music_player库
ADD_SUBDIRECTORY(${CMAKE_SOURCE_DIR}/src/main/cpp/MusicPlayer)