package com.cgfay.image.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.imagelibrary.R;
import com.cgfay.uitls.utils.BitmapUtils;

public class ImagePreviewActivity extends AppCompatActivity {

    public static String PATH = "PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        String path = getIntent().getStringExtra(PATH);
        TextView textView = findViewById(R.id.image_path);
        textView.setText("图片路径：" + path);
        ImageView imageView = findViewById(R.id.image_preview);
        imageView.setImageBitmap(BitmapUtils.getBitmapFromFile(path));
    }
}
