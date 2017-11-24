package com.cgfay.caincamera.stickers;

/**
 * 贴纸Json解析器
 * Created by cain.huang on 2017/11/24.
 */

public class StickerJsonParser {
    private static StickerJsonParser mInstance;

    public static StickerJsonParser getInstance() {
        if (mInstance == null) {
            mInstance = new StickerJsonParser();
        }
        return mInstance;
    }

    private StickerJsonParser(){}

}
