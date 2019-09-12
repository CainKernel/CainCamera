package com.cgfay.filter.glfilter.stickers;

import android.content.Context;

import com.cgfay.filter.glfilter.base.GLImageGroupFilter;
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerFrameData;
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerNormalData;
import com.cgfay.filter.glfilter.stickers.bean.StaticStickerNormalData;

/**
 * 动态贴纸滤镜
 */
public class GLImageDynamicStickerFilter extends GLImageGroupFilter {

    public GLImageDynamicStickerFilter(Context context, DynamicSticker sticker) {
        super(context);
        if (sticker == null || sticker.dataList == null) {
            return;
        }
        // 如果存在普通贴纸数据，则添加普通贴纸滤镜
        for (int i = 0; i < sticker.dataList.size(); i++) {
            if (sticker.dataList.get(i) instanceof DynamicStickerNormalData) {
                mFilters.add(new DynamicStickerNormalFilter(context, sticker));
                break;
            }
        }
        // 判断是否存在前景贴纸滤镜
        for (int i = 0; i < sticker.dataList.size(); i++) {
            if (sticker.dataList.get(i) instanceof DynamicStickerFrameData) {
                mFilters.add(new DynamicStickerFrameFilter(context, sticker));
                break;
            }
        }

        // 判断添加贴纸
        for (int i = 0; i < sticker.dataList.size(); i++) {
            if (sticker.dataList.get(i) instanceof StaticStickerNormalData) {
                mFilters.add(new StaticStickerNormalFilter(context, sticker));
                break;
            }
        }
    }

}
