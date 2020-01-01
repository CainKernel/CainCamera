package com.cgfay.caincamera.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.fragment.FFMediaRecordFragment;

public class FFMediaRecordActivity extends AppCompatActivity {

    private static final String FRAGMENT_FFMEDIA_RECORD = "FRAGMENT_FFMEDIA_RECORD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmedia_record);
        if (null == savedInstanceState) {
            FFMediaRecordFragment fragment = new FFMediaRecordFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment, FRAGMENT_FFMEDIA_RECORD)
                    .commit();
        }
    }

}
