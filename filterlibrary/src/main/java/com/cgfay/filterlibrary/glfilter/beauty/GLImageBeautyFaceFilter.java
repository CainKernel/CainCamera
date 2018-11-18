package com.cgfay.filterlibrary.glfilter.beauty;

import android.content.Context;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageDrawElementsFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageGaussianBlurFilter;
import com.cgfay.filterlibrary.glfilter.beauty.bean.BeautyParam;
import com.cgfay.filterlibrary.glfilter.beauty.bean.IBeautify;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.landmarklibrary.LandmarkEngine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 人脸美化滤镜，主要是做美牙、亮眼、消除法令纹、消除卧蚕和眼袋等美化操作
 */
public class GLImageBeautyFaceFilter extends GLImageDrawElementsFilter implements IBeautify {

    // 顶点坐标数组最大长度，这里主要用于复用缓冲
    private static final int MaxLength = 100;
    private float[] mVertices = new float[MaxLength];

    // 用于高斯模糊处理
    private GLImageGaussianBlurFilter mBlurFilter;
    private GLImageGaussianBlurFilter mBlurNextFilter;

    private int mBlurTextureHandle;
    private int mBlurTexture2Handle;
    private int mMaskTextureHandle;
    private int mTeethLookupTextureHandle;

    private int mBrightEyeStrengthHandle;
    private int mTeethStrengthHandle;
    private int mNasolabialStrengthHandle;
    private int mFurrowStrengthHandle;
    private int mEyeBagStrengthHandle;

    private int mProcessTypeHandle;

    // 坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mMaskTextureBuffer;

    // 高斯模糊纹理
    private int mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE;
    private int mBlurTexture2 = OpenGLUtils.GL_NOT_TEXTURE;

    private int mEyeMaskTexture;        // 眼睛遮罩纹理
    private int mTeethMaskTexture;      // 嘴巴(牙齿)遮罩纹理
    private int mTeethLookupTexture;    // 美牙的lookup table 纹理

    private float mBrightEyeStrength;   // 亮眼程度
    private float mBeautyTeethStrength; // 美牙程度
    private float mNasolabialStrength;  // 法令纹消除程度
    private float mFurrowStrength;      // 卧蚕消除程度
    private float mEyeBagStrength;      // 眼袋消除程度

    private int mProcessType = 0; // 处理类型

    public GLImageBeautyFaceFilter(Context context) {
        super(context, OpenGLUtils.getShaderFromAssets(context, "shader/beauty/vertex_beauty_face.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/beauty/fragment_beauty_face.glsl"));
        mBlurFilter = new GLImageGaussianBlurFilter(context);
        mBlurFilter.setBlurSize(1.0f);
        mBlurNextFilter = new GLImageGaussianBlurFilter(context);
        mBlurNextFilter.setBlurSize(0.3f);


        mEyeMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_eye_mask.png");
        mTeethMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/teeth_mask.png");
        mTeethLookupTexture = OpenGLUtils.createTextureFromAssets(context, "texture/teeth_beauty_lookup.png");

        mBrightEyeStrength = 0;
        mBeautyTeethStrength = 0;
        mNasolabialStrength = 0;
        mFurrowStrength = 0;
        mEyeBagStrength = 0;
    }

    @Override
    protected void initBuffers() {
        releaseBuffers();
        mVertexBuffer = ByteBuffer.allocateDirect(MaxLength * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.position(0);

        mMaskTextureBuffer = ByteBuffer.allocateDirect(MaxLength * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mMaskTextureBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(MaxLength * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        mIndexBuffer.position(0);
    }

    @Override
    protected void releaseBuffers() {
        super.releaseBuffers();
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mMaskTextureBuffer != null) {
            mMaskTextureBuffer.clear();
            mMaskTextureBuffer = null;
        }
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture");
            mBlurTexture2Handle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture2");
            mMaskTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "maskTexture");
            mTeethLookupTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "teethLookupTexture");

            mBrightEyeStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "brightEyeStrength");
            mTeethStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "teethStrength");
            mNasolabialStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "nasolabialStrength");
            mFurrowStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "furrowStrength");
            mEyeBagStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "eyeBagStrength");
            mProcessTypeHandle = GLES30.glGetUniformLocation(mProgramHandle, "processType");
        }
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mBlurFilter != null) {
            mBlurFilter.onInputSizeChanged((int)(width / 3.0f), (int)(height / 3.0f));
        }
        if (mBlurNextFilter != null) {
            mBlurNextFilter.onInputSizeChanged((int)(width / 3.0f), (int)(height / 3.0f));
        }
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if (mBlurFilter != null) {
            mBlurFilter.onDisplaySizeChanged(width, height);
        }
        if (mBlurNextFilter != null) {
            mBlurNextFilter.onDisplaySizeChanged(width, height);
        }
    }


    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 先将原图图像绘制到FBO中
        setInteger(mProcessTypeHandle, 0);
        updateBuffer(0, -1);
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);

        if (LandmarkEngine.getInstance().hasFace()) {
            if (mBlurFilter != null) {
                mBlurTexture = mBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
            }
            if (mBlurNextFilter != null) {
                mBlurTexture2 = mBlurNextFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
            }
            // 逐个人脸进行亮眼、美牙、消除法令纹、消除卧蚕和眼袋等处理
            for (int faceIndex = 0; faceIndex < LandmarkEngine.getInstance().getFaceSize(); faceIndex++) {
                // 1、亮眼处理
                if (mBrightEyeStrength != 0.0) {
                    updateBuffer(1, faceIndex);
                    setInteger(mProcessTypeHandle, 1);
                    setFloat(mBrightEyeStrengthHandle, mBrightEyeStrength);
                    super.drawFrameBuffer(textureId, mVertexBuffer, mMaskTextureBuffer);
                }

                // 2、美牙处理
                if (mBeautyTeethStrength != 0.0) {
                    updateBuffer(2, faceIndex);
                    setInteger(mProcessTypeHandle, 2);
                    setFloat(mTeethStrengthHandle, mBeautyTeethStrength);
                    super.drawFrameBuffer(textureId, mVertexBuffer, mMaskTextureBuffer);
                }

                // TODO 法令纹、卧蚕、眼袋由于还没遮罩图，暂时不做处理
//                // 3、消除法令纹
//                if (mNasolabialStrength != 0.0) {
//                    updateBuffer(3, faceIndex);
//                    setInteger(mProcessTypeHandle, 3);
//                    setFloat(mNasolabialStrengthHandle, mNasolabialStrength);
//                    super.drawFrameBuffer(textureId, mVertexBuffer, mMaskTextureBuffer);
//                }
//                // 4、消除卧蚕眼袋
//                if (mFurrowStrength != 0.0 && mEyeBagStrength != 0.0) {
//                    updateBuffer(4, faceIndex);
//                    setInteger(mProcessTypeHandle, 4);
//                    setFloat(mFurrowStrengthHandle, mFurrowStrength);
//                    setFloat(mEyeBagStrengthHandle, mEyeBagStrength);
//                    super.drawFrameBuffer(textureId, mVertexBuffer, mMaskTextureBuffer);
//                }
            }

        }

        return mFrameBufferTextures[0];

    }

    /**
     * 更新缓冲
     * @param type 索引类型，0表示原图，1表示亮眼，2表示美牙，3表示消除法令纹，4表示消除卧蚕眼袋
     * @param faceIndex 人脸索引
     */
    private void updateBuffer(int type, int faceIndex) {
        mProcessType = type;
        switch (type) {
            case 1: // 亮眼
                // 更新眼睛顶点坐标
                LandmarkEngine.getInstance().getBrightEyeVertices(mVertices, faceIndex);
                mVertexBuffer.clear();
                mVertexBuffer.put(mVertices);
                mVertexBuffer.position(0);
                // 更新眼睛遮罩纹理坐标
                mMaskTextureBuffer.clear();
                mMaskTextureBuffer.put(mEyeMaskTextureVertices);
                mMaskTextureBuffer.position(0);
                // 更新眼睛索引
                mIndexBuffer.clear();
                mIndexBuffer.put(mEyeIndices);
                mIndexBuffer.position(0);
                mIndexLength = mEyeIndices.length;
                break;

            case 2: // 美牙
                // 更新美牙顶点坐标
                LandmarkEngine.getInstance().getBeautyTeethVertices(mVertices, faceIndex);
                mVertexBuffer.clear();
                mVertexBuffer.put(mVertices);
                mVertexBuffer.position(0);
                // 更新美牙遮罩纹理坐标
                mMaskTextureBuffer.clear();
                mMaskTextureBuffer.put(mTeethMaskTextureVertices);
                mMaskTextureBuffer.position(0);
                // 更新美牙索引
                mIndexBuffer.clear();
                mIndexBuffer.put(mTeethIndices);
                mIndexBuffer.position(0);
                mIndexLength = mTeethIndices.length;
                break;

            case 0: // 原图
            default:    // 其他类型也是原图
                mIndexBuffer.clear();
                mIndexBuffer.put(TextureRotationUtils.Indices);
                mIndexBuffer.position(0);
                mIndexLength = 6;
                break;
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();

        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurTextureHandle, mBlurTexture, 1);
        }

        if (mBlurTexture2 != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurTexture2Handle, mBlurTexture2, 2);
        }

        // 根据不同类型绑定不同的遮罩纹理
        switch (mProcessType) {
            case 1: // 亮眼，绑定眼睛遮罩
                OpenGLUtils.bindTexture(mMaskTextureHandle, mEyeMaskTexture, 3);
                break;

            case 2:
                OpenGLUtils.bindTexture(mMaskTextureHandle, mTeethMaskTexture, 3);
                break;
        }

        // lut纹理
        OpenGLUtils.bindTexture(mTeethLookupTextureHandle, mTeethLookupTexture, 4);
    }


    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer(width, height);
        if (mBlurFilter != null) {
            mBlurFilter.initFrameBuffer((int)(width / 3.0f), (int)(height / 3.0f));
        }
        if (mBlurNextFilter != null) {
            mBlurNextFilter.initFrameBuffer((int)(width / 3.0f), (int)(height / 3.0f));
        }
    }

    @Override
    public void destroyFrameBuffer() {
        super.destroyFrameBuffer();
        if (mBlurFilter != null) {
            mBlurFilter.destroyFrameBuffer();
        }
        if (mBlurFilter != null) {
            mBlurFilter.destroyFrameBuffer();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mBlurFilter != null) {
            mBlurFilter.release();
            mBlurFilter = null;
        }
        if (mBlurNextFilter != null) {
            mBlurNextFilter.release();
            mBlurNextFilter = null;
        }
    }

    @Override
    public void onBeauty(BeautyParam beauty) {
        if (beauty != null) {
            mBrightEyeStrength = clamp(beauty.eyeBrightIntensity, 0.0f, 1.0f);
            mBeautyTeethStrength = clamp(beauty.teethBeautyIntensity, 0.0f, 1.0f);
            mNasolabialStrength = clamp(beauty.nasolabialFoldsIntensity, 0.0f, 1.0f);
            mFurrowStrength = clamp(beauty.eyeFurrowsIntensity, 0.0f, 1.0f);
            mEyeBagStrength = clamp(beauty.eyeBagsIntensity, 0.0f, 1.0f);
        }
    }


    /**
     * 眼睛部分索引
     */
    private static final short[] mEyeIndices = new short[] {
            0, 5, 1,
            1, 5, 12,
            12, 5, 13,
            12, 13, 4,
            12, 4, 2,
            2, 4, 3,

            6, 7, 11,
            7, 11, 14,
            14, 11, 15,
            14, 15, 10,
            14, 10, 8,
            8, 10, 9
    };

    /**
     * 眼睛遮罩纹理坐标
     */
    private static final float[] mEyeMaskTextureVertices = new float[] {
            0.102757f, 0.465517f,
            0.175439f, 0.301724f,
            0.370927f, 0.310345f,
            0.446115f, 0.603448f,
            0.353383f, 0.732759f,
            0.197995f, 0.689655f,

            0.566416f, 0.629310f,
            0.659148f, 0.336207f,
            0.802005f, 0.318966f,
            0.884712f, 0.465517f,
            0.812030f, 0.681034f,
            0.681704f, 0.750023f,

            0.273183f, 0.241379f,
            0.275689f, 0.758620f,

            0.721805f, 0.275862f,
            0.739348f, 0.758621f,
    };

    /**
     * 美牙索引
     */
    private static final short[] mTeethIndices = new short[] {
            0, 11, 1,
            1, 11, 10,
            1, 10, 2,
            2, 10, 3,
            3, 10, 9,
            3, 9, 8,
            3, 8, 4,
            4, 8, 5,
            5, 8, 7,
            5, 7, 6,
    };

    /**
     * 美牙遮罩纹理坐标
     */
    private static final float[] mTeethMaskTextureVertices = new float[] {
            0.154639f, 0.378788f,
            0.295533f, 0.287879f,
            0.398625f, 0.196970f,
            0.512027f, 0.287879f,
            0.611684f, 0.212121f,
            0.728523f, 0.287879f,
            0.872852f, 0.378788f,
            0.742268f, 0.704546f,
            0.639176f, 0.848485f,
            0.522337f, 0.636364f,
            0.398625f, 0.833333f,
            0.240550f, 0.651515f,
    };
}
