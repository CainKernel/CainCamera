package com.cgfay.caincamera.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.Window;
import android.view.WindowManager;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.activity.videoedit.VideoEditActivity;
import com.cgfay.caincamera.adapter.PhotoViewAdapter;
import com.cgfay.caincamera.bean.MediaMeta;
import com.cgfay.caincamera.photo_edit.PhotoEditManager;
import com.cgfay.utilslibrary.PermissionUtils;
import com.cgfay.utilslibrary.AsyncRecyclerview;

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

    // 显示列表
    private AsyncRecyclerview mPhototView;
    private GridLayoutManager mLayoutManager;
    private PhotoViewAdapter mPhotoAdapter;
    // 媒体库中的图片数据
    List<MediaMeta> mImageLists;

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
        mImageLists = new ArrayList<MediaMeta>();
    }

    @Override
    protected void onDestroy() {
        PhotoEditManager.getInstance().release();
        super.onDestroy();
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
        if (mImageLists.get(position).getMimeType().startsWith("video/")) {
            Intent intent = new Intent(PhotoViewActivity.this, VideoEditActivity.class);
            intent.putExtra(VideoEditActivity.PATH, mImageLists.get(position).getPath());
            startActivity(intent);
        } else if (mImageLists.get(position).getMimeType().startsWith("image/")) {
            Intent intent = new Intent(PhotoViewActivity.this, ImageEditActivity.class);
            intent.putExtra(ImageEditActivity.PATH, mImageLists.get(position).getPath());
            startActivity(intent);
        }
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
        List<MediaMeta> removedImages = new ArrayList<MediaMeta>();
        if (!multiSelectEnable) { // 单选
            if (mCurrentSelecetedIndex != -1) {
                MediaMeta image = mImageLists.remove(mCurrentSelecetedIndex);
                removedImages.add(image);

            }
        } else { // 多选
            Iterator<MediaMeta> it = mImageLists.iterator();
            while (it.hasNext()) {
                MediaMeta image = it.next();
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

    MediaMetadataRetriever mRetriever;

    // 扫描媒体库
    private class ScanMediaStoreTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver resolver = getContentResolver();
            Cursor cursor = null;
            try {
                // 查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
                cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null ,null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Images.Media.DATA));
                        // 跳过不存在图片的路径，比如第三方应用删除了图片不更新媒体库，此时会出现不存在的图片
                        // 剔除gif，编辑图片时，不支持gif
                        File file = new File(path);
                        if (!file.exists() || cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                                .toLowerCase().endsWith("gif")) {
                            continue;
                        }
                        MediaMeta image = new MediaMeta();
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

            try {
                // 查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
                cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null, null ,null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Video.Media.DATA));
                        // 跳过不存在图片的路径，比如第三方应用删除了图片不更新媒体库，此时会出现不存在的图片
                        File file = new File(path);
                        if (!file.exists()) {
                            continue;
                        }
                        MediaMeta video = new MediaMeta();
                        video.setId(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Video.Media._ID))); //获取唯一id
                        video.setName(cursor.getString(cursor
                                .getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))); //文件名
                        video.setPath(path); //文件路径
                        video.setWidth(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Video.Media.WIDTH))); // 宽度
                        video.setHeight(cursor.getInt(cursor
                                .getColumnIndex(MediaStore.Video.Media.HEIGHT))); // 高度
                        if (mRetriever == null) {
                            mRetriever = new MediaMetadataRetriever();
                        }
                        mRetriever.setDataSource(path);
                        String rotation = mRetriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                        video.setOrientation(Integer.parseInt(rotation)); // 旋转角度
                        video.setMimeType(mRetriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_MIMETYPE)); // mimeType类型
                        video.setTime(cursor.getLong(cursor
                                .getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))); // 拍摄的时间
                        video.setSize(cursor.getLong(cursor
                                .getColumnIndex(MediaStore.Video.Media.SIZE))); // 设置大小
                        mImageLists.add(video);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (mRetriever != null) {
                    mRetriever.release();
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
