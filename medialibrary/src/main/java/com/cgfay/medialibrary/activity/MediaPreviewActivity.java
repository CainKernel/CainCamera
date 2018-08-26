package com.cgfay.medialibrary.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.adapter.PreviewPagerAdapter;
import com.cgfay.medialibrary.model.AlbumItem;
import com.cgfay.medialibrary.model.MediaItem;
import com.cgfay.medialibrary.engine.MediaScanParam;
import com.cgfay.medialibrary.scanner.MediaScanner;
import com.cgfay.utilslibrary.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class MediaPreviewActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener, MediaScanner.MediaScanCallbacks {

    public static final String CURRENT_ALBUM = "current_album";
    public static final String CURRENT_MEDIA = "current_media";

    public static final String CURRENT_POSITION = "current_position";

    private ViewPager mPager;
    private PreviewPagerAdapter mAdapter;
    private MediaScanner mMediaScanner;

    // 当前索引
    private int mCurrentPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_preview);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.addOnPageChangeListener(this);
        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mMediaScanner = new MediaScanner(this, this);
        AlbumItem album = getIntent().getParcelableExtra(CURRENT_ALBUM);
        mMediaScanner.scanAlbum(album);

        // 跳转到编辑页面
        findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MediaScanParam.getInstance().mediaSelectedListener != null) {
                    MediaItem item = mAdapter.getMediaItem(mCurrentPosition);
                    List<Uri> uriList = new ArrayList<>();
                    uriList.add(item.getContentUri());
                    List<String> pathList = new ArrayList<>();
                    pathList.add(FileUtils.getUriPath(MediaPreviewActivity.this, item.getContentUri()));
                    MediaScanParam.getInstance().mediaSelectedListener.onSelected(uriList, pathList, item.isVideo());
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        setForResult();
        super.onBackPressed();
    }

    /**
     * 返回当前媒体对象值
     */
    private void setForResult() {
        Intent intent = new Intent();
        intent.putExtra(MediaPreviewActivity.CURRENT_POSITION, mCurrentPosition);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaScanner.destroy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onMediaScanFinish(Cursor cursor) {
        List<MediaItem> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(MediaItem.valueOf(cursor));
        }
        if (items.isEmpty()) {
            return;
        }
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
        adapter.addMediaItems(items);
        adapter.notifyDataSetChanged();
        MediaItem selectedMedia = getIntent().getParcelableExtra(CURRENT_MEDIA);
        int index = items.indexOf(selectedMedia);
        mPager.setCurrentItem(index, false);
        mCurrentPosition = index;
    }

    @Override
    public void onMediaScanReset() {

    }

}
