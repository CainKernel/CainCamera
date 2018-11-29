package com.cgfay.caincamera.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cgfay.caincamera.R;
import com.cgfay.cameralibrary.engine.PreviewEngine;
import com.cgfay.cameralibrary.engine.model.AspectRatio;
import com.cgfay.cameralibrary.engine.model.GalleryType;
import com.cgfay.cameralibrary.listener.OnGallerySelectedListener;
import com.cgfay.cameralibrary.listener.OnPreviewCaptureListener;
import com.cgfay.ffmpeglibrary.activity.AVMediaPlayerActivity;
import com.cgfay.ffmpeglibrary.activity.VideoRecordActivity;
import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.MakeupHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.imagelibrary.activity.ImageEditActivity;
import com.cgfay.medialibrary.engine.MediaScanEngine;
import com.cgfay.medialibrary.listener.OnCaptureListener;
import com.cgfay.medialibrary.listener.OnMediaSelectedListener;
import com.cgfay.medialibrary.loader.impl.GlideMediaLoader;
import com.cgfay.medialibrary.model.MimeType;
import com.cgfay.utilslibrary.utils.PermissionUtils;
import com.cgfay.videolibrary.activity.VideoEditActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0;

    private boolean mOnClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initView();
        initResources();
    }

    private void checkPermissions() {
        boolean cameraEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.CAMERA);
        boolean storageWriteEnable = PermissionUtils.permissionChecking(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean recordAudio = PermissionUtils.permissionChecking(this,
                Manifest.permission.RECORD_AUDIO);
        if (!cameraEnable || !storageWriteEnable || !recordAudio) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CODE);
        }
    }

    private void initView() {
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_edit_video).setOnClickListener(this);
        findViewById(R.id.btn_edit_picture).setOnClickListener(this);
        findViewById(R.id.btn_test_music).setOnClickListener(this);
        findViewById(R.id.btn_test_recorder).setOnClickListener(this);
        findViewById(R.id.btn_test_media_player).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnClick = false;
    }

    @Override
    public void onClick(View v) {
        if (mOnClick) {
            return;
        }
        mOnClick = true;
        switch (v.getId()) {
            case R.id.btn_camera: {
                previewCamera();
                break;
            }

            case R.id.btn_edit_video: {
                scanMedia(false, false,true);
                break;
            }

            case R.id.btn_edit_picture: {
                scanMedia(false, true, false);
                break;
            }

            // 测试音乐播放器
            case R.id.btn_test_music: {
                startActivity(new Intent(MainActivity.this, MusicScanActivity.class));
                break;
            }

            // 测试录制器
            case R.id.btn_test_recorder: {
                startActivity(new Intent(MainActivity.this, VideoRecordActivity.class));
                break;
            }

            // 测试视频播放器
            case R.id.btn_test_media_player: {
                MediaScanEngine.from(this)
                        .setMimeTypes(MimeType.ofAll())
                        .ImageLoader(new GlideMediaLoader())
                        .spanCount(4)
                        .showCapture(false)
                        .showImage(false)
                        .showVideo(true)
                        .enableSelectGif(false)
                        .setMediaSelectedListener(new OnMediaSelectedListener() {
                            @Override
                            public void onSelected(List<Uri> uriList, List<String> pathList, boolean isVideo) {
                                if (isVideo) {
                                    Intent intent = new Intent(MainActivity.this, AVMediaPlayerActivity.class);
                                    intent.putExtra(AVMediaPlayerActivity.PATH, pathList.get(0));
                                    startActivity(intent);
                                }
                            }
                        })
                        .scanMedia();
                break;
            }
        }
    }

    /**
     * 初始化动态贴纸、滤镜等资源
     */
    private void initResources() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ResourceHelper.initAssetsResource(MainActivity.this);
                FilterHelper.initAssetsFilter(MainActivity.this);
                MakeupHelper.initAssetsMakeup(MainActivity.this);
            }
        }).start();
    }

    /**
     * 打开预览页面
     */
    private void previewCamera() {
        PreviewEngine.from(this)
                .setCameraRatio(AspectRatio.Ratio_16_9)
                .showFacePoints(false)
                .showFps(true)
                .setGalleryListener(new OnGallerySelectedListener() {
                    @Override
                    public void onGalleryClickListener(GalleryType type) {
                        scanMedia(type == GalleryType.ALL);
                    }
                })
                .setPreviewCaptureListener(new OnPreviewCaptureListener() {
                    @Override
                    public void onMediaSelectedListener(String path, GalleryType type) {
                        if (type == GalleryType.PICTURE) {
                            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
                            intent.putExtra(ImageEditActivity.PATH, path);
                            startActivity(intent);
                        } else if (type == GalleryType.VIDEO) {
                            Intent intent = new Intent(MainActivity.this, VideoEditActivity.class);
                            intent.putExtra(VideoEditActivity.PATH, path);
                            startActivity(intent);
                        }
                    }
                })
                .startPreview();
    }

    /**
     * 扫描媒体库
     */
    private void scanMedia(boolean enableGif) {
        scanMedia(enableGif, true, true);
    }

    /**
     * 扫描媒体库
     * @param enableGif
     * @param enableImage
     * @param enableVideo
     */
    private void scanMedia(boolean enableGif, boolean enableImage, boolean enableVideo) {
        MediaScanEngine.from(this)
                .setMimeTypes(MimeType.ofAll())
                .ImageLoader(new GlideMediaLoader())
                .spanCount(4)
                .showCapture(true)
                .showImage(enableImage)
                .showVideo(enableVideo)
                .enableSelectGif(enableGif)
                .setCaptureListener(new OnCaptureListener() {
                    @Override
                    public void onCapture() {
                        previewCamera();
                    }
                })
                .setMediaSelectedListener(new OnMediaSelectedListener() {
                    @Override
                    public void onSelected(List<Uri> uriList, List<String> pathList, boolean isVideo) {
                        if (isVideo) {
                            Intent intent = new Intent(MainActivity.this, VideoEditActivity.class);
                            intent.putExtra(VideoEditActivity.PATH, pathList.get(0));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
                            intent.putExtra(ImageEditActivity.PATH, pathList.get(0));
                            startActivity(intent);
                        }
                    }
                })
                .scanMedia();
    }
}
