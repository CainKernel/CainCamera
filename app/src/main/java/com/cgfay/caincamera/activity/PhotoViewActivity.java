package com.cgfay.caincamera.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.adapter.PhotoViewAdapter;
import com.cgfay.caincamera.bean.ImageMeta;
import com.cgfay.caincamera.utils.PermissionUtils;
import com.cgfay.caincamera.view.AsyncRecyclerview;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhotoViewActivity extends AppCompatActivity
        implements PhotoViewAdapter.OnItemClickLitener {

    private static final int REQUEST_STORAGE_READ = 0x01;
    private static final int COLUMNSIZE = 3;

    private boolean multiSelectEnable = false;
    private int mCurrentSelecetedIndex = -1; // 单选模式下的当前位置

    private AsyncRecyclerview mPhototView;
    private GridLayoutManager mLayoutManager;
    private PhotoViewAdapter mPhotoAdapter;
    // 媒体库中的图片数据
    List<ImageMeta> mImageLists;

    // 编辑图片
    private RelativeLayout mPhotoEditLayout;
    private GLSurfaceView mPhotoEditView;
    private PhotoEditRenderer mEditRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        multiSelectEnable = getIntent().getBooleanExtra("multiSelect", false);
        if (PermissionUtils.permissionChecking(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startAsyncScaneMedia();
        } else {
            requestStorageReadPermission();
        }
        initView();
    }

    private void initView() {
        // 显示媒体库数据
        mPhototView = (AsyncRecyclerview) findViewById(R.id.photo_view);
        mLayoutManager = new GridLayoutManager(PhotoViewActivity.this, COLUMNSIZE);
        mPhototView.setLayoutManager(mLayoutManager);
        mImageLists = new ArrayList<ImageMeta>();
        // 编辑图片
        mPhotoEditLayout = (RelativeLayout) findViewById(R.id.layout_photo_edit);
        mPhotoEditView = (GLSurfaceView) findViewById(R.id.photo_edit_view);
        mPhotoEditView.setEGLContextClientVersion(3);
        mEditRenderer = new PhotoEditRenderer(this);
        mPhotoEditView.setRenderer(mEditRenderer);
        mPhotoEditView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
        mPhotoAdapter.setLongClickEnable(true);
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
        mPhotoEditLayout.setVisibility(View.VISIBLE);
        // 设置数据
        mEditRenderer.setImageMeta(mImageLists.get(mCurrentSelecetedIndex));
        // 请求渲染
        mPhotoEditView.requestRender();
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
