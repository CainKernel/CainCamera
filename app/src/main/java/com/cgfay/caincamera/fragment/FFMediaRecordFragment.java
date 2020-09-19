package com.cgfay.caincamera.fragment;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.presenter.FFMediaRecordPresenter;
import com.cgfay.caincamera.renderer.FFRecordRenderer;
import com.cgfay.caincamera.widget.GLRecordView;
import com.cgfay.camera.widget.RecordButton;
import com.cgfay.camera.widget.RecordProgressView;
import com.cgfay.uitls.utils.NotchUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.utils.StatusBarUtils;


/**
 * 利用FFmpeg录制视频
 */
public class FFMediaRecordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FFMediaRecordFragment";

    private FragmentActivity mActivity;
    private Handler mMainHandler;

    private View mContentView;
    private GLRecordView mGLRecordView;
    private RecordProgressView mProgressView;
    private RecordButton mRecordButton;

    private View mBtnSwitch;
    private Button mBtnNext;
    private Button mBtnDelete;

    private AudioManager mAudioManager;

    private FFMediaRecordPresenter mPresenter;
    private FFRecordRenderer mRenderer;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_ffmedia_record, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        mPresenter = new FFMediaRecordPresenter(mActivity, this);
        mPresenter.setRecordSeconds(15);
        mRenderer = new FFRecordRenderer(mPresenter);

        // 录制预览
        mGLRecordView = (GLRecordView) mContentView.findViewById(R.id.gl_record_view);
        mGLRecordView.setEGLContextClientVersion(3);
        mGLRecordView.setRenderer(mRenderer);
        mGLRecordView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // 进度条
        mProgressView = (RecordProgressView) mContentView.findViewById(R.id.record_progress_view);

        mBtnSwitch = mContentView.findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(v -> {
            if (!mPresenter.isRecording()) {
                mPresenter.switchCamera();
            }
        });

        // 录制按钮
        mRecordButton = mContentView.findViewById(R.id.btn_record);
        mRecordButton.addRecordStateListener(new RecordButton.RecordStateListener() {
            @Override
            public void onRecordStart() {
                mPresenter.startRecord();
            }

            @Override
            public void onRecordStop() {
                mPresenter.stopRecord();
            }

            @Override
            public void onZoom(float percent) {

            }
        });

        // 下一步
        mBtnNext = mContentView.findViewById(R.id.btn_next);
        mBtnNext.setOnClickListener(this);

        // 删除按钮
        mBtnDelete = mContentView.findViewById(R.id.btn_delete);
        mBtnDelete.setOnClickListener(this);

        // 判断是否存在刘海屏
        if (NotchUtils.hasNotchScreen(mActivity)) {
            View view = mContentView.findViewById(R.id.view_safety_area);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.height = StatusBarUtils.getStatusBarHeight(mActivity);
            view.setLayoutParams(params);
        }
        showViews();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        handleFullScreen();
        mGLRecordView.onResume();
        mPresenter.onResume();
        mPresenter.setAudioEnable(PermissionUtils.permissionChecking(this, Manifest.permission.RECORD_AUDIO));
    }

    @Override
    public void onPause() {
        super.onPause();
        mGLRecordView.onPause();
        mPresenter.onPause();
        mRenderer.clear();
    }

    @Override
    public void onDestroy() {
        mPresenter.release();
        mPresenter = null;
        super.onDestroy();
    }

    private void runOnUiThread(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    private void handleFullScreen() {
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        // 是否全面屏
        if (NotchUtils.hasNotchScreen(mActivity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                mActivity.getWindow().setAttributes(lp);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_delete) {
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
            mProgressDialog = ProgressDialog.show(mActivity, "正在合成", "请稍后");
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
            mToast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }
}
