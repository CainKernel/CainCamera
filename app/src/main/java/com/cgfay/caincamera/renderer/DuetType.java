package com.cgfay.caincamera.renderer;

/**
 * 同框类型
 */
public enum DuetType {
    DUET_TYPE_NONE(0),          // 没有同框
    DUET_TYPE_LEFT_RIGHT(1),    // 左右同框
    DUET_TYPE_UP_DOWN(2),       // 上下同框
    DUET_TYPE_BIG_SMALL(3)      // 大小同框
    ;

    private int value;

    DuetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DuetType valueOf(int value) {
        switch (value) {
            case 1:
                return DUET_TYPE_LEFT_RIGHT;
            case 2:
                return DUET_TYPE_UP_DOWN;
            case 3:
                return DUET_TYPE_BIG_SMALL;
            default:
                return DUET_TYPE_NONE;
        }
    }
}
