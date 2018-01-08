package com.cgfay.caincamera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.view.FrameThumbnailView;
import com.cgfay.caincamera.view.VideoTextureView;

public class VideoEditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VideoEditActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

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
    // 缩略图选择区域
    private RelativeLayout mLayoutThumb;
    private LinearLayout mLayoutThumbnail;
    private FrameThumbnailView mThumbnailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
        mVideoPath = getIntent().getStringExtra(PATH);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
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


        mBtnDone.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        mLayoutPen.setOnClickListener(this);
        mLayoutIcon.setOnClickListener(this);
        mLayoutText.setOnClickListener(this);
        mLayoutCutTime.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 取消
            case R.id.btn_cancel:

                break;

            // 完成
            case R.id.btn_done:
                break;

            // 画笔
            case R.id.layout_pen:
                break;

            // 贴纸
            case R.id.layout_icon:
                break;

            // 文字
            case R.id.layout_text:
                break;

            // 剪辑
            case R.id.layout_cut_time:
                break;

        }
    }


}
