//
// Created by Administrator on 2018/3/6.
//

#ifndef CAINCAMERA_COLOR_H
#define CAINCAMERA_COLOR_H

#include <math.h>
#include "ImageUtils.h"

// RGB 转 HIS，采用几何推导法
// http://blog.csdn.net/yangleo1987/article/details/53171623

class Color {

public:
    Color() : color(-1), h(-1), s(-1), i(-1) {}

    Color(int color) : color(color), h(-1), s(-1), i(-1) {}

    Color(int r, int g, int b) : h(-1), s(-1), i(-1) {
        color = rgb2Color(r, g, b);
    }

    int alpha() {
        return (color & 0xFF000000) >> 24;
    }

    int red() {
        return  (color & 0x00FF0000) >> 16;
    }

    int green() {
        return (color & 0x0000FF00) >> 8;
    }

    int blue() {
        return (color & 0x000000FF);
    }

    int grayScale() {
        return (red() + green() + blue()) / 3;
    }

    int getColor() {
        return color;
    }


    double H() {
        if (h == -1) {
            double r = red() / 255.0;
            double g = green() / 255.0;
            double b = blue() / 255.0;

            double theta = acos(
                    ((r - g) + (r - b)) * 0.5 /
                    pow((pow((r - g), 2) + (r - b) * (g - b)), 0.5)
            );
            if (b <= g) {
                h = theta;
            } else {
                h = 360 - theta;
            }
        }
        return h;
    }

    double S() {
        if (s == -1) {
            int minColor = 0;
            minColor = red() < green() ? red() : green();
            minColor = minColor < blue() ? minColor : blue();

            double r = red() / 255.0;
            double g = green() / 255.0;
            double b = blue() / 255.0;
            minColor = minColor / 255.0;

            s = 1 - (3.0 * minColor) / (r + g + b);
        }
        return s;
    }

    double I() {
        if (i == -1) {
            double r = red() / 255.0;
            double g = green() / 255.0;
            double b = blue() / 255.0;

            i = (r + g + b) / 3.0;
        }
        return i;
    }


private:
    int color;
    double h;
    double s;
    double i;
};


#endif //CAINCAMERA_COLOR_H
