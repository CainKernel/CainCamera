package com.cgfay.caincamera.activity;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.fragment.MusicMergeFragment;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.fragment.MusicPickerFragment;

/**
 * 视频音乐合成
 */
public class MusicMergeActivity extends AppCompatActivity {

    public static final String PATH = "video_path";

    private static final String FRAGMENT_MUSIC_MERGE = "fragment_music_merge";

    private String mVideoPath;

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
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> hideNavigationBar());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigationBar();
        setContentView(R.layout.activity_music_merge);
        if (null == savedInstanceState) {
            mVideoPath = getIntent().getStringExtra(PATH);
            MusicPickerFragment fragment = new MusicPickerFragment();
            fragment.addOnMusicSelectedListener(listener);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private MusicPickerFragment.OnMusicSelectedListener listener =
            new MusicPickerFragment.OnMusicSelectedListener() {
        @Override
        public void onMusicSelectClose() {
            finish();
        }

        @Override
        public void onMusicSelected(MusicData musicData) {
            MusicMergeFragment fragment = MusicMergeFragment.newInstance();
            fragment.setVideoPath(mVideoPath);
            fragment.setMusicPath(musicData.getPath(), musicData.getDuration());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, FRAGMENT_MUSIC_MERGE)
                    .commit();
        }
    };

}
