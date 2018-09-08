//
// Created by cain on 2018/9/8.
//


#include "FilterUtils.h"
#include <stdlib.h>
#include <memory.h>

/**
 * 初始化整形数组
 * @param size          大小
 * @param arrayPointer  数组指针
 * @return
 */
int newUnsignedIntArray(unsigned int** arrayPointer, unsigned int size) {
    unsigned int numBytes = size * sizeof(unsigned int);
    *arrayPointer = (unsigned int*) malloc(numBytes);
    if (arrayPointer == NULL) {
        return -1;
    }

    memset(*arrayPointer, 0, numBytes);
    return 0;
}

/**
 * 释放无符号整形数组
 * @param arrayPointer
 */
void freeUnsignedIntArray(unsigned int** arrayPointer) {
    if (*arrayPointer != NULL) {
        free(*arrayPointer);
        *arrayPointer = NULL;
    }
}