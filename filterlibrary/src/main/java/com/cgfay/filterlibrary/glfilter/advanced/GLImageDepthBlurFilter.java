package com.cgfay.filterlibrary.glfilter.advanced;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 景深滤镜
 */
public class GLImageDepthBlurFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputTexture;         // 输入纹理\n" +
            "uniform sampler2D blurImageTexture;     // 经过高斯模糊处理的纹理\n" +
            "uniform float inner;    // 内圆半径\n" +
            "uniform float outer;    // 外圆半径\n" +
            "uniform float width;    // 纹理宽度\n" +
            "uniform float height;   // 纹理高度\n" +
            "uniform vec2 center;    // 中心点的位置\n" +
            "uniform vec3 line1;     // 前景深\n" +
            "uniform vec3 line2;     // 后景深\n" +
            "uniform float intensity;// 景深程度\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 originalColor = texture2D(inputTexture, textureCoordinate);\n" +
            "    vec4 tempColor;\n" +
            "    float ratio = height / width;\n" +
            "    vec2 ellipse = vec2(1, ratio * ratio);\n" +
            "    float fx = (textureCoordinate.x - center.x);\n" +
            "    float fy = (textureCoordinate.y - center.y);\n" +
            "    // 用椭圆方程求离中心点的距离\n" +
            "    float dist = sqrt(fx * fx * ellipse.x + fy * fy * ellipse.y);\n" +
            "    // 如果小于内圆半径，则直接输出原图，否则拿原始纹理跟高斯模糊的纹理按照不同的半径进行alpha混合\n" +
            "    if (dist < inner) {\n" +
            "        tempColor = originalColor;\n" +
            "    } else {\n" +
            "        vec3 point = vec3(textureCoordinate.x, textureCoordinate.y, 1.0);\n" +
            "        float value1 = dot(line1, point);\n" +
            "        float value2 = dot(line2, point);\n" +
            "        if (value1 >= 0.0 && value2 >= 0.0) {\n" +
            "            tempColor = originalColor;\n" +
            "        } else {\n" +
            "            vec4 blurColor = texture2D(blurImageTexture, textureCoordinate);\n" +
            "            float lineAlpha = max(-value1 / 0.15, -value2 / 0.15);\n" +
            "            float alpha = (dist - inner)/outer;\n" +
            "            alpha = min(lineAlpha, alpha);\n" +
            "            alpha = clamp(alpha, 0.0, 1.0);\n" +
            "            tempColor = mix(originalColor, blurColor, alpha);\n" +
            "        }\n" +
            "    }\n" +
            "    gl_FragColor = mix(originalColor, tempColor, intensity);\n" +
            "}";

    private int mBlurImageHandle;
    private int mInnerHandle;
    private int mOuterHandle;
    private int mWidthHandle;
    private int mHeightHandle;
    private int mCenterHandle;
    private int mLine1Handle;
    private int mLine2Handle;
    private int mIntensityHandle;

    // 高斯模糊滤镜
    private GLImageGaussianBlurFilter mGaussianBlurFilter;

    // 高斯模糊图像缩放半径
    private float mBlurScale = 0.5f;
    // 存储经过高斯模糊处理的纹理id
    private int mBlurTexture;

    public GLImageDepthBlurFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageDepthBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mGaussianBlurFilter = new GLImageGaussianBlurFilter(context);
        mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurImageHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurImageTexture");
            mInnerHandle = GLES30.glGetUniformLocation(mProgramHandle, "inner");
            mOuterHandle = GLES30.glGetUniformLocation(mProgramHandle, "outer");
            mWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "width");
            mHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "height");
            mCenterHandle = GLES30.glGetUniformLocation(mProgramHandle, "center");
            mLine1Handle = GLES30.glGetUniformLocation(mProgramHandle, "line1");
            mLine2Handle = GLES30.glGetUniformLocation(mProgramHandle, "line2");
            mIntensityHandle = GLES30.glGetUniformLocation(mProgramHandle, "intensity");
            initUniformData();
        }
    }

    /**
     * 初始化同一变量值
     */
    private void initUniformData() {
        setFloat(mInnerHandle, 0.35f);
        setFloat(mOuterHandle, 0.12f);
        setPoint(mCenterHandle, new PointF(0.5f, 0.5f));
        setFloatVec3(mLine1Handle, new float[] {0.0f, 0.0f, -0.15f});
        setFloatVec3(mLine2Handle, new float[] {0.0f, 0.0f, -0.15f});
        setFloat(mIntensityHandle, 1.0f);
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(getTextureType(), mBlurTexture);
            GLES30.glUniform1i(mBlurImageHandle, 1);
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setFloat(mWidthHandle, width);
        setFloat(mHeightHandle, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.onDisplaySizeChanged(width, height);
        }
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mGaussianBlurFilter != null) {
            mBlurTexture = mGaussianBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        }
        return super.drawFrame(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mGaussianBlurFilter != null) {
            mBlurTexture = mGaussianBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        }
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
    }

    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer(width, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.initFrameBuffer((int)(width * mBlurScale), (int)(height * mBlurScale));
        }
    }

    @Override
    public void destroyFrameBuffer() {
        super.destroyFrameBuffer();
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.destroyFrameBuffer();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.release();
            mGaussianBlurFilter = null;
        }
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, new int[]{mBlurTexture}, 0);
        }
    }
}
