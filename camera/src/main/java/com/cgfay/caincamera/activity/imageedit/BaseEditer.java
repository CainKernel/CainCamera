package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * 编辑器基类
 * Created by Administrator on 2018/3/12.
 */

public class BaseEditer {

    protected WeakReference<TextView> mWeakTextView;
    protected Context mContext;
    protected LayoutInflater mInflater;   // 布局加载器器

    public BaseEditer(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
    }

    protected void initView() {

    }

    /**
     * 设置用于显示滤镜值的TextView
     * @param textView
     */
    public void setTextView(TextView textView) {
        if (mWeakTextView != null) {
            mWeakTextView.clear();
        }
        mWeakTextView = new WeakReference<TextView>(textView);
    }

}
