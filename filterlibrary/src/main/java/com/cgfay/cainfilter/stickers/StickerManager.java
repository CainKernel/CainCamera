package com.cgfay.cainfilter.stickers;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 贴纸管理器
 * Created by cain on 2017/11/10.
 */

public class StickerManager {

    private static StickerManager mInstance;

    private HandlerThread mParserThread;
    private Handler mParserHandler;

    public static StickerManager getInstance() {
        if (mInstance == null) {
            mInstance = new StickerManager();
        }
        return mInstance;
    }

    private StickerManager() {}

    /**
     * 开始贴纸解析线程
     */
    public void startStickerParserThread() {
        mParserThread = new HandlerThread("ParserThread");
        mParserThread.start();
        mParserHandler = new Handler(mParserThread.getLooper());
    }


    /**
     * 释放贴纸解析线程
     */
    public void releaseStickerParserThead() {
        if (mParserThread == null) {
            if (mParserThread != null) {
                mParserThread.quitSafely();
                try {
                    mParserThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mParserThread = null;
            }
            return;
        }
        mParserHandler.removeCallbacksAndMessages(null);
        mParserThread.quitSafely();
        try {
            mParserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mParserThread = null;
        mParserThread = null;
    }

    /**
     * 解析贴纸
     * @param stickerId 贴纸对应的Id
     */
    public void parserSticker(final int stickerId) {
        if (mParserHandler != null) {
            mParserHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalParserSticker(stickerId);
                }
            });
        }
    }

    /**
     * 解析贴纸
     * @param stickerId
     */
    private void internalParserSticker(int stickerId) {

    }

}
