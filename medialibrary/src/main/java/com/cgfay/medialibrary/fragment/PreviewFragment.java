package com.cgfay.medialibrary.fragment;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.util.Util;
import com.cgfay.medialibrary.R;
import com.cgfay.medialibrary.model.MediaItem;
import com.cgfay.medialibrary.engine.MediaScanParam;
import com.cgfay.medialibrary.utils.MediaMetadataUtils;

public class PreviewFragment extends Fragment {

    private static final String CURRENT_MEDIA = "current_media";

    private ImageView mImageView;
    private ImageView mPlayView;
    private VideoView mVideoView;


    public static PreviewFragment newInstance(MediaItem item) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CURRENT_MEDIA, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MediaItem item = getArguments().getParcelable(CURRENT_MEDIA);
        if (item == null) {
            return;
        }

        mPlayView = view.findViewById(R.id.iv_play);
        if (item.isImage()) { // 根据类型判断是图片还是视频
            mImageView = (ImageView) view.findViewById(R.id.image_view);
            Point size = MediaMetadataUtils.getBitmapSize(item.getContentUri(), getActivity());
            if (item.isGif()) {
                MediaScanParam.getInstance().mediaLoader.loadGif(getContext(), size.x, size.y,
                        mImageView, item.getContentUri());
            } else {
                MediaScanParam.getInstance().mediaLoader.loadImage(getContext(), size.x, size.y,
                        mImageView, item.getContentUri());
            }
            mImageView.setVisibility(View.VISIBLE);
            mPlayView.setVisibility(View.GONE);
        } else {
            mVideoView = view.findViewById(R.id.video_view);
            mVideoView.setVisibility(View.VISIBLE);
            setupVideo(item.getContentUri());

            mVideoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVideoView.isPlaying()) {
                        if (mVideoView.canPause()) {
                            mVideoView.pause();
                        }
                        mPlayView.setVisibility(View.VISIBLE);
                    }
                }
            });

            mPlayView.setVisibility(View.VISIBLE);
            mPlayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVideoView != null) {
                        if (!mVideoView.isPlaying()) {
                            mVideoView.start();
                            mPlayView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
        }
    }

    /**
     * 设置视频
     * @param uri
     */
    private void setupVideo(Uri uri) {
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 这里定位到首帧
                mVideoView.seekTo(0);
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.seekTo(0);
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoView.stopPlayback();
                return false;
            }
        });
        mVideoView.setVideoURI(uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoView != null && !mVideoView.isPlaying()) {
            mVideoView.resume();
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
}
