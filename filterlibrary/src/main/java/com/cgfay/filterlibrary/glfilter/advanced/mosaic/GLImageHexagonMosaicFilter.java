package com.cgfay.filterlibrary.glfilter.advanced.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

public class GLImageHexagonMosaicFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "uniform float mosaicSize;   // 马赛克大小\n" +
            "\n" +
            "void main (void)\n" +
            "{\n" +
            "    float length = mosaicSize;\n" +
            "    float TR = 0.866025;\n" +
            "    float x = textureCoordinate.x;\n" +
            "    float y = textureCoordinate.y;\n" +
            "    int wx = int(x / 1.5 / length);\n" +
            "    int wy = int(y / TR / length);\n" +
            "    vec2 v1, v2, vn;\n" +
            "    if (wx/2 * 2 == wx) {\n" +
            "        if (wy/2 * 2 == wy) {\n" +
            "            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy));\n" +
            "            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy + 1));\n" +
            "        } else {\n" +
            "            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy + 1));\n" +
            "            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy));\n" +
            "        }\n" +
            "    } else {\n" +
            "        if (wy/2 * 2 == wy) {\n" +
            "            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy + 1));\n" +
            "            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy));\n" +
            "        } else {\n" +
            "            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy));\n" +
            "            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy + 1));\n" +
            "        }\n" +
            "    }\n" +
            "    float s1 = sqrt(pow(v1.x - x, 2.0) + pow(v1.y - y, 2.0));\n" +
            "    float s2 = sqrt(pow(v2.x - x, 2.0) + pow(v2.y - y, 2.0));\n" +
            "    if (s1 < s2) {\n" +
            "        vn = v1;\n" +
            "    } else {\n" +
            "        vn = v2;\n" +
            "    }\n" +
            "    vec4  color = texture2D(inputTexture, vn);\n" +
            "    gl_FragColor = color;\n" +
            "}";

    private int mMosaicSizeHandle;
    private float mMosaicSize;

    public GLImageHexagonMosaicFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageHexagonMosaicFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMosaicSizeHandle = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize");
            setMosaicSize(30.0f);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniform1f(mMosaicSizeHandle, mMosaicSize * (1.0f / Math.min(mImageWidth, mImageHeight)));
    }

    /**
     * 设置马赛克大小
     * @param size
     */
    public void setMosaicSize(float size) {
        mMosaicSize = size;
    }
}
