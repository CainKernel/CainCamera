package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.EffectFilterAdapter;
import com.cgfay.cainfilter.camerarender.ColorFilterManager;
import com.cgfay.cainfilter.camerarender.ParamsManager;
import com.cgfay.utilslibrary.AsyncRecyclerview;
import com.cgfay.utilslibrary.BitmapUtils;

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ImageEditActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";

    private String mImagePath;

    // 内容栏
    private FrameLayout mLayoutContent;
    private ImageView mImageView;

    private LayoutInflater mInflater;   // 布局加载器器

    // 顶部导航栏
    private RelativeLayout mLayoutNavigation;
    private Button mBtnBack;
    private Button mBtnNext;
    private TextView mEditTitle;

    private FrameLayout mLayoutBottom;  // 底部编辑栏
    // 底部按钮scrollview
    private HorizontalScrollView mScrollView;
    private Button mBtnBeautify;        // 一键美化
    private Button mBtnFilters;         // 特效
    private Button mBtnCropRotate;      // 裁剪旋转
    private Button mBtnStickers;        // 贴纸
    private Button mBtnFont;            // 文字
    private Button mBtnAdjust;          // 调节
    private Button mBtnGraffiti;        // 涂鸦
    private Button mBtnMosaic;          // 马赛克
    private Button mBtnEdge;            // 边框
    private Button mBtnEnhancement;     // 夜景增强
    private Button mBtnBlur;            // 虚化
    private Button mBtnMatBlur;         // 抠图虚化

    // 图片管理器
    ImageEditManager mEditManager;

    // 处于编辑状态
    private boolean mEditerShowing = false;

    // 特效列表
    private AsyncRecyclerview mFilterListView;
    private LinearLayoutManager mFilterListManager;
    private int mColorIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_edit);
        mImagePath = getIntent().getStringExtra(PATH);
        mInflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
        initImageEditManager();
        initFilterListView();
    }

    /**
     * 初始化视图
     */
    private void initView() {

        // 顶部导航栏
        mLayoutNavigation = (RelativeLayout) findViewById(R.id.layout_navigation);
        mEditTitle = (TextView) findViewById(R.id.edit_title);
        mEditTitle.setText(getResources().getText(R.string.image_edit_title));
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);

        // 内容栏
        mLayoutContent = (FrameLayout) findViewById(R.id.layout_content);
        mImageView = (ImageView) findViewById(R.id.iv_image);

        // 底部编辑栏
        mScrollView = (HorizontalScrollView) mInflater
                .inflate(R.layout.view_image_edit_bottom, null);
        mLayoutBottom = (FrameLayout) findViewById(R.id.layout_bottom);
        resetBottomView();

        mBtnBeautify = (Button) findViewById(R.id.btn_beautify);
        mBtnFilters = (Button) findViewById(R.id.btn_filters);
        mBtnCropRotate = (Button) findViewById(R.id.btn_crop_rotate);
        mBtnStickers = (Button) findViewById(R.id.btn_stickers);
        mBtnFont = (Button) findViewById(R.id.btn_font);
        mBtnAdjust = (Button) findViewById(R.id.btn_adjust);
        mBtnGraffiti = (Button) findViewById(R.id.btn_grafitti);
        mBtnMosaic = (Button) findViewById(R.id.btn_mosaic);
        mBtnEdge = (Button) findViewById(R.id.btn_edge);
        mBtnEnhancement = (Button) findViewById(R.id.btn_enhancement);
        mBtnBlur = (Button) findViewById(R.id.btn_blur);
        mBtnMatBlur = (Button) findViewById(R.id.btn_mat_blur);

        mBtnBeautify.setOnClickListener(this);
        mBtnFilters.setOnClickListener(this);
        mBtnCropRotate.setOnClickListener(this);
        mBtnStickers.setOnClickListener(this);
        mBtnFont.setOnClickListener(this);
        mBtnAdjust.setOnClickListener(this);
        mBtnGraffiti.setOnClickListener(this);
        mBtnMosaic.setOnClickListener(this);
        mBtnEdge.setOnClickListener(this);
        mBtnEnhancement.setOnClickListener(this);
        mBtnBlur.setOnClickListener(this);
        mBtnMatBlur.setOnClickListener(this);

    }

    /**
     * 初始化特效列表
     */
    private void initFilterListView() {
        // 滤镜列表
        mFilterListView = (AsyncRecyclerview) mInflater
                .inflate(R.layout.view_video_edit_filters, null);
        mFilterListManager = new LinearLayoutManager(this);
        mFilterListManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(mFilterListManager);
        // TODO 滤镜适配器
        EffectFilterAdapter adapter = new EffectFilterAdapter(this,
                ColorFilterManager.getInstance().getFilterType(),
                ColorFilterManager.getInstance().getFilterName());

        mFilterListView.setAdapter(adapter);
        adapter.addItemClickListener(new EffectFilterAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(int position) {
                mColorIndex = position;
                if (VERBOSE) {
                    Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                            + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
                }
            }
        });
    }

    /**
     * 初始化图片编辑器
     */
    private void initImageEditManager() {
        mEditManager = new ImageEditManager(this, mImagePath, mImageView);
        mEditManager.startImageEditThread();
        mEditManager.setSourceImage();
    }

    @Override
    public void onBackPressed() {
        if (isShowingEditView()) {
            resetBottomView();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mEditManager.stopImageEditThread();
        mEditManager.release();
        mEditManager = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 返回
            case R.id.btn_back:
                finish();
                break;

            // 下一步
            case R.id.btn_next:
                saveImage();
                break;

            // 一键美化
            case R.id.btn_beautify:
                showBeautifyView();
                break;

            // 特效
            case R.id.btn_filters:
                showFilterView();
                break;

            // 裁剪旋转
            case R.id.btn_crop_rotate:
                showCropRotateView();
                break;

            // 贴纸
            case R.id.btn_stickers:
                showStickersView();
                break;

            // 文字
            case R.id.btn_font:
                addFonts();
                break;

            // 调节
            case R.id.btn_adjust:
                showAdjustView();
                break;

            // 涂鸦
            case R.id.btn_grafitti:
                showGrafittiView();
                break;

            // 马赛克
            case R.id.btn_mosaic:
                showMosaicView();
                break;

            // 边框
            case R.id.btn_edge:
                showEdgeView();
                break;

            // 夜景增强
            case R.id.btn_enhancement:
                showEnhancementView();
                break;

            // 虚化
            case R.id.btn_blur:
                showBlurView();
                break;

            // 抠图虚化
            case R.id.btn_mat_blur:
                showMatBlurView();
                break;
        }
    }

    /**
     * 重置底部栏
     */
    private void resetBottomView() {
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mScrollView);
        mLayoutNavigation.setVisibility(View.VISIBLE);
        mEditerShowing = false;
    }

    /**
     * 保存图片
     */
    private void saveImage() {
        mImageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(mImageView.getDrawingCache());
        mImageView.setDrawingCacheEnabled(false);
        String path = ParamsManager.AlbumPath + "CainCamera_" + System.currentTimeMillis() + ".jpeg";
        BitmapUtils.saveBitmap(this, path, bitmap);
        Toast.makeText(this, path + " 保存成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示一键美化视图
     */
    private void showBeautifyView() {

    }

    /**
     * 显示特效视图
     */
    private void showFilterView() {
        if (mFilterListView == null) {
            initFilterListView();
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mFilterListView);
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
    }

    /**
     * 显示贴纸视图
     */
    private void showStickersView() {

    }

    /**
     * 添加文字
     */
    private void addFonts() {

    }

    /**
     * 显示涂鸦视图
     */
    private void showGrafittiView() {

    }

    /**
     * 显示裁剪旋转视图
     */
    private void showCropRotateView() {

    }

    /**
     * 显示调节视图
     */
    private void showAdjustView() {

    }

    /**
     * 显示马赛克视图
     */
    private void showMosaicView() {

    }

    /**
     * 显示边框视图
     */
    private void showEdgeView() {

    }

    /**
     * 显示夜景增强视图
     */
    private void showEnhancementView() {

    }

    /**
     * 显示虚化视图
     */
    private void showBlurView() {

    }

    /**
     * 显示抠图虚化视图
     */
    private void showMatBlurView() {

    }

    /**
     * 是否处于编辑视图，用于点击返回按钮时，直接
     * @return
     */
    private boolean isShowingEditView() {
        return mEditerShowing;
    }
}



