package com.cgfay.videolibrary.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.videolibrary.R;
import com.cgfay.videolibrary.adapter.VideoFilterAdapter;
import com.cgfay.videolibrary.widget.IndicatorProgress;

/**
 * 特效页面
 */
public class VideoEffectFragment extends BaseVideoFilterFragment implements RecyclerView.OnItemTouchListener {

    private GestureDetectorCompat mGestureDetector;

    private boolean mLongPressed;

    private IndicatorProgress.IndicatorScrollListener mIndicatorScrollListener;

    private OnItemLongPressedListener mLongPressedListener;
    private OnEditListener mEditListener;

    protected Button mBtnDelete;
    protected Button mBtnUndo;
    private IndicatorProgress mIndicatorProgress;

    private Handler mHandler = new Handler();

    public VideoEffectFragment() {
        super();
    }

    @Override
    protected void initFilters() {
//        mGlFilterType.addAll(GLImageFilterManager.getEffectTypes());
//        mFilterName.addAll(GLImageFilterManager.getEffectNames());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_effect, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnDelete = (Button) view.findViewById(R.id.btn_delete);
        mBtnUndo = (Button) view.findViewById(R.id.btn_undo);
        mBtnDelete.setOnClickListener(mClickListener);
        mBtnUndo.setOnClickListener(mClickListener);

        mIndicatorProgress = (IndicatorProgress) view.findViewById(R.id.indicator_progress);
        mIndicatorProgress.setIndicatorScrollListener(mIndicatorScrollListener);

        mFilterListView = (RecyclerView) view.findViewById(R.id.list_effect);
        mFilterLayoutManager = new LinearLayoutManager(getActivity());
        mFilterLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(mFilterLayoutManager);
        VideoFilterAdapter adapter = new VideoFilterAdapter(getActivity(), mGlFilterType, mFilterName);
        mFilterListView.setAdapter(adapter);
        mGestureDetector = new GestureDetectorCompat(mFilterListView.getContext(),
                new ItemTouchHelperGestureListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        mFilterListView.addOnItemTouchListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mFilterListView.removeOnItemTouchListener(this);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        if (e.getAction() == MotionEvent.ACTION_CANCEL || e.getAction() == MotionEvent.ACTION_UP) {
            if (mLongPressed) {
                mLongPressed = false;
                if (mLongPressedListener != null) {
                    View child = mFilterListView.findChildViewUnder(e.getX(), e.getY());
                    int position = mFilterListView.getChildAdapterPosition(child);
                    mLongPressedListener.onLongPressedFinished(position);
                }
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public void showBtnDelete(final boolean showing) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBtnDelete.setVisibility(showing ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void showBtnUndo(final boolean showing) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBtnUndo.setVisibility(showing ? View.VISIBLE : View.GONE);
            }
        });
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_delete) {
                if (mEditListener != null) {
                    mEditListener.onEffectDelete();
                }
                if (mIndicatorProgress != null) {
                    mIndicatorProgress.deleteColorRect();
                }
            } else if (v.getId() == R.id.btn_undo) {
                if (mEditListener != null) {
                    mEditListener.onEffectUndoDelete();
                }
                if (mIndicatorProgress != null) {
                    mIndicatorProgress.undoDeleteColorRect();
                }
            }
        }
    };

    public IndicatorProgress getIndicatorProgress() {
        return mIndicatorProgress;
    }

    /**
     * 设置边框滑动监听
     * @param listener
     */
    public void setIndicatorScrollListener(IndicatorProgress.IndicatorScrollListener listener) {
        mIndicatorScrollListener = listener;
    }

    /**
     * 添加编辑监听器
     */
    public void addOnEditListener(OnEditListener listener) {
        mEditListener = listener;
    }

    public interface OnEditListener {

        void onEffectDelete();

        void onEffectUndoDelete();
    }

    /**
     * 添加item列表长按监听器
     * @param listener
     */
    public void addOnItemLongPressedListener(OnItemLongPressedListener listener) {
        mLongPressedListener = listener;
    }

    /**
     * item长按监听器
     */
    public interface OnItemLongPressedListener {
        // 长按准备
        void onLongPressedPrepared(int position);
        // 放弃
        void onLongPressedCancel(int position);
        // 长按开始
        void onLongPressedStarted(int position);
        // 长按结束
        void onLongPressedFinished(int position);
    }

    /**
     * item列表点击事件处理
     */
    private class ItemTouchHelperGestureListener implements GestureDetector.OnGestureListener {

        /**
         * 按下屏幕就会触发
         * @param e
         * @return
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        /**
         * 按下的时间超过瞬间，且在按下的时候没有松开或者拖动就会执行
         * @param e
         */
        @Override
        public void onShowPress(MotionEvent e) {
            mLongPressed = false;
            if (mLongPressedListener != null) {
                View child = mFilterListView.findChildViewUnder(e.getX(), e.getY());
                int position = mFilterListView.getChildAdapterPosition(child);
                mLongPressedListener.onLongPressedPrepared(position);
            }
        }

        /**
         * 一次单独的轻击操作，普通点击事件
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mLongPressed = false;
            if (mLongPressedListener != null) {
                View child = mFilterListView.findChildViewUnder(e.getX(), e.getY());
                int position = mFilterListView.getChildAdapterPosition(child);
                mLongPressedListener.onLongPressedCancel(position);
            }
            return false;
        }

        /**
         * 在屏幕上拖动事件
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        /**
         * 长按触摸屏，超过一定时长，就会触发这个事件
         * @param e
         */
        @Override
        public void onLongPress(MotionEvent e) {
            mLongPressed = true;
            if (mLongPressedListener != null) {
                View child = mFilterListView.findChildViewUnder(e.getX(), e.getY());
                int position = mFilterListView.getChildAdapterPosition(child);
                mLongPressedListener.onLongPressedStarted(position);
            }
        }

        /**
         * 滑屏，用户按下触摸屏、快速移动后松开
         * @param e1
         * @param e2
         * @param velocityX
         * @param velocityY
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

}
