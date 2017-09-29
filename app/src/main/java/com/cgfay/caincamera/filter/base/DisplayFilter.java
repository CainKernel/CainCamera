package com.cgfay.caincamera.filter.base;

/**
 * 预览的滤镜
 * Created by cain.huang on 2017/9/29.
 */
public class DisplayFilter extends BaseImageFilter {

    public DisplayFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }

    public DisplayFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
}
