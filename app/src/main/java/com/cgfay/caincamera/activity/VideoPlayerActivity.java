package com.cgfay.caincamera.activity;

import androidx.appcompat.app.AppCompatActivity;

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
        if (savedInstanceState == null) {
            String path = getIntent().getStringExtra(PATH);
            VideoPlayerFragment fragment = VideoPlayerFragment.newInstance(path);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, FRAGMENT_VIDEO_PLAYER)
                    .commit();
        }
    }
}
