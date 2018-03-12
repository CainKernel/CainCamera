package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.cgfay.caincamera.R;

import java.lang.ref.WeakReference;

/**
 * 一键美化
 * Created by Administrator on 2018/3/12.
 */

public class BeautifyEditer extends BaseEditer implements View.OnClickListener {

    private WeakReference<TextView> mWeakTextView;

    private Context mContext;
    private LayoutInflater mInflater;   // 布局加载器器

    private HorizontalScrollView mLayoutBeautify;
    private Button mBtnSource;  // 原片
    private Button mBtnAuto;    // 自动
    private Button mBtnFood;    // 美食
    private Button mBtnObject;  // 静物
    private Button mBtnScreen;  // 风景
    private Button mBtnPerson;  // 人物

    public BeautifyEditer(Context context) {
        super(context);
    }

    @Override
    protected void initView() {
        mLayoutBeautify = (HorizontalScrollView) mInflater
                .inflate(R.layout.view_image_edit_beautify, null);

        mBtnSource = (Button) mLayoutBeautify.findViewById(R.id.btn_source);
        mBtnAuto = (Button) mLayoutBeautify.findViewById(R.id.btn_auto);
        mBtnFood = (Button) mLayoutBeautify.findViewById(R.id.btn_food);
        mBtnObject = (Button) mLayoutBeautify.findViewById(R.id.btn_object);
        mBtnScreen = (Button) mLayoutBeautify.findViewById(R.id.btn_screen);
        mBtnPerson = (Button) mLayoutBeautify.findViewById(R.id.btn_person);

        mBtnSource.setOnClickListener(this);
        mBtnAuto.setOnClickListener(this);
        mBtnFood.setOnClickListener(this);
        mBtnObject.setOnClickListener(this);
        mBtnScreen.setOnClickListener(this);
        mBtnPerson.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 原片
            case R.id.btn_source:
                break;

            // 自动
            case R.id.btn_auto:
                break;

            // 食物
            case R.id.btn_food:
                break;

            // 静物
            case R.id.btn_object:
                break;

            // 风景
            case R.id.btn_screen:
                break;

            // 人物
            case R.id.btn_person:
                break;
        }
    }



    public HorizontalScrollView getLayoutBeautify() {
        return mLayoutBeautify;
    }
}
