package com.cgfay.caincamera.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.PhotoViewAdapter;
import com.cgfay.caincamera.bean.ImageMeta;
import com.cgfay.caincamera.photo_edit.PhotoEditManager;
import com.cgfay.caincamera.utils.PermissionUtils;
import com.cgfay.caincamera.view.AspectFrameLayout;
import com.cgfay.caincamera.view.AsyncRecyclerview;
import com.cgfay.caincamera.view.PhotoEditSurfaceView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhotoViewActivity extends AppCompatActivity implements PhotoViewAdapter.OnItemClickLitener,
        SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private static final int REQUEST_STORAGE_READ = 0x01;
    private static final int COLUMNSIZE = 3;
    // 原始滤镜值
    private static final float[] OriginalValues = new float[] {0, 1.0f, 0, 0, 1.0f, 0};

    // 滤镜值索引
    private static final int BrightnessIndex = 0;
    private static final int ContrastIndex = 1;
    private static final int ExposureIndex = 2;
    private static final int HueIndex = 3;
    private static final int SaturationIndex = 4;
    private static final int SharpnessIndex = 5;

    // Seekbar的最大值
    private static final int SeekBarMax = 100;

    private boolean multiSelectEnable = false;
    private int mCurrentSelecetedIndex = -1; // 单选模式下的当前位置

    // 显示列表
    private AsyncRecyclerview mPhototView;
    private GridLayoutManager mLayoutManager;
    private PhotoViewAdapter mPhotoAdapter;
    // 媒体库中的图片数据
    List<ImageMeta> mImageLists;

    // 编辑图片
    private FrameLayout mPhotoEditLayout;
    private AspectFrameLayout mAspectFrameLayout;
    private PhotoEditSurfaceView mPhotoEditView;
    private SeekBar mSeekbar;
    private Button mBrightness;
    private Button mContrast;
    private Button mExposure;
    private Button mHue;
    private Button mSaturation;
    private Button mSharpness;
    private TextView mValueShow;
    // 用于记录六种选项值
    private float[] mValues = OriginalValues;
    private int mCurrentFilterIndex = BrightnessIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo_view);
        multiSelectEnable = getIntent().getBooleanExtra("multiSelect", false);
        initView();
        if (PermissionUtils.permissionChecking(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startAsyncScaneMedia();
        } else {
            requestStorageReadPermission();
        }
    }

    private void initView() {
        // 显示媒体库数据
        mPhototView = (AsyncRecyclerview) findViewById(R.id.photo_view);
        mLayoutManager = new GridLayoutManager(PhotoViewActivity.this, COLUMNSIZE);
        mPhototView.setLayoutManager(mLayoutManager);
        mImageLists = new ArrayList<ImageMeta>();
        // 编辑图片
        mPhotoEditLayout = (FrameLayout) findViewById(R.id.layout_photo_edit);
        mAspectFrameLayout = (AspectFrameLayout) findViewById(R.id.layout_aspect);

        mSeekbar = (SeekBar) findViewById(R.id.edit_value);
        mBrightness = (Button) findViewById(R.id.btn_brightness);
        mContrast = (Button) findViewById(R.id.btn_contrast);
        mExposure = (Button) findViewById(R.id.btn_exposure);
        mHue = (Button) findViewById(R.id.btn_hue);
        mSaturation = (Button)findViewById(R.id.btn_saturation);
        mSharpness = (Button) findViewById(R.id.btn_sharpness);
        mValueShow = (TextView) findViewById(R.id.show_value);

        mSeekbar.setMax(SeekBarMax);
        mSeekbar.setOnSeekBarChangeListener(this);
        mBrightness.setOnClickListener(this);
        mContrast.setOnClickListener(this);
        mExposure.setOnClickListener(this);
        mHue.setOnClickListener(this);
        mSaturation.setOnClickListener(this);
        mSharpness.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PhotoEditManager.getInstance().startPhotoEditThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PhotoEditManager.getInstance().destoryPhotoEditThread();
    }

    /**
     * 扫描媒体库
     */
    private void startAsyncScaneMedia() {
        ScanMediaStoreTask task = new ScanMediaStoreTask();
        task.execute();
        setupAdapter();
    }

    /**
     * 设置适配器
     */
    private void setupAdapter() {
        mPhotoAdapter = new PhotoViewAdapter(PhotoViewActivity.this, mImageLists);
        mPhotoAdapter.addItemClickListener(this);
        mPhotoAdapter.setMultiSelectEnable(multiSelectEnable);
        mPhototView.setAdapter(mPhotoAdapter);
//        mPhotoAdapter.setLongClickEnable(true);
    }

    /**
     * 请求权限
     */
    private void requestStorageReadPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_READ);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 读取存储权限
            case REQUEST_STORAGE_READ:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAsyncScaneMedia();
                }
                break;
        }
    }

    @Override
    public void onSingleSelected(int position) {
        if (mCurrentSelecetedIndex != -1) {
            mImageLists.get(mCurrentSelecetedIndex).setSelected(false);
        }
        // 更新当前选中的模式
        mCurrentSelecetedIndex = position;
        // 预览编辑选中的照片
        showPhotoEditView();
    }

    @Override
    public void onMultiSelected(int position) {

    }

    @Override
    public void onItemLongPressed() {
        // item长按触发，用于显示删除、分享等功能。

    }

    /**
     * 取消所有选中的图片
     */
    private void cancelAllSelected() {
        if (mImageLists != null) {
            for (int i = 0; i < mImageLists.size(); i++) {
                mImageLists.get(i).setSelected(false);
            }
        }
        mCurrentSelecetedIndex = -1;
    }

    /**
     * 删除所有选中的图片
     */
    private void deleteSelectedImage() {
        // 获取所有需要被删除的元素
        List<ImageMeta> removedImages = new ArrayList<ImageMeta>();
        if (!multiSelectEnable) { // 单选
            if (mCurrentSelecetedIndex != -1) {
                ImageMeta image = mImageLists.remove(mCurrentSelecetedIndex);
                removedImages.add(image);

            }
        } else { // 多选
            Iterator<ImageMeta> it = mImageLists.iterator();
            while (it.hasNext()) {
                ImageMeta image = it.next();
                if (image.isSelected()) {
                    it.remove();
                }
                removedImages.add(image);
            }
        }
        // 更新位置
        mPhotoAdapter.notifyDataSetChanged();
        // 异步删除

    }

    /**
     * 显示图片编辑界面
     */
    private void showPhotoEditView() {
        // 在开始预览之前先设置图像元数据
        ImageMeta imageMeta = mImageLists.get(mCurrentSelecetedIndex);
        PhotoEditManager.getInstance().setImageMeta(imageMeta);
        // 显示预览画面
        mPhototView.setVisibility(View.GONE);
        mPhotoEditLayout.setVisibility(View.VISIBLE);
        mPhotoEditView = new PhotoEditSurfaceView(this);
        mAspectFrameLayout.addView(mPhotoEditView);

        float ratio = (float) imageMeta.getWidth() / (float) imageMeta.getHeight();
        mAspectFrameLayout.setAspectRatio(ratio);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 亮度
            case R.id.btn_brightness:
                PhotoEditManager.getInstance().setBrightness(mValues[BrightnessIndex]);
                mCurrentFilterIndex = BrightnessIndex;
                mSeekbar.setProgress((int) (mValues[BrightnessIndex] * SeekBarMax));
                break;

            // 对比度
            case R.id.btn_contrast:
                PhotoEditManager.getInstance().setContrast(mValues[ContrastIndex]);
                mCurrentFilterIndex = ContrastIndex;
                mSeekbar.setProgress((int) (mValues[ContrastIndex] * SeekBarMax / 2.0f));
                break;

            // 曝光
            case R.id.btn_exposure:
                PhotoEditManager.getInstance().setExposure(mValues[ExposureIndex]);
                mCurrentFilterIndex = ExposureIndex;
                mSeekbar.setProgress((int) (mValues[ExposureIndex] * SeekBarMax));
                break;

            // 色调
            case R.id.btn_hue:
                PhotoEditManager.getInstance().setHue(mValues[HueIndex]);
                mCurrentFilterIndex = HueIndex;
                mSeekbar.setProgress((int) (mValues[HueIndex] * SeekBarMax / 360f));
                break;

            // 饱和度
            case R.id.btn_saturation:
                PhotoEditManager.getInstance().setSaturation(mValues[SaturationIndex]);
                mCurrentFilterIndex = SaturationIndex;
                mSeekbar.setProgress((int) (mValues[SaturationIndex] * SeekBarMax / 2.0f));
                break;

            // 锐度
            case R.id.btn_sharpness:
                PhotoEditManager.getInstance().setSharpness(mValues[SharpnessIndex]);
                mCurrentFilterIndex = SharpnessIndex;
                mSeekbar.setProgress((int) (mValues[SharpnessIndex] * SeekBarMax));
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        setFilterValues(progress);
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
                PhotoEditManager.getInstance().setBrightness(value);
                break;

            case ContrastIndex:
                PhotoEditManager.getInstance().setContrast(value);
                break;

            case ExposureIndex:
                PhotoEditManager.getInstance().setExposure(value);
                break;

            case HueIndex:
                PhotoEditManager.getInstance().setHue(value);
                break;

            case SaturationIndex:
                PhotoEditManager.getInstance().setSaturation(value);
                break;

            case SharpnessIndex:
                PhotoEditManager.getInstance().setSharpness(value);
                break;
        }
    }

    /**
     * 重置滤镜为原始值
     */
    private void resetFilterValues() {
        mValues = OriginalValues;
        PhotoEditManager.getInstance().setBrightness(OriginalValues[BrightnessIndex]);
        PhotoEditManager.getInstance().setContrast(OriginalValues[ContrastIndex]);
        PhotoEditManager.getInstance().setExposure(OriginalValues[ExposureIndex]);
        PhotoEditManager.getInstance().setHue(OriginalValues[HueIndex]);
        PhotoEditManager.getInstance().setSaturation(OriginalValues[SaturationIndex]);
        PhotoEditManager.getInstance().setSharpness(OriginalValues[SharpnessIndex]);
    }

    // 扫描媒体库
    private class ScanMediaStoreTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = null;
            try {
                // 查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
                cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null ,null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));
                        // 跳过不存在图片的路径，比如第三方应用删除了图片不更新媒体库，此时会出现不存在的图片
                        File file = new File(path);
                        if (!file.exists()) {
                            continue;
                        }
                        ImageMeta image = new ImageMeta();
                        image.setId(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Images.Media._ID))); //获取唯一id
                        image.setName(cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))); //文件名
                        image.setMimeType(cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.MIME_TYPE))); // mimeType类型
                        image.setPath(path); //文件路径
                        image.setWidth(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Images.Media.WIDTH))); // 宽度
                        image.setHeight(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Images.Media.HEIGHT))); // 高度
                        image.setOrientation(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Images.Media.ORIENTATION))); // 旋转角度
                        image.setTime(cursor.getLong(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATE_TAKEN))); // 拍摄的时间
                        image.setSize(cursor.getLong(cursor
                                .getColumnIndex(MediaStore.Images.Media.SIZE))); // 设置大小
                        mImageLists.add(image);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mPhotoAdapter != null) {
                mPhotoAdapter.notifyDataSetChanged();
            }
        }
    }

}
