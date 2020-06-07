//
// Created by CainHuang on 2020/5/23.
//

#include <math.h>
#include "Geometry.h"

const CGPoint CGPointZero = { 0, 0 };

const CGSize CGSizeZero = {0, 0};

const CGRect CGRectZero = { CGPointZero, CGSizeZero};

/* Make a point from `(x, y)'. */
CGPoint CGPointMake(CGFloat x, CGFloat y) {
    CGPoint point;
    point.x = x;
    point.y = y;
    return point;
}

/* Make a size from `(width, height)'. */
CGSize CGSizeMake(CGFloat width, CGFloat height) {
    CGSize size;
    size.width = width;
    size.height = height;
    return size;
}

/* Make a vector from `(dx, dy)'. */
CGVector CGVectorMake(CGFloat dx, CGFloat dy) {
    CGVector vector;
    vector.dx = dx;
    vector.dy = dy;
    return vector;
}

/* Make a rect from `(x, y; width, height)'. */
CGRect CGRectMake(CGFloat x, CGFloat y, CGFloat width, CGFloat height) {
    CGRect rect;
    rect.origin.x = x;
    rect.origin.y = y;
    rect.size.width = width;
    rect.size.height = height;
    return rect;
}

/* Return the leftmost x-value of `rect'. */
CGFloat CGRectGetMinX(CGRect rect) {
    return rect.origin.x;
}

/* Return the midpoint x-value of `rect'. */
CGFloat CGRectGetMidX(CGRect rect) {
    return (rect.origin.x + rect.size.width) / 2;
}

/* Return the rightmost x-value of `rect'. */
CGFloat CGRectGetMaxX(CGRect rect) {
    return (rect.origin.x + rect.size.width);
}

/* Return the bottommost y-value of `rect'. */
CGFloat CGRectGetMinY(CGRect rect) {
    return rect.origin.y;
}

/* Return the midpoint y-value of `rect'. */
CGFloat CGRectGetMidY(CGRect rect) {
    return (rect.origin.y + rect.size.height) / 2;
}

/* Return the topmost y-value of `rect'. */
CGFloat CGRectGetMaxY(CGRect rect) {
    return (rect.origin.y + rect.size.height);
}

/* Return the width of `rect'. */
CGFloat CGRectGetWidth(CGRect rect) {
    return rect.size.width;
}

/* Return the height of `rect'. */
CGFloat CGRectGetHeight(CGRect rect) {
    return rect.size.height;
}

/* Return true if `point1' and `point2' are the same, false otherwise. */
bool CGPointEqualToPoint(CGPoint point1, CGPoint point2) {
    return (point1.x == point2.x) && (point1.y == point2.y);
}

/* Return true if `size1' and `size2' are the same, false otherwise. */
bool CGSizeEqualToSize(CGSize size1, CGSize size2) {
    return (size1.width == size2.width) && (size1.height == size2.height);
}

/* Return true if `rect1' and `rect2' are the same, false otherwise. */
bool CGRectEqualToRect(CGRect rect1, CGRect rect2) {
    return CGPointEqualToPoint(rect1.origin, rect2.origin) && CGSizeEqualToSize(rect1.size, rect2.size);
}

/* Return true if `rect' is empty (that is, if it has zero width or height),
   false otherwise. A null rect is defined to be empty. */
bool CGRectIsEmpty(CGRect rect) {
    return CGSizeEqualToSize(rect.size, CGSizeZero);
}

/* Inset `rect' by `(dx, dy)' -- i.e., offset its origin by `(dx, dy)', and
   decrease its size by `(2*dx, 2*dy)'. */
CGRect CGRectInset(CGRect rect, CGFloat dx, CGFloat dy) {
    return CGRectMake(rect.origin.x + dx, rect.origin.y + dy, rect.size.width - dx, rect.size.height - dy);
}

/* Return the union of `r1' and `r2'. */
CGRect CGRectUnion(CGRect r1, CGRect r2) {
    // 获取最小的x，y坐标
    CGFloat left = fmin(CGRectGetMinX(r1), CGRectGetMinX(r2));
    CGFloat top = fmin(CGRectGetMinY(r1), CGRectGetMinY(r2));
    // 获取最大x，y坐标
    CGFloat right = fmax(CGRectGetMaxX(r1), CGRectGetMaxX(r2));
    CGFloat bottom = fmax(CGRectGetMaxY(r1), CGRectGetMaxY(r2));
    return CGRectMake(left, top, right - left, bottom - top);
}

/* Return the intersection of `r1' and `r2'. This may return a null rect. */
CGRect CGRectIntersection(CGRect r1, CGRect r2) {
    CGFloat left = fmax(CGRectGetMinX(r1), CGRectGetMinX(r2));
    CGFloat top = fmax(CGRectGetMinY(r1), CGRectGetMinY(r2));

    CGFloat right = fmin(CGRectGetMaxX(r1), CGRectGetMaxX(r2));
    CGFloat bottom = fmin(CGRectGetMaxY(r1), CGRectGetMaxY(r2));

    return CGRectMake(left, top, right - left, bottom - top);
}

/* Offset `rect' by `(dx, dy)'. */
CGRect CGRectOffset(CGRect rect, CGFloat dx, CGFloat dy) {
    return CGRectMake(rect.origin.x + dx, rect.origin.y + dy, rect.size.width, rect.size.height);
}

/* Return true if `point' is contained in `rect', false otherwise. */
bool CGRectContainsPoint(CGRect rect, CGPoint point) {
    return (CGRectGetMinX(rect) <= point.x) && (CGRectGetMinY(rect) <= point.y)
            && (CGRectGetMaxX(rect) >= point.x) && (CGRectGetMaxY(rect) >= point.y);
}

/* Return true if `rect2' is contained in `rect1', false otherwise. `rect2'
   is contained in `rect1' if the union of `rect1' and `rect2' is equal to
   `rect1'. */
bool CGRectContainsRect(CGRect rect1, CGRect rect2) {
    return (CGRectGetMinX(rect1) <= CGRectGetMinX(rect2))
            && (CGRectGetMinY(rect1) <= CGRectGetMinY(rect2))
            && (CGRectGetMaxX(rect1) >= CGRectGetMaxX(rect2))
            && (CGRectGetMaxY(rect1) >= CGRectGetMaxY(rect2));
}

/* Return true if `rect1' intersects `rect2', false otherwise. `rect1'
   intersects `rect2' if the intersection of `rect1' and `rect2' is not the
   null rect. */
bool CGRectIntersectsRect(CGRect rect1, CGRect rect2) {
    CGRect rect = CGRectIntersection(rect1, rect2);
    return (CGRectGetWidth(rect) > 0) && (CGRectGetHeight(rect) > 0);
}