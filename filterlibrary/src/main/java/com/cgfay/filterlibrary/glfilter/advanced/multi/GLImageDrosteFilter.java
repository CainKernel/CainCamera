package com.cgfay.filterlibrary.glfilter.advanced.multi;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 德罗斯特效应滤镜
 */
public class GLImageDrosteFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform float repeat; // 画面重复的次数\n" +
            "\n" +
            "void main() {\n" +
            "    vec2 uv = textureCoordinate;\n" +
            "    // 反向UV坐标\n" +
            "    vec2 invertedUV = 1.0 - uv;\n" +
            "    // 计算重复次数之后的uv值以及偏移值\n" +
            "    vec2 fiter = floor(uv * repeat * 2.0);\n" +
            "    vec2 riter = floor(invertedUV * repeat * 2.0);\n" +
            "    vec2 iter = min(fiter, riter);\n" +
            "    float minOffset = min(iter.x, iter.y);\n" +
            "    // 偏移值\n" +
            "    vec2 offset = (vec2(0.5, 0.5) / repeat) * minOffset;\n" +
            "    // 当前实际的偏移值\n" +
            "    vec2 currenOffset = 1.0 / (vec2(1.0, 1.0) - offset * 2.0);\n" +
            "    // 计算出当前的实际UV坐标\n" +
            "    vec2 currentUV = (uv - offset) * currenOffset;\n" +
            "    \n" +
            "    gl_FragColor = texture2D(inputTexture, fract(currentUV));\n" +
            "}";

    private int mRepeatHandle;
    private float repeat;

    public GLImageDrosteFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageDrosteFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mRepeatHandle = GLES30.glGetUniformLocation(mProgramHandle, "repeat");
            setRepeat(4);
        }
    }

    /**
     * 设置重复次数
     * @param repeat
     */
    public void setRepeat(int repeat) {
        this.repeat = repeat;
        setFloat(mRepeatHandle, repeat);
    }
}
