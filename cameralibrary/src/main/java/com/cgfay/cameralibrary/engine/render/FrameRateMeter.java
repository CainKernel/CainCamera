package com.cgfay.cameralibrary.engine.render;

/**
 * FPS计算类
 * Created by cain.huang on 2017/12/19.
 */

public final class FrameRateMeter {

    private static final long TIMETRAVEL = 1;
    private static final long TIMETRAVEL_MS = TIMETRAVEL * 1000;
    private static final long TIMETRAVEL_MAX_DIVIDE = 2 * TIMETRAVEL_MS;

    private int mTimes;
    private float mCurrentFps;
    private long mUpdateTime;

    public FrameRateMeter() {
        mTimes = 0;
        mCurrentFps = 0;
        mUpdateTime = 0;
    }

    /**
     * 计算绘制帧数据
     */
    public void drawFrameCount() {
        long currentTime = System.currentTimeMillis();
        if (mUpdateTime == 0) {
            mUpdateTime = currentTime;
        }
        if ((currentTime - mUpdateTime) > TIMETRAVEL_MS) {
            mCurrentFps = ((float) mTimes / (currentTime - mUpdateTime)) * 1000.0f;
            mUpdateTime = currentTime;
            mTimes = 0;
        }
        mTimes++;
    }

    /**
     * 获取FPS
     * @return
     */
    public float getFPS() {
        if ((System.currentTimeMillis() - mUpdateTime) > TIMETRAVEL_MAX_DIVIDE) {
            return 0;
        } else {
            return mCurrentFps;
        }
    }
}
