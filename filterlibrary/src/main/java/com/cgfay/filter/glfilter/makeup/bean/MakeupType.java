package com.cgfay.filter.glfilter.makeup.bean;

/**
 * 彩妆类型
 */
public enum MakeupType {

    NONE("none", -1),           // 无彩妆
    SHADOW("shadow", 0),        // 阴影
    PUPIL("pupil", 1),          // 瞳孔
    EYESHADOW("eyeshadow", 2),  // 眼影
    EYELINER("eyeliner", 3),    // 眼线
    EYELASH("eyelash", 4),      // 睫毛
    EYELID("eyelid", 5),        // 眼皮
    EYEBROW("eyebrow", 6),      // 眉毛
    BLUSH("blush", 7),          // 腮红
    LIPSTICK("lipstick", 8);    // 口红/唇彩

    private String name;
    private int index;

    MakeupType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 根据名称取得类型
     * @param name
     * @return
     */
    public static MakeupType getType(String name) {
        switch (name) {
            case "shadow":
                return SHADOW;

            case "pupil":
                return PUPIL;

            case "eyeshadow":
                return EYESHADOW;

            case "eyeliner":
                return EYELINER;

            case "eyelash":
                return EYELASH;

            case "eyelid":
                return EYELID;

            case "eyebrow":
                return EYEBROW;

            case "blush":
                return BLUSH;

            case "lipstick":
                return LIPSTICK;

            default:
                return NONE;
        }
    }

    /**
     * 根据索引取得类型
     * @param index
     * @return
     */
    public static MakeupType getType(int index) {
        switch (index) {
            case 0:
                return SHADOW;

            case 1:
                return PUPIL;

            case 2:
                return EYESHADOW;

            case 3:
                return EYELINER;

            case 4:
                return EYELASH;

            case 5:
                return EYELID;

            case 6:
                return EYEBROW;

            case 7:
                return BLUSH;

            case 8:
                return LIPSTICK;

            default:
                return NONE;
        }
    }

    /**
     * 彩妆索引
     */
    public static class MakeupIndex {
        public static final int LipstickIndex = 0;  // 口红/唇彩
        public static final int BlushIndex = 1;     // 腮红
        public static final int ShadowIndex = 2;    // 阴影
        public static final int EyebrowIndex = 3;   // 眉毛
        public static final int EyeshadowIndex = 4; // 眼影
        public static final int EyelinerIndex = 5;  // 眼线
        public static final int EyelashIndex = 6;   // 睫毛
        public static final int EyelidIndex = 7;    // 眼皮
        public static final int PupilIndex = 8;     // 瞳孔
        public static final int MakeupSize = 9;     // 支持的彩妆数量
    }

}
