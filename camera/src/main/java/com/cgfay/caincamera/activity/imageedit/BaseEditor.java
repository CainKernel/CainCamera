package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * 编辑器基类
 * Created by Administrator on 2018/3/12.
 */

public class BaseEditor {

    protected WeakReference<TextView> mWeakTextView;
    protected Context mContext;
    protected LayoutInflater mInflater;   // 布局加载器器
    protected WeakReference<ImageEditManager> mWeakManager;

    public BaseEditor(Context context, ImageEditManager manager) {
        mContext = context;
        mWeakManager = new WeakReference<ImageEditManager>(manager);
        mInflater = (LayoutInflater) mContext.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initView();
    }

    /**
     * 初始化视图
     */
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

    /**
     * 重置所有值
     */
    public void resetAllChanged() {

    }
}
