package com.cgfay.cainfilter.glfilter.base;

/**
 * 预览的滤镜
 * Created by cain.huang on 2017/9/29.
 */
public class GLDisplayFilter extends GLImageFilter {

    public GLDisplayFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }

    public GLDisplayFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
}
