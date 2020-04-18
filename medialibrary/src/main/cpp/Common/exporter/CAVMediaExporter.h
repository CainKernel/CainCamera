//
// Created by CainHuang on 2020-04-06.
//

#ifndef CAVMEDIAEXPORTER_H
#define CAVMEDIAEXPORTER_H

/**
 * 媒体导出器
 */
class CAVMediaExporter {
public:
    CAVMediaExporter();

    virtual ~CAVMediaExporter();

    void init();

    void release();

    void notify(int msg, int arg1 = -1, int arg2 = -1);
};


#endif //CAVMEDIAEXPORTER_H
