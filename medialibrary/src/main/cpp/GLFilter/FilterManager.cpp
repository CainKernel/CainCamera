//
// Created by CainHuang on 2019/3/19.
//

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

GLFilter *FilterManager::getFilter(FilterInfo *filterInfo) {
    if (filterInfo->id != -1) {
        return getFilter(filterInfo->id);
    } else if (filterInfo->name != nullptr) {
        return getFilter(filterInfo->name);
    }
    return nullptr;
}

GLFilter *FilterManager::getFilter(const char *name) {
    return nullptr;
}

GLFilter *FilterManager::getFilter(const int id) {
    switch (id) {

        case 0x200: { // TODO 模糊分屏特效，滤镜暂未实现
            return nullptr;
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
