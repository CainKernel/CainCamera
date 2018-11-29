package com.cgfay.ffmpeglibrary.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.ffmpeglibrary.R;

/**
 * 自研的媒体播放器测试页面
 */
public class AVMediaPlayerActivity extends AppCompatActivity {

    public static final String PATH = "PATH";
    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        if (savedInstanceState == null) {
            mPath = getIntent().getStringExtra(PATH);
        }
    }
}
