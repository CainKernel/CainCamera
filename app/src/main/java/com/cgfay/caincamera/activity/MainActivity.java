package com.cgfay.caincamera.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.cgfay.caincamera.R;
import com.cgfay.camera.PreviewEngine;
import com.cgfay.camera.fragment.NormalMediaSelector;
import com.cgfay.camera.model.AspectRatio;

import com.cgfay.camera.listener.OnPreviewCaptureListener;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.MakeupHelper;
import com.cgfay.filter.glfilter.resource.ResourceHelper;
import com.cgfay.image.activity.ImageEditActivity;
import com.cgfay.picker.MediaPicker;
import com.cgfay.picker.selector.OnMediaSelector;
import com.cgfay.picker.model.MediaData;
import com.cgfay.picker.utils.MediaMetadataUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.video.activity.VideoEditActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0;

    private static final int DELAY_CLICK = 500;

    private boolean mOnClick;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        initView();
        if (PermissionUtils.permissionChecking(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            initResources();
        }
        mHandler = new Handler();
    }

    private void checkPermissions() {
        PermissionUtils.requestPermissions(this, new String[] {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CODE);
    }

    private void initView() {
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_edit_video).setOnClickListener(this);
        findViewById(R.id.btn_edit_picture).setOnClickListener(this);
        findViewById(R.id.btn_speed_record).setOnClickListener(this);
        findViewById(R.id.btn_edit_music_merge).setOnClickListener(this);
        findViewById(R.id.btn_ff_media_record).setOnClickListener(this);
        findViewById(R.id.btn_music_player).setOnClickListener(this);
        findViewById(R.id.btn_video_player).setOnClickListener(this);
        findViewById(R.id.btn_duet_record).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnClick = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initResources();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mOnClick) {
            return;
        }
        mOnClick = true;
        mHandler.postDelayed(()->{
            mOnClick = false;
        }, DELAY_CLICK);
        switch (v.getId()) {
            case R.id.btn_camera: {
                previewCamera();
                break;
            }

            case R.id.btn_edit_video: {
                scanMedia(false,true);
                break;
            }

            case R.id.btn_edit_picture: {
                scanMedia(true, false);
                break;
            }

            case R.id.btn_speed_record: {
                Intent intent = new Intent(MainActivity.this, SpeedRecordActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.btn_edit_music_merge: {
                musicMerge();
                break;
            }

            case R.id.btn_ff_media_record: {
                ffmpegRecord();
                break;
            }

            case R.id.btn_music_player: {
                musicPlayerTest();
                break;
            }

            case R.id.btn_video_player: {
                videoPlayerTest();
                break;
            }

            case R.id.btn_duet_record: {
                duetRecord();
                break;
            }
        }
    }

    /**
     * 初始化动态贴纸、滤镜等资源
     */
    private void initResources() {
        new Thread(() -> {
            ResourceHelper.initAssetsResource(MainActivity.this);
            FilterHelper.initAssetsFilter(MainActivity.this);
            MakeupHelper.initAssetsMakeup(MainActivity.this);
        }).start();
    }

    /**
     * 打开预览页面
     */
    private void previewCamera() {
        if (PermissionUtils.permissionChecking(this, Manifest.permission.CAMERA)) {
            PreviewEngine.from(this)
                    .setCameraRatio(AspectRatio.Ratio_16_9)
                    .showFacePoints(false)
                    .showFps(true)
                    .backCamera(true)
                    .setPreviewCaptureListener((path, type) -> {
                        if (type == OnPreviewCaptureListener.MediaTypePicture) {
                            Intent intent = new Intent(MainActivity.this, ImageEditActivity.class);
                            intent.putExtra(ImageEditActivity.IMAGE_PATH, path);
                            intent.putExtra(ImageEditActivity.DELETE_INPUT_FILE, true);
                            startActivity(intent);
                        } else if (type == OnPreviewCaptureListener.MediaTypeVideo) {
                            Intent intent = new Intent(MainActivity.this, VideoEditActivity.class);
                            intent.putExtra(VideoEditActivity.VIDEO_PATH, path);
                            startActivity(intent);
                        }
                    })
                    .startPreview();
        } else {
            checkPermissions();
        }
    }

    /**
     * 扫描媒体库
     * @param enableImage
     * @param enableVideo
     */
    private void scanMedia(boolean enableImage, boolean enableVideo) {
        MediaPicker.from(this)
                .showImage(enableImage)
                .showVideo(enableVideo)
                .setMediaSelector(new NormalMediaSelector())
                .show();
    }

    /**
     * 音视频混合
     */
    private void musicMerge() {
        MediaPicker.from(this)
                .showCapture(true)
                .showImage(false)
                .showVideo(true)
                .setMediaSelector(new MusicMergeMediaSelector())
                .show();
    }

    private class MusicMergeMediaSelector implements OnMediaSelector {
        @Override
        public void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList) {
            if (mediaDataList.size() == 1) {
                Intent intent = new Intent(context, MusicMergeActivity.class);
                intent.putExtra(MusicMergeActivity.PATH, MediaMetadataUtils.getPath(context, mediaDataList.get(0).getContentUri()));
                context.startActivity(intent);
            }
        }
    }

    /**
     * 使用FFmpeg 录制视频
     */
    private void ffmpegRecord() {
        startActivity(new Intent(MainActivity.this, FFMediaRecordActivity.class));
    }

    private void musicPlayerTest() {
        startActivity(new Intent(MainActivity.this, MusicPlayerActivity.class));
    }

    private void videoPlayerTest() {
        MediaPicker.from(this)
                .showCapture(true)
                .showImage(false)
                .showVideo(true)
                .setMediaSelector(new VideoPlayerTestSelector())
                .show();
    }

    private static class VideoPlayerTestSelector implements OnMediaSelector {
        @Override
        public void onMediaSelect(@NonNull Context context, @NonNull List<MediaData> mediaDataList) {
            if (mediaDataList.size() == 1) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra(VideoPlayerActivity.PATH, MediaMetadataUtils.getPath(context, mediaDataList.get(0).getContentUri()));
                context.startActivity(intent);
            }
        }
    }

    /**
     * duet record
     */
    private void duetRecord() {
        MediaPicker.from(this)
                .showImage(false)
                .showVideo(true)
                .setMediaSelector((context, mediaDataList) -> {
                    if (mediaDataList.size() > 0) {
                        onDuetRecord(mediaDataList.get(0));
                    }
                })
                .show();
    }

    /**
     * 模拟同框录制
     * @param mediaData 媒体数据
     */
    private void onDuetRecord(@NonNull MediaData mediaData) {
        Intent intent = new Intent(MainActivity.this, DuetRecordActivity.class);
        intent.putExtra(DuetRecordActivity.DUET_MEDIA, mediaData);
        startActivity(intent);
    }
}
