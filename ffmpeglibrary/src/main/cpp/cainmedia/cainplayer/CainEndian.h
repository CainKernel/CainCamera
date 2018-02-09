//
// Created by Administrator on 2018/2/9.
//

#ifndef CAINCAMERA_CAINENDIAN_H
#define CAINCAMERA_CAINENDIAN_H

#define CAIN_LIL_ENDIAN  1234
#define CAIN_BIG_ENDIAN  4321

#ifndef BYTEORDER
#ifdef __linux__
#include <endian.h>
#define CAIN_BYTEORDER  __BYTE_ORDER
#else /* __linux __ */
#if defined(__hppa__) || \
    defined(__m68k__) || defined(mc68000) || defined(_M_M68K) || \
    (defined(__MIPS__) && defined(__MISPEB__)) || \
    defined(__ppc__) || defined(__POWERPC__) || defined(_M_PPC) || \
    defined(__sparc__)
#define CAIN_BYTEORDER   CAIN_BIG_ENDIAN
#else
#define CAIN_BYTEORDER   CAIN_LIL_ENDIAN
#endif
#endif /* __linux __ */

#endif /* !CAIN_BYTEORDER */

#endif //CAINCAMERA_CAINENDIAN_H
