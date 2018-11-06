package com.cgfay.filterlibrary.glfilter.color;

import android.content.Context;
import android.opengl.GLES30;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filterlibrary.glfilter.base.GLImageAudioFilter;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

/**
 * 颜色滤镜基类
 */
public class DynamicColorBaseFilter extends GLImageAudioFilter {

    // 颜色滤镜参数
    protected DynamicColorData mDynamicColorData;
    protected DynamicColorLoader mDynamicColorLoader;

    public DynamicColorBaseFilter(Context context, DynamicColorData dynamicColorData, String unzipPath) {
        super(context, (dynamicColorData == null || TextUtils.isEmpty(dynamicColorData.vertexShader)) ? VERTEX_SHADER
                        : getShaderString(context, unzipPath, dynamicColorData.vertexShader),
                (dynamicColorData == null || TextUtils.isEmpty(dynamicColorData.fragmentShader)) ? FRAGMENT_SHADER_2D
                        : getShaderString(context, unzipPath, dynamicColorData.fragmentShader));
        mDynamicColorData = dynamicColorData;
        mDynamicColorLoader = new DynamicColorLoader(this, mDynamicColorData, unzipPath);
        mDynamicColorLoader.onBindUniformHandle(mProgramHandle);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mDynamicColorLoader != null) {
            mDynamicColorLoader.onInputSizeChange(width, height);
        }
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        if (mDynamicColorLoader != null) {
            mDynamicColorLoader.onDrawFrameBegin();
        }
    }

    @Override
    public void release() {
        super.release();
        if (mDynamicColorLoader != null) {
            mDynamicColorLoader.release();
        }
    }

    /**
     * 设置强度，调节滤镜的轻重程度
     * @param strength
     */
    public void setStrength(float strength) {
        if (mDynamicColorLoader != null) {
            mDynamicColorLoader.setStrength(strength);
        }
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
