package com.cgfay.cainfilter.glfilter.sticker;

import com.cgfay.cainfilter.glfilter.base.GLImageFilter;

/**
 * 贴纸滤镜，贴纸在经过透视变换、人脸在侧脸、抬头、低头等都会产生一个姿态角，
 * 根据姿态角做综合变换，我们可以得到贴纸在视锥体中的实际三维坐标。贴纸在经过三维坐标的变换后，如果需要跟
 * 原来的图像做混合处理，则需要自己手动计算透视变换，取得变换后投影到屏幕的实际UV坐标，然后在fragment shader
 * 里面做混合处理，比如我要变换各种颜色、色调等，则可以改变贴纸的颜色、色调、明亮程度等、然后再跟原图像进行混合
 * 这样同一个贴纸也能产生不同的颜色、色调效果
 * Created by cain.huang on 2017/11/24.
 */

public class GLStickerFilter extends GLImageFilter {

    protected static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                                   \n" +
            "uniform mat4 uTexMatrix;                                   \n" +
            "attribute vec4 aPosition;                                  \n" +
            "attribute vec4 aTextureCoord;                              \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "void main() {                                              \n" +
            "    gl_Position = uMVPMatrix * aPosition;                  \n" +
            "    textureCoordinate = aTextureCoord.xy;                  \n" +
            "}                                                          \n";

    protected static final String FRAGMENT_SHADER =
            "precision mediump float;                                       \n" +
            "varying vec2 textureCoordinate;                                \n" +
            "uniform sampler2D inputTexture;                                \n" +
            "void main() {                                                  \n" +
            "    gl_FragColor = texture2D(inputTexture, textureCoordinate); \n" +
            "}                                                              \n";

    // 贴纸集合
    private GLStickerFilterSet mStickerFilterSet;

    public GLStickerFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLStickerFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mStickerFilterSet = new GLStickerFilterSet(mProgramHandle, muMVPMatrixLoc, maPositionLoc,
                maTextureCoordLoc, mInputTextureLoc);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mStickerFilterSet != null) {
            mStickerFilterSet.onInputSizeChanged(width, height);
        }
    }

    @Override
    protected void unBindValue() {
        super.unBindValue();
        // 在绘制完原图解除绑定后，执行绘制贴纸操作，利用同一个program，减少GPU不断创建和切换program的开销
        // 动态贴纸使用的program都是同一个，姿态角也是同一个,
        // 只是VertexCoordinate 和 TextureCoordinate坐标不一样而已
        // 因此，在将原图绘制到FBO之后，就可以直接使用同一个program，将贴纸绘制到同一个FBO中
        if (mStickerFilterSet != null) {
            mStickerFilterSet.drawSubSticker();
        }
    }

    @Override
    public void release() {
        // 释放资源
        mStickerFilterSet.release();
        mStickerFilterSet = null;
        super.release();
    }
}
