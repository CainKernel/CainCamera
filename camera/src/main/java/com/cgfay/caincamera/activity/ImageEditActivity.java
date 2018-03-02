package com.cgfay.caincamera.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.caincamera.R;

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ImageEditActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

    private String mImagePath;

    private ImageView mImageView;

    private Button mBtnBack;
    private Button mBtnNext;
    private TextView mEditTitle;

    private Button mBtnBeautify;
    private Button mBtnFilters;
    private Button mBtnFont;
    private Button mBtnGraffiti;
    private Button mBtnCrop;
    private Button mBtnRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_edit);
        mImagePath = getIntent().getStringExtra(PATH);
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {

        mImageView = (ImageView) findViewById(R.id.iv_image);
        mEditTitle = (TextView) findViewById(R.id.edit_title);

        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnBeautify = (Button) findViewById(R.id.btn_beautify);
        mBtnFilters = (Button) findViewById(R.id.btn_filters);
        mBtnFont = (Button) findViewById(R.id.btn_font);
        mBtnGraffiti = (Button) findViewById(R.id.btn_graffiti);
        mBtnCrop = (Button) findViewById(R.id.btn_crop);
        mBtnRotate = (Button) findViewById(R.id.btn_rotate);

        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnBeautify.setOnClickListener(this);
        mBtnFilters.setOnClickListener(this);
        mBtnFont.setOnClickListener(this);
        mBtnGraffiti.setOnClickListener(this);
        mBtnCrop.setOnClickListener(this);
        mBtnRotate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 返回
            case R.id.btn_back:
                break;

            // 下一步
            case R.id.btn_next:
                break;

            // 美颜
            case R.id.btn_beautify:
                break;

            // 滤镜
            case R.id.btn_filters:
                break;

            // 文字
            case R.id.btn_font:
                break;

            // 涂鸦
            case R.id.btn_graffiti:
                break;

            // 裁剪
            case R.id.btn_crop:
                break;

            // 旋转
            case R.id.btn_rotate:
                break;
        }
    }
}



