package com.cgfay.video.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.cgfay.video.R;
import com.cgfay.video.fragment.VideoCropFragment;

public class VideoCropActivity extends AppCompatActivity {

    public static final String PATH = "path";

    private static final String FRAGMENT_VIDEO_CROP = "fragment_video_crop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);
        setContentView(R.layout.activity_video_crop);
        if (null == savedInstanceState) {
            String videoPath = getIntent().getStringExtra(PATH);
            VideoCropFragment fragment = VideoCropFragment.newInstance();
            fragment.setVideoPath(videoPath);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_content, fragment, FRAGMENT_VIDEO_CROP)
                    .addToBackStack(FRAGMENT_VIDEO_CROP)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // 判断fragment栈中的个数，如果只有一个，则表示当前只处于视频编辑主页面点击返回
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            VideoCropFragment fragment = (VideoCropFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_VIDEO_CROP);
            if (fragment != null) {
                fragment.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
