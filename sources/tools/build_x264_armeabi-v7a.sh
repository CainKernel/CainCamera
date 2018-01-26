#!/bin/sh

# 编译选项
EXTRA_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=neon -D__ARM_ARCH_7__ -D__ARM_ARCH_7A__"

# x264 源码目录
X264_SOURCE=${ROOT_SOURCE}/build/x264
# 输出路径
PREFIX=${X264_SOURCE}/android/armeabi-v7a

# 配置和编译
cd ${X264_SOURCE}
./configure \
--prefix=${PREFIX} \
--enable-static \
--enable-pic \
--enable-strip \
--host=arm-linux-androideabi \
--cross-prefix=${TOOLCHAIN}/bin/arm-linux-androideabi- \
--sysroot=${PLATFORM} \
--extra-cflags="-Os -fpic ${EXTRA_CFLAGS}" \
--extra-ldflags="" \
${ADDITIONAL_CONFIGURE_FLAG}

make clean
make -j4
make install
