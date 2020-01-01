package com.cgfay.image.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.cgfay.imagelibrary.R;
import com.cgfay.image.fragment.ImageEditedFragment;

public class ImageEditActivity extends AppCompatActivity {

    private static final String FRAGMENT_IMAGE = "fragment_image";

    public static final String IMAGE_PATH = "image_path";
    public static final String DELETE_INPUT_FILE = "delete_input_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
        if (null == savedInstanceState) {
            ImageEditedFragment fragment = new ImageEditedFragment();
            fragment.setImagePath(getIntent().getStringExtra(IMAGE_PATH), getIntent().getBooleanExtra(DELETE_INPUT_FILE, false));
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
