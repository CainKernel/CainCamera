package com.cgfay.filterlibrary.glfilter.advanced.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 圆形马赛克
 */
public class GLImageCircleMosaicFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "uniform float imageWidth;     // 图片宽度\n" +
            "uniform float imageHeight;    // 图片高度\n" +
            "uniform float mosaicSize;\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec2 texSize = vec2(imageWidth, imageHeight);\n" +
            "    // 计算实际图像位置\n" +
            "    vec2 xy = vec2(textureCoordinate.x * texSize.x, textureCoordinate.y * texSize.y);\n" +
            "    // 计算某一个小mosaic的中心坐标\n" +
            "    vec2 mosaicCenter = vec2(floor(xy.x / mosaicSize) * mosaicSize + 0.5 * mosaicSize,\n" +
            "                         floor(xy.y / mosaicSize) * mosaicSize + 0.5 * mosaicSize);\n" +
            "    // 计算距离中心的长度\n" +
            "    vec2 delXY = mosaicCenter - xy;\n" +
            "    float delLength = length(delXY);\n" +
            "    // 换算回纹理坐标系\n" +
            "    vec2 uvMosaic = vec2(mosaicCenter.x / texSize.x, mosaicCenter.y / texSize.y);\n" +
            "\n" +
            "    vec4 color;\n" +
            "    if (delLength < 0.5 * mosaicSize) {\n" +
            "        color = texture2D(inputTexture, uvMosaic);\n" +
            "    } else {\n" +
            "        color = texture2D(inputTexture, textureCoordinate);\n" +
            "    }\n" +
            "    gl_FragColor = color;\n" +
            "}";

    private int mImageWidthHandle;
    private int mImageHeightHandle;
    private int mMosaicSizeLoc;

    private float mMosaicSize;

    public GLImageCircleMosaicFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageCircleMosaicFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mImageWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageWidth");
            mImageHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageHeight");
            mMosaicSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize");
            setMosaicSize(30.0f);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mMosaicSizeLoc, mMosaicSize);
        GLES30.glUniform1f(mImageWidthHandle, mImageWidth);
        GLES30.glUniform1f(mImageHeightHandle, mImageHeight);
    }

    /**
     * 设置马赛克大小
     * @param size
     */
    public void setMosaicSize(float size) {
        mMosaicSize = size;
    }
}
