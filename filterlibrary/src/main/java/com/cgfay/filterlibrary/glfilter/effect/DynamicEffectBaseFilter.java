package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.effect.bean.DynamicEffectData;
import com.cgfay.filterlibrary.glfilter.resource.ResourceCodec;
import com.cgfay.filterlibrary.glfilter.resource.ResourceDataCodec;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.utilslibrary.utils.BitmapUtils;

import java.io.IOException;
import java.util.HashMap;

/**
 * 动态特效滤镜基类
 */
public class DynamicEffectBaseFilter extends GLImageFilter {

    // 时间戳，单位：秒
    protected float mTimeStamp;

    // 动态特效数据
    protected DynamicEffectData mDynamicEffectData;

    // 特效所在的文件夹
    protected String mFolderPath;
    // 资源加载器
    protected ResourceDataCodec mResourceCodec;

    // 统一变量数值列表
    protected HashMap<String, Integer> mUniformDataHandleList = new HashMap<>();
    // 统一变量纹理列表
    protected HashMap<String, Integer> mUniformSamplerHandleList = new HashMap<>();
    // 纹理列表
    private int[] mTextureList;

    // 纹理宽度句柄
    private int mTextureWidthHandle;
    // 纹理高度句柄
    private int mTextureHeightHandle;

    public DynamicEffectBaseFilter(Context context, DynamicEffectData dynamicEffectData, String unzipPath) {
        super(context, (dynamicEffectData == null || TextUtils.isEmpty(dynamicEffectData.vertexShader)) ? VERTEX_SHADER
                        : getShaderString(context, unzipPath, dynamicEffectData.vertexShader),
                (dynamicEffectData == null || TextUtils.isEmpty(dynamicEffectData.fragmentShader)) ? FRAGMENT_SHADER
                        : getShaderString(context, unzipPath, dynamicEffectData.fragmentShader));
        mDynamicEffectData = dynamicEffectData;
        mFolderPath = unzipPath.startsWith("file://") ? unzipPath.substring("file://".length()) : unzipPath;
        initEffectUniformHandle();
    }

    /**
     * 绑定统一变量
     */
    protected void initEffectUniformHandle() {
        if (mDynamicEffectData != null) {
            // 创建资源加载器并初始化
            Pair pair = ResourceCodec.getResourceFile(mFolderPath);
            if (pair != null) {
                mResourceCodec = new ResourceDataCodec(mFolderPath + "/" + (String) pair.first, mFolderPath + "/" + pair.second);
            }
            if (mResourceCodec != null) {
                try {
                    mResourceCodec.init();
                } catch (IOException e) {
                    Log.e(TAG, "initEffectUniformHandle: ", e);
                    mResourceCodec = null;
                }
            }

            // 创建统一变量纹理列表
            if (mDynamicEffectData.uniformSamplerList != null && mDynamicEffectData.uniformSamplerList.size() > 0) {
                mTextureList = new int[mDynamicEffectData.uniformSamplerList.size()];
                // 绑定纹理句柄和创建纹理
                for (int i = 0; i < mDynamicEffectData.uniformSamplerList.size(); i++) {
                    DynamicEffectData.UniformSampler sampler = mDynamicEffectData.uniformSamplerList.get(i);
                    if (sampler != null) {
                        int uniform = GLES30.glGetUniformLocation(mProgramHandle, sampler.uniform);
                        mUniformSamplerHandleList.put(sampler.uniform, uniform);
                        // 创建纹理
                        Bitmap bitmap = null;
                        if (mResourceCodec != null) {
                            bitmap = mResourceCodec.loadBitmap(sampler.value);
                        }
                        if (bitmap == null) {
                            bitmap = BitmapUtils.getBitmapFromFile(mFolderPath + "/" + sampler.value);
                        }
                        if (bitmap != null) {
                            mTextureList[i] = OpenGLUtils.createTexture(bitmap);
                            bitmap.recycle();
                        } else {
                            mTextureList[i] = OpenGLUtils.GL_NOT_TEXTURE;
                        }
                    }
                }
            }

            // 绑定数值纹理
            if (mDynamicEffectData.uniformDataList != null && mDynamicEffectData.uniformDataList.size() > 0) {
                for (int i = 0; i < mDynamicEffectData.uniformDataList.size(); i++) {
                    DynamicEffectData.UniformData uniformData = mDynamicEffectData.uniformDataList.get(i);
                    if (uniformData != null) {
                        int uniform = GLES30.glGetUniformLocation(mProgramHandle, uniformData.uniform);
                        mUniformDataHandleList.put(uniformData.uniform, uniform);
                    }
                }
            }

            // 是否需要绑定宽高
            if (mDynamicEffectData.texelSize) {
                mTextureWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "textureWidth");
                mTextureHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "textureHeight");
            } else {
                mTextureWidthHandle = OpenGLUtils.GL_NOT_INIT;
                mTextureHeightHandle = OpenGLUtils.GL_NOT_INIT;
            }
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        // TODO 绑定纹理句柄，根据时间戳和时间间隔计算数值索引
        if (mDynamicEffectData == null) {
            return;
        }
        // 根据时间戳计算出索引，然后根据索引计算出统一变量需要绑定的数值
        int frameIndex = (int) (mTimeStamp / mDynamicEffectData.duration);
        for (int i = 0; i < mDynamicEffectData.uniformDataList.size(); i++) {
            DynamicEffectData.UniformData uniformData = mDynamicEffectData.uniformDataList.get(i);
            if (uniformData != null && uniformData.value != null) {
                if (frameIndex > uniformData.value.length) {
                    // 绑定实际数值
                    int currentIndex = frameIndex % uniformData.value.length;
                    if (mUniformDataHandleList.get(uniformData.uniform) != null) {
                        GLES30.glUniform1f(mUniformDataHandleList.get(uniformData.uniform), uniformData.value[currentIndex]);
                    }
                }
            }
        }
        // 绑定纹理
        for (int i = 0; i < mDynamicEffectData.uniformSamplerList.size(); i++) {
            DynamicEffectData.UniformSampler uniformSampler = mDynamicEffectData.uniformSamplerList.get(i);
            if (uniformSampler != null && uniformSampler.value != null) {
                if (mUniformSamplerHandleList.get(uniformSampler.uniform) != null) {
                    OpenGLUtils.bindTexture(mUniformSamplerHandleList.get(uniformSampler.uniform), mTextureList[i], i + 1);
                }
            }
        }

        // 是否需要绑定宽高
        if (mDynamicEffectData.texelSize) {
            GLES30.glUniform1i(mTextureWidthHandle, mImageWidth);
            GLES30.glUniform1i(mTextureHeightHandle, mImageHeight);
        }
    }

    /**
     * 设置时间戳
     * @param timeStamp
     */
    public void setTimeStamp(float timeStamp) {
        mTimeStamp = timeStamp;
    }

    /**
     * 根据解压路径和shader名称读取shader的字符串内容
     * @param unzipPath
     * @param shaderName
     * @return
     */
    protected static String getShaderString(Context context, String unzipPath, String shaderName) {
        if (TextUtils.isEmpty(unzipPath) || TextUtils.isEmpty(shaderName)) {
            throw new IllegalArgumentException("shader is empty!");
        }
        String path = unzipPath + "/" + shaderName;
        if (path.startsWith("assets://")) {
            return OpenGLUtils.getShaderFromAssets(context, path.substring("assets://".length()));
        } else if (path.startsWith("file://")) {
            return OpenGLUtils.getShaderFromFile(path.substring("file://".length()));
        }
        return OpenGLUtils.getShaderFromFile(path);
    }
}
