package com.cgfay.caincamera.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.bean.MusicItem;
import com.cgfay.caincamera.fragment.MusicScanFragment;
import com.cgfay.ffmpeglibrary.activity.AVMusicPlayerActivity;

/**
 * 查找音乐
 */
public class MusicScanActivity extends AppCompatActivity
        implements MusicScanFragment.OnMusicSelectedListener {

    private MusicScanFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_scan);

        if (null == savedInstanceState) {
            mFragment = new MusicScanFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFragment != null) {
            mFragment.addOnMusicSelectedListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFragment != null) {
            mFragment.addOnMusicSelectedListener(null);
        }
    }

    @Override
    public void onMusicSelected(MusicItem music) {
        Intent intent = new Intent(MusicScanActivity.this, AVMusicPlayerActivity.class);
        intent.putExtra(AVMusicPlayerActivity.PATH, music.getSongUrl());
        startActivity(intent);
        finish();
    }
}
