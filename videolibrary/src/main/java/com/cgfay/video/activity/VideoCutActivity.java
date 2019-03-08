package com.cgfay.video.activity;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.cgfay.video.R;
import com.cgfay.video.fragment.VideoCutFragment;

public class VideoCutActivity extends AppCompatActivity {

    public static final String PATH = "path";

    private static final String FRAGMENT_VIDEO_CROP = "fragment_video_cut";

    protected void hideNavigationBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    hideNavigationBar();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigationBar();
        setContentView(R.layout.activity_video_crop);
        if (null == savedInstanceState) {
            String videoPath = getIntent().getStringExtra(PATH);
            VideoCutFragment fragment = VideoCutFragment.newInstance();
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
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
