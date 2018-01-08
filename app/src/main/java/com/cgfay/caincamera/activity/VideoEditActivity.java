package com.cgfay.caincamera.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.type.StateType;
import com.cgfay.caincamera.view.FrameThumbnailView;
import com.cgfay.caincamera.view.VideoTextureView;

public class VideoEditActivity extends AppCompatActivity implements View.OnClickListener,
        FrameThumbnailView.BorderScrollListener {

    private static final String TAG = "VideoEditActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

    private static final int MSG_SET_IMAGE = 0x01;

    // 背景颜色
    private int[] mGraffitiBackgrounds = new int[] {
            R.drawable.graffiti_white,
            R.drawable.graffiti_red,
            R.drawable.graffiti_yellow,
            R.drawable.graffiti_blue,
            R.drawable.graffiti_green
    };

    // 涂鸦颜色
    private int[] mGraffitiColors = new int[] {
            R.color.graffitiWhite,
            R.color.graffitiRed,
            R.color.graffitiYellow,
            R.color.graffitiBlue,
            R.color.graffitiGreen
    };

    private String mVideoPath;
    private VideoTextureView mTextureView;

    // 屏幕宽高
    private int mWindowWidth;
    private int mWindowHeight;
    // 视频宽高
    private int mVideoWidth;
    private int mVideoHeight;
    // 视图实际宽高
    private int mViewWidth;
    private int mViewHeight;

    // 导航栏
    private RelativeLayout mLayoutOperation;
    private Button mBtnCancel;
    private Button mBtnDone;

    // 底部功能按钮
    private LinearLayout mLayoutBottom;
    private RelativeLayout mLayoutPen;
    private ImageView mPenView;
    private RelativeLayout mLayoutIcon;
    private RelativeLayout mLayoutText;
    private RelativeLayout mLayoutCutTime;
    // 字体颜色选择区域
    private LinearLayout mLayoutColor;
    // 贴纸选择区域
    private RelativeLayout mLayoutSticker;
    // 文件
    private EditText mEditText;
    // 缩略图选择区域
    private RelativeLayout mLayoutThumb;
    private LinearLayout mLayoutThumbnail;
    private FrameThumbnailView mThumbnailView;

    private int mOperationHight;
    private int mInnerSize;
    private int mOuterSize;

    private int mColorPosition;
    @ColorInt
    private int mCurrentColor; // 当前颜色值

    private int mVideoDuration; // 视频时长
    private int mStartTime; // 开始位置
    private int mEndTime; // 结束位置
    private int mFrameSize; // 显示的帧数目
    private int mSingleFrameTime; // 每帧图片表示的时长
    private ThumbnailTask mThumbnailTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_edit);
        mVideoPath = getIntent().getStringExtra(PATH);
        mWindowWidth = getWindowManager().getDefaultDisplay().getWidth();
        mWindowHeight = getWindowManager().getDefaultDisplay().getHeight();
        initView();
        initCanvasColors();
        initVideoPlayer();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mOperationHight = (int)getResources().getDimension(R.dimen.dp40);
        mInnerSize = (int)getResources().getDimension(R.dimen.dp20);
        mOuterSize = (int)getResources().getDimension(R.dimen.dp25);

        mTextureView = (VideoTextureView) findViewById(R.id.video_view);

        mLayoutOperation = (RelativeLayout) findViewById(R.id.layout_operation);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnDone = (Button) findViewById(R.id.btn_done);

        mLayoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        mLayoutPen = (RelativeLayout) findViewById(R.id.layout_pen);
        mPenView = (ImageView) findViewById(R.id.iv_pen);
        mLayoutIcon = (RelativeLayout) findViewById(R.id.layout_icon);
        mLayoutText = (RelativeLayout) findViewById(R.id.layout_text);
        mLayoutCutTime = (RelativeLayout) findViewById(R.id.layout_cut_time);

        mLayoutColor = (LinearLayout) findViewById(R.id.layout_color);
        mLayoutSticker = (RelativeLayout)findViewById(R.id.layout_sticker);
        mLayoutThumb = (RelativeLayout) findViewById(R.id.layout_thumb);
        mLayoutThumbnail = (LinearLayout) findViewById(R.id.layout_thumbnail);
        mThumbnailView = (FrameThumbnailView) findViewById(R.id.thumbnailView);
        mThumbnailView.setOnBorderScrollListener(this);

        mEditText = (EditText) findViewById(R.id.et_text);

        mBtnDone.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        mLayoutPen.setOnClickListener(this);
        mLayoutIcon.setOnClickListener(this);
        mLayoutText.setOnClickListener(this);
        mLayoutCutTime.setOnClickListener(this);
    }

    /**
     * 初始化颜色选择器
     */
    private void initCanvasColors() {
        for (int i = 0; i < mGraffitiBackgrounds.length; i++) {
            RelativeLayout relativeLayout = new RelativeLayout(this);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            relativeLayout.setLayoutParams(layoutParams);
            // 自定义颜色
            View view = new View(this);
            view.setBackgroundDrawable(getResources().getDrawable(mGraffitiBackgrounds[i]));
            RelativeLayout.LayoutParams innerParams =
                    new RelativeLayout.LayoutParams(mInnerSize, mInnerSize);
            innerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            view.setLayoutParams(innerParams);
            relativeLayout.addView(view);

            // 选中的外圆大小
            final View selectView = new View(this);
            selectView.setBackgroundResource(R.drawable.color_select);
            RelativeLayout.LayoutParams outerParams =
                    new RelativeLayout.LayoutParams(mOuterSize, mOuterSize);
            outerParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            selectView.setLayoutParams(outerParams);
            if(i != 0) {
                selectView.setVisibility(View.GONE);
            }
            relativeLayout.addView(selectView);

            final int position = i;
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mColorPosition != position) {
                        selectView.setVisibility(View.VISIBLE);
                        ViewGroup parent = (ViewGroup) v.getParent();
                        ViewGroup childView = (ViewGroup) parent.getChildAt(mColorPosition);
                        childView.getChildAt(1).setVisibility(View.GONE);
                        // 选择颜色
                        mCurrentColor = getResources().getColor(mGraffitiColors[position]);
                        mColorPosition = position;
                    }
                }
            });

            mLayoutColor.addView(relativeLayout, i);
        }
        // TODO 最右边添加一个返回按钮，用于取消选中
    }


    /**
     * 初始化播放器
     */
    private void initVideoPlayer() {
        mTextureView.setVideoPath(mVideoPath);
        mTextureView.setPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 设定时长
                mVideoDuration = mTextureView.getDuration();
                mStartTime = 0;
                mEndTime = mVideoDuration;
                // 开始播放
                mTextureView.setLooping(true);
                mTextureView.start();
            }
        });

        mTextureView.setPlayStateListener(new VideoTextureView.PlayStateListener() {
            @Override
            public void onStateChanged(StateType state) {
                if (state == StateType.PLAYING) {
                    mVideoWidth = mTextureView.getVideoWidth();
                    mVideoHeight = mTextureView.getVideoHeight();
                    Log.d("onnStateChanged", "windowWidth = " + mWindowWidth + ", windowHeight = " + mWindowHeight);

                    float ratio = mVideoWidth * 1f / mVideoHeight;
                    double viewAspectRatio = (double) mWindowWidth / mWindowHeight;
                    double aspectDiff = ratio / viewAspectRatio - 1;
                    if (Math.abs(aspectDiff) >= 0.01) {
                        mViewWidth = mWindowWidth;
                        mViewHeight = (int)(mWindowWidth / ratio);
                    }
                    ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
                    layoutParams.width = mViewWidth;
                    layoutParams.height = mViewHeight;
                    mTextureView.setLayoutParams(layoutParams);
                    // 设定涂鸦视图大小

                }
            }

            @Override
            public void onPlayChangedError(StateType state, String message) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 取消
            case R.id.btn_cancel:
                operationCancel();
                break;

            // 完成
            case R.id.btn_done:
                operationDone();
                break;

            // 画笔
            case R.id.layout_pen:
                showColor();
                break;

            // 贴纸
            case R.id.layout_icon:
                showSticker();
                break;

            // 文字
            case R.id.layout_text:
                showEditText();
                break;

            // 剪辑
            case R.id.layout_cut_time:
                showCutTimeView();
                break;

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView.start();
    }

    @Override
    public void OnBorderScroll(float start, float end) {
        calculatePlayTime();
    }

    @Override
    public void onScrollStateChange() {
        seekToNewPosition();
    }

    /**
     * 操作取消
     */
    private void operationCancel() {
        resetBottomEditView();
    }

    /**
     * 操作完成
     */
    private void operationDone() {
        resetBottomEditView();
    }

    /**
     * 显示画笔颜色
     */
    private void showColor() {
        resetBottomEditView();
        mLayoutColor.setVisibility(View.VISIBLE);
    }

    /**
     * 显示贴纸
     */
    private void showSticker() {
        resetBottomEditView();
        mLayoutSticker.setVisibility(View.VISIBLE);
    }

    /**
     * 显示文字编辑
     */
    private void showEditText() {
        resetBottomEditView();
        mEditText.setVisibility(View.VISIBLE);
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
    }

    /**
     * 显示视频剪辑视图
     */
    private void showCutTimeView() {
        resetBottomEditView();
        mLayoutBottom.setVisibility(View.GONE);
        mLayoutThumb.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) mLayoutOperation.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM;
        mLayoutOperation.setLayoutParams(layoutParams);
        showThumbnailView();
        // TODO TextureView缩小
    }

    /**
     * 重置底部功能栏
     */
    private void resetBottomEditView() {
        mLayoutBottom.setVisibility(View.VISIBLE);
        mLayoutColor.setVisibility(View.GONE);
        mLayoutSticker.setVisibility(View.GONE);
        mEditText.setVisibility(View.GONE);
        mEditText.clearFocus();
        mLayoutThumb.setVisibility(View.GONE);
        FrameLayout.LayoutParams layoutParams =
                (FrameLayout.LayoutParams) mLayoutOperation.getLayoutParams();
        layoutParams.gravity = Gravity.TOP;
        mLayoutOperation.setLayoutParams(layoutParams);
        // TODO TextureView恢复
    }


    /**
     * 显示缩略图
     */
    private void showThumbnailView() {
        // 计算视图
        int pixWidth = (int) 500.0f / mVideoDuration * mWindowWidth;
        mThumbnailView.setMinInterval(pixWidth);
        mFrameSize = 10; // 显示10帧
        mSingleFrameTime = mVideoDuration / mFrameSize * 1000;
        // 缩略图宽度 TODO 此时的mThumbnailView并不能获取到width，以后再做优化
        int width = mWindowWidth / mFrameSize;
        for (int i = 0; i < mFrameSize; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(
                    new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setBackgroundColor(Color.parseColor("#000000"));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            mLayoutThumbnail.addView(imageView);
        }
        if (mThumbnailTask == null) {
            mThumbnailTask = new ThumbnailTask();
            mThumbnailTask.execute();
        }
    }


    /**
     * 计算播放的时间区域
     */
    private void calculatePlayTime() {
        float left = mThumbnailView.getLeftInterval();
        float percent = left / mThumbnailView.getWidth();
        mStartTime = (int) (mVideoDuration * percent);

        float right = mThumbnailView.getRightInterval();
        percent = right / mThumbnailView.getWidth();
        mEndTime = (int) (mVideoDuration * percent);
    }

    /**
     * 切换到新的位置
     */
    private void seekToNewPosition() {
        mTextureView.loopRegion(mStartTime, mEndTime);
    }

    /**
     * 处理回调
     */
    private Handler mProcessHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_IMAGE:
                    ImageView imageView = (ImageView) mLayoutThumbnail.getChildAt(msg.arg1);
                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                    break;
            }
        }
    };


    /**
     * 获取缩略图线程
     */
    private class ThumbnailTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(mVideoPath);
            for (int i = 0; i < mFrameSize; i++) {
                Bitmap bitmap = metadataRetriever.getFrameAtTime(mSingleFrameTime * i,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                mProcessHandler.sendMessage(
                        mProcessHandler.obtainMessage(MSG_SET_IMAGE, i, -1, bitmap));
            }
            metadataRetriever.release();
            return true;
        }
    }

}
