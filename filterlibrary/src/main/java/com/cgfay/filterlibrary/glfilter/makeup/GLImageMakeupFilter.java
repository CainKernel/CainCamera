package com.cgfay.filterlibrary.glfilter.makeup;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;
import android.util.SparseArray;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupBaseData;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupType;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.landmarklibrary.LandmarkEngine;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 彩妆滤镜
 */
public class GLImageMakeupFilter extends GLImageFilter {

    private int mMaskTextureHandle;     // 遮罩纹理句柄
    private int mMaterialTextureHandle; // 素材纹理句柄
    private int mStrengthHandle;        // 强度句柄
    private int mMakeupTypeHandle;      // 彩妆类型句柄

    // 彩妆加载器列表
    private SparseArray<MakeupBaseLoader> mLoaderArrays = new SparseArray<MakeupBaseLoader>();

    public GLImageMakeupFilter(Context context) {
        this(context, null);
    }

    public GLImageMakeupFilter(Context context, DynamicMakeup dynamicMakeup) {
        super(context, OpenGLUtils.getShaderFromAssets(context, "shader/makeup/vertex_makeup.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/makeup/fragment_makeup.glsl"));
        // 初始化空的彩妆列表
        for (int i = 0; i < MakeupType.MakeupIndex.MakeupSize; i++) {
            mLoaderArrays.put(i, null);
        }
        // 创建加载器
        if (dynamicMakeup != null && dynamicMakeup.makeupList != null) {
            for (int i = 0; i < dynamicMakeup.makeupList.size(); i++) {
                if (dynamicMakeup.makeupList.get(i) != null) {
                    MakeupBaseData makeupData = dynamicMakeup.makeupList.get(i);
                    MakeupBaseLoader loader;
                    if (makeupData.makeupType.getName().equals("pupil")) {
                        loader = new MakeupPupilLoader(this, makeupData, dynamicMakeup.unzipPath);
                    } else {
                        loader = new MakeupNormalLoader(this, makeupData, dynamicMakeup.unzipPath);
                    }
                    loader.init(context);
                    mLoaderArrays.put(dynamicMakeup.makeupList.get(i).makeupType.getIndex(), loader);
                }
            }
        }
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMaskTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "maskTexture");
            mMaterialTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "materialTexture");
            mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength");
            mMakeupTypeHandle = GLES30.glGetUniformLocation(mProgramHandle, "makeupType");
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        // 直接绘制原图时需要用到
        GLES30.glUniform1i(mMakeupTypeHandle, 0);
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 1、绘制原图到FBO
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        // 2、逐个人脸绘制彩妆
        if (LandmarkEngine.getInstance().hasFace()) {
            for (int faceIndex = 0; faceIndex < LandmarkEngine.getInstance().getFaceSize(); faceIndex++) {
                for (int i = 0; i < mLoaderArrays.size(); i++) {
                    if (mLoaderArrays.get(i) != null) {
                        mLoaderArrays.get(i).drawMakeup(faceIndex, textureId, vertexBuffer, textureBuffer);
                    }
                }
            }
        }
        return mFrameBufferTextures[0];
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        for (int i = 0; i < mLoaderArrays.size(); i++) {
            if (mLoaderArrays.get(i) != null) {
                mLoaderArrays.get(i).onInputSizeChanged(width, height);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        for (int i = 0; i < mLoaderArrays.size(); i++) {
            if (mLoaderArrays.get(i) != null) {
                mLoaderArrays.get(i).release();
            }
        }
        mLoaderArrays.clear();
        mLoaderArrays = null;
    }

    /**
     * 切换彩妆数据，这是切换某一个彩妆的API
     * @param makeupData    彩妆数据，有可能为null
     * @param folderPath    彩妆所在的文件夹
     */
    public void changeMakeupData(MakeupBaseData makeupData, String folderPath) {
        if (makeupData == null || makeupData.makeupType == null) {
            return;
        }
        if (mLoaderArrays.get(makeupData.makeupType.getIndex()) != null) {
            mLoaderArrays.get(makeupData.makeupType.getIndex()).changeMakeupData(makeupData, folderPath);
            mLoaderArrays.get(makeupData.makeupType.getIndex()).init(mContext);
        } else {
            MakeupBaseLoader loader;
            if (makeupData.makeupType.getName().equals("pupil")) {
                loader = new MakeupPupilLoader(this, makeupData, folderPath);
            } else {
                loader = new MakeupNormalLoader(this, makeupData, folderPath);
            }
            loader.init(mContext);
            loader.onInputSizeChanged(mImageWidth, mImageHeight);
            mLoaderArrays.put(makeupData.makeupType.getIndex(), loader);
        }
    }

    /**
     * 切换彩妆列表，这里是一整套彩妆切换的API
     * @param dynamicMakeup
     */
    public void changeMakeupData(DynamicMakeup dynamicMakeup) {
        // 重置旧的彩妆数据加载器
        for (int i = 0; i < mLoaderArrays.size(); i++) {
            if (mLoaderArrays.get(i) != null) {
                mLoaderArrays.get(i).reset();
            }
        }
        // 将新的彩妆加载器添加进来
        if (dynamicMakeup != null && dynamicMakeup.makeupList != null) {
            for (int i = 0; i < dynamicMakeup.makeupList.size(); i++) {
                if (dynamicMakeup.makeupList.get(i) != null) {
                    MakeupBaseData makeupData = dynamicMakeup.makeupList.get(i);
                    // 如果加载器存在，则直接切换素材并初始化
                    if (mLoaderArrays.get(makeupData.makeupType.getIndex()) != null) {
                        mLoaderArrays.get(makeupData.makeupType.getIndex()).changeMakeupData(makeupData, dynamicMakeup.unzipPath);
                        mLoaderArrays.get(makeupData.makeupType.getIndex()).init(mContext);
                    } else {
                        MakeupBaseLoader loader;
                        if (makeupData.makeupType.getName().equals("pupil")) {
                            loader = new MakeupPupilLoader(this, makeupData, dynamicMakeup.unzipPath);
                        } else {
                            loader = new MakeupNormalLoader(this, makeupData, dynamicMakeup.unzipPath);
                        }
                        loader.init(mContext);
                        loader.onInputSizeChanged(mImageWidth, mImageHeight);
                        mLoaderArrays.put(dynamicMakeup.makeupList.get(i).makeupType.getIndex(), loader);
                    }
                }
            }
        }
    }

    /**
     * 绘制彩妆
     * @param inputTexture      输入图像纹理，如果原图不存在，则直接返回不做彩妆处理
     * @param materialTexture   素材纹理，对于唇彩来说，这是lut纹理，对于其他普通彩妆来说，就是素材贴图纹理，
     *                          当不存在素材纹理时，需要传入OpenGLUtils.GL_NOT_TEXTURE，比如美瞳绘制完后需要做裁剪
     * @param maskTexture       遮罩纹理，有可能遮罩纹理不存在，
     *                          当不存在遮罩时，需要传入OpenGLUtils.GL_NOT_TEXTURE
     * @param vertexBuffer  图像顶点坐标缓冲
     * @param textureBuffer 素材/遮罩的纹理缓冲
     * @param indexBuffer   素材/遮罩索引缓冲
     * @param makeupType    彩妆类型
     * @param strength      彩妆强度
     */
    public void drawMakeup(int inputTexture, int materialTexture, int maskTexture,
                           FloatBuffer vertexBuffer, FloatBuffer textureBuffer, ShortBuffer indexBuffer,
                           int makeupType, float strength) {
        drawMakeup(mFrameBuffers[0], inputTexture, materialTexture, maskTexture,
                vertexBuffer, textureBuffer, indexBuffer, makeupType, strength);
    }

    /**
     * 绘制彩妆
     * @param frameBuffer       指定绘制的FrameBuffer id，这里指的是绘制到某个FBO中
     * @param inputTexture      输入图像纹理，如果原图不存在，则直接返回不做彩妆处理
     * @param materialTexture   素材纹理，对于唇彩来说，这是lut纹理，对于其他普通彩妆来说，就是素材贴图纹理，
     *                          当不存在素材纹理时，需要传入OpenGLUtils.GL_NOT_TEXTURE，比如美瞳绘制完后需要做裁剪
     * @param maskTexture       遮罩纹理，有可能遮罩纹理不存在，
     *                          当不存在遮罩时，需要传入OpenGLUtils.GL_NOT_TEXTURE
     * @param vertexBuffer  图像顶点坐标缓冲
     * @param textureBuffer 素材/遮罩的纹理缓冲
     * @param indexBuffer   素材/遮罩索引缓冲
     * @param makeupType    彩妆类型
     * @param strength      彩妆强度
     */
    public void drawMakeup(int frameBuffer, int inputTexture, int materialTexture, int maskTexture,
                           FloatBuffer vertexBuffer, FloatBuffer textureBuffer, ShortBuffer indexBuffer,
                           int makeupType, float strength) {

        // 如果输入图像纹理不存在，则直接退出，没有索引数据，则表示数据不存在，不需要绘制
        if (inputTexture == OpenGLUtils.GL_NOT_TEXTURE || indexBuffer == null) {
            return;
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer);
        GLES30.glUseProgram(mProgramHandle);
        runPendingOnDrawTasks();
        // 使能混合功能
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_COLOR);
        // 绑定顶点坐标缓冲
        if (vertexBuffer != null) {
            vertexBuffer.position(0);
            GLES30.glVertexAttribPointer(mPositionHandle, 2,
                    GLES30.GL_FLOAT, false, 0, vertexBuffer);
            GLES30.glEnableVertexAttribArray(mPositionHandle);
        }
        // 绑定纹理坐标缓冲，绘制彩妆时，需要绑定遮罩的纹理坐标缓冲，这里是为了节省资源做的复用流程
        if (textureBuffer != null) {
            textureBuffer.position(0);
            GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                    GLES30.GL_FLOAT, false, 0, textureBuffer);
            GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);
        }

        // 绑定输入纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(getTextureType(), inputTexture);
        GLES30.glUniform1i(mInputTextureHandle, 0);

        // 绑定素材纹理，素材纹理可能不存在，不存在时不需要绑定
        if (materialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(getTextureType(), materialTexture);
            GLES30.glUniform1i(mMaterialTextureHandle, 1);
        }

        // 绑定遮罩纹理，遮罩纹理有可能不存在，不存在时不需要绑定
        if (maskTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
            GLES30.glBindTexture(getTextureType(), maskTexture);
            GLES30.glUniform1i(mMaskTextureHandle, 2);
        }
        GLES30.glUniform1i(mMakeupTypeHandle, makeupType);
        GLES30.glUniform1f(mStrengthHandle, strength);

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexBuffer.capacity(), GLES30.GL_UNSIGNED_SHORT, indexBuffer);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES30.glBindTexture(getTextureType(), 0);
        GLES30.glDisable(GLES30.GL_BLEND);

        GLES30.glUseProgram(0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }
}
