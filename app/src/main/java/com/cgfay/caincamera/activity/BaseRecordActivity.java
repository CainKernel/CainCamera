package com.cgfay.caincamera.activity;

import android.graphics.SurfaceTexture;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public abstract class BaseRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 显示控件
     */
    public abstract void showViews();

    /**
     * 隐藏控件
     */
    public abstract void hideViews();

    /**
     * 设置录制进度
     */
    public abstract void setRecordProgress(float progress);

    /**
     * 加入一段视频
     */
    public abstract void addProgressSegment(float progress);

    /**
     * 删除一段视频
     */
    public abstract void deleteProgressSegment();

    /**
     * 绑定SurfaceTexture
     */
    public abstract void bindSurfaceTexture(@NonNull SurfaceTexture surfaceTexture);

    /**
     * 帧可用刷新
     */
    public abstract void onFrameAvailable();

    /**
     * 更新纹理大小
     */
    public abstract void updateTextureSize(int width, int height);

    /**
     * 显示对话框
     */
    public abstract void showProgressDialog();

    /**
     * 隐藏对话框
     */
    public abstract void hideProgressDialog();

    /**
     * 显示Toast提示
     */
    public abstract void showToast(String tips);

}