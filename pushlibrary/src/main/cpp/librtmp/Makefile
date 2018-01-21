VERSION=v2.3

prefix=/usr/local

CC=$(CROSS_COMPILE)gcc
LD=$(CROSS_COMPILE)ld
AR=$(CROSS_COMPILE)ar

SYS=posix
CRYPTO=OPENSSL
#CRYPTO=GNUTLS
DEF_POLARSSL=-DUSE_POLARSSL
DEF_OPENSSL=-DUSE_OPENSSL
DEF_GNUTLS=-DUSE_GNUTLS
DEF_=-DNO_CRYPTO
REQ_GNUTLS=gnutls
REQ_OPENSSL=libssl,libcrypto
LIBZ=-lz
LIBS_posix=
LIBS_mingw=-lws2_32 -lwinmm -lgdi32
LIB_GNUTLS=-lgnutls -lgcrypt $(LIBZ)
LIB_OPENSSL=-lssl -lcrypto $(LIBZ)
LIB_POLARSSL=-lpolarssl $(LIBZ)
CRYPTO_LIB=$(LIB_$(CRYPTO)) $(LIBS_$(SYS))
CRYPTO_REQ=$(REQ_$(CRYPTO))
CRYPTO_DEF=$(DEF_$(CRYPTO))

SO_posix=so.0
SO_mingw=dll
SO_EXT=$(SO_$(SYS))

SHARED=yes
SODEF_yes=-fPIC
SOLIB_yes=librtmp.$(SO_EXT)
SOINST_yes=install_$(SO_EXT)
SO_DEF=$(SODEF_$(SHARED))
SO_LIB=$(SOLIB_$(SHARED))
SO_INST=$(SOINST_$(SHARED))

DEF=-DRTMPDUMP_VERSION=\"$(VERSION)\" $(CRYPTO_DEF) $(XDEF)
OPT=-O2
CFLAGS=-Wall $(XCFLAGS) $(INC) $(DEF) $(OPT) $(SO_DEF)

incdir=$(prefix)/include/librtmp
bindir=$(prefix)/bin
libdir=$(prefix)/lib
mandir=$(prefix)/man
BINDIR=$(DESTDIR)$(bindir)
INCDIR=$(DESTDIR)$(incdir)
LIBDIR=$(DESTDIR)$(libdir)
MANDIR=$(DESTDIR)$(mandir)

OBJS=rtmp.o log.o amf.o hashswf.o parseurl.o

all:	librtmp.a $(SO_LIB)

clean:
	rm -f *.o *.a *.so *.$(SO_EXT)

librtmp.a: $(OBJS)
	$(AR) rs $@ $?

librtmp.$(SO_EXT): $(OBJS)
	$(CC) -shared -Wl,-soname,$@ $(LDFLAGS) -o $@ $^ $> $(CRYPTO_LIB)
	ln -sf $@ librtmp.so

log.o: log.c log.h Makefile
rtmp.o: rtmp.c rtmp.h rtmp_sys.h handshake.h dh.h log.h amf.h Makefile
amf.o: amf.c amf.h bytes.h log.h Makefile
hashswf.o: hashswf.c http.h rtmp.h rtmp_sys.h Makefile
parseurl.o: parseurl.c rtmp.h rtmp_sys.h log.h Makefile

librtmp.pc: librtmp.pc.in Makefile
	sed -e "s;@prefix@;$(prefix);" -e "s;@VERSION@;$(VERSION);" \
		-e "s;@CRYPTO_REQ@;$(CRYPTO_REQ);" librtmp.pc.in > $@

install:	install_base $(SO_INST)

install_base:	librtmp.a librtmp.pc
	-mkdir -p $(INCDIR) $(LIBDIR)/pkgconfig $(MANDIR)/man3
	cp amf.h http.h log.h rtmp.h $(INCDIR)
	cp librtmp.a $(LIBDIR)
	cp librtmp.pc $(LIBDIR)/pkgconfig
	cp librtmp.3 $(MANDIR)/man3

install_so.0:	librtmp.so.0
	cp librtmp.so.0 $(LIBDIR)
	cd $(LIBDIR); ln -sf librtmp.so.0 librtmp.so

install_dll:	librtmp.dll
	cp librtmp.dll $(BINDIR)
