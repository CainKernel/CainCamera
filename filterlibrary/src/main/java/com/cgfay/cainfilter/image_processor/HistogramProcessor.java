package com.cgfay.cainfilter.image_processor;

import android.graphics.Bitmap;

/**
 * 直方图
 * Created by cain on 2017/8/12.
 */

public class HistogramProcessor {

    private Bitmap mBitmap;
    private int[] mHistogram;
    private BitType mBitType = BitType.BIT_8; // 默认位数
    private int mSize = 256; // 直方图维度
    private boolean isCancel = false;
    private ProcessCallBack mCallback;

    public HistogramProcessor() {

    }

    /**
     * 添加处理回调
     * @param callBack
     */
    public void addProcessCallBacks(ProcessCallBack callBack) {
        mCallback = callBack;
    }

    /**
     * 设置图片
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        mBitmap = bitmap;
        // 计算位数
        Bitmap.Config config = mBitmap.getConfig();
        switch (config) {
            case ALPHA_8:
                mBitType = BitType.BIT_8;
                break;

            case RGB_565:
            case ARGB_4444:
                mBitType = BitType.BIT_16;
                break;

            case ARGB_8888:
                mBitType = BitType.BIT_32;
                break;
        }

    }

    /**
     * 直方图的亮度值数目
     * @param size
     */
    public void setHistogramSize(int size) {
        mSize = size;
    }

    /**
     * 处理程序
     */
    private void processor() {
        // 处理开始
        if (mCallback != null) {
            mCallback.onProcessStart();
        }
        // 处理过程
        if (mBitmap != null && !mBitmap.isRecycled()) {
            switch (mBitType) {
                case BIT_8:
                    mHistogram = processBit8();
                    break;

                case BIT_16:
                case BIT_32:
                    procesMoreThanBit8(32);
                    break;
            }
        }

        // 处理完成
        if (mCallback != null) {
            mCallback.onProcessFinish();
        }
    }

    /**
     * 处理8位图像
     * @return
     */
    private int[] processBit8() {
        int[] histogram = new int[mSize];
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 是否取消处理
                if (isCancel) {
                    if (mCallback != null) {
                        mCallback.onProcessCancel();
                    }
                    return null;
                }
                int pixel = mBitmap.getPixel(j, i);
                histogram[pixel] = histogram[pixel] +1;
            }
        }
        return histogram;
    }

    /**
     * 处理大于8位的图像，不可能利用直方图的每一个强度值计算直方图条目，这个太多了，我们采取装箱的方式来实现。
     * @param histogramCount 直方图的尺寸，就是有多少条直方图柱子
     * @return
     */
    private int[] procesMoreThanBit8(int histogramCount) {
        int[] histogram = new int[histogramCount];
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // 是否取消处理
                if (isCancel) {
                    if (mCallback != null) {
                        mCallback.onProcessCancel();
                    }
                    return null;
                }
                int pixel = mBitmap.getPixel(j, i);
                int index = pixel * histogramCount / mSize;
                histogram[index] = histogram[index] + 1;
            }
        }
        return histogram;
    }

    /**
     * 获取直方图
     * @return
     */
    public int[] getHistogram() {
        return mHistogram;
    }

    /**
     * 处理直方图均匀化
     * @return 返回均匀化后的图片
     */
    public Bitmap processHomogenize() {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return null;
        }

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        int area = width * height;
        Bitmap bitmap = Bitmap.createBitmap(width, height, mBitmap.getConfig());
        // 如果直方图不存在，首先计算直方图
        if (mHistogram == null) {
            processor();
        }
        // 计算累积直方图
        int[] histogram = mHistogram;
        for (int i = 1; i < histogram.length; i++) {
            histogram[i] = histogram[i - 1] + histogram[i];
        }
        // 均匀化
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = bitmap.getPixel(j, i);
                int result = histogram[pixel] * (mSize - 1) / area;
                bitmap.setPixel(j, i, result);
            }
        }
        return bitmap;
    }

    /**
     * 直方图匹配
     * @param hA
     * @param hB
     * @return
     */
    public int[] matchHistograms(int[] hA, int[] hB) {
        int K = hA.length;
        double[] PA = cdf(hA);
        double[] PR = cdf(hB);
        int[] F = new int[K];

        // 计算映射函数
        for (int i = 0; i < K; i++) {
            int j = K - 1;
            do {
                F[i] = j;
                j--;
            } while (j >= 0 &&PA[i] <= PR[j]);
        }

        return F;
    }

    /**
     * 累积分布函数
     * @param h
     * @return
     */
    private double[] cdf(int[] h) {
        int K = h.length;
        int n = 0;
        for (int i = 0; i < K; i++) {
            n += h[i];
        }

        double[] P = new double[K];
        int c = h[0];
        P[0] = (double) c/n;
        for (int i = 0; i < K; i++) {
            c += h[i];
            P[i] = (double)c/n;
        }
        return P;
    }
}
