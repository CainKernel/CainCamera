#!/bin/sh

#编译目录
BUILD_SOURCE=${ROOT_SOURCE}/build
#FFmpeg的源码目录
FFMPEG_SOURCE=${BUILD_SOURCE}/ffmpeg

# x264的目录
X264_INCLUDE=${BUILD_SOURCE}/x264/android/armeabi-v7a/include
X264_LIB=${BUILD_SOURCE}/x264/android/armeabi-v7a/lib

# arm v7vfp
CPU=armeabi-v7a
OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=neon -marm -march=armv7-a "
ADDI_CFLAGS="-marm"
PREFIX=${FFMPEG_SOURCE}/android/${CPU}

cd $FFMPEG_SOURCE

# 移除旧的文件夹
rm -rf ${PREFIX}
# 重新创建新的文件夹
mkdir -p ${PREFIX}

# 配置
./configure \
--prefix=${PREFIX} \
--arch=arm \
--cpu=armv7-a \
--target-os=android \
--enable-cross-compile \
--cross-prefix=${TOOLCHAIN}/bin/arm-linux-androideabi- \
--sysroot=${PLATFORM} \
--extra-cflags="-I${X264_INCLUDE} -I${PLATFORM}/usr/include" \
--extra-ldflags="-L${X264_LIB}" \
--cc=${TOOLCHAIN}/bin/arm-linux-androideabi-gcc \
--nm=${TOOLCHAIN}/bin/arm-linux-androideabi-nm \
--disable-shared \
--enable-static \
--enable-gpl \
--enable-version3 \
--enable-pthreads \
--enable-runtime-cpudetect \
--enable-small \
--disable-network \
--disable-vda \
--disable-iconv \
--enable-asm \
--enable-neon \
--enable-yasm \
--disable-encoders \
--enable-libx264 \
--enable-encoder=h263 \
--enable-encoder=libx264 \
--enable-encoder=aac \
--enable-encoder=mpeg4 \
--enable-encoder=mjpeg \
--enable-encoder=png \
--enable-encoder=gif \
--enable-encoder=bmp \
--disable-muxers \
--enable-muxer=h264 \
--enable-muxer=flv \
--enable-muxer=gif \
--enable-muxer=mp3 \
--enable-muxer=dts \
--enable-muxer=mp4 \
--enable-muxer=mov \
--enable-muxer=mpegts \
--disable-decoders \
--enable-decoder=aac \
--enable-decoder=aac_latm \
--enable-decoder=mp3 \
--enable-decoder=h263 \
--enable-decoder=h264 \
--enable-decoder=mpeg4 \
--enable-decoder=mjpeg \
--enable-decoder=gif \
--enable-decoder=png \
--enable-decoder=bmp \
--enable-decoder=yuv4 \
--disable-demuxers \
--enable-demuxer=image2 \
--enable-demuxer=h263 \
--enable-demuxer=h264 \
--enable-demuxer=flv \
--enable-demuxer=gif \
--enable-demuxer=aac \
--enable-demuxer=ogg \
--enable-demuxer=dts \
--enable-demuxer=mp3 \
--enable-demuxer=mov \
--enable-demuxer=m4v \
--enable-demuxer=concat \
--enable-demuxer=mpegts \
--enable-demuxer=mjpeg \
--enable-demuxer=mpegvideo \
--enable-demuxer=rawvideo \
--enable-demuxer=yuv4mpegpipe \
--disable-parsers \
--enable-parser=aac \
--enable-parser=ac3 \
--enable-parser=h264 \
--enable-parser=mjpeg \
--enable-parser=png \
--enable-parser=bmp\
--enable-parser=mpegvideo \
--enable-parser=mpegaudio \
--disable-protocols \
--enable-protocol=file \
--enable-protocol=hls \
--enable-protocol=concat \
--disable-filters \
--disable-filters \
--enable-filter=aresample \
--enable-filter=asetpts \
--enable-filter=setpts \
--enable-filter=ass \
--enable-filter=scale \
--enable-filter=concat \
--enable-filter=atempo \
--enable-filter=movie \
--enable-filter=overlay \
--enable-filter=rotate \
--enable-filter=transpose \
--enable-filter=hflip \
--enable-zlib \
--disable-outdevs \
--disable-doc \
--disable-ffplay \
--disable-ffmpeg \
--disable-ffserver \
--disable-debug \
--disable-ffprobe \
--disable-postproc \
--enable-avdevice \
--disable-symver \
--disable-stripping \
--extra-cflags="-Os -fpic ${OPTIMIZE_CFLAGS}" \
--extra-ldflags="${ADDI_LDFLAGS}" \
${ADDITIONAL_CONFIGURE_FLAG}

make clean
make -j8
make install

# 合并到libffmpeg.so
${TOOLCHAIN}/bin/arm-linux-androideabi-ld \
-rpath-link=${PLATFORM}/usr/lib \
-L${PLATFORM}/usr/lib \
-L${PREFIX}/lib \
-L${X264_LIB} \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
${PREFIX}/libffmpeg.so \
libavcodec/libavcodec.a \
libavfilter/libavfilter.a \
libswresample/libswresample.a \
libavformat/libavformat.a \
libavutil/libavutil.a \
libswscale/libswscale.a \
libavdevice/libavdevice.a \
${X264_LIB}/libx264.a \
-lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
${TOOLCHAIN}/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a

#回到根目录
cd ${ROOT_SOURCE}



