package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import com.cgfay.caincamera.R;

/**
 * 一键美化
 * Created by Administrator on 2018/3/12.
 */

public class BeautifyEditer extends BaseEditer implements View.OnClickListener {

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
        mBtnAuto.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
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
        resetButtonColor();
        switch (v.getId()) {
            // 原片
            case R.id.btn_source:
                renderSource();
                break;

            // 自动
            case R.id.btn_auto:
                renderAuto();
                break;

            // 食物
            case R.id.btn_food:
                renderFood();
                break;

            // 静物
            case R.id.btn_object:
                renderObject();
                break;

            // 风景
            case R.id.btn_screen:
                renderScreeen();
                break;

            // 人物
            case R.id.btn_person:
                renderPerson();
                break;
        }
    }

    private void renderSource() {
        mBtnSource.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    private void renderAuto() {
        mBtnAuto.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    private void renderFood() {
        mBtnFood.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    private void renderObject() {
        mBtnObject.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    private void renderScreeen() {
        mBtnScreen.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    private void renderPerson() {
        mBtnPerson.setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    private void resetButtonColor( ) {
        mBtnSource.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnAuto.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnFood.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnObject.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnScreen.setTextColor(mContext.getResources().getColor(R.color.white));
        mBtnPerson.setTextColor(mContext.getResources().getColor(R.color.white));
    }

    public HorizontalScrollView getLayoutBeautify() {
        return mLayoutBeautify;
    }
}
