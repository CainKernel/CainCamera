package com.cgfay.video.bean;

/**
 * 速度枚举
 */
public enum VideoSpeed {

    SPEED_L1(-1, 0.50f),    // L1 : 慢 (倍速: 0.75)
    SPEED_L2(0, 1.0f),      // L2 : 标准 (倍速: 1.0, 默认速度)
    SPEED_L3(1, 2.0f);      // L3 : 较快 (倍速: 2.0)

    private int type;
    private float speed;

    public int getType() {
        return type;
    }

    public float getSpeed() {
        return speed;
    }

    VideoSpeed(int type, float speed) {
        this.type = type;
        this.speed = speed;
    }
}
