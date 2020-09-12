package com.cgfay.caincamera.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.os.Bundle;
import com.cgfay.caincamera.R;
import com.cgfay.caincamera.presenter.RecordPresenter;
import com.cgfay.caincamera.renderer.DuetRecordRenderer;
import com.cgfay.caincamera.renderer.DuetType;
import com.cgfay.caincamera.widget.GLRecordView;
import com.cgfay.camera.widget.RecordButton;
import com.cgfay.camera.widget.RecordProgressView;
import com.cgfay.picker.model.MediaData;
import com.cgfay.uitls.utils.NotchUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.utils.StatusBarUtils;

/**
 * 同框录制
 */
public class DuetRecordActivity extends BaseRecordActivity implements View.OnClickListener {

    public static final String DUET_MEDIA = "DUET_MEDIA";

    private GLRecordView mGLRecordView;
    private RecordProgressView mProgressView;
    private RecordButton mRecordButton;

    private DuetRecordRenderer mRenderer;
    private RecordPresenter mPresenter;

    private View mBtnSwitch;
    private Button mBtnNext;
    private Button mBtnDelete;

    private Button mBtnDuet;
    private Button mBtnDuetFlip;
    private LinearLayout mLayoutDuetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duet_record);
        mPresenter = new RecordPresenter(this);
        mPresenter.setRecordSeconds(15);
        mRenderer = new DuetRecordRenderer(mPresenter);
        MediaData duetMedia = getIntent().getParcelableExtra(DUET_MEDIA);
        if (duetMedia != null) {
            mRenderer.setDuetVideo(duetMedia);
        }
        // 录制预览
        mGLRecordView = (GLRecordView) findViewById(R.id.gl_record_view);
        mGLRecordView.setEGLContextClientVersion(3);
        mGLRecordView.setRenderer(mRenderer);
        mGLRecordView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // 进度条
        mProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);

        // 录制按钮
        mRecordButton = (RecordButton) findViewById(R.id.btn_record);
        mRecordButton.addRecordStateListener(new RecordButton.RecordStateListener() {
            @Override
            public void onRecordStart() {
                mPresenter.startRecord();
                mRenderer.playVideo();
            }

            @Override
            public void onRecordStop() {
                mPresenter.stopRecord();
                mRenderer.stopVideo();
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

        // 选择同框类型
        mBtnDuet = findViewById(R.id.btn_next_duet);
        mBtnDuet.setOnClickListener(this);
        // 翻转同框
        mBtnDuetFlip = findViewById(R.id.btn_duet_flip);
        mBtnDuetFlip.setOnClickListener(this);
        mBtnDuetFlip.setVisibility(duetMedia != null ? View.VISIBLE : View.GONE);

        // 同框类型
        mLayoutDuetType = findViewById(R.id.layout_duet_type);
        mLayoutDuetType.findViewById(R.id.btn_duet_left_right).setOnClickListener(this);
        mLayoutDuetType.findViewById(R.id.btn_duet_up_down).setOnClickListener(this);
        mLayoutDuetType.findViewById(R.id.btn_duet_big_small).setOnClickListener(this);

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
        getWindow().setFlags(LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        // 是否全面屏
        if (NotchUtils.hasNotchScreen(this)) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            LayoutParams lp = getWindow().getAttributes();
            if (VERSION.SDK_INT >= VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
            getWindow().setAttributes(lp);
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
        } else if (id == R.id.btn_next_duet) {
            mLayoutDuetType.setVisibility(View.VISIBLE);
            mBtnDuet.setVisibility(View.GONE);
            mBtnDuetFlip.setVisibility(View.GONE);
        } else if (id == R.id.btn_duet_flip) {
            mRenderer.flip();
        } else if (id == R.id.btn_duet_left_right) {
            mRenderer.setDuetType(DuetType.DUET_TYPE_LEFT_RIGHT);
            hidDuetTypeViews();
        } else if (id == R.id.btn_duet_up_down) {
            mRenderer.setDuetType(DuetType.DUET_TYPE_UP_DOWN);
            hidDuetTypeViews();
        } else if (id == R.id.btn_duet_big_small) {
            mRenderer.setDuetType(DuetType.DUET_TYPE_BIG_SMALL);
            hidDuetTypeViews();
        }
    }

    private void hidDuetTypeViews() {
        if (mLayoutDuetType != null) {
            mLayoutDuetType.setVisibility(View.GONE);
        }
        if (mBtnDuet != null) {
            mBtnDuet.setVisibility(View.VISIBLE);
        }
        if (mBtnDuetFlip != null) {
            mBtnDuetFlip.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏控件
     */
    @Override
    public void hideViews() {
        runOnUiThread(() -> {
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
    @Override
    public void showViews() {
        runOnUiThread(() -> {
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
    @Override
    public void setRecordProgress(float progress) {
        runOnUiThread(() -> {
            mProgressView.setProgress(progress);
        });
    }

    /**
     * 添加一段进度
     * @param progress
     */
    @Override
    public void addProgressSegment(float progress) {
        runOnUiThread(() -> {
            mProgressView.addProgressSegment(progress);
        });
    }

    /**
     * 删除一段进度
     */
    @Override
    public void deleteProgressSegment() {
        runOnUiThread(() -> {
            mProgressView.deleteProgressSegment();
        });
    }

    /**
     * 绑定相机输出的SurfaceTexture
     * @param surfaceTexture
     */
    @Override
    public void bindSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        mGLRecordView.queueEvent(() -> mRenderer.bindSurfaceTexture(surfaceTexture));
    }

    /**
     * 更新预览纹理大小
     * @param width
     * @param height
     */
    @Override
    public void updateTextureSize(int width, int height) {
        if (mRenderer != null) {
            mRenderer.setTextureSize(width, height);
        }
    }

    /**
     * 刷新画面
     */
    @Override
    public void onFrameAvailable() {
        if (mGLRecordView != null) {
            mGLRecordView.requestRender();
        }
    }


    private Dialog mProgressDialog;
    /**
     * 显示合成进度
     */
    @Override
    public void showProgressDialog() {
        runOnUiThread(() -> {
            mProgressDialog = ProgressDialog.show(this, "正在合成", "正在合成");
        });
    }

    /**
     * 隐藏合成进度
     */
    @Override
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
    @Override
    public void showToast(String msg) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

}