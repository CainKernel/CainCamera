package com.cgfay.filterlibrary.glfilter.advanced.multi;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.advanced.GLImageGaussianBlurFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 边框模糊滤镜，跟FaceU的边框滤镜效果一致
 */
public class GLImageFrameBlurFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision mediump float;\n" +
            "uniform sampler2D inputTexture; // 原始图像\n" +
            "uniform sampler2D blurTexture;  // 经过高斯模糊的图像\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "uniform float blurOffsetX;  // x轴边框模糊偏移值\n" +
            "uniform float blurOffsetY;  // y轴边框模糊偏移值\n" +
            "\n" +
            "void main() {\n" +
            "    // uv坐标\n" +
            "    vec2 uv = textureCoordinate.xy;\n" +
            "    vec4 color;\n" +
            "    // 中间为原图，需要缩小\n" +
            "    if (uv.x >= blurOffsetX && uv.x <= 1.0 - blurOffsetX\n" +
            "        && uv.y >= blurOffsetY && uv.y <= 1.0 - blurOffsetY) {\n" +
            "        // 内部缩放值\n" +
            "        float scaleX = 1.0 / (1.0 - 2.0 * blurOffsetX);\n" +
            "        float scaleY = 1.0 / (1.0 - 2.0 * blurOffsetY);\n" +
            "        // 计算出内部新的UV坐标\n" +
            "        vec2 newUV = vec2((uv.x - blurOffsetX) * scaleX, (uv.y - blurOffsetY) * scaleY);\n" +
            "        color = texture2D(inputTexture, newUV);\n" +
            "    } else { // 边框部分使用高斯模糊的图像\n" +
            "        color = texture2D(blurTexture, uv);\n" +
            "    }\n" +
            "    gl_FragColor = color;\n" +
            "}";

    private int mBlurTextureHandle;
    private int mBlurOffsetXHandle;
    private int mBlurOffsetYHandle;
    private float blurOffsetX;
    private float blurOffsetY;

    // 高斯模糊滤镜
    private GLImageGaussianBlurFilter mGaussianBlurFilter;
    // 高斯模糊图像缩放半径
    private float mBlurScale = 0.5f;
    private int mBlurTexture;

    public GLImageFrameBlurFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageFrameBlurFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mGaussianBlurFilter = new GLImageGaussianBlurFilter(mContext);
        mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE;
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture");
            mBlurOffsetXHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetX");
            mBlurOffsetYHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetY");
            setBlurOffset(0.15f, 0.15f);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(getTextureType(), mBlurTexture);
            GLES30.glUniform1i(mBlurTextureHandle, 1);
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mGaussianBlurFilter != null) {
            mGaussianBlurFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
            mGaussianBlurFilter.initFrameBuffer((int)(width * mBlurScale), (int)(height * mBlurScale));
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

    /**
     * 模糊的偏移值
     * @param offsetX 偏移值 0.0 ~ 1.0f
     * @param offsetY 偏移值 0.0 ~ 1.0f
     */
    public void setBlurOffset(float offsetX, float offsetY) {
        if (offsetX < 0.0f) {
            offsetX = 0.0f;
        } else if (offsetX > 1.0f) {
            offsetX = 1.0f;
        }
        this.blurOffsetX = offsetX;
        if (offsetY < 0.0f) {
            offsetY = 0.0f;
        } else if (offsetY > 1.0f) {
            offsetY = 1.0f;
        }
        this.blurOffsetY = offsetY;

        setFloat(mBlurOffsetXHandle, blurOffsetX);
        setFloat(mBlurOffsetYHandle, blurOffsetY);
    }

}
