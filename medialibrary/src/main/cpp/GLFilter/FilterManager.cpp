//
// Created by CainHuang on 2019/3/19.
//

#include <mutex>
#include "FilterManager.h"

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

GLFilter *FilterManager::getFilter(const char *name) {
    return nullptr;
}

GLFilter *FilterManager::getFilter(const int id) {
    return nullptr;
}
