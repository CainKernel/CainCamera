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
include_directories( src/main/cpp/cainmedia/common
                     src/main/cpp/cainmedia/imagefilter )

# 添加自身的jni库
add_library( imagefilter

             SHARED

             src/main/cpp/cainmedia/ImageProcess.cpp
             src/main/cpp/cainmedia/imagefilter/GrayFilter.cpp )

# 查找Android存在的库
find_library( log-lib

              log )

# 链接库文件
target_link_libraries(
                       imagefilter

                       ${log-lib} )