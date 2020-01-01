package com.cgfay.video.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.video.R;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.fragment.MusicPickerFragment;
import com.cgfay.video.fragment.VideoEditFragment;

public class VideoEditActivity extends AppCompatActivity implements VideoEditFragment.OnSelectMusicListener,
        MusicPickerFragment.OnMusicSelectedListener {

    public static final String VIDEO_PATH = "videoPath";

    private static final String FRAGMENT_VIDEO_EDIT = "fragment_video_edit";
    private static final String FRAGMENT_MUSIC_SELECT = "fragment_video_music_select";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
        if (null == savedInstanceState) {
            String videoPath = getIntent().getStringExtra(VIDEO_PATH);
            VideoEditFragment fragment = VideoEditFragment.newInstance();
            fragment.setOnSelectMusicListener(this);
            fragment.setVideoPath(videoPath);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_content, fragment, FRAGMENT_VIDEO_EDIT)
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
    protected void onDestroy() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            VideoEditFragment fragment = (VideoEditFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_VIDEO_EDIT);
            if (fragment != null) {
                fragment.setOnSelectMusicListener(null);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onOpenMusicSelectPage() {
        MusicPickerFragment fragment = new MusicPickerFragment();
        fragment.addOnMusicSelectedListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_slide_up, 0)
                .add(R.id.fragment_content, fragment)
                .addToBackStack(FRAGMENT_MUSIC_SELECT)
                .commit();
    }

    @Override
    public void onMusicSelectClose() {
        getSupportFragmentManager().popBackStack(FRAGMENT_VIDEO_EDIT, 0);
    }

    @Override
    public void onMusicSelected(MusicData musicData) {
        getSupportFragmentManager().popBackStack(FRAGMENT_VIDEO_EDIT, 0);
        VideoEditFragment fragment = (VideoEditFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_VIDEO_EDIT);
        if (fragment != null) {
            fragment.setSelectedMusic(musicData.getPath(), musicData.getDuration());
        }
    }
}
