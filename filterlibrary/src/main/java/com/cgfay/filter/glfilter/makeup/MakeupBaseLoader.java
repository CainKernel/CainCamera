package com.cgfay.filter.glfilter.makeup;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.util.Log;
import android.util.Pair;

import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData;
import com.cgfay.filter.glfilter.makeup.bean.MakeupLipstickData;
import com.cgfay.filter.glfilter.makeup.bean.MakeupNormaData;
import com.cgfay.filter.glfilter.resource.ResourceCodec;
import com.cgfay.filter.glfilter.resource.ResourceDataCodec;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 美妆加载器基类
 */
public abstract class MakeupBaseLoader {

    protected static final String TAG = "MakeupLoader";

    // 输入图像宽高
    protected int mImageWidth, mImageHeight;
    // 美妆强度
    protected float mStrength;
    // 美妆处理类型，跟 fragment_makeup.glsl中的保持一致
    // 0表示绘制原图，1表示直接绘制美妆素材，2表示利用遮罩裁剪(美瞳)，3表示唇彩
    protected int mMakeupType;
    // 彩妆数据
    protected MakeupBaseData mMakeupData;
    // 彩妆解压的文件夹
    private String mFolderPath;
    // 资源加载器
    private ResourceDataCodec mResourceCodec;
    // 遮罩纹理
    protected int mMaskTexture;
    // 素材纹理
    protected int mMaterialTexture;
    // 顶点坐标
    protected float[] mVertices = null;
    // 顶点坐标缓冲
    protected FloatBuffer mVertexBuffer = null;
    // 素材/遮罩纹理缓冲
    protected FloatBuffer mTextureBuffer = null;
    // 索引缓冲
    protected ShortBuffer mIndexBuffer = null;

    // 是否允许绘制
    private boolean mEnableRender;

    // 滤镜对象
    protected final WeakReference<GLImageMakeupFilter> mWeakFilter;

    public MakeupBaseLoader(GLImageMakeupFilter filter, MakeupBaseData makeupData, String folderPath) {
        mWeakFilter = new WeakReference<>(filter);
        mMakeupData = makeupData;
        mFolderPath = folderPath.startsWith("file://") ? folderPath.substring("file://".length()) : folderPath;
        mMakeupType = 0;
        mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE;
        mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
        mStrength = makeupData == null? 1.0f : makeupData.strength;
        initBuffers();
    }

    /**
     * 初始化，第一次创建时调用
     * @param context
     */
    public void init(Context context) {
        if (mMakeupData != null) {
            mEnableRender = true;
            // 根据类型加载遮罩纹理
            switch (mMakeupData.makeupType) {
                // 这几个没有遮罩的
                case SHADOW:    // 阴影
                case BLUSH:     // 腮红
                case EYEBROW:   // 眉毛
                    mMakeupType = 1;
                    mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE;
                    break;

                // 使用眼睛的遮罩
                case PUPIL: // 美瞳，美瞳部分需要做裁剪
                    mMakeupType = 2;
                    if (mMaskTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                        mMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_eye_mask.png");
                    }
                    break;

                // 使用眼睛的遮罩
                case EYESHADOW: // 眼影
                case EYELINER:  // 眼线
                case EYELASH:   // 睫毛
                case EYELID:    // 眼皮
                    mMakeupType = 1;
                    if (mMaskTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                        mMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_eye_mask.png");
                    }
                    break;

                // 唇彩加载嘴唇的遮罩
                case LIPSTICK:
                    mMakeupType = 3;
                    if (mMaskTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                        mMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_lips_mask.png");
                    }
                    break;

                // 不能识别的类型，直接绘制原图
                default:
                    mMakeupType = 0;
                    mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE;
                    mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
                    break;
            }
            loadMaterialTexture(mFolderPath);
        } else {
            mEnableRender = false;
            mMakeupType = 0;
            mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE;
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
    }

    /**
     * 加载素材纹理
     * @param unzipPath 资源解压的文件夹路径
     */
    protected void loadMaterialTexture(String unzipPath) {

        if (mResourceCodec != null) {
            mResourceCodec = null;
        }

        // 如果数据为空，则需要销毁旧的素材纹理数据
        if (mMakeupData == null) {
            if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                GLES30.glDeleteTextures(1, new int[] { mMaterialTexture }, 0);
                mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
            }
            return;
        }
        Pair pair = ResourceCodec.getResourceFile(unzipPath);
        if (pair != null) {
            mResourceCodec = new ResourceDataCodec(unzipPath + "/" + (String) pair.first, unzipPath + "/" + pair.second);
        }
        if (mResourceCodec != null) {
            try {
                mResourceCodec.init();
            } catch (IOException e) {
                Log.e(TAG, "loadMaterialTexture: ", e);
                mResourceCodec = null;
            }
        }

        // 如果是唇彩，则加载lookupTable 数据，否则加载普通素材数据
        Bitmap bitmap = null;
        if (mMakeupData.makeupType.getName().equals("lipstick")) {
            bitmap = mResourceCodec.loadBitmap(((MakeupLipstickData)mMakeupData).lookupTable);
        } else if (((MakeupNormaData) mMakeupData).materialData != null) {
            bitmap = mResourceCodec.loadBitmap(((MakeupNormaData) mMakeupData).materialData.name);
        }

        // 判断是否取得素材或者lut纹理图片
        if (bitmap != null) {
            if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                GLES30.glDeleteTextures(1, new int[] { mMaterialTexture }, 0);
                mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
            }
            mMaterialTexture = OpenGLUtils.createTexture(bitmap);
            bitmap.recycle();
        } else {
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
    }


    /**
     * 输入图像你宽高
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * 重置，仅释放素材资源
     */
    public void reset() {
        if (mMaskTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, new int[] { mMaskTexture }, 0);
            mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
        mEnableRender = false;
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mMaskTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, new int[] { mMaskTexture }, 0);
            mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
        if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, new int[] { mMaterialTexture }, 0);
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
        }
        mWeakFilter.clear();
        releaseBuffers();
    }

    /**
     * 初始化缓冲区
     */
    protected abstract void initBuffers();

    /**
     * 释放缓冲
     */
    protected void releaseBuffers() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
        if (mIndexBuffer != null) {
            mIndexBuffer.clear();
            mIndexBuffer = null;
        }
    }

    /**
     * 绘制彩妆
     * @param faceIndex     人脸索引
     * @param inputTexture  输入图像纹理
     * @param vertexBuffer  输入图像顶点坐标缓冲
     * @param textureBuffer 输入图像纹理纹理坐标缓冲
     */
    public void drawMakeup(int faceIndex, int inputTexture, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 更新顶点纹理
        updateVertices(faceIndex);
        // 绘制彩妆
        if (mWeakFilter.get() != null && mEnableRender) {
            mWeakFilter.get().drawMakeup(inputTexture, mMaterialTexture, mMaskTexture, mVertexBuffer,
                    mTextureBuffer, mIndexBuffer, mMakeupType, mStrength);
        }
    }

    /**
     * 更新顶点坐标
     * @param faceIndex
     */
    protected abstract void updateVertices(int faceIndex);

    /**
     * 切换彩妆数据
     * @param makeupData
     */
    public void changeMakeupData(MakeupBaseData makeupData, String folderPath) {
        mMakeupData = makeupData;
        mFolderPath = folderPath.startsWith("file://") ? folderPath.substring("file://".length()) : folderPath;
        // 加载彩妆素材纹理
        if (mMakeupData != null) {
            mStrength = mMakeupData.strength;
            loadMaterialTexture(mFolderPath);
        } else {
            mStrength = 0.0f;
            if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                GLES30.glDeleteTextures(1, new int[] {mMaterialTexture}, 0);
                mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE;
            }
        }
    }

    /**
     * 设置强度
     * @param strength
     */
    public void setStrength(float strength) {
        if (strength < 0) {
            strength = 0;
        } else if (strength > 1.0f) {
            strength = 1.0f;
        }
        mStrength = strength;
    }

    /**
     * 恢复默认强度
     */
    public void resetStrength() {
        mStrength = mMakeupData == null ? 1.0f : mMakeupData.strength;
    }
}
