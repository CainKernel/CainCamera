package com.cgfay.caincamera.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.fragment.VideoPlayerFragment;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String PATH = "PATH";

    private static final String FRAGMENT_VIDEO_PLAYER = "fragment_video_player";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_VIDEO_PLAYER);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
        String path = getIntent().getStringExtra(PATH);
        VideoPlayerFragment playerFragment = VideoPlayerFragment.newInstance(path);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, playerFragment, FRAGMENT_VIDEO_PLAYER)
                .commit();
    }
}
