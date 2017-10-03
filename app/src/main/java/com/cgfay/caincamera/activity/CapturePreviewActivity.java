package com.cgfay.caincamera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.type.GalleryType;
import com.cgfay.caincamera.utils.FileUtils;
import com.cgfay.caincamera.view.AspectFrameLayout;
import com.cgfay.caincamera.view.preview.GifSurfaceView;
import com.cgfay.caincamera.view.preview.PictureSurfaceView;
import com.cgfay.caincamera.view.preview.PreviewSurfaceView;
import com.cgfay.caincamera.view.preview.VideoSurfaceView;

import java.io.File;

public class CapturePreviewActivity extends AppCompatActivity
        implements View.OnClickListener {
    private static final String TAG = "CapturePreviewActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";
    // 路径
    private String mPath;

    // layout
    private AspectFrameLayout mPreviewLayout;
    // 预览
    private PreviewSurfaceView mSurfaceView;
    // 取消
    private Button mBtnCancel;
    // 保存
    private Button mBtnSave;
    // 分享
    private Button mBtnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capture_preview);
        mPath = getIntent().getStringExtra(PATH);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mPreviewLayout = (AspectFrameLayout) findViewById(R.id.layout_preview);

        if (ParamsManager.mGalleryType == GalleryType.GIF) {
            mSurfaceView = new GifSurfaceView(this);
        } else if (ParamsManager.mGalleryType == GalleryType.PICTURE) {
            mSurfaceView = new PictureSurfaceView(this);
        } else if (ParamsManager.mGalleryType == GalleryType.VIDEO) {
            mSurfaceView = new VideoSurfaceView(this, mPreviewLayout);
        }
        mSurfaceView.setPath(mPath);
        mPreviewLayout.addView(mSurfaceView);

        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnShare = (Button) findViewById(R.id.btn_share);

        mBtnCancel.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mBtnShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                executeCancel();
                break;

            case R.id.btn_save:
                executeSave();
                break;

            case R.id.btn_share:
                executeShare();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        executeCancel();
    }

    /**
     * 执行取消动作
     */
    private void executeCancel() {
        // 删除文件
        File file = new File(mPath);
        if (file.exists()) {
            file.delete();
        }
        // 关掉页面
        finish();
    }


    /**
     * 执行保存操作
     */
    private void executeSave() {
        File file = new File(mPath);
        String newPath = ParamsManager.AlbumPath + file.getName();
        FileUtils.copyFile(mPath, newPath);
    }


    /**
     * 执行分享操作
     */
    private void executeShare() {

    }
}
