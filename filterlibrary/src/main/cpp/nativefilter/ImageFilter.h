//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_IMAGEFILTER_H
#define CAINCAMERA_IMAGEFILTER_H

/**
 * 滤镜基类
 */
class ImageFilter {

public:
    ImageFilter();
    virtual ~ImageFilter();

public:
    virtual int process(void *pixels, unsigned int width, unsigned int height);

protected:
    /**
     * 限定最大最小值
     * @param t
     * @param min
     * @param max
     * @return
     */
    int clampValue(const int& t, const int& min, const int& max) {
        if (t < max) {
            return ((t > min) ? t : min) ;
        }
        return max;
    }
};

#endif //CAINCAMERA_IMAGEFILTER_H
