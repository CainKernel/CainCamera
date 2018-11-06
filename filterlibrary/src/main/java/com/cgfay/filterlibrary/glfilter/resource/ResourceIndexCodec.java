package com.cgfay.filterlibrary.glfilter.resource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * 索引读取器
 */
public class ResourceIndexCodec extends ResourceCodec {

    // 索引列表
    private int[] mIndexArrays;
    // 文件大小列表
    private int[] mSizeArrays;

    public ResourceIndexCodec(String indexPath, String dataPath) {
        super(indexPath, dataPath);
    }

    @Override
    public void init() throws IOException {
        super.init();
        // 获取索引大小
        int length = 0;
        for (Iterator iterator = mIndexMap.keySet().iterator(); iterator.hasNext();) {
            length = Math.max(getIndex((String) iterator.next()), length);
        }
        mIndexArrays = new int[length + 1];
        mSizeArrays = new int[length + 1];
        for (int i = 0; i < mIndexArrays.length; i++) {
            mIndexArrays[i] = -1;
            mSizeArrays[i] = -1;
        }

        Map.Entry entry;
        for (Iterator iterator = mIndexMap.entrySet().iterator(); iterator.hasNext();) {
            entry = (Map.Entry)iterator.next();
            int index = getIndex((String) entry.getKey());
            if ((index >= 0) && (index < mIndexArrays.length)) {
                mIndexArrays[index] = (Integer)((Pair)entry.getValue()).first;
                mSizeArrays[index] = (Integer)((Pair)entry.getValue()).second;
            }
        }
    }

    /**
     * 提取索引值
     * @param fileName
     * @return
     */
    private int getIndex(String fileName) {
        // 文件名类似：xxx_001.png，后length -7 ~ length - 4即为索引
        String indexStr = fileName.substring(fileName.length() - 7, fileName.length() - 4);
        int index = 0;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "getIndex: ", e);
        }
        return index;
    }

    /**
     * 根据索引加载资源
     * @param index
     * @return
     */
    public Bitmap loadResource(int index) {
        if ((index < 0) || (index >= mIndexArrays.length)) {
            return null;
        }
        int pos = mIndexArrays[index];
        int size = mSizeArrays[index];
        if ((pos == -1) || (size == -1)) {
            return null;
        }
        return BitmapFactory.decodeByteArray(mDataBuffer.array(), mDataBuffer.arrayOffset() + pos, size);
    }

}
