package com.cgfay.filterlibrary.glfilter.color;

import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES30;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData;
import com.cgfay.filterlibrary.glfilter.resource.ResourceCodec;
import com.cgfay.filterlibrary.glfilter.resource.ResourceDataCodec;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.utilslibrary.utils.BitmapUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * 滤镜资源加载器
 */
public class DynamicColorLoader {

    private static final String TAG = "DynamicColorLoader";

    // 滤镜所在的文件夹
    private String mFolderPath;
    // 动态滤镜数据
    private DynamicColorData mColorData;
    // 资源索引加载器
    private ResourceDataCodec mResourceCodec;
    // 动态滤镜
    private final WeakReference<DynamicColorBaseFilter> mWeakFilter;
    // 统一变量列表
    private HashMap<String, Integer> mUniformHandleList = new HashMap<>();
    // 纹理列表
    private int[] mTextureList;

    // 句柄
    private int mTexelWidthOffsetHandle = OpenGLUtils.GL_NOT_INIT;
    private int mTexelHeightOffsetHandle = OpenGLUtils.GL_NOT_INIT;
    private int mStrengthHandle = OpenGLUtils.GL_NOT_INIT;
    private float mStrength = 1.0f;
    private float mTexelWidthOffset = 1.0f;
    private float mTexelHeightOffset = 1.0f;

    public DynamicColorLoader(DynamicColorBaseFilter filter, DynamicColorData colorData, String folderPath) {
        mWeakFilter = new WeakReference<>(filter);
        mFolderPath = folderPath.startsWith("file://") ? folderPath.substring("file://".length()) : folderPath;
        mColorData = colorData;
        mStrength = (colorData == null) ? 1.0f : colorData.strength;
        Pair pair = ResourceCodec.getResourceFile(mFolderPath);
        if (pair != null) {
            mResourceCodec = new ResourceDataCodec(mFolderPath + "/" + (String) pair.first, mFolderPath + "/" + pair.second);
        }
        if (mResourceCodec != null) {
            try {
                mResourceCodec.init();
            } catch (IOException e) {
                Log.e(TAG, "DynamicColorLoader: ", e);
                mResourceCodec = null;
            }
        }
        if (!TextUtils.isEmpty(mColorData.audioPath)) {
            if (mWeakFilter.get() != null) {
                mWeakFilter.get().setAudioPath(Uri.parse(mFolderPath + "/" + mColorData.audioPath));
                mWeakFilter.get().setLooping(mColorData.audioLooping);
            }
        }
        loadColorTexture();
    }

    /**
     * 加载纹理
     */
    private void loadColorTexture() {
        if (mColorData.uniformDataList == null || mColorData.uniformDataList.size() <= 0) {
            return;
        }
        mTextureList = new int[mColorData.uniformDataList.size()];
        for (int dataIndex = 0; dataIndex < mColorData.uniformDataList.size(); dataIndex++) {
            Bitmap bitmap = null;
            if (mResourceCodec != null) {
                bitmap = mResourceCodec.loadBitmap(mColorData.uniformDataList.get(dataIndex).value);
            }
            if (bitmap == null) {
                bitmap = BitmapUtils.getBitmapFromFile(mFolderPath + "/" + String.format(mColorData.uniformDataList.get(dataIndex).value));
            }
            if (bitmap != null) {
                mTextureList[dataIndex] = OpenGLUtils.createTexture(bitmap);
                bitmap.recycle();
            } else {
                mTextureList[dataIndex] = OpenGLUtils.GL_NOT_TEXTURE;
            }
        }
    }


    /**
     * 绑定统一变量句柄
     * @param programHandle
     */
    public void onBindUniformHandle(int programHandle) {
        if (programHandle == OpenGLUtils.GL_NOT_INIT || mColorData == null) {
            return;
        }
        mStrengthHandle = GLES30.glGetUniformLocation(programHandle, "strength");
        if (mColorData.texelOffset) {
            mTexelWidthOffsetHandle = GLES30.glGetUniformLocation(programHandle, "texelWidthOffset");
            mTexelHeightOffsetHandle = GLES30.glGetUniformLocation(programHandle, "texelHeightOffset");
        } else {
            mTexelWidthOffsetHandle = OpenGLUtils.GL_NOT_INIT;
            mTexelHeightOffsetHandle = OpenGLUtils.GL_NOT_INIT;
        }
        for (int uniformIndex = 0; uniformIndex < mColorData.uniformList.size(); uniformIndex++) {
            String uniformString = mColorData.uniformList.get(uniformIndex);
            int handle = GLES30.glGetUniformLocation(programHandle, uniformString);
            mUniformHandleList.put(uniformString, handle);
        }
    }

    /**
     * 输入纹理大小
     * @param width
     * @param height
     */
    public void onInputSizeChange(int width, int height) {
        mTexelWidthOffset = 1.0f / width;
        mTexelHeightOffset = 1.0f / height;
    }

    /**
     * 绑定滤镜纹理，只需要绑定一次就行，不用重复绑定，减少开销
     */
    public void onDrawFrameBegin() {
        if (mStrengthHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mStrengthHandle, mStrength);
        }
        if (mTexelWidthOffsetHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mTexelWidthOffsetHandle, mTexelWidthOffset);
        }
        if (mTexelHeightOffsetHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mTexelHeightOffsetHandle, mTexelHeightOffset);
        }

        if (mTextureList == null || mColorData == null) {
            return;
        }
        // 逐个绑定纹理
        for (int dataIndex = 0; dataIndex < mColorData.uniformDataList.size(); dataIndex++) {
            for (int uniformIndex = 0; uniformIndex < mUniformHandleList.size(); uniformIndex++) {
                // 如果统一变量存在，则直接绑定纹理
                Integer handle = mUniformHandleList.get(mColorData.uniformDataList.get(dataIndex).uniform);
                if (handle != null && mTextureList[dataIndex] != OpenGLUtils.GL_NOT_TEXTURE) {
                    OpenGLUtils.bindTexture(handle, mTextureList[dataIndex], dataIndex + 1);
                }
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mTextureList != null && mTextureList.length > 0) {
            GLES30.glDeleteTextures(mTextureList.length, mTextureList, 0);
            mTextureList = null;
        }
        if (mWeakFilter.get() != null) {
            mWeakFilter.clear();
        }
    }

    /**
     * 设置强度
     * @param strength
     */
    public void setStrength(float strength) {
        mStrength = strength;
    }

}
