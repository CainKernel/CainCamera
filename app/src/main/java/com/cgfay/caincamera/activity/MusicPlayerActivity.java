package com.cgfay.caincamera.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.fragment.MusicPlayerFragment;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.fragment.MusicPickerFragment;

public class MusicPlayerActivity extends AppCompatActivity {

    private static final String FRAGMENT_MUSIC_PLAYER = "fragment_music_player";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        if (null == savedInstanceState) {
            MusicPickerFragment fragment = new MusicPickerFragment();
            fragment.addOnMusicSelectedListener(listener);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, FRAGMENT_MUSIC_PLAYER)
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
                    MusicPlayerFragment fragment = MusicPlayerFragment.newInstance(musicData.getPath());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment, FRAGMENT_MUSIC_PLAYER)
                            .commit();
                }
            };
}
