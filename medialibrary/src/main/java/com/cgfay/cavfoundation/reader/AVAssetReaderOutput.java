package com.cgfay.cavfoundation.reader;

import com.cgfay.cavfoundation.AVMediaType;

/**
 * 输出接口
 */
public interface AVAssetReaderOutput {

    /**
     * 媒体类型
     */
    AVMediaType getMediaType();

    /**
     * 是否复制数据
     */
    boolean isAlwaysCopiesSampleData();

    /**
     * 设置是否复制解码数据
     */
    void setAlwaysCopiesSampleData(boolean alwaysCopiesSampleData);

    /**
     * 设置是否支持随机访问
     */
    void setSupportsRandomAccess(boolean supportsRandomAccess);

    /**
     * 判断是否支持随机访问
     */
    boolean isSupportsRandomAccess();

//    /**
//     * 复制下一帧数据
//     */
//    public ByteBuffer copyNextSampleBuffer();
}
