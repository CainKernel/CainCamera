package com.cgfay.filterlibrary.glfilter.advanced.mosaic;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 三角形马赛克滤镜
 */
public class GLImageTriangleMosaicFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "// len 是六边形的边长\n" +
            "uniform float mosaicSize;\n" +
            "\n" +
            "void main (void){\n" +
            "    const float TR = 0.866025;  // .5*(3)^.5\n" +
            "    const float PI6 = 0.523599; // PI/6\n" +
            "\n" +
            "    float x = textureCoordinate.x;\n" +
            "    float y = textureCoordinate.y;\n" +
            "\n" +
            "    // 1.5*len 是矩形矩阵的长，TR*len 是宽\n" +
            "    // 计算矩形矩阵的顶点坐标 (0,0)(0,1)(1,0)(1,1)\n" +
            "    int wx = int(x/(1.5 * mosaicSize));\n" +
            "    int wy = int(y/(TR * mosaicSize));\n" +
            "\n" +
            "    vec2 v1, v2, vn;\n" +
            "\n" +
            "    // 判断是矩形的哪个顶点，上半部还是下半部\n" +
            "    if (wx / 2 * 2 == wx) {\n" +
            "        if (wy/2 * 2 == wy) {\n" +
            "            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy));\n" +
            "            v2 = vec2(mosaicSize * 1.5 * float(wx + 1), mosaicSize * TR * float(wy + 1));\n" +
            "        } else {\n" +
            "            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy + 1));\n" +
            "            v2 = vec2(mosaicSize * 1.5 * float(wx + 1), mosaicSize * TR * float(wy));\n" +
            "        }\n" +
            "    } else {\n" +
            "        if (wy/2 * 2 == wy) {\n" +
            "            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy + 1));\n" +
            "            v2 = vec2(mosaicSize * 1.5 * float(wx+1), mosaicSize * TR * float(wy));\n" +
            "        } else {\n" +
            "            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy));\n" +
            "            v2 = vec2(mosaicSize * 1.5 * float(wx + 1), mosaicSize * TR * float(wy+1));\n" +
            "        }\n" +
            "    }\n" +
            "    // 计算参考点与当前纹素的距离\n" +
            "    float s1 = sqrt(pow(v1.x - x, 2.0) + pow(v1.y - y, 2.0));\n" +
            "    float s2 = sqrt(pow(v2.x - x, 2.0) + pow(v2.y - y, 2.0));\n" +
            "    // 选择距离小的参考点\n" +
            "    if (s1 < s2) {\n" +
            "        vn = v1;\n" +
            "    } else {\n" +
            "        vn = v2;\n" +
            "    }\n" +
            "\n" +
            "    vec4 mid = texture2D(inputTexture, vn);\n" +
            "    float a = atan((x - vn.x)/(y - vn.y)); // 计算夹角\n" +
            "    // 分别计算六个三角形的中心点坐标，之后将作为参考点\n" +
            "    vec2 area1 = vec2(vn.x, vn.y - mosaicSize * TR / 2.0);\n" +
            "    vec2 area2 = vec2(vn.x + mosaicSize / 2.0, vn.y - mosaicSize * TR / 2.0);\n" +
            "    vec2 area3 = vec2(vn.x + mosaicSize / 2.0, vn.y + mosaicSize * TR / 2.0);\n" +
            "    vec2 area4 = vec2(vn.x, vn.y + mosaicSize * TR / 2.0);\n" +
            "    vec2 area5 = vec2(vn.x - mosaicSize / 2.0, vn.y + mosaicSize * TR / 2.0);\n" +
            "    vec2 area6 = vec2(vn.x - mosaicSize / 2.0, vn.y - mosaicSize * TR / 2.0);\n" +
            "\n" +
            "    // 根据夹角判断是哪个三角形\n" +
            "    if (a >= PI6 && a < PI6 * 3.0) {\n" +
            "        vn = area1;\n" +
            "    } else if (a >= PI6 * 3.0 && a < PI6 * 5.0) {\n" +
            "        vn = area2;\n" +
            "    } else if ((a >= PI6 * 5.0 && a <= PI6 * 6.0) || (a < -PI6 * 5.0 && a > -PI6 * 6.0)) {\n" +
            "        vn = area3;\n" +
            "    } else if (a < -PI6 * 3.0 && a >= -PI6 * 5.0) {\n" +
            "        vn = area4;\n" +
            "    } else if(a <= -PI6 && a> -PI6 * 3.0) {\n" +
            "        vn = area5;\n" +
            "    } else if (a > -PI6 && a < PI6) {\n" +
            "        vn = area6;\n" +
            "    }\n" +
            "\n" +
            "    vec4 color = texture2D(inputTexture, vn);\n" +
            "    gl_FragColor = color;\n" +
            "}";

    private int mMosaicSizeHandle;
    private float mMosaicSize;

    public GLImageTriangleMosaicFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageTriangleMosaicFilter(Context context, String vertexShader, String fragmentShader) {
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
