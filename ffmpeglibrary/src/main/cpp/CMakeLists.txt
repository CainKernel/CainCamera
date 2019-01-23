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
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# 加载头文件
include_directories( ${CMAKE_SOURCE_DIR}/common
                     ${CMAKE_SOURCE_DIR}/include )

# 添加ffmpeg库
add_library( ffmpeg SHARED IMPORTED )
set_target_properties( ffmpeg
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libffmpeg.so )

# 添加recorder库
add_subdirectory(${CMAKE_SOURCE_DIR}/MediaRecorder)

# 添加metadata_retriever库
add_subdirectory(${CMAKE_SOURCE_DIR}/Metadata)

# 添加music_player库
add_subdirectory(${CMAKE_SOURCE_DIR}/MusicPlayer)