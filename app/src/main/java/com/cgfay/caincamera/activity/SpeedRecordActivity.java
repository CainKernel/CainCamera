package com.cgfay.caincamera.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.presenter.RecordPresenter;
import com.cgfay.caincamera.renderer.RecordRenderer;
import com.cgfay.caincamera.widget.GLRecordView;
import com.cgfay.camera.widget.RecordButton;
import com.cgfay.camera.widget.RecordProgressView;
import com.cgfay.camera.widget.RecordSpeedLevelBar;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec;
import com.cgfay.media.recorder.SpeedMode;
import com.cgfay.uitls.utils.NotchUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.utils.StatusBarUtils;

import java.io.File;

/**
 * 倍速录制测试
 */
public class SpeedRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private GLRecordView mGLRecordView;
    private RecordProgressView mProgressView;
    private RecordSpeedLevelBar mRecordSpeedBar;
    private RecordButton mRecordButton;

    private RecordRenderer mRenderer;
    private RecordPresenter mPresenter;

    private View mBtnSwitch;
    private Button mBtnNext;
    private Button mBtnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_record);
        mPresenter = new RecordPresenter(this);
        mPresenter.setRecordSeconds(15);
        mRenderer = new RecordRenderer(mPresenter);
        // 录制预览
        mGLRecordView = (GLRecordView) findViewById(R.id.gl_record_view);
        mGLRecordView.setEGLContextClientVersion(3);
        mGLRecordView.setRenderer(mRenderer);
        mGLRecordView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLRecordView.addOnTouchScroller(mTouchScroller);
        // 进度条
        mProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);

        // 速度条
        mRecordSpeedBar = (RecordSpeedLevelBar) findViewById(R.id.record_speed_bar);
        mRecordSpeedBar.setOnSpeedChangedListener((speed) -> {
            mPresenter.setSpeedMode(SpeedMode.valueOf(speed.getSpeed()));
        });

        // 录制按钮
        mRecordButton = (RecordButton) findViewById(R.id.btn_record);
        mRecordButton.addRecordStateListener(new RecordButton.RecordStateListener() {
            @Override
            public void onRecordStart() {
                mGLRecordView.post(() -> {
                    mPresenter.onBindSharedContext(mRenderer.getCurrentContext());
                    mPresenter.startRecord();
                });
            }

            @Override
            public void onRecordStop() {
                mPresenter.stopRecord();
            }

            @Override
            public void onZoom(float percent) {

            }
        });

        // 切换相机
        mBtnSwitch = findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(this);

        // 下一步
        mBtnNext = findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);

        // 删除按钮
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnDelete.setOnClickListener(this);

        // 判断是否存在刘海屏
        if (NotchUtils.hasNotchScreen(this)) {
            View view = findViewById(R.id.view_safety_area);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.height = StatusBarUtils.getStatusBarHeight(this);
            view.setLayoutParams(params);
        }

        showViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleFullScreen();
        mGLRecordView.onResume();
        mPresenter.onResume();
        mPresenter.setAudioEnable(PermissionUtils.permissionChecking(this, Manifest.permission.RECORD_AUDIO));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLRecordView.onPause();
        mPresenter.onPause();
        mRenderer.clear();
    }

    @Override
    protected void onDestroy() {
        mPresenter.release();
        mPresenter = null;
        super.onDestroy();
    }

    private void handleFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        // 是否全面屏
        if (NotchUtils.hasNotchScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(lp);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_switch) {
            mPresenter.switchCamera();
        } else if (id == R.id.btn_delete) {
            mPresenter.deleteLastVideo();
        } else if (id == R.id.btn_next) {
            mPresenter.mergeAndEdit();
        }
    }

    /**
     * 隐藏控件
     */
    public void hidViews() {
        runOnUiThread(() -> {
            if (mRecordSpeedBar != null) {
                mRecordSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnDelete != null) {
                mBtnDelete.setVisibility(View.GONE);
            }
            if (mBtnNext != null) {
                mBtnNext.setVisibility(View.GONE);
            }
            if (mBtnSwitch != null) {
                mBtnSwitch.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 显示控件
     */
    public void showViews() {
        runOnUiThread(() -> {
            if (mRecordSpeedBar != null) {
                mRecordSpeedBar.setVisibility(View.VISIBLE);
            }
            boolean showEditEnable = mPresenter.getRecordVideoSize() > 0;
            if (mBtnDelete != null) {
                mBtnDelete.setVisibility(showEditEnable ? View.VISIBLE : View.GONE);
            }
            if (mBtnNext != null) {
                mBtnNext.setVisibility(showEditEnable ? View.VISIBLE : View.GONE);
            }
            if (mBtnSwitch != null) {
                mBtnSwitch.setVisibility(View.VISIBLE);
            }
            if (mRecordButton != null) {
                mRecordButton.reset();
            }
        });
    }

    /**
     * 设置进度
     * @param progress
     */
    public void setProgress(float progress) {
        runOnUiThread(() -> {
            mProgressView.setProgress(progress);
        });
    }

    /**
     * 添加一段进度
     * @param progress
     */
    public void addProgressSegment(float progress) {
        runOnUiThread(() -> {
            mProgressView.addProgressSegment(progress);
        });
    }

    /**
     * 删除一段进度
     */
    public void deleteProgressSegment() {
        runOnUiThread(() -> {
            mProgressView.deleteProgressSegment();
        });
    }

    /**
     * 绑定相机输出的SurfaceTexture
     * @param surfaceTexture
     */
    public void bindSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        mGLRecordView.queueEvent(() -> mRenderer.bindSurfaceTexture(surfaceTexture));
    }

    /**
     * 更新预览纹理大小
     * @param width
     * @param height
     */
    public void updateTextureSize(int width, int height) {
        if (mRenderer != null) {
            mRenderer.setTextureSize(width, height);
        }
    }

    /**
     * 刷新画面
     */
    public void onFrameAvailable() {
        if (mGLRecordView != null) {
            mGLRecordView.requestRender();
        }
    }


    private Dialog mProgressDialog;
    /**
     * 显示合成进度
     */
    public void showProgressDialog() {
        runOnUiThread(() -> {
            mProgressDialog = ProgressDialog.show(this, "正在合成", "正在合成");
        });
    }

    /**
     * 隐藏合成进度
     */
    public void hideProgressDialog() {
        runOnUiThread(() -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        });
    }

    private Toast mToast;
    /**
     * 显示Toast提示
     * @param msg
     */
    public void showToast(String msg) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(SpeedRecordActivity.this, msg, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

    // ----------------------------------------- 切换滤镜 -------------------------------------------
    private int mFilterIndex;
    private GLRecordView.OnTouchScroller mTouchScroller = new GLRecordView.OnTouchScroller() {
        @Override
        public void swipeBack() {
            mFilterIndex++;
            if (mFilterIndex >= FilterHelper.getFilterList().size()) {
                mFilterIndex = 0;
            }
            changeDynamicFilter(mFilterIndex);
        }

        @Override
        public void swipeFrontal() {
            mFilterIndex--;
            if (mFilterIndex < 0) {
                int count = FilterHelper.getFilterList().size();
                mFilterIndex = count > 0 ? count - 1 : 0;
            }
            changeDynamicFilter(mFilterIndex);
        }

        @Override
        public void swipeUpper(boolean startInLeft, float distance) {

        }

        @Override
        public void swipeDown(boolean startInLeft, float distance) {

        }
    };

    /**
     * 切换动态滤镜
     * @param filterIndex
     */
    public void changeDynamicFilter(int filterIndex) {
        if (mGLRecordView != null) {
            mGLRecordView.queueEvent(() -> {
                String folderPath = FilterHelper.getFilterDirectory(this) + File.separator +
                        FilterHelper.getFilterList().get(filterIndex).unzipFolder;
                DynamicColor color = null;
                if (!FilterHelper.getFilterList().get(filterIndex).unzipFolder.equalsIgnoreCase("none")) {
                    try {
                        color = ResourceJsonCodec.decodeFilterData(folderPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mRenderer.changeDynamicFilter(SpeedRecordActivity.this, color);
            });
        }
    }
}
