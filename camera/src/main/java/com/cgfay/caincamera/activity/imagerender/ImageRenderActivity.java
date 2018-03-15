package com.cgfay.caincamera.activity.imagerender;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.EffectFilterAdapter;
import com.cgfay.cainfilter.camerarender.ColorFilterManager;
import com.cgfay.cainfilter.camerarender.ParamsManager;
import com.cgfay.utilslibrary.AspectFrameLayout;
import com.cgfay.utilslibrary.AsyncRecyclerview;
import com.cgfay.utilslibrary.BitmapUtils;
import com.cgfay.utilslibrary.CainSurfaceView;

import java.nio.ByteBuffer;

public class ImageRenderActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback,SeekBar.OnSeekBarChangeListener, OnRenderListener {

    private static final String TAG = "ImageRenderActivity";
    private static final boolean VERBOSE = true;

    public static final String PATH = "path";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    // 原始滤镜值
    private static final float[] OriginalValues = new float[] {0, 1.0f, 0, 0, 1.0f, 0};

    private String mImagePath;
    private int mOriginWidth;
    private int mOriginHeight;

    private Button mBtnBack;
    private Button mBtnNext;
    private TextView mEditTitle;

    private int mColorIndex = 0;

    private AspectFrameLayout mLayoutAspect;
    private CainSurfaceView mImageSurfaceView;

    // 显示滤镜
    private AsyncRecyclerview mFiltersListView;
    private LinearLayoutManager mEffectManager;
    private boolean mShowFilters;

    // 底部栏
    private FrameLayout mLayoutBottom;
    private LinearLayout mLayoutBottomMenu;
    private Button mBtnFilters;
    private Button mBtnAdjust;

    private LayoutInflater mInflater;   // 布局加载器器


    // 滤镜值索引
    private static final int BrightnessIndex = 0;
    private static final int ContrastIndex = 1;
    private static final int ExposureIndex = 2;
    private static final int HueIndex = 3;
    private static final int SaturationIndex = 4;
    private static final int SharpnessIndex = 5;

    // Seekbar的最大值
    private static final int SeekBarMax = 100;

    // 底部调节栏
    private boolean mShowAdjust;
    private LinearLayout mLayoutAdjust;
    private SeekBar mSeekbar;
    private Button mBtnBrightness;
    private Button mBtnContrast;
    private Button mBtnExposure;
    private Button mBtnHue;
    private Button mBtnSaturation;
    private Button mBtnSharpness;
    private TextView mValueShow;
    // 用于记录六种选项值
    private float[] mValues = OriginalValues;
    private int mCurrentFilterIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_render);
        mImagePath = getIntent().getStringExtra(PATH);
        mOriginWidth = getIntent().getIntExtra(WIDTH, 0);
        mOriginHeight = getIntent().getIntExtra(HEIGHT, 0);
        mInflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
        initEffectView();

        ImageRenderManager.getInstance().startImageEditThread();
        ImageRenderManager.getInstance().setImagePath(mImagePath);

        // 计算屏幕宽高，用于防止加载大图片导致内存溢出
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        ImageRenderManager.getInstance().setScreenSize(width, height);
        ParamsManager.context = this;
    }

    private void initView() {

        mEditTitle = (TextView) findViewById(R.id.edit_title);
        mEditTitle.setText(getResources().getText(R.string.image_edit_title));

        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);

        mLayoutAspect = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        if (mOriginWidth != 0 && mOriginHeight != 0) {
            mLayoutAspect.setAspectRatio(mOriginWidth / mOriginHeight);
        }
        mImageSurfaceView = new CainSurfaceView(this);
        mImageSurfaceView.getHolder().addCallback(this);
        mLayoutAspect.addView(mImageSurfaceView);
        mLayoutAspect.requestLayout();

        mLayoutBottom = (FrameLayout) findViewById(R.id.layout_bottom);
        mLayoutBottomMenu = (LinearLayout) mInflater.inflate(R.layout.view_image_render_bottom_menu, null);
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mLayoutBottomMenu);
        mBtnFilters = (Button) mLayoutBottomMenu.findViewById(R.id.btn_filters);
        mBtnAdjust = (Button) mLayoutBottomMenu.findViewById(R.id.btn_adjust);
        mBtnFilters.setOnClickListener(this);
        mBtnAdjust.setOnClickListener(this);


        mValueShow = (TextView) findViewById(R.id.show_value);

        mLayoutAdjust = (LinearLayout) mInflater.inflate(R.layout.view_image_edit_adjust, null);
        mSeekbar = (SeekBar) mLayoutAdjust.findViewById(R.id.edit_value);
        mBtnBrightness = (Button) mLayoutAdjust.findViewById(R.id.btn_brightness);
        mBtnContrast = (Button) mLayoutAdjust.findViewById(R.id.btn_contrast);
        mBtnExposure = (Button) mLayoutAdjust.findViewById(R.id.btn_exposure);
        mBtnHue = (Button) mLayoutAdjust.findViewById(R.id.btn_hue);
        mBtnSaturation = (Button) mLayoutAdjust.findViewById(R.id.btn_saturation);
        mBtnSharpness = (Button) mLayoutAdjust.findViewById(R.id.btn_sharpness);

        mSeekbar.setMax(SeekBarMax);
        mSeekbar.setOnSeekBarChangeListener(this);
        mBtnBrightness.setOnClickListener(this);
        mBtnContrast.setOnClickListener(this);
        mBtnExposure.setOnClickListener(this);
        mBtnHue.setOnClickListener(this);
        mBtnSaturation.setOnClickListener(this);
        mBtnSharpness.setOnClickListener(this);

    }

    private void initEffectView() {
        mFiltersListView = (AsyncRecyclerview) mInflater.inflate(R.layout.view_image_edit_effect, null);
        mEffectManager = new LinearLayoutManager(this);
        mEffectManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFiltersListView.setLayoutManager(mEffectManager);

        EffectFilterAdapter adapter = new EffectFilterAdapter(this,
                ColorFilterManager.getInstance().getFilterType(),
                ColorFilterManager.getInstance().getFilterName());

        mFiltersListView.setAdapter(adapter);
        adapter.addItemClickListener(new EffectFilterAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(int position) {
                mColorIndex = position;
                ImageRenderManager.getInstance().changeFilterType(
                        ColorFilterManager.getInstance().getColorFilterType(position));
                if (VERBOSE) {
                    Log.d("changeFilter", "index = " + mColorIndex + ", filter name = "
                            + ColorFilterManager.getInstance().getColorFilterName(mColorIndex));
                }
            }
        });
    }


    /**
     * 重置底部栏
     */
    private void resetBottomView() {
        mLayoutBottom.removeAllViews();
        mLayoutBottom.addView(mLayoutBottomMenu);
    }

    @Override
    public void onBackPressed() {
        if (mShowFilters || mShowAdjust) {
            mShowFilters = false;
            mShowAdjust = false;
            resetBottomView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        ParamsManager.context = null;
        ImageRenderManager.getInstance().destoryImageEditThread();
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 返回
            case R.id.btn_back:
                finish();
                break;

            // 保存图片
            case R.id.btn_next:
                ImageRenderManager.getInstance().saveImage(this);
                break;

            // 滤镜
            case R.id.btn_filters:
                mLayoutBottom.removeAllViews();
                mLayoutBottom.addView(mFiltersListView);
                mShowFilters = true;
                break;

            // 调节
            case R.id.btn_adjust:
                mLayoutBottom.removeAllViews();
                mLayoutBottom.addView(mLayoutAdjust);
                mShowAdjust = true;
                mCurrentFilterIndex = BrightnessIndex;
                mBtnBrightness.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;

            // 亮度
            case R.id.btn_brightness: {
                resetAdjustButtonColor();
                ImageRenderManager.getInstance().setBrightness(mValues[BrightnessIndex]);
                mCurrentFilterIndex = BrightnessIndex;
                int progress = (int) (mValues[BrightnessIndex] * SeekBarMax);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnBrightness.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 对比度
            case R.id.btn_contrast: {
                resetAdjustButtonColor();
                ImageRenderManager.getInstance().setContrast(mValues[ContrastIndex]);
                mCurrentFilterIndex = ContrastIndex;
                int progress = (int) (mValues[ContrastIndex] * SeekBarMax / 2.0f);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnContrast.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 曝光
            case R.id.btn_exposure: {
                resetAdjustButtonColor();
                ImageRenderManager.getInstance().setExposure(mValues[ExposureIndex]);
                mCurrentFilterIndex = ExposureIndex;
                int progress = (int) (mValues[ExposureIndex] * SeekBarMax);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnExposure.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 色调
            case R.id.btn_hue: {
                resetAdjustButtonColor();
                ImageRenderManager.getInstance().setHue(mValues[HueIndex]);
                mCurrentFilterIndex = HueIndex;
                int progress = (int) (mValues[HueIndex] * SeekBarMax / 360f);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnHue.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 饱和度
            case R.id.btn_saturation: {
                resetAdjustButtonColor();
                ImageRenderManager.getInstance().setSaturation(mValues[SaturationIndex]);
                mCurrentFilterIndex = SaturationIndex;
                int progress = (int) (mValues[SaturationIndex] * SeekBarMax / 2.0f);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnSaturation.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            }

            // 锐度
            case R.id.btn_sharpness: {
                resetAdjustButtonColor();
                ImageRenderManager.getInstance().setSharpness(mValues[SharpnessIndex]);
                mCurrentFilterIndex = SharpnessIndex;
                int progress = (int) (mValues[SharpnessIndex] * SeekBarMax);
                mSeekbar.setProgress(progress);
                setFilterValues(progress);
                mBtnSharpness.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            }
        }
    }


    @Override
    public void onSaveImageListener(ByteBuffer buffer, int width, int height) {
        String filename = ParamsManager.AlbumPath
                + "CainCamera_" + System.currentTimeMillis() + ".png";

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);
        Bitmap bitmap = BitmapUtils.flipBitmap(bmp, false, true, true);
        BitmapUtils.saveBitmap(this, filename, bitmap);
        bitmap.recycle();
        if (VERBOSE) {
            Log.d(TAG, "Saved " + width + "x" + height + " frame as '" + filename);
        }
        Toast.makeText(this, filename + " 保存成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 重置按钮颜色
     */
    private void resetAdjustButtonColor() {
        mBtnBrightness.setTextColor(getResources().getColor(R.color.white));
        mBtnContrast.setTextColor(getResources().getColor(R.color.white));
        mBtnExposure.setTextColor(getResources().getColor(R.color.white));
        mBtnHue.setTextColor(getResources().getColor(R.color.white));
        mBtnSaturation.setTextColor(getResources().getColor(R.color.white));
        mBtnSharpness.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            setFilterValues(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mValueShow.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mValueShow.setVisibility(View.GONE);
    }

    /**
     * 设置滤镜值
     * @param progress seekbar的进度值
     */
    private void setFilterValues(int progress) {
        float value = (float) progress / (float) SeekBarMax;
        // 计算百分比
        float text = (float)Math.round(value * 100);
        String string = text + "%";
        // 设置显示的值
        mValueShow.setText(string);
        // 调整实际的率净值
        if (mCurrentFilterIndex == HueIndex) {
            value = value * 360f; // 色调在0 ~ 360度之间变化
        } else if (mCurrentFilterIndex == SaturationIndex
                || mCurrentFilterIndex == ContrastIndex) {
            value = value * 2.0f; // 对比度在0 ~ 2之间变化
        }
        // 缓存当前的滤镜值
        mValues[mCurrentFilterIndex] = value;
        switch (mCurrentFilterIndex) {
            case BrightnessIndex:
                ImageRenderManager.getInstance().setBrightness(value);
                break;

            case ContrastIndex:
                ImageRenderManager.getInstance().setContrast(value);
                break;

            case ExposureIndex:
                ImageRenderManager.getInstance().setExposure(value);
                break;

            case HueIndex:
                ImageRenderManager.getInstance().setHue(value);
                break;

            case SaturationIndex:
                ImageRenderManager.getInstance().setSaturation(value);
                break;

            case SharpnessIndex:
                ImageRenderManager.getInstance().setSharpness(value);
                break;
        }
    }

    /**
     * 重置滤镜为原始值
     */
    private void resetFilterValues() {
        mValues = OriginalValues;
        ImageRenderManager.getInstance().setBrightness(OriginalValues[BrightnessIndex]);
        ImageRenderManager.getInstance().setContrast(OriginalValues[ContrastIndex]);
        ImageRenderManager.getInstance().setExposure(OriginalValues[ExposureIndex]);
        ImageRenderManager.getInstance().setHue(OriginalValues[HueIndex]);
        ImageRenderManager.getInstance().setSaturation(OriginalValues[SaturationIndex]);
        ImageRenderManager.getInstance().setSharpness(OriginalValues[SharpnessIndex]);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ImageRenderManager.getInstance().surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        float imageAspect = 0;
        if (mOriginWidth != 0 & mOriginHeight != 0) {
            imageAspect = (float) mOriginWidth / (float) mOriginHeight;
        }
        float aspect = (float) width / (float) height;
        Log.d(TAG, "surfaceChanged: imageAspect = " + imageAspect + ", aspect = " + aspect);
        if (imageAspect > aspect) {
            height = (int) (width / imageAspect);
        } else if (imageAspect != 0) {
            width = (int) (height * imageAspect);
        }
        mLayoutAspect.setAspectRatio(width / height);
        mLayoutAspect.requestLayout();
        mImageSurfaceView.requestLayout();
        ImageRenderManager.getInstance().surfaceChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ImageRenderManager.getInstance().surfaceDestoryed();
    }
}
