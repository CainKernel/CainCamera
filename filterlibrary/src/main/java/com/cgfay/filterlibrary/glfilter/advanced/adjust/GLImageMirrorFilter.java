package com.cgfay.filterlibrary.glfilter.advanced.adjust;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 镜像翻转
 * Created by cain.huang on 2017/7/21.
 */
public class GLImageMirrorFilter extends GLImageFilter {

    private static final String FRAGMENT_MORROR = "" +
            "precision mediump float;\n" +
            "varying mediump vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform float Angle;\n" +
            "uniform float MirrorX;\n" +
            "uniform float MirrorY;\n" +
            "\n" +
            "mediump vec2 getCenterRotateXY(mediump float x, mediump float y, mediump float angle) {\n" +
            "    mediump float cx, cy;\n" +
            "    mediump vec2 xy;\n" +
            "    mediump float rotateRadians = radians(angle);\n" +
            "    mediump float fZoom = 1.0;\n" +
            "    cx = x - 0.5;\n" +
            "    cy = y - 0.5;\n" +
            "    xy.x = cx * cos(rotateRadians) - cy * sin(rotateRadians);\n" +
            "    xy.y = cx * sin(rotateRadians) + cy * cos(rotateRadians);\n" +
            "    xy *= fZoom;\n" +
            "    xy += vec2(0.5);\n" +
            "    if (xy.x > 1.0) {\n" +
            "        xy.x = 2.0 - xy.x;\n" +
            "    } else if (xy.x < 0.0) {\n" +
            "        xy.x = abs(xy.x);\n" +
            "    }\n" +
            "    if (xy.y > 1.0) {\n" +
            "        xy.y = 2.0 - xy.y;\n" +
            "    } else if (xy.y < 0.0) {\n" +
            "        xy.y = abs(xy.y);\n" +
            "    }\n" +
            "\n" +
            "    xy = clamp(xy, 0.0, 1.0);\n" +
            "    return xy;\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    vec3 color;\n" +
            "    vec2 textPos = getCenterRotateXY(textureCoordinate.x, textureCoordinate.y, Angle);\n" +
            "    if(MirrorX > 0.001) {\n" +
            "        textPos = vec2(1.0-textPos.x, textPos.y);\n" +
            "    }\n" +
            "    if(MirrorY > 0.001) {\n" +
            "        textPos = vec2(textPos.x, 1.0-textPos.y);\n" +
            "    }\n" +
            "    color =texture2D(inputTexture, textPos).rgb;\n" +
            "    gl_FragColor = vec4(color, 1.0);\n" +
            "}";

    private int mAngleHandle;
    private int mMirrorXHandle;
    private int mMirrorYHandle;

    private float mAngle;
    private float mMirrorX;
    private float mMirrorY;

    public GLImageMirrorFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_MORROR);
    }

    public GLImageMirrorFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mAngleHandle = GLES30.glGetUniformLocation(mProgramHandle, "Angle");
        mMirrorXHandle = GLES30.glGetUniformLocation(mProgramHandle, "MirrorX");
        mMirrorYHandle = GLES30.glGetUniformLocation(mProgramHandle, "MirrorY");
        setAngle(0.0f);
        setMirrorX(0.0f);
        setMirrorY(0.0f);
    }

    /**
     * 设置旋转角度
     * @param angle
     */
    public void setAngle(float angle) {
        mAngle = angle;
        setFloat(mAngleHandle, mAngle);
    }

    /**
     * x坐标
     * @param mirrorX
     */
    public void setMirrorX(float mirrorX) {
        mMirrorX = mirrorX;
        setFloat(mMirrorXHandle, mMirrorX);
    }

    /**
     * y坐标
     * @param mirrorY
     */
    public void setMirrorY(float mirrorY) {
        mMirrorY = mirrorY;
        setFloat(mMirrorYHandle, mMirrorY);
    }
}
