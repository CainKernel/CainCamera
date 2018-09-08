//
// Created by cain on 2018/9/9.
//

#include <malloc.h>
#include <stdlib.h>
#include <memory.h>
#include "StackBlurFilter.h"
#include "FilterUtils.h"

StackBlurFilter::StackBlurFilter() : radius(2) {

}

StackBlurFilter::~StackBlurFilter() {

}

int StackBlurFilter::process(void *pixels, unsigned int width,
                             unsigned int height) {
    if (width == 0 || height == 0) {
        return -1;
    }

    int wm = width - 1;
    int hm = height - 1;
    int wh = width * height;
    int div = radius + radius + 1;

    int *currentPixels = (int *)pixels;

    short *r = (short *) malloc(wh * sizeof(short));
    short *g = (short *) malloc(wh * sizeof(short));
    short *b = (short *) malloc(wh * sizeof(short));
    int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;

    int *vmin = (int *) malloc(max(width, height) * sizeof(int));

    int divsum = (div + 1) >> 1;
    divsum *= divsum;
    short *dv = (short *) malloc(256 * divsum * sizeof(short));
    for (i = 0; i < 256 * divsum; i++) {
        dv[i] = (short) (i / divsum);
    }

    yw = yi = 0;

    int(*stack)[3] = (int (*)[3]) malloc(div * 3 * sizeof(int));
    int stackpointer;
    int stackstart;
    int *sir;
    int rbs;
    int r1 = radius + 1;
    int routsum, goutsum, boutsum;
    int rinsum, ginsum, binsum;

    for (y = 0; y < height; y++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        for (i = -radius; i <= radius; i++) {
            p = currentPixels[yi + (min(wm, max(i, 0)))];
            sir = stack[i + radius];
            sir[0] = (p & 0xff0000) >> 16;
            sir[1] = (p & 0x00ff00) >> 8;
            sir[2] = (p & 0x0000ff);

            rbs = r1 - abs(i);
            rsum += sir[0] * rbs;
            gsum += sir[1] * rbs;
            bsum += sir[2] * rbs;
            if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
            }
            else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
            }
        }
        stackpointer = radius;

        for (x = 0; x < width; x++) {

            r[yi] = dv[rsum];
            g[yi] = dv[gsum];
            b[yi] = dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            sir = stack[stackstart % div];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];

            if (y == 0) {
                vmin[x] = min(x + radius + 1, wm);
            }
            p = currentPixels[yw + vmin[x]];

            sir[0] = (p & 0xff0000) >> 16;
            sir[1] = (p & 0x00ff00) >> 8;
            sir[2] = (p & 0x0000ff);

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            sir = stack[(stackpointer) % div];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];

            yi++;
        }
        yw += width;
    }
    for (x = 0; x < width; x++) {
        rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
        yp = -radius * width;
        for (i = -radius; i <= radius; i++) {
            yi = max(0, yp) + x;

            sir = stack[i + radius];

            sir[0] = r[yi];
            sir[1] = g[yi];
            sir[2] = b[yi];

            rbs = r1 - abs(i);

            rsum += r[yi] * rbs;
            gsum += g[yi] * rbs;
            bsum += b[yi] * rbs;

            if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
            }
            else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
            }

            if (i < hm) {
                yp += width;
            }
        }
        yi = x;
        stackpointer = radius;
        for (y = 0; y < height; y++) {
            // Preserve alpha channel: ( 0xff000000 & currentPixels[yi] )
            currentPixels[yi] = (0xff000000 & currentPixels[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

            rsum -= routsum;
            gsum -= goutsum;
            bsum -= boutsum;

            stackstart = stackpointer - radius + div;
            sir = stack[stackstart % div];

            routsum -= sir[0];
            goutsum -= sir[1];
            boutsum -= sir[2];

            if (x == 0) {
                vmin[y] = min(y + r1, hm) * width;
            }
            p = x + vmin[y];

            sir[0] = r[p];
            sir[1] = g[p];
            sir[2] = b[p];

            rinsum += sir[0];
            ginsum += sir[1];
            binsum += sir[2];

            rsum += rinsum;
            gsum += ginsum;
            bsum += binsum;

            stackpointer = (stackpointer + 1) % div;
            sir = stack[stackpointer];

            routsum += sir[0];
            goutsum += sir[1];
            boutsum += sir[2];

            rinsum -= sir[0];
            ginsum -= sir[1];
            binsum -= sir[2];

            yi += width;
        }
    }

    free(r);
    free(g);
    free(b);
    free(vmin);
    free(dv);
    free(stack);

    return 0;
}

void StackBlurFilter::setRadius(int radius) {
    this->radius = radius;
}