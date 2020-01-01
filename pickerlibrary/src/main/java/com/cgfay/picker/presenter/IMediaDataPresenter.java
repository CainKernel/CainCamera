package com.cgfay.picker.presenter;

import androidx.annotation.NonNull;

import com.cgfay.picker.model.MediaData;

import java.util.List;

public interface IMediaDataPresenter {

    /** 获取选中索引 */
    int getSelectedIndex(@NonNull MediaData mediaData);

    /** 添加媒体数据 */
    void addSelectedMedia(@NonNull MediaData mediaData);

    /** 移除选中媒体数据 */
    void removeSelectedMedia(@NonNull MediaData mediaData);

    /** 清空所有数据 */
    void clear();

    /** 获取选中的媒体数据 */
    List<MediaData> getSelectedMediaDataList();

}
