package com.cgfay.video.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.cgfay.media.CAVMediaMetadataRetriever;
import com.cgfay.media.CAVMetadata;
import com.cgfay.video.R;
import com.cgfay.video.bean.VideoFrameNode;
import com.cgfay.video.bean.VideoSpeed;

import java.util.ArrayList;

/**
 * 视频帧裁剪页面
 */
public class VideoCutViewBar extends View {


    private final int CROP_TIME_MAX_15 = 15000;

    private final int CROP_TIME_MAX_120 = 120000;

    private final int CROP_TIME_MIN = 1500; // 最小长度为1500毫秒

    private final int TOUCH_EDGE = 30;

    private int mMaxTime = CROP_TIME_MAX_15;

    private long mFrameTime = 1000L;

    // 当前速度
    private VideoSpeed mCurrentSpeed = VideoSpeed.SPEED_L2;

    private String mVideoPath;
    private boolean loadFinish;

    // 帧列表
    private ArrayList<VideoFrameNode> mLoadingFrames;

    private VideoFrameNode mCurrentNode;
    private VideoFrameNode mFirstNode;
    private VideoFrameNode mLastNode;
    private VideoFrameNode mReleaseNode;

    private int mVideoWidth;        // 宽度
    private int mVideoHeight;       // 高度
    private int mVideoRotation;     // 旋转角度
    private long mVideoDuration;    // 时长
    private float mVideoSar;        // 长宽比

    private float mViewFrameWidth;  // 帧宽度
    private int mViewNumber = 1;    // 控件显示多少帧

    private boolean loadFail;   // 加载失败

    // 裁剪参数
    private float mCropMax = CROP_TIME_MAX_15;  // 最大允许选取时间
    private float mCropStart = 0;               // 选取的开始时间
    private float mCropEnd = mCropMax;          // 选取的结束时间

//    // 减速处理，TODO 滑动时播放器的音频会有些问题，暂时不开减速处理
//    private SlideHandler mSlideHandler = new SlideHandler();

    // 帧提取器
    private CAVMediaMetadataRetriever mMetadataRetriever;

    public VideoCutViewBar(Context context) {
        this(context,null);
    }

    public VideoCutViewBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoCutViewBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMetadataRetriever = new CAVMediaMetadataRetriever();
        mLoadingFrames = new ArrayList<>();
        new LoadThread().start();
    }

    public void release() {
        mReleaseNode = mCurrentNode;
        synchronized (mLoadingFrames) {
            mLoadingFrames.clear();
        }
        new ReleaseThread().start();
        mMetadataRetriever.release();
        mMetadataRetriever = null;
//        mSlideHandler.removeCallbacksAndMessages(null);
    }

    private void reset() {
        if (mFirstNode == null) {
            return ;
        }
        synchronized (mLoadingFrames) {
            mLoadingFrames.clear();
        }
        VideoFrameNode node = mFirstNode;
        if (mCurrentNode != null) {
            synchronized (mCurrentNode) {
                mCurrentNode = null;
                mFirstNode = null;
                mLastNode = null;
                mExcursionX = 0;
            }
        }
        while (node != null) {
            if (node.getBitmap() != null && !node.getBitmap().isRecycled()) {
                node.getBitmap().recycle();
            }
            VideoFrameNode nextNode = node.next();
            node.setNext(null);
            node = nextNode;
            if (nextNode != null) {
                nextNode.setPrev(null);
            }
        }
//        if (mSlideHandler.hasMessages(SLIDE_MESSAGE)) {
//            mSlideHandler.removeMessages(SLIDE_MESSAGE);
//        }
        invalidate();
    }

    public VideoSpeed getCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
        mReleaseNode = mCurrentNode;
        synchronized (mLoadingFrames) {
            mLoadingFrames.clear();
        }
        reset();
        try {
            mMetadataRetriever.setDataSource(mVideoPath);
            CAVMetadata metadata = mMetadataRetriever.getMetadata();

            mVideoWidth = metadata.getInt(CAVMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            mVideoHeight = metadata.getInt(CAVMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            mVideoRotation = metadata.getInt(CAVMediaMetadataRetriever.METADATA_KEY_ROTAE);
            mVideoDuration = metadata.getInt(CAVMediaMetadataRetriever.METADATA_KEY_DURATION);

            if (mVideoRotation % 180 != 0) {
                int temp = mVideoWidth;
                mVideoWidth = mVideoHeight;
                mVideoHeight = temp;
            }

            if (mCropMax > mVideoDuration * mCurrentSpeed.getSpeed()) {
                mCropMax = mVideoDuration * mCurrentSpeed.getSpeed();
                mCropEnd = mCropMax;
            }

            if (mOnOnVideoCropViewBarListener != null) {
                mOnOnVideoCropViewBarListener.onRangeChange(0, (long) (mCropEnd - mCropStart));
            }

            mVideoSar = mVideoWidth / (float) mVideoHeight;
            mViewFrameWidth = (float) getMeasuredHeight() * mVideoSar / mViewNumber;
            if (mVideoDuration == 0 || mVideoWidth == 0 || mVideoHeight == 0) {
                loadFail = true;
                if (mOnOnVideoCropViewBarListener != null) {
                    mOnOnVideoCropViewBarListener.onError("failed to load metadata");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            loadFail = true;
            if (mOnOnVideoCropViewBarListener != null) {
                mOnOnVideoCropViewBarListener.onError(e.getMessage() == null ? "not error message" : e.getMessage());
            }
        }
    }

    public void setSpeed(VideoSpeed videoSpeed) {
        mCurrentSpeed = videoSpeed;
        if (mVideoDuration / mCurrentSpeed.getSpeed() > mMaxTime) {
            mCropMax = mMaxTime;
        } else {
            mCropMax = mVideoDuration / mCurrentSpeed.getSpeed();
        }
        mCropEnd = mCropMax;
        mCropStart = 0;

        if (mOnOnVideoCropViewBarListener != null) {
            mOnOnVideoCropViewBarListener.onRangeChange(0, (long) (mCropEnd - mCropStart));
        }
        reset();
    }

    public void setMaxTime(int maxTime) {
        mMaxTime = maxTime;
        if (mViewFrameWidth != 0) {
            mFrameTime = (long) (mMaxTime /(getMeasuredWidth() / mViewFrameWidth)); //view的宽度表示15秒，算出每一帧应该截取的时间
        }
        if (mVideoDuration / mCurrentSpeed.getSpeed() > mMaxTime) {
            mCropMax = mMaxTime;
        } else {
            mCropMax = mVideoDuration / mCurrentSpeed.getSpeed();
        }
        mCropEnd = mCropMax;
        mCropStart = 0;

        if (mOnOnVideoCropViewBarListener != null) {
            mOnOnVideoCropViewBarListener.onRangeChange(0, (long) (mCropEnd - mCropStart));
        }
        reset();
    }

    /**
     * 处理触摸移动事件
     */
    private void processEventMove() {

        if (loadFail) {
            return;
        }

        if (mCurrentNode != null && mCurrentNode.getFrameTime() == 0 && mExcursionX < 0) {

            mExcursionX = 0;
            if (mOnOnVideoCropViewBarListener != null) {
                mOnOnVideoCropViewBarListener.onTouchChange((long) ((mCurrentNode == null ? 0
                        : mCurrentNode.getFrameTime()
                        + (mExcursionX / mViewFrameWidth * (mFrameTime*mCurrentSpeed.getSpeed())))
                        + mCropStart * mCurrentSpeed.getSpeed()));
            }
//            if (mSlideHandler.hasMessages(SLIDE_MESSAGE)) {
//                mSlideHandler.removeMessages(SLIDE_MESSAGE);
//            }
            return;
        }
        if (mVideoDuration != 0 && ((mLastNode != null && mLastNode.getFrameTime() + (mFrameTime * mCurrentSpeed.getSpeed()) > mVideoDuration && mExcursionX > 0)
                || (mCurrentNode != null && mCurrentNode.getFrameTime() + (mFrameTime * mCurrentSpeed.getSpeed()) > mVideoDuration && mExcursionX > 0))
                || (mCurrentNode != null && mCurrentNode.getFrameTime() + (mCropEnd - mCropStart) * mCurrentSpeed.getSpeed() + (mFrameTime * mCurrentSpeed.getSpeed()) > mVideoDuration && mExcursionX > 0)) {
            mExcursionX = 0;
            if (mOnOnVideoCropViewBarListener != null) {
                mOnOnVideoCropViewBarListener.onTouchChange((long) ((mCurrentNode == null ? 0
                        : mCurrentNode.getFrameTime()
                        + (mExcursionX / mViewFrameWidth * (mFrameTime * mCurrentSpeed.getSpeed())))
                        + mCropStart * mCurrentSpeed.getSpeed()));
            }
//            if (mSlideHandler.hasMessages(SLIDE_MESSAGE)) {
//                mSlideHandler.removeMessages(SLIDE_MESSAGE);
//            }
            return;
        }


        if (mCurrentNode == null) {
            return ;
        }
        if (mViewFrameWidth >= 0) {
            if (mExcursionX > mViewFrameWidth) {
                synchronized (mCurrentNode) {
                    if (mCurrentNode.next() == null) {
                        VideoFrameNode node = new VideoFrameNode();
                        node.setFrameTime((long) (mCurrentNode.getFrameTime() + (mFrameTime * mCurrentSpeed.getSpeed())));
                        node.setPrev(mCurrentNode);
                        mCurrentNode.setNext(node);
                        if (node.getFrameTime() <= mVideoDuration) {
                            if (mLastNode == null) {
                                mLastNode = node;
                            } else {
                                synchronized (mLastNode) {
                                    if (mLastNode.getFrameTime() < node.getFrameTime()) {
                                        node.setPrev(mLastNode);//关联链表，替换链表尾
                                        mLastNode.setNext(node);
                                        mLastNode = node;
                                    }
                                }
                            }
                        }
                        synchronized (mLoadingFrames) {
                            mLoadingFrames.add(node);
                        }
                    }
                    mCurrentNode = mCurrentNode.next();
                    if (mFirstNode != null && mCurrentNode != mFirstNode.next()
                            && mCurrentNode.getFrameTime() > mFirstNode.getFrameTime()) {
                        removeFront();
                    }
                }
                mExcursionX -= mViewFrameWidth;
            } else if (mExcursionX < -mViewFrameWidth && mCurrentNode.getFrameTime() != 0) {
                synchronized (mCurrentNode) {
                    if (mCurrentNode.prev() == null) {
                        VideoFrameNode node = new VideoFrameNode();
                        node.setFrameTime((long) (mCurrentNode.getFrameTime() - (mFrameTime * mCurrentSpeed.getSpeed())));
                        if (node.getFrameTime() < 0) {
                            mExcursionX = 0;
                            return ;
                        }
                        node.setNext(mCurrentNode);
                        mCurrentNode.setPrev(node);
                        if (mFirstNode == null) {
                            mFirstNode = node;
                        } else {
                            synchronized (mFirstNode) {
                                if (mFirstNode.getFrameTime() > node.getFrameTime()) {
                                    node.setNext(mFirstNode);//关联链表，替换链表头
                                    mFirstNode.setPrev(node);
                                    mFirstNode = node;
                                }
                            }
                        }
                        synchronized (mLoadingFrames) {
                            mLoadingFrames.add(node);
                        }
                    }
                    mCurrentNode = mCurrentNode.prev();
                    removeLast();
                }
                mExcursionX += mViewFrameWidth;
            }
        }
        if (mOnOnVideoCropViewBarListener != null) {
            mOnOnVideoCropViewBarListener.onTouchChange((long) ((mCurrentNode == null ? 0
                    : mCurrentNode.getFrameTime()
                    + (mExcursionX / mViewFrameWidth * (mFrameTime*mCurrentSpeed.getSpeed())))
                    + mCropStart * mCurrentSpeed.getSpeed()));
        }
    }

    /**
     * 移除头部结点
     */
    private void removeFront() {
        if (mFirstNode == null) {
            return;
        }
        synchronized (mFirstNode) {
            VideoFrameNode node = mFirstNode.next();
            if (node == null) {
                return;
            }
            node.setPrev(null);
            mFirstNode.setPrev(null);
            mFirstNode.setNext(null);
            if (mFirstNode.getBitmap() != null && !mFirstNode.getBitmap().isRecycled()) {
                mFirstNode.getBitmap().recycle();
                mFirstNode.setBitmap(null);
            }
            mFirstNode = node;
        }
    }

    /**
     * 移除尾部结点
     */
    private void removeLast() {
        if (mLastNode == null) {
            return ;
        }
        synchronized (mLastNode) {
            VideoFrameNode node = mLastNode.prev();
            if (node == null) {
                return ;
            }
            node.setNext(null);
            mLastNode.setPrev(null);
            mLastNode.setNext(null);
            if (mLastNode.getBitmap() != null && !mLastNode.getBitmap().isRecycled()) {
                mLastNode.getBitmap().recycle();
                mLastNode.setBitmap(null);
            }
            mLastNode = node;
        }
    }

    private VelocityTracker mVelocityTracker;
    private final int SLIDE_MESSAGE = 0x100;
    private int mExcursionX;
    private float mMoveX;

    // 选中控件裁剪边沿标志
    private boolean touchEdge;
    private boolean touchRight = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mMoveX = event.getX();
                if (mOnOnVideoCropViewBarListener != null) {
                    mOnOnVideoCropViewBarListener.onTouchDown();
                }

                int start = (int) (mCropStart * (getMeasuredWidth() / (float) mMaxTime));
                int end = (int) (mCropEnd * (getMeasuredWidth() / (float) mMaxTime));

                // 判断点击的地方是否在控件的裁剪边沿，点击位置左右偏移TOUCH_EDGE像素均表示边沿地方
                if (mMoveX >= start - TOUCH_EDGE && mMoveX <= start + TOUCH_EDGE) {
                    touchEdge = true;
                    touchRight = false;
                    break;
                } else if (mMoveX <= end + TOUCH_EDGE && mMoveX >= end - TOUCH_EDGE) {
                    touchEdge = true;
                    touchRight = true;
                    break;
                }

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
//                if (mSlideHandler.hasMessages(SLIDE_MESSAGE)) {
//                    mSlideHandler.removeMessages(SLIDE_MESSAGE);
//                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (touchEdge) {
                    if (touchRight) {
                        mCropEnd -= (mMoveX - event.getX()) / (getMeasuredWidth() / (float) mMaxTime);
                        if (mCropEnd <= mCropStart + CROP_TIME_MIN) {
                            mCropEnd = mCropStart + CROP_TIME_MIN;
                        }
                        if (mCropEnd > mCropMax) {
                            mCropEnd = mCropMax;
                        }
                    } else {
                        mCropStart -= (mMoveX - event.getX()) / (getMeasuredWidth() / (float) mMaxTime);
                        if (mCropStart + CROP_TIME_MIN >= mCropEnd) {
                            mCropStart = mCropEnd - CROP_TIME_MIN;
                        }
                        if (mCropStart < 0) {
                            mCropStart = 0;
                        }
                    }
                    if (mOnOnVideoCropViewBarListener != null) {
                        mOnOnVideoCropViewBarListener.onRangeChange((long) ((mCurrentNode == null ? 0
                                        : mCurrentNode.getFrameTime()
                                        + (mExcursionX / mViewFrameWidth * (mFrameTime * mCurrentSpeed.getSpeed())))
                                        + mCropStart * mCurrentSpeed.getSpeed()),
                                (long) (mCropEnd - mCropStart));
                    }
                    mMoveX = event.getX();
                    invalidate();
                    break;
                }

                // 设置检测间隔
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                mExcursionX += (int) (mMoveX - event.getX());
                processEventMove();
                mMoveX = event.getX();
                invalidate();
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mOnOnVideoCropViewBarListener != null) {
                    mOnOnVideoCropViewBarListener.onTouchUp();
                }
                if (touchEdge) {
                    touchEdge = false;
                    break;
                }
//                if (Math.abs(mVelocityTracker.getXVelocity()) > 1000) {
//                    Message message = mSlideHandler.obtainMessage();
//                    int xVelocity = (int) mVelocityTracker.getXVelocity();
//                    if (xVelocity > 8000) {
//                        xVelocity = 8000;
//                    } else if (xVelocity < -8000) {
//                        xVelocity = -8000;
//                    }
//                    message.arg1 = xVelocity;
//                    message.what = SLIDE_MESSAGE;
//                    mSlideHandler.sendMessage(message);
//                }
                break;
            }

            default: {
                break;
            }
        }
        return true;
    }

//    /**
//     * 滑动减速Handler
//     */
//    private class SlideHandler extends Handler {
//
//        @Override
//        public void handleMessage(Message msg) {
//            int slideVt = msg.arg1;
//            mExcursionX += -slideVt / 120;
//            processEventMove();
//            invalidate();
//            if (slideVt < 0) {
//                Message message = mSlideHandler.obtainMessage();
//                message.arg1 = slideVt + 120;
//                message.what = SLIDE_MESSAGE;
//                if (slideVt + 120 < -500) {
//                    mSlideHandler.sendMessageDelayed(message,10);
//                }
//            } else if (slideVt > 0) {
//                Message message = mSlideHandler.obtainMessage();
//                message.arg1 = slideVt - 120;
//                message.what = SLIDE_MESSAGE;
//                if (slideVt - 120 > 500) {
//                    mSlideHandler.sendMessageDelayed(message,10);
//                }
//            }
//        }
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mVideoSar != 0f) {
            mViewFrameWidth = (float) getMeasuredHeight() * mVideoSar / mViewNumber;
        }
        if (mViewFrameWidth != 0) {
            mFrameTime = (long) (mMaxTime / (getMeasuredWidth() / mViewFrameWidth));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (loadFail) {
            return ;
        }

        if (mCurrentNode == null) {
            VideoFrameNode node = new VideoFrameNode();
            node.setFrameTime(0);
            synchronized (mLoadingFrames) {
                mLoadingFrames.add(node);
            }
            mCurrentNode = node;
            mFirstNode = node;
            synchronized (mLoadingFrames) {
                mLoadingFrames.add(node);
            }
        }
        mViewFrameWidth = (float) getMeasuredHeight() * mVideoSar / mViewNumber;
        int frameSize = (int) (getMeasuredWidth() / mViewFrameWidth) + 2;
        VideoFrameNode node;
        synchronized (mCurrentNode) {
            node = mCurrentNode.prev();
            if (node == null) {
                node = new VideoFrameNode();
                node.setFrameTime((long) (mCurrentNode.getFrameTime() - mFrameTime * mCurrentSpeed.getSpeed()));
                if (node.getFrameTime() <= 0) {
                    node.setFrameTime(0);
                }
                node.setNext(mCurrentNode);
                mCurrentNode.setPrev(node);

                if (mFirstNode == null) {
                    mFirstNode = node;
                } else {
                    synchronized (mFirstNode) {
                        if (mFirstNode.getFrameTime() > node.getFrameTime()) {
                            node.setNext(mFirstNode);//关联链表，替换链表头
                            mFirstNode.setPrev(node);
                            mFirstNode = node;
                        }
                    }
                }
                synchronized (mLoadingFrames) {
                    mLoadingFrames.add(node);
                }
            }
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        for (int i = -1; i < frameSize; i++) {
            if (node == null) {
                break;
            }

            // TODO 裁剪帧的左右位置，防止图像拉伸
            if (node.getBitmap() != null && !node.getBitmap().isRecycled()) {
                int left = (int) (mViewFrameWidth * i - mExcursionX);
                int right = (int) (mViewFrameWidth * (i + 1) - mExcursionX);
                Rect srcRect = null;
                if (node.getFrameTime() + mFrameTime * mCurrentSpeed.getSpeed() > mVideoDuration) {
                    right = (int) (left + (mViewFrameWidth * (mVideoDuration - node.getFrameTime()) / (mFrameTime * mCurrentSpeed.getSpeed())));
                    srcRect = new Rect(0,0, (int) (mViewFrameWidth * (mVideoDuration - node.getFrameTime())/(mFrameTime * mCurrentSpeed.getSpeed())), getMeasuredHeight());
                }
                Rect rect = new Rect(left,0, right, getMeasuredHeight());
                canvas.drawBitmap(node.getBitmap(), srcRect, rect, paint);
            }
            if (node.getFrameTime() > mVideoDuration) {
                break;
            }
            if (node.next() == null) {
                VideoFrameNode frameNode = new VideoFrameNode();
                frameNode.setFrameTime((long) (node.getFrameTime() + (mFrameTime * mCurrentSpeed.getSpeed())));
                node.setNext(frameNode);
                frameNode.setPrev(node);
                if (frameNode.getFrameTime() <= mVideoDuration) {
                    synchronized (mCurrentNode) {
                        if (mLastNode == null) {
                            mLastNode = node;
                        } else {
                            synchronized (mLastNode) {
                                if (mLastNode.getFrameTime() < node.getFrameTime()) {
                                    node.setPrev(mLastNode);
                                    mLastNode.setNext(node);
                                    mLastNode = node;
                                }
                            }
                        }
                    }
                    synchronized (mLoadingFrames) {
                        mLoadingFrames.add(frameNode);
                    }
                }
                continue;
            }

            node = node.next();
        }

        // 顶层拖拽控件
        paint.setColor(getResources().getColor(R.color.video_cut_bar_foreground));
        int start = (int) (mCropStart * (getMeasuredWidth()/(float) mMaxTime));
        int end = (int) (mCropEnd * (getMeasuredWidth()/(float) mMaxTime));

        int touchSize = TOUCH_EDGE;

        Rect leftRect = new Rect(start,0,start + touchSize, getMeasuredHeight());
        canvas.drawRect(leftRect, paint);

        Rect topRect = new Rect(start,0, end,10);
        canvas.drawRect(topRect, paint);

        Rect rightRect = new Rect(end - touchSize,0, end, getMeasuredHeight());
        canvas.drawRect(rightRect, paint);

        Rect bottomRect = new Rect(start,getMeasuredHeight() - 10, end, getMeasuredHeight());
        canvas.drawRect(bottomRect, paint);

        // 三横线
        paint.setColor(0xFF000000);
        for (int i = -1; i < 3; i++) {
            canvas.drawLine(start + touchSize/5, (getMeasuredHeight()/2 + i*10),
                    start + touchSize - touchSize/5, (getMeasuredHeight()/2 + i*10), paint);
        }

        for (int i = -1; i < 3; i++) {
            canvas.drawLine(end - touchSize/5, (getMeasuredHeight()/2 + i*10),
                    end - touchSize + touchSize/5, (getMeasuredHeight()/2 + i*10), paint);
        }

    }

    /**
     * 加载图片
     * @param frameNumber 毫秒/ms
     * @return
     */
    public Bitmap getVideoFrame(long frameNumber) {
        if (loadFail) {
            return Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
        }
        return mMetadataRetriever.getScaledFrameAtTime(frameNumber * 1000L, (int)mViewFrameWidth, (int) (mViewFrameWidth / mVideoSar));
    }

    /**
     * 加载线程
     */
    class LoadThread extends Thread {

        @Override
        public void run() {
            while (!loadFinish) {
                if (loadFail) {
                    return;
                }
                synchronized (mLoadingFrames) {
                    if (mLoadingFrames.isEmpty()) {
                        continue;
                    }
                }
                VideoFrameNode node;
                synchronized (mLoadingFrames) {
                    if (!mLoadingFrames.isEmpty()) {
                        node = mLoadingFrames.remove(0);
                    } else {
                        node = null;
                    }
                }
                if (node != null) {
                    node.setBitmap(getVideoFrame(node.getFrameTime()));
                    postInvalidate();
                }
            }
        }
    }


    /**
     * 释放资源的线程
     */
    private class ReleaseThread extends Thread {

        @Override
        public void run() {
            if (mReleaseNode == null) {
                return ;
            }
            synchronized (mReleaseNode) {
                if (mReleaseNode == null) {
                    return ;
                }
                VideoFrameNode prevNode = mReleaseNode.prev();
                while (prevNode != null) {
                    if(prevNode.getBitmap() != null && !prevNode.getBitmap().isRecycled()){
                        prevNode.getBitmap().recycle();
                        prevNode.setBitmap(null);
                    }
                    VideoFrameNode node = prevNode;
                    prevNode = prevNode.prev();
                    node.setPrev(null);
                    if(prevNode != null){
                        prevNode.setNext(null);
                    }
                }

                VideoFrameNode nextNode = mReleaseNode.next();
                while (nextNode != null) {
                    if (nextNode.getBitmap() != null && !nextNode.getBitmap().isRecycled()) {
                        nextNode.getBitmap().recycle();
                        nextNode.setBitmap(null);
                    }
                    VideoFrameNode node = nextNode;
                    nextNode = nextNode.next();
                    node.setNext(null);
                    if (nextNode != null) {
                        nextNode.setPrev(null);
                    }
                }
                mReleaseNode = null;
            }
            loadFinish = true;
            System.gc();
        }
    }

    /**
     * 触摸监听回调
     */
    public interface OnVideoCropViewBarListener {

        void onTouchDown();

        void onTouchUp();

        void onTouchChange(long time);

        void onRangeChange(long time, long range);

        void onError(String error);
    }

    public void setOnVideoCropViewBarListener(OnVideoCropViewBarListener listener) {
        mOnOnVideoCropViewBarListener = listener;
    }

    private OnVideoCropViewBarListener mOnOnVideoCropViewBarListener;

}
