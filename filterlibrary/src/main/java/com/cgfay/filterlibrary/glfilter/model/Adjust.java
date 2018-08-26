package com.cgfay.filterlibrary.glfilter.model;

public class Adjust {
    // 亮度值 -1.0f ~ 1.0f
    public float brightness;
    // 对比度 0.0 ~ 4.0f
    public float contrast;
    // 曝光 -10.0f ~ 10.0f
    public float exposure;
    // 色调 0 ~ 360
    public float hue;
    // 饱和度 0 ~ 2.0f
    public float saturation;
    // 锐度 -4.0f ~ 4.0f
    public float sharpness;

    public Adjust() {
        brightness = 0.0f;
        contrast = 1.0f;
        exposure = 0.0f;
        hue = 0.0f;
        saturation = 1.0f;
        sharpness = 0.0f;
    }
}
