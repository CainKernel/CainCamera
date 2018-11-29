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

# 添加源文件
add_library( musicPlayer

             SHARED

             # library
             source/AudioQueue.cpp
             source/AVAudioDecoder.cpp
             source/AVMusicPlayer.cpp
             source/PlayerCallback.cpp
             source/PlayerStatus.cpp

             # controller
             JniPlayerCallback.cpp
             PlayerController.cpp)

# 链接静态库
target_link_libraries( musicPlayer

                       ffmpeg
                       soundtouch

                       android
                       log
                       OpenSLES )

