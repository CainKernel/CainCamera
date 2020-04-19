//
// Created by CainHuang on 2020-04-06.
//

#ifndef CAVMEDIAEXPORTER_H
#define CAVMEDIAEXPORTER_H

#include <memory>
#include <video/GLVideoRender.h>
#include "AudioExporter.h"
#include "VideoExporter.h"

/**
 * 媒体导出器
 */
class CAVMediaExporter {
public:
    CAVMediaExporter();

    virtual ~CAVMediaExporter();

    void init();

    void release();

    void setDataSource(const char *path);

    void setOutputPath(const char *path);

    void setSpeed(float speed);

    void setRange(float start, float end);

    void prepare();

    void cancel();

    void notify(int msg, int arg1 = -1, int arg2 = -1);

private:
    std::shared_ptr<VideoExporter> mVideoExporter;
};


#endif //CAVMEDIAEXPORTER_H
