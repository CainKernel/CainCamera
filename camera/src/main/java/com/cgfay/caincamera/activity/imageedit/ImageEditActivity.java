package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.cgfay.cainfilter.camerarender.ParamsManager;
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

    private RelativeLayout mLayoutBottomOperation;
    private Button mBtnCancel;
    private Button mBtnApply;
    private Button mBtnReset;

    // 处于编辑状态
    private boolean mEditerShowing = false;

    // 显示滤镜名称/滤镜值
    private TextView mShowValueTextView;

    // 一键美化编辑器
    private BeautifyEditer mBeautifyEditer;
    // 特效编辑器
    private FilterEditer mFilterEditer;
    // 裁剪旋转编辑器
    private CropRotateEditer mCropRotateEditer;
    // 调节编辑器
    private AdjustEditer mAdjustEditer;

    // 夜景增强编辑器
    private EnhancementEditer mEnhancementEditer;
    // 虚化编辑器
    private BlurEditer mBlurEditer;
    // 抠图虚化编辑器
    private MatBlurEditer mMatBlurEditer;

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

        mShowValueTextView = (TextView) findViewById(R.id.show_value);

        // 底部操作栏
        mLayoutBottomOperation = (RelativeLayout) findViewById(R.id.layout_bottom_operation);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnReset = (Button) findViewById(R.id.btn_reset);
        mBtnApply = (Button) findViewById(R.id.btn_apply);

        mBtnCancel.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
        mBtnApply.setOnClickListener(this);

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



    @Override
    public void onBackPressed() {
        if (isShowingEditView()) {
            cancelCurrentOperation();
            return;
        }
        super.onBackPressed();
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

            // 取消编辑
            case R.id.btn_cancel:
                cancelCurrentOperation();
                break;

            // 重置编辑
            case R.id.btn_reset:
                resetCurrentOperation();
                break;

            // 应用编辑
            case R.id.btn_apply:
                applyCurrentOperation();
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
        mLayoutBottomOperation.setVisibility(View.GONE);
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
        if (mBeautifyEditer == null) {
            mBeautifyEditer = new BeautifyEditer(this);
            mBeautifyEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mBeautifyEditer.getLayoutBeautify());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 显示特效视图
     */
    private void showFilterView() {
        if (mFilterEditer == null) {
            mFilterEditer = new FilterEditer(this);
            mFilterEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mFilterEditer.getFilterListView());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 显示裁剪旋转视图
     */
    private void showCropRotateView() {
        if (mCropRotateEditer == null) {
            mCropRotateEditer = new CropRotateEditer(this);
            mCropRotateEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mCropRotateEditer.getLayoutCropRotate());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 显示贴纸视图
     */
    private void showStickersView() {
        Toast.makeText(this, "贴纸功能暂未实现", Toast.LENGTH_SHORT).show();
    }

    /**
     * 添加文字
     */
    private void addFonts() {
        Toast.makeText(this, "文字功能暂未实现", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示涂鸦视图
     */
    private void showGrafittiView() {
        Toast.makeText(this, "涂鸦功能暂未实现", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示调节视图
     */
    private void showAdjustView() {
        if (mAdjustEditer == null) {
            mAdjustEditer = new AdjustEditer(this);
            mAdjustEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mAdjustEditer.getLayoutAdjust());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 显示马赛克视图
     */
    private void showMosaicView() {

        Toast.makeText(this, "马赛克功能暂未实现", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示边框视图
     */
    private void showEdgeView() {

        Toast.makeText(this, "边框功能暂未实现", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示夜景增强视图
     */
    private void showEnhancementView() {
        if (mEnhancementEditer == null) {
            mEnhancementEditer = new EnhancementEditer(this);
            mEnhancementEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mEnhancementEditer.getLayoutEnhancement());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 显示虚化视图
     */
    private void showBlurView() {
        if (mBlurEditer == null) {
            mBlurEditer = new BlurEditer(this);
            mBlurEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mBlurEditer.getLayoutBlur());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 显示抠图虚化视图
     */
    private void showMatBlurView() {
        if (mMatBlurEditer == null) {
            mMatBlurEditer = new MatBlurEditer(this);
            mMatBlurEditer.setTextView(mShowValueTextView);
        }
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mMatBlurEditer.getLayoutMatblur());
        mLayoutNavigation.setVisibility(View.GONE);
        mEditerShowing = true;
        mLayoutBottomOperation.setVisibility(View.VISIBLE);
    }

    /**
     * 是否处于编辑视图，用于点击返回按钮时，隐藏编辑页面还是退出页面
     * @return
     */
    private boolean isShowingEditView() {
        return mEditerShowing;
    }


    /**
     * 取消当前编辑操作
     */
    private void cancelCurrentOperation() {
        resetBottomView();
    }

    /**
     * 重置当前编辑操作
     */
    private void resetCurrentOperation() {

    }

    /**
     * 应用当前编辑
     */
    private void applyCurrentOperation() {
        resetBottomView();
    }
}



