package com.cgfay.caincamera.filter.beauty;

import android.opengl.GLES30;

import com.cgfay.caincamera.filter.base.BaseImageFilter;

/**
 * 处理画面白皙还是红润
 * Created by cain on 2017/7/30.
 */
public class WhitenOrReddenFilter extends BaseImageFilter {

    private static final String FRAGMENT_SHADER =
            "precision highp float;\n"+
            "varying mediump vec2 textureCoordinate;\n"+
            "uniform sampler2D inputTexture;\n"+
            "uniform float redden;\n"+
            "uniform float whitening;\n"+
            "uniform float pinking;\n"+
            "void main () {\n"+
            "    lowp vec4 softColor;\n"+
            "    softColor.xyz = texture2D (inputTexture, textureCoordinate).xyz;\n"+
            "    softColor.w = 1.0;\n"+
            "    if ((whitening != 0.0)) {\n"+
            "        softColor.xyz = clamp (mix (softColor.xyz, (vec3(1.0, 1.0, 1.0) -\n"+
            "        ((vec3(1.0, 1.0, 1.0) - softColor.xyz) * (vec3(1.0, 1.0, 1.0) - softColor.xyz))),\n"+
            "        (whitening * dot (vec3(0.299, 0.587, 0.114), softColor.xyz))), 0.0, 1.0);\n"+
            "    };\n"+
            "\n"+
            "    if ((redden != 0.0)) {\n"+
            "        lowp vec3 tmpvar_2;\n"+
            "        tmpvar_2 = mix (softColor.xyz, (vec3(1.0, 1.0, 1.0) -\n"+
            "            ((vec3(1.0, 1.0, 1.0) - softColor.xyz) * (vec3(1.0, 1.0, 1.0) - softColor.xyz))),\n"+
            "        (0.2 * redden));\n"+
            "\n"+
            "        lowp vec3 tmpvar_3 = mix (vec3(dot (tmpvar_2, vec3(0.299, 0.587, 0.114))),\n"+
            "            tmpvar_2, (1.0 + redden));\n"+
            "        lowp vec3 tmpvar_4 = mix (tmpvar_3.xyy, tmpvar_3, 0.5);\n"+
            "        lowp float tmpvar_5 = dot (tmpvar_4, vec3(0.299, 0.587, 0.114));\n"+
            "\n"+
            "        softColor.xyz = clamp (mix (tmpvar_3, mix (tmpvar_4, sqrt(tmpvar_4), tmpvar_5),\n"+
            "                (redden * tmpvar_5)), 0.0, 1.0);\n"+
            "    };\n"+
            "    if ((pinking != 0.0)) {\n"+
            "        lowp vec3 tmpvar_6;\n"+
            "        tmpvar_6.x = ((sqrt(softColor.x) * 0.41) + (0.59 * softColor.x));\n"+
            "        tmpvar_6.y = ((sqrt(softColor.y) * 0.568) + (0.432 * softColor.y));\n"+
            "        tmpvar_6.z = ((sqrt(softColor.z) * 0.7640001) + (0.2359999 * softColor.z));\n"+
            "        softColor.xyz = clamp (mix (softColor.xyz, tmpvar_6,\n"+
            "            (pinking * dot (vec3(0.299, 0.587, 0.114), softColor.xyz))), 0.0, 1.0);\n"+
            "    };\n"+
            "    gl_FragColor = softColor;\n"+
            "}";

    private int mReddenLoc;
    private int mWhitenLoc;
    private int mPinkingLoc;

    public WhitenOrReddenFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public WhitenOrReddenFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mReddenLoc = GLES30.glGetUniformLocation(mProgramHandle, "redden");
        mWhitenLoc = GLES30.glGetUniformLocation(mProgramHandle, "whitening");
        mPinkingLoc = GLES30.glGetUniformLocation(mProgramHandle, "pinking");
        setReddenValue(1.0f);
        setWhitenValue(1.0f);
        setPinkingValue(1.0f);
    }

    /**
     * 设置红色值
     * @param reddenValue
     */
    public void setReddenValue(float reddenValue) {
        setFloat(mReddenLoc, reddenValue);
    }

    /**
     * 设置白色值
     * @param whitenValue
     */
    public void setWhitenValue(float whitenValue) {
        setFloat(mWhitenLoc, whitenValue);
    }

    /**
     * 设置粉色值
     * @param pinkingValue
     */
    public void setPinkingValue(float pinkingValue) {
        setFloat(mPinkingLoc, pinkingValue);
    }
}
