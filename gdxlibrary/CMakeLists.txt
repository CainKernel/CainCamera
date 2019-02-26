
cmake_minimum_required(VERSION 3.4.1)

add_library(
             nativegdx

             SHARED
             # jni interface
            src/main/cpp/Matrix4.cpp)


find_library( log-lib log )

target_link_libraries(
                       nativegdx

                       -ljnigraphics
                       -landroid
                       ${log-lib} )