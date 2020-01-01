package com.cgfay.picker.presenter;

import androidx.annotation.NonNull;

import com.cgfay.picker.model.MediaData;

import java.util.ArrayList;
import java.util.List;

public class MediaDataPresenter implements IMediaDataPresenter {

    private List<MediaData> mSelectedMediaList = new ArrayList<>();

    @Override
    public int getSelectedIndex(@NonNull MediaData mediaData) {
        return mSelectedMediaList.indexOf(mediaData);
    }

    @Override
    public void addSelectedMedia(@NonNull MediaData mediaData) {
        mSelectedMediaList.add(mediaData);
    }

    @Override
    public void removeSelectedMedia(@NonNull MediaData mediaData) {
        mSelectedMediaList.remove(mediaData);
    }

    @Override
    public void clear() {
        mSelectedMediaList.clear();
    }

    @Override
    public List<MediaData> getSelectedMediaDataList() {
        return mSelectedMediaList;
    }
}
