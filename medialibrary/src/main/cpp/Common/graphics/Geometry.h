//
// Created by CainHuang on 2020/5/23.
//

#ifndef GEOMETRY_H
#define GEOMETRY_H

#ifdef __cplusplus
extern "C" {
#endif

typedef float CGFloat;

typedef struct CGPoint {
    CGFloat x;
    CGFloat y;
} CGPoint;

typedef struct CGSize {
    CGFloat width;
    CGFloat height;
} CGSize;

typedef struct CGVector {
    CGFloat dx;
    CGFloat dy;
} CGVector;

typedef struct CGRect {
    CGPoint origin;
    CGSize size;
} CGRect;

extern const CGPoint CGPointZero;

extern const CGSize CGSizeZero;

extern const CGRect CGRectZero;

/* Make a point from `(x, y)'. */
CGPoint CGPointMake(CGFloat x, CGFloat y);

/* Make a size from `(width, height)'. */
CGSize CGSizeMake(CGFloat width, CGFloat height);

/* Make a vector from `(dx, dy)'. */
CGVector CGVectorMake(CGFloat dx, CGFloat dy);

/* Make a rect from `(x, y; width, height)'. */
CGRect CGRectMake(CGFloat x, CGFloat y, CGFloat width, CGFloat height);

/* Return the leftmost x-value of `rect'. */
CGFloat CGRectGetMinX(CGRect rect);

/* Return the midpoint x-value of `rect'. */
CGFloat CGRectGetMidX(CGRect rect);

/* Return the rightmost x-value of `rect'. */
CGFloat CGRectGetMaxX(CGRect rect);

/* Return the bottommost y-value of `rect'. */
CGFloat CGRectGetMinY(CGRect rect);

/* Return the midpoint y-value of `rect'. */
CGFloat CGRectGetMidY(CGRect rect);

/* Return the topmost y-value of `rect'. */
CGFloat CGRectGetMaxY(CGRect rect);

/* Return the width of `rect'. */
CGFloat CGRectGetWidth(CGRect rect);

/* Return the height of `rect'. */
CGFloat CGRectGetHeight(CGRect rect);

/* Return true if `point1' and `point2' are the same, false otherwise. */
bool CGPointEqualToPoint(CGPoint point1, CGPoint point2);

/* Return true if `size1' and `size2' are the same, false otherwise. */
bool CGSizeEqualToSize(CGSize size1, CGSize size2);

/* Return true if `rect1' and `rect2' are the same, false otherwise. */
bool CGRectEqualToRect(CGRect rect1, CGRect rect2);

/* Return true if `rect' is empty (that is, if it has zero width or height),
   false otherwise. A null rect is defined to be empty. */
bool CGRectIsEmpty(CGRect rect);

/* Inset `rect' by `(dx, dy)' -- i.e., offset its origin by `(dx, dy)', and
   decrease its size by `(2*dx, 2*dy)'. */
CGRect CGRectInset(CGRect rect, CGFloat dx, CGFloat dy);

/* Return the union of `r1' and `r2'. */
CGRect CGRectUnion(CGRect r1, CGRect r2);

/* Return the intersection of `r1' and `r2'. This may return a null rect. */
CGRect CGRectIntersection(CGRect r1, CGRect r2);

/* Offset `rect' by `(dx, dy)'. */
CGRect CGRectOffset(CGRect rect, CGFloat dx, CGFloat dy);

/* Return true if `point' is contained in `rect', false otherwise. */
bool CGRectContainsPoint(CGRect rect, CGPoint point);

/* Return true if `rect2' is contained in `rect1', false otherwise. `rect2'
   is contained in `rect1' if the union of `rect1' and `rect2' is equal to
   `rect1'. */
bool CGRectContainsRect(CGRect rect1, CGRect rect2);

/* Return true if `rect1' intersects `rect2', false otherwise. `rect1'
   intersects `rect2' if the intersection of `rect1' and `rect2' is not the
   null rect. */
bool CGRectIntersectsRect(CGRect rect1, CGRect rect2);

#ifdef __cplusplus
}
#endif

#endif //GEOMETRY_H
