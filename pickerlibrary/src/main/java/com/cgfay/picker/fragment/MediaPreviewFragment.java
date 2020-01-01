package com.cgfay.picker.fragment;

import android.graphics.PointF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.cgfay.picker.model.MediaData;
import com.cgfay.picker.widget.subsamplingview.ImageSource;
import com.cgfay.picker.widget.subsamplingview.OnImageEventListener;
import com.cgfay.picker.widget.subsamplingview.SubsamplingScaleImageView;
import com.cgfay.scan.R;
import com.cgfay.uitls.utils.DisplayUtils;

/**
 * 媒体预览页面
 */
public class MediaPreviewFragment extends AppCompatDialogFragment {

    private static final String CURRENT_MEDIA = "current_media";

    private static final float MAX_SCALE = 15f;
    private static final int LONG_IMG_ASPECT_RATIO = 3;
    private static final int LONG_IMG_MINIMUM_LENGTH = 1500;

    private SubsamplingScaleImageView mOriginImageView;

    private VideoView mVideoView;

    public static MediaPreviewFragment newInstance(MediaData mediaData) {
        MediaPreviewFragment fragment = new MediaPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CURRENT_MEDIA, mediaData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.PickerPreviewStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoView != null && !mVideoView.isPlaying()) {
            mVideoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.canPause()) {
            mVideoView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    private void initView(@NonNull View view) {
        if (getArguments() == null) {
            removeFragment();
            return;
        }
        final MediaData mediaData = getArguments().getParcelable(CURRENT_MEDIA);
        if (mediaData == null) {
            removeFragment();
            return;
        }
        if (mediaData.isImage()) {
            mOriginImageView = view.findViewById(R.id.scale_image_view);
            mOriginImageView.setVisibility(View.VISIBLE);
            mOriginImageView.setMaxScale(MAX_SCALE);
            mOriginImageView.setOnClickListener(v -> removeFragment());
            mOriginImageView.setOnImageEventListener(new OnImageEventListener() {
                @Override
                public void onImageLoaded(int width, int height) {
                    calculatePictureScale(mOriginImageView, width, height);
                }
            });
            mOriginImageView.setImage(ImageSource.uri(mediaData.getPath()));
        } else {
            view.setOnClickListener(v -> {
                removeFragment();
            });
            mVideoView = view.findViewById(R.id.video_view);
            mVideoView.setVisibility(View.VISIBLE);
            setupVideo(mediaData.getPath());
        }
    }

    /**
     * 计算图片缩放距离
     * @param view
     * @param width
     * @param height
     */
    private void calculatePictureScale(@NonNull SubsamplingScaleImageView view, int width, int height) {
        if (height >= LONG_IMG_MINIMUM_LENGTH
                && height / width >= LONG_IMG_ASPECT_RATIO) {
            float scale = DisplayUtils.getScreenWidth(getContext()) / (float) width;
            float centerX = DisplayUtils.getScreenWidth(getContext()) / 2;
            view.setScaleAndCenterWithAnim(scale, new PointF(centerX, 0.0f));
            view.setDoubleTapZoomScale(scale);
        }
    }

    /**
     * 设置视频路径
     * @param path
     */
    private void setupVideo(@NonNull String path) {
        mVideoView.setOnPreparedListener(mp -> {
            mVideoView.seekTo(0);
        });
        mVideoView.setOnCompletionListener(mp -> mVideoView.seekTo(0));
        mVideoView.setOnErrorListener((mp, what, extra) -> {
            mVideoView.stopPlayback();
            return false;
        });
        mVideoView.setVideoPath(path);
        mVideoView.start();
    }

    /**
     * 移除Fragment
     */
    private void removeFragment() {
        if (getParentFragment() != null) {
            getParentFragment()
                    .getChildFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitNowAllowingStateLoss();
        } else if (getActivity() != null) {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitNowAllowingStateLoss();
        }
    }
}
