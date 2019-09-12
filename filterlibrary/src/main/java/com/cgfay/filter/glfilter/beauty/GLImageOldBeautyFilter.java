package com.cgfay.filter.glfilter.beauty;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.beauty.bean.BeautyParam;
import com.cgfay.filter.glfilter.beauty.bean.IBeautify;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * 旧的美颜滤镜
 * Created by cain on 2017/7/30.
 */
public class GLImageOldBeautyFilter extends GLImageFilter implements IBeautify {

    private int mWidthLoc;
    private int mHeightLoc;
    private int mOpacityLoc;

    // 高斯模糊处理的图像缩放倍数
    private float mBlurScale = 0.5f;

    private GLImageBeautyComplexionFilter mComplexionBeautyFilter;

    public GLImageOldBeautyFilter(Context context) {
        this(context, VERTEX_SHADER, OpenGLUtils.getShaderFromAssets(context,
                "shader/beauty/fragment_old_beauty.glsl"));
    }

    public GLImageOldBeautyFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
        mComplexionBeautyFilter = new GLImageBeautyComplexionFilter(context);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mWidthLoc = GLES30.glGetUniformLocation(mProgramHandle, "width");
        mHeightLoc = GLES30.glGetUniformLocation(mProgramHandle, "height");
        mOpacityLoc = GLES30.glGetUniformLocation(mProgramHandle, "opacity");
        setSkinBeautyLevel(1.0f);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged((int) (width * mBlurScale), (int)(height * mBlurScale));
        // 宽高变更时需要重新设置宽高值
        setInteger(mWidthLoc, (int) (width * mBlurScale));
        setInteger(mHeightLoc, (int)(height * mBlurScale));
        if (mComplexionBeautyFilter != null) {
            mComplexionBeautyFilter.onInputSizeChanged(width, height);
        }
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if (mComplexionBeautyFilter != null) {
            mComplexionBeautyFilter.onDisplaySizeChanged(width, height);
        }
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        int currentTexture = textureId;
        if (mComplexionBeautyFilter != null) {
            currentTexture = mComplexionBeautyFilter.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer);
        }
        return super.drawFrame(currentTexture, vertexBuffer, textureBuffer);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        int currentTexture = textureId;
        if (mComplexionBeautyFilter != null) {
            currentTexture = mComplexionBeautyFilter.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer);
        }
        return super.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer);
    }

    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer((int) (width * mBlurScale), (int)(height *mBlurScale));
        if (mComplexionBeautyFilter != null) {
            mComplexionBeautyFilter.initFrameBuffer(width, height);
        }
    }

    @Override
    public void destroyFrameBuffer() {
        super.destroyFrameBuffer();
        if (mComplexionBeautyFilter != null) {
            mComplexionBeautyFilter.destroyFrameBuffer();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mComplexionBeautyFilter != null) {
            mComplexionBeautyFilter.release();
            mComplexionBeautyFilter = null;
        }
    }

    @Override
    public void onBeauty(BeautyParam beauty) {
        if (mComplexionBeautyFilter != null) {
            mComplexionBeautyFilter.setComplexionLevel(beauty.complexionIntensity);
        }
        setSkinBeautyLevel(beauty.beautyIntensity);
    }

    /**
     * 设置磨皮程度
     * @param percent 0.0 ~ 1.0
     */
    public void setSkinBeautyLevel(float percent) {
        float opacity;
        if (percent <= 0) {
            opacity = 0.0f;
        } else {
            opacity = calculateOpacity(percent);
        }
        setFloat(mOpacityLoc, opacity);
    }

    /**
     * 根据百分比计算出实际的磨皮程度
     * @param percent 0% ~ 100%
     * @return
     */
    private float calculateOpacity(float percent) {
        if (percent > 1.0f) {
            percent = 1.0f;
        }
        float result = (float) (1.0f - (1.0f - percent + 0.02) / 2.0f);

        return result;
    }
}
