//
// Created by CainHuang on 2019/3/19.
//

#ifndef FILTERMANAGER_H
#define FILTERMANAGER_H

#include <filter/GLFilter.h>

class FilterManager {
public:
    FilterManager();

    virtual ~FilterManager();

    static FilterManager *getInstance();

    // 根据名称获取滤镜
    GLFilter *getFilter(const char *name);

    // 根据id来获取滤镜
    GLFilter *getFilter(const int id);

private:
    static FilterManager *instance;
    static std::mutex mutex;
};


#endif //FILTERMANAGER_H
