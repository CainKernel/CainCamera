package com.cgfay.video.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.video.R;
import com.cgfay.video.fragment.VideoCutFragment;

public class VideoCutActivity extends AppCompatActivity {

    public static final String PATH = "path";

    private static final String FRAGMENT_VIDEO_CROP = "fragment_video_cut";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_crop);
        if (null == savedInstanceState) {
            String videoPath = getIntent().getStringExtra(PATH);
            VideoCutFragment fragment = VideoCutFragment.newInstance();
            fragment.setVideoPath(videoPath);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_content, fragment, FRAGMENT_VIDEO_CROP)
                    .commit();
        }
    }
}
