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

# 设置一些C/C++选项flag
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC -fpermissive -std=c++11 -D__STDC_CONSTANT_MACROS -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H ")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -O2 -DSYS=posix -DNO_CRYPTO")


#--------------------------------------- 编译libpolarssl.a静态库 ------------------------------------
# 包含头文件
include_directories( src/main/cpp/libpolarssl/include )

# 加载polarssl的源文件并编译成polayssl.a静态库
add_library( polarssl

	         STATIC

	         src/main/cpp/libpolarssl/library/aes.c
	         src/main/cpp/libpolarssl/library/arc4.c
	         src/main/cpp/libpolarssl/library/asn1parse.c
	         src/main/cpp/libpolarssl/library/asn1write.c
	         src/main/cpp/libpolarssl/library/base64.c
	         src/main/cpp/libpolarssl/library/bignum.c
	         src/main/cpp/libpolarssl/library/blowfish.c
	         src/main/cpp/libpolarssl/library/camellia.c
	         src/main/cpp/libpolarssl/library/certs.c
	         src/main/cpp/libpolarssl/library/cipher.c
	         src/main/cpp/libpolarssl/library/cipher_wrap.c
	         src/main/cpp/libpolarssl/library/ctr_drbg.c
	         src/main/cpp/libpolarssl/library/debug.c
	         src/main/cpp/libpolarssl/library/des.c
	         src/main/cpp/libpolarssl/library/dhm.c
	         src/main/cpp/libpolarssl/library/entropy.c
	         src/main/cpp/libpolarssl/library/entropy_poll.c
	         src/main/cpp/libpolarssl/library/error.c
	         src/main/cpp/libpolarssl/library/gcm.c
	         src/main/cpp/libpolarssl/library/havege.c
	         src/main/cpp/libpolarssl/library/md.c
	         src/main/cpp/libpolarssl/library/md_wrap.c
	         src/main/cpp/libpolarssl/library/md2.c
	         src/main/cpp/libpolarssl/library/md4.c
	         src/main/cpp/libpolarssl/library/md5.c
	         src/main/cpp/libpolarssl/library/net.c
	         src/main/cpp/libpolarssl/library/padlock.c
	         src/main/cpp/libpolarssl/library/pbkdf2.c
	         src/main/cpp/libpolarssl/library/pem.c
	         src/main/cpp/libpolarssl/library/pkcs5.c
	         src/main/cpp/libpolarssl/library/pkcs11.c
	         src/main/cpp/libpolarssl/library/pkcs12.c
	         src/main/cpp/libpolarssl/library/rsa.c
	         src/main/cpp/libpolarssl/library/sha1.c
	         src/main/cpp/libpolarssl/library/sha2.c
	         src/main/cpp/libpolarssl/library/sha4.c
	         src/main/cpp/libpolarssl/library/ssl_cache.c
	         src/main/cpp/libpolarssl/library/ssl_cli.c
	         src/main/cpp/libpolarssl/library/ssl_srv.c
	         src/main/cpp/libpolarssl/library/ssl_tls.c
	         src/main/cpp/libpolarssl/library/timing.c
	         src/main/cpp/libpolarssl/library/version.c
	         src/main/cpp/libpolarssl/library/x509parse.c
	         src/main/cpp/libpolarssl/library/x509write.c
	         src/main/cpp/libpolarssl/library/xtea.c )

# 链接Android中的zlib库
target_link_libraries( polarssl z )

# 加载 libfaac.a静态库
add_library( faac
             STATIC
             IMPORTED )
set_target_properties( faac
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/libfaac.a )

# 加载 libx264.a静态库
add_library( x264
             STATIC
             IMPORTED )
set_target_properties( x264
                       PROPERTIES IMPORTED_LOCATION
                       ${distribution_DIR}/libx264.a )

# 包含头文件
include_directories( src/main/cpp
                     src/main/cpp/librtmp )

# 加载librtmp源文件编译成 librtmp.a静态库
add_library( rtmp

             STATIC

             src/main/cpp/librtmp/amf.c
             src/main/cpp/librtmp/hashswf.c
             src/main/cpp/librtmp/log.c
             src/main/cpp/librtmp/parseurl.c
             src/main/cpp/librtmp/rtmp.c )

# 链接ploarssl静态库
target_link_libraries( rtmp

                      polarssl z )

# 加载pusher的源文件并编译成libpusher.so动态库
add_library( pusher

             SHARED

             src/main/cpp/pusher/cain_pusher.cpp
             src/main/cpp/pusher/queue.c
             src/main/cpp/pusher/RtmpPush.cpp )


# 查找Android中包含的库
find_library( log-lib

              log )

# 链接需要的库
target_link_libraries( pusher

                       # 需要链接的库
                       rtmp
                       x264
                       faac

                       ${log-lib} )





