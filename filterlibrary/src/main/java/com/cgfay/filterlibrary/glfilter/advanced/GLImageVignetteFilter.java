package com.cgfay.filterlibrary.glfilter.advanced;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 暗角(虚光照)滤镜
 */
public class GLImageVignetteFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER =
            " uniform sampler2D inputTexture;\n" +
            " varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform lowp vec2 vignetteCenter;\n" +
            " uniform lowp vec3 vignetteColor;\n" +
            " uniform highp float vignetteStart;\n" +
            " uniform highp float vignetteEnd;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     /*\n" +
            "     lowp vec3 rgb = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "     lowp float d = distance(textureCoordinate, vec2(0.5,0.5));\n" +
            "     rgb *= (1.0 - smoothstep(vignetteStart, vignetteEnd, d));\n" +
            "     gl_FragColor = vec4(vec3(rgb),1.0);\n" +
            "      */\n" +
            "     \n" +
            "     lowp vec3 rgb = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "     lowp float d = distance(textureCoordinate, \n" +
            "                             vec2(vignetteCenter.x, vignetteCenter.y));\n" +
            "     lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);\n" +
            "     gl_FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), \n" +
            "                         mix(rgb.y, vignetteColor.y, percent), \n" +
            "                         mix(rgb.z, vignetteColor.z, percent), 1.0);\n" +
            " }";

    private int mVignetteCenterHandle;
    private int mVignetteColorHandle;
    private int mVignetteStartHandle;
    private int mVignetteEndHandle;

    private PointF mVignetteCenter;

    private float[] mVignetteColor;

    private float mVignetteStart;

    private float mVignetteEnd;

    public GLImageVignetteFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageVignetteFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mVignetteCenterHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteCenter");
        mVignetteColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteColor");
        mVignetteStartHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteStart");
        mVignetteEndHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteEnd");
        setVignetteCenter(new PointF(0.5f, 0.5f));
        setVignetteColor(new float[] {0.0f, 0.0f, 0.0f});
        setVignetteStart(0.3f);
        setVignetteEnd(0.75f);
    }

    /**
     * 设置暗角中心，默认为纹理中心(0.5f, 0.5f)
     * @param vignetteCenter
     */
    public void setVignetteCenter(final PointF vignetteCenter) {
        mVignetteCenter = vignetteCenter;
        setPoint(mVignetteCenterHandle, mVignetteCenter);
    }

    /**
     * 设置暗角的颜色
     * @param vignetteColor
     */
    public void setVignetteColor(final float[] vignetteColor) {
        mVignetteColor = vignetteColor;
        setFloatVec3(mVignetteColorHandle, mVignetteColor);
    }

    /**
     * 设置暗角开始位置
     * @param vignetteStart 0.0f ~ 1.0f
     */
    public void setVignetteStart(final float vignetteStart) {
        mVignetteStart = vignetteStart;
        setFloat(mVignetteStartHandle, mVignetteStart);
    }

    /**
     * 设置暗角结束位置
     * @param vignetteEnd 0.0f ~ 1.0f
     */
    public void setVignetteEnd(final float vignetteEnd) {
        mVignetteEnd = vignetteEnd;
        setFloat(mVignetteEndHandle, mVignetteEnd);
    }
}
