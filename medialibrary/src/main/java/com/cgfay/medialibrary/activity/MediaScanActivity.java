package com.cgfay.medialibrary.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.adapter.AlbumScanAdapter;
import com.cgfay.medialibrary.adapter.MediaScanAdapter;
import com.cgfay.medialibrary.loader.impl.GlideMediaLoader;
import com.cgfay.medialibrary.model.AlbumItem;
import com.cgfay.medialibrary.model.MediaItem;
import com.cgfay.medialibrary.engine.MediaScanParam;
import com.cgfay.medialibrary.scanner.AlbumScanner;
import com.cgfay.medialibrary.fragment.MediaScanFragment;
import com.cgfay.utilslibrary.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体扫描页面
 */
public class MediaScanActivity extends AppCompatActivity implements AlbumScanner.AlbumScanCallbacks,
        MediaScanAdapter.OnCaptureClickListener, MediaScanAdapter.OnMediaItemSelectedListener {

    private static final int CURRENT_MEDIA = 0x01;

    private AlbumScanner mAlbumScanner;

    private RelativeLayout mLayoutTitle;
    private TextView mAlbumTitle;
    private ImageView mAlbumArrow;

    private FrameLayout mLayoutContainer;
    private FrameLayout mLayoutEmptyPage;

    private FrameLayout mLayoutAlbumSelect;
    private ListView mListAlbums;
    private AlbumScanAdapter mAlbumScanAdapter;

    // 显示相册选择页面
    private boolean mShowAlbumSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_scan);
        mAlbumScanner = new AlbumScanner(this, this);
        mAlbumScanner.scanAlbums();
        initView();
    }

    private void initView() {
        mLayoutTitle = (RelativeLayout) findViewById(R.id.layout_album_title);
        mAlbumTitle = (TextView) findViewById(R.id.album_title);
        mAlbumArrow = (ImageView) findViewById(R.id.album_arrow);
        mLayoutContainer = (FrameLayout) findViewById(R.id.fragment_container);

        // TODO 待优化（使用ViewStub进行延迟加载）
        mLayoutEmptyPage = (FrameLayout) findViewById(R.id.layout_empty_page);

        // 相册列表
        mLayoutAlbumSelect = (FrameLayout) findViewById(R.id.layout_album_select);
        mAlbumScanAdapter = new AlbumScanAdapter(this, null, false);
        mListAlbums = (ListView)findViewById(R.id.lv_albums);
        mListAlbums.setAdapter(mAlbumScanAdapter);
        mListAlbums.setOnItemClickListener(mAlbumItemClickListener);

        mLayoutTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // flip arrow and show album select page
                showAlbumSelectPage(!mShowAlbumSelected);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumScanner != null) {
            mAlbumScanner.destroy();
        }
    }

    @Override
    public void onAlbumScanFinish(final Cursor cursor) {
        mAlbumScanAdapter.swapCursor(cursor);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                cursor.moveToPosition(mAlbumScanner.getCurrentSelection());
                AlbumItem item = AlbumItem.valueOf(cursor);
                if (item.isAll() && MediaScanParam.getInstance().showCapture) {
                    item.addCaptureCount();
                }
                mAlbumTitle.setText(item.getDisplayName(MediaScanActivity.this));
                onAlbumSelectedChanged(item);
            }
        });
    }

    @Override
    public void onAlbumScanReset() {
        if (mAlbumScanAdapter != null) {
            mAlbumScanAdapter.swapCursor(null);
        }
    }

    /**
     * 相册列表选中监听器
     */
    private AdapterView.OnItemClickListener mAlbumItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mAlbumScanner != null) {
                mAlbumScanner.setCurrentSelection(position);
            }
            if (mAlbumScanAdapter != null) {
                mAlbumScanAdapter.getCursor().moveToPosition(position);
                AlbumItem albumItem = AlbumItem.valueOf(mAlbumScanAdapter.getCursor());

                mAlbumTitle.setText(albumItem.getDisplayName(MediaScanActivity.this));
                if (albumItem.isAll() && MediaScanParam.getInstance().showCapture) {
                    albumItem.addCaptureCount();
                }
                onAlbumSelectedChanged(albumItem);
            }
        }
    };

    /**
     * 是否显示相册选择页面
     * @param show
     */
    private void showAlbumSelectPage(boolean show) {
        mAlbumArrow.setPivotX(mAlbumArrow.getWidth() / 2);
        mAlbumArrow.setPivotY(mAlbumArrow.getHeight() / 2);
        mShowAlbumSelected = show;
        if (mShowAlbumSelected) {
            mAlbumArrow.setRotation(180);
            mLayoutAlbumSelect.setVisibility(View.VISIBLE);
        } else {
            mAlbumArrow.setRotation(0);
            mLayoutAlbumSelect.setVisibility(View.GONE);
        }
    }

    /**
     * 改变选中的相册
     * @param item 相册item
     */
    private void onAlbumSelectedChanged(AlbumItem item) {
        if (item.isAll() && item.isEmpty()) {
            showAlbumSelectPage(false);
            mLayoutContainer.setVisibility(View.GONE);
            mLayoutEmptyPage.setVisibility(View.VISIBLE);
        } else {
            showAlbumSelectPage(false);
            mLayoutContainer.setVisibility(View.VISIBLE);
            mLayoutEmptyPage.setVisibility(View.GONE);

            MediaScanParam.getInstance().mediaLoader = new GlideMediaLoader();

            // 判断是否已经存在Fragment，如果Fragment已存在则直接更新需要扫描的相册
            MediaScanFragment fragment = (MediaScanFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment == null) {
                fragment = MediaScanFragment.newInstance(item);
                fragment.addCaptureClickListener(this);
                fragment.addMediaItemClickListener(this);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commitAllowingStateLoss();
            } else {
                fragment.updateAlbum(item);
            }
        }
    }

    @Override
    public void onCapture() {
        if (MediaScanParam.getInstance().captureListener != null) {
            MediaScanParam.getInstance().captureListener.onCapture();
        }
    }

    @Override
    public void onMediaItemSelected(AlbumItem albumItem, MediaItem mediaItem, int position) {
        if (MediaScanParam.getInstance().mediaSelectedListener != null) {

            List<Uri> uriList = new ArrayList<Uri>();
            uriList.add(mediaItem.getContentUri());

            List<String> pathList = new ArrayList<>();
            pathList.add(FileUtils.getUriPath(this, mediaItem.getContentUri()));
            boolean isVideo = mediaItem.isVideo();
            MediaScanParam.getInstance().mediaSelectedListener.onSelected(uriList, pathList, isVideo);
        }
        finish();
    }

    // 记录跳转到预览页面时的位置
    private int mPreviewPosition;

    @Override
    public void onMediaItemPreview(AlbumItem albumItem, MediaItem mediaItem, int position) {
        mPreviewPosition = position;
        Intent intent = new Intent(this, MediaPreviewActivity.class);
        intent.putExtra(MediaPreviewActivity.CURRENT_ALBUM, albumItem);
        intent.putExtra(MediaPreviewActivity.CURRENT_MEDIA, mediaItem);
        startActivityForResult(intent, CURRENT_MEDIA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 返回时滚动到当前页面，处理预览页面滑动导致item超出了原来屏幕显示的范围，需要滚动到当前的位置
        if (requestCode == CURRENT_MEDIA && resultCode == RESULT_OK) {
            MediaScanFragment fragment = (MediaScanFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment != null) {
                fragment.scrollToPosition(data.getIntExtra(MediaPreviewActivity.CURRENT_POSITION, mPreviewPosition));
            }
        }
    }
}
