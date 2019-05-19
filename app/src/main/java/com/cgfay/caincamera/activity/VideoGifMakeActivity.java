package com.cgfay.caincamera.activity;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.fragment.VideoGifMakeFragment;

/**
 * 视频转gif
 */
public class VideoGifMakeActivity extends AppCompatActivity {

    public static final String PATH = "video_path";

    private static final String FRAGMENT_VIDEO_GIF_MAKE = "fragment_video_gif_make";

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
        setContentView(R.layout.activity_music_merge);
        if (null == savedInstanceState) {
            String videoPath = getIntent().getStringExtra(PATH);
            VideoGifMakeFragment fragment = VideoGifMakeFragment.newInstance();
            fragment.setVideoPath(videoPath);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_content, fragment)
                    .addToBackStack(FRAGMENT_VIDEO_GIF_MAKE)
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
