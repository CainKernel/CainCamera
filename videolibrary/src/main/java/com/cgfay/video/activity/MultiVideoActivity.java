package com.cgfay.video.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cgfay.video.R;
import com.cgfay.video.fragment.MultiVideoFragment;

import java.util.ArrayList;

/**
 * 多段视频，音乐卡点
 */
public class MultiVideoActivity extends AppCompatActivity {

    public static final String PATH = "path";

    private static final String FRAGMENT_MULTI_VIDEO = "fragment_multi_video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_video);
        if (null == savedInstanceState) {
            ArrayList<String> pathList = getIntent().getStringArrayListExtra(PATH);
            if (pathList != null && pathList.size() > 0) {
                MultiVideoFragment fragment = MultiVideoFragment.newInstance(pathList);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_content, fragment, FRAGMENT_MULTI_VIDEO)
                        .commitAllowingStateLoss();
            } else {
                finish();
            }
        }
    }
}
