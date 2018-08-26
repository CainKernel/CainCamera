package com.cgfay.imagelibrary.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.cgfay.imagelibrary.R;
import com.cgfay.imagelibrary.fragment.ImageEditedFragment;

public class ImageEditActivity extends AppCompatActivity {

    private static final String FRAGMENT_IMAGE = "fragment_image";

    public static final String PATH = "Path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
        if (null == savedInstanceState) {
            ImageEditedFragment fragment = new ImageEditedFragment();
            fragment.setImagePath(getIntent().getStringExtra(PATH));
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, FRAGMENT_IMAGE)
                    .addToBackStack(FRAGMENT_IMAGE)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        // 判断fragment栈中的个数，如果只有一个，则表示当前只处于图片编辑主页面点击返回状态
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 1) {
            getSupportFragmentManager().popBackStack();
        } else if (backStackEntryCount == 1) {
            ImageEditedFragment fragment = (ImageEditedFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENT_IMAGE);
            if (fragment != null) {
                fragment.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
