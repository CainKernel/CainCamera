package com.cgfay.videolibrary.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.videolibrary.bean.Music;
import com.cgfay.videolibrary.R;
import com.cgfay.videolibrary.fragment.MusicSelectFragment;
import com.cgfay.videolibrary.fragment.VideoEditFragment;

public class VideoEditActivity extends AppCompatActivity
        implements VideoEditFragment.OnPageOperationListener,
        MusicSelectFragment.OnMusicSelectedListener {

    public static final String PATH = "Path";

    private static final String FRAGMENT_VIDEO_EDIT = "fragment_video_edit";
    private static final String FRAGMENT_MUSIC_SELECT = "fragment_video_music_select";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
        if (null == savedInstanceState) {
            String videoPath = getIntent().getStringExtra(PATH);
            VideoEditFragment fragment = VideoEditFragment.newInstance();
            fragment.setOnPageOperationListener(this);
            fragment.setVideoPath(videoPath);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, FRAGMENT_VIDEO_EDIT)
                    .addToBackStack(FRAGMENT_VIDEO_EDIT)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // 判断fragment栈中的个数，如果只有一个，则表示当前只处于视频编辑主页面点击返回
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            VideoEditFragment fragment = (VideoEditFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_VIDEO_EDIT);
            if (fragment != null) {
                fragment.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onOpenMusicSelectPage() {
        MusicSelectFragment fragment = new MusicSelectFragment();
        fragment.addOnMusicSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack(FRAGMENT_MUSIC_SELECT)
                .commit();
    }

    @Override
    public void onMusicSelected(Music music) {
        getSupportFragmentManager().popBackStack(FRAGMENT_VIDEO_EDIT, 0);
        VideoEditFragment fragment = (VideoEditFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_VIDEO_EDIT);
        if (fragment != null) {
            fragment.setSelectedMusic(music);
        }
    }

}
