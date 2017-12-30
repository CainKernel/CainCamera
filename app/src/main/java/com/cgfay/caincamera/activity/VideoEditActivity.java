package com.cgfay.caincamera.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.caincamera.R;

public class VideoEditActivity extends AppCompatActivity {

    // 背景颜色
    private int[] mGraffitiBackgrounds = new int[] {
            R.drawable.graffiti_white,
            R.drawable.graffiti_red,
            R.drawable.graffiti_yellow,
            R.drawable.graffiti_blue,
            R.drawable.graffiti_green
    };

    // 涂鸦颜色
    private int[] mGraffitiColors = new int[] {
            R.color.graffitiWhite,
            R.color.graffitiRed,
            R.color.graffitiYellow,
            R.color.graffitiBlue,
            R.color.graffitiGreen
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
    }
}
