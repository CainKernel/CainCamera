package com.cgfay.media.recorder;

/**
 * 速度模式
 * @author CainHuang
 * @date 2019/6/30
 */
public enum SpeedMode {
    MODE_EXTRA_SLOW(1, 1/3f),  // 极慢
    MODE_SLOW(2, 0.5f),         // 慢
    MODE_NORMAL(3, 1.0f),       // 标准
    MODE_FAST(4, 2.0f),         // 快
    MODE_EXTRA_FAST(5, 3.0f);   // 极快

    private int type;
    private float speed;

    SpeedMode(int type, float speed) {
        this.type = type;
        this.speed = speed;
    }

    public int getType() {
        return type;
    }

    public float getSpeed() {
        return speed;
    }

    public static SpeedMode valueOf(int type) {
        if (type == 1) {
            return MODE_EXTRA_SLOW;
        } else if (type == 2) {
            return MODE_SLOW;
        } else if (type == 3) {
            return MODE_NORMAL;
        } else if (type == 4) {
            return MODE_FAST;
        } else if (type == 5) {
            return MODE_EXTRA_FAST;
        } else {
            return MODE_NORMAL;
        }
    }

    public static SpeedMode valueOf(float speed) {
        if (speed == 1/3f) {
            return MODE_EXTRA_SLOW;
        } else if (speed == 1/2f) {
            return MODE_SLOW;
        } else if (speed == 1.0f) {
            return MODE_NORMAL;
        } else if (speed == 2.0f) {
            return MODE_FAST;
        } else if (speed == 3.0f) {
            return MODE_EXTRA_FAST;
        } else {
            return MODE_NORMAL;
        }
    }
}
