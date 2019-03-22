//
// Created by CainHuang on 2019/3/19.
//

#include <cstring>
#include "FilterManager.h"
#include "Filter.h"

FilterManager *FilterManager::instance = 0;
std::mutex FilterManager::mutex;

FilterManager::FilterManager() {

}

FilterManager::~FilterManager() {

}

FilterManager *FilterManager::getInstance() {
    if (!instance) {
        std::unique_lock<std::mutex> lock(mutex);
        if (!instance) {
            instance = new (std::nothrow) FilterManager();
        }
    }
    return instance;
}

void FilterManager::destroy() {
    if (instance) {
        std::unique_lock<std::mutex> lock(mutex);
        if (instance) {
            delete instance;
            instance = nullptr;
        }
    }
}

GLFilter *FilterManager::getFilter(FilterInfo *filterInfo) {
    if (filterInfo->id != -1) {
        return getFilter(filterInfo->id);
    } else if (filterInfo->name != nullptr) {
        return getFilter(filterInfo->name);
    }
    return nullptr;
}

GLFilter *FilterManager::getFilter(const char *name) {

    // 分屏特效
    if (!strcmp("模糊分屏", name)) {
        return new GLFrameBlurFilter();
    }
    if (!strcmp("黑白三屏", name)) {
        return new GLFrameBlackWhiteThreeFilter();
    }
    if (!strcmp("两屏", name)) {
        return new GLFrameTwoFilter();
    }
    if (!strcmp("三屏", name)) {
        return new GLFrameThreeFilter();
    }
    if (!strcmp("四屏", name)) {
        return new GLFrameFourFilter();
    }
    if (!strcmp("六屏", name)) {
        return new GLFrameSixFilter();
    }
    if (!strcmp("九屏", name)){
        return new GLFrameNineFilter();
    }
    return nullptr;
}

GLFilter *FilterManager::getFilter(const int id) {
    switch (id) {

        // 分屏特效
        case 0x200: { // 模糊分屏特效
            return new GLFrameBlurFilter();
        }

        case 0x201:{ // 黑白三屏特效
            return new GLFrameBlackWhiteThreeFilter();
        }
        case 0x202: { // 两屏特效
            return new GLFrameTwoFilter();
        }
        case 0x203: { // 三屏特效
            return new GLFrameThreeFilter();
        }
        case 0x204: { // 四屏特效
            return new GLFrameFourFilter();
        }
        case 0x205: { // 六屏特效
            return new GLFrameSixFilter();
        }
        case 0x206: { // 九屏特效
            return new GLFrameNineFilter();
        }
    }
    return nullptr;
}
