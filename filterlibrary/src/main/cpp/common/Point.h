//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_POINT_H
#define CAINCAMERA_POINT_H


class Point {

public:
    Point();

    Point(int x, int y);

    virtual ~Point();

public:
    int x;
    int y;
};


#endif //CAINCAMERA_POINT_H
