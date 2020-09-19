package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * 编码监听器
 */
public interface OnEncodeListener {

    /**
     * 添加轨道
     * @param mediaFormat 格式参数
     * @return  轨道索引
     */
    int onAddTrack(@NonNull CAVMediaEncoder encoder, @NonNull MediaFormat mediaFormat);

    /**
     * 写入额外参数
     * @param trackIndex    轨道索引
     * @param extraData     额外数据
     * @param bufferInfo    缓冲区信息
     */
    void onWriteExtraData(int trackIndex, @NonNull ByteBuffer extraData,
                          @NonNull MediaCodec.BufferInfo bufferInfo);

    /**
     * 写入编码后的数据
     * @param trackIndex    轨道索引
     * @param encodeData    编码数据
     * @param bufferInfo    缓冲区信息
     */
    void onWriteFrame(int trackIndex, @NonNull ByteBuffer encodeData,
                      @NonNull MediaCodec.BufferInfo bufferInfo);
}
