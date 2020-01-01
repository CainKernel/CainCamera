package com.cgfay.picker.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cgfay.picker.MediaPickerParam;
import com.cgfay.picker.presenter.IMediaDataPresenter;
import com.cgfay.picker.presenter.MediaDataPresenter;
import com.cgfay.picker.scanner.IMediaDataReceiver;
import com.cgfay.scan.R;
import com.cgfay.picker.adapter.MediaItemDecoration;
import com.cgfay.picker.adapter.MediaDataAdapter;
import com.cgfay.picker.model.AlbumData;
import com.cgfay.picker.model.MediaData;
import com.cgfay.picker.scanner.MediaDataScanner;
import com.cgfay.uitls.utils.PermissionUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.Disposable;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * 媒体列表页
 */
public abstract class MediaDataFragment extends Fragment implements IMediaDataReceiver,
        MediaDataAdapter.OnMediaDataChangeListener {

    protected static final String TAG = "MediaDataFragment";

    public static final int TypeImage = 1;
    public static final int TypeVideo = 2;
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = {TypeImage, TypeVideo})
    @Retention(RetentionPolicy.SOURCE)
    @interface MimeType {}


    protected Context mContext;
    protected Handler mMainHandler;

    private View mLayoutBlank;

    protected RecyclerView mMediaDataListView;
    protected MediaDataAdapter mMediaDataAdapter;

    protected volatile boolean mLoadingMore;

    protected MediaDataScanner mDataScanner;

    protected Disposable mUpdateDisposable;

    protected IMediaDataPresenter mPresenter;

    protected OnSelectedChangeListener mSelectedChangeListener;

    protected boolean mMultiSelect;

    protected MediaPickerParam mMediaPickerParam;

    public MediaDataFragment() {
        mMainHandler = new Handler(Looper.getMainLooper());
        mPresenter = new MediaDataPresenter();
        mMediaPickerParam = new MediaPickerParam();
        mMultiSelect = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mDataScanner != null) {
            mDataScanner.setUserVisible(isVisibleToUser);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentLayout(), container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (PermissionUtils.permissionChecking(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            initDataProvider();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDataScanner != null) {
            mDataScanner.resume();
        }
        Log.d(TAG, "onResume: " + getMimeType(getMediaType()));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDataScanner != null) {
            mDataScanner.pause();
        }
        Log.d(TAG, "onPause: " + getMimeType(getMediaType()));
    }

    @Override
    public void onDestroy() {
        if (mDataScanner != null) {
            mDataScanner.destroy();
            mDataScanner = null;
        }
        if (mUpdateDisposable != null) {
            mUpdateDisposable.dispose();
            mUpdateDisposable = null;
        }
        super.onDestroy();
    }

    /**
     * 初始化控件
     * @param rootView
     */
    protected void initView(@NonNull View rootView) {
        initBlankView(rootView);
        mMediaDataListView = rootView.findViewById(R.id.rv_media_thumb_list);
        mMediaDataListView.addItemDecoration(new MediaItemDecoration(mMediaPickerParam.getSpanCount(),
                mMediaPickerParam.getSpaceSize(), mMediaPickerParam.isHasEdge()));
        mMediaDataListView.setLayoutManager(new GridLayoutManager(rootView.getContext(),
                mMediaPickerParam.getSpanCount()));
        if (mMediaDataListView.getItemAnimator() != null) {
            ((SimpleItemAnimator) mMediaDataListView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        mMediaDataAdapter = new MediaDataAdapter();
        mMediaDataAdapter.addOnMediaDataChangeListener(this);
        mMediaDataListView.setAdapter(mMediaDataAdapter);
        setItemImageSize();

        // 滚动监听到了底部之后，添加从媒体数据提供者中取出缓存的数据
        mMediaDataListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mDataScanner.isPageScan() && dy > 0 && !mLoadingMore
                        && isSlideToBottomLine(mMediaDataListView, MediaDataScanner.PAGE_SIZE / 2)) {
                    mLoadingMore = true;
                    mMediaDataListView.post(() -> {
                        if (mDataScanner == null) {
                            return;
                        }
                        List<MediaData> mediaData = mDataScanner.getCacheMediaData();
                        if (mediaData.size() > 0) {
                            mMediaDataAdapter.appendNewMediaData(mediaData);
                        }
                        mLoadingMore = false;
                    });
                }
            }
        });
    }

    /**
     * 初始化空白控件
     * @param rootView
     */
    private void initBlankView(@NonNull View rootView) {
        mLayoutBlank = rootView.findViewById(R.id.layout_blank);
        TextView blankView = rootView.findViewById(R.id.tv_blank_view);
        String emptyTips = rootView.getResources().getString(R.string.media_empty);
        blankView.setText(String.format(emptyTips, getTitle()));
    }

    /**
     * 判断是否快滚动到底部
     * @param recyclerView
     * @param offsetLine
     * @return
     */
    protected boolean isSlideToBottomLine(@NonNull RecyclerView recyclerView, int offsetLine) {
        final RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            int itemCount = manager.getItemCount();
            int lastPosition = ((LinearLayoutManager) manager).findLastCompletelyVisibleItemPosition();
            if (lastPosition != itemCount && lastPosition >= itemCount - offsetLine) {
                return true;
            }
        }

        return false;
    }

    /**
     * 指定加载图片的大小
     */
    private void setItemImageSize() {
        int divSize = (int)(getResources().getDimension(R.dimen.dp4) * (mMediaPickerParam.getSpanCount() + 1));
        int imageSize = getResources().getDisplayMetrics().widthPixels - divSize;
        int resize = imageSize / mMediaPickerParam.getSpanCount();
        mMediaDataAdapter.setThumbnailResize(resize);
    }

    public void setMediaPickerParam(MediaPickerParam pickerParam) {
        mMediaPickerParam = pickerParam;
    }

    /**
     * 获取媒体数据
     * @return
     */
    public List<MediaData> getMediaDataList() {
        return mMediaDataAdapter.getMediaDataList();
    }

    public void reset() {
        mPresenter.clear();
    }

    /**
     * 获取选中媒体数据
     * @return
     */
    public List<MediaData> getSelectedMediaDataList() {
        return mPresenter.getSelectedMediaDataList();
    }

    /**
     * 是否多选
     * @return
     */
    public boolean isMultiSelect() {
        return mMultiSelect;
    }

    /**
     * 初始化数据提供者
     */
    protected abstract void initDataProvider();

    /**
     * 获取布局
     * @return
     */
    protected abstract @LayoutRes int getContentLayout();

    /**
     * 获取媒体类型
     * @return
     */
    protected abstract @MimeType int getMediaType();

    /**
     * 获取页面标题
     * @return
     */
    public abstract String getTitle();

    /**
     * 获取mimeType类型字符串
     * @param mimeType
     * @return
     */
    protected String getMimeType(@MimeType int mimeType) {
        if (mimeType == TypeImage) {
            return "Image";
        } else if (mimeType == TypeVideo) {
            return "Video";
        } else {
            return "unknown";
        }
    }

    /**
     * 加载某个相册的媒体数据
     * @param album
     */
    public void loadAlbumMedia(@NonNull AlbumData album) {
        if (mDataScanner != null) {
            mDataScanner.loadAlbumMedia(album);
        }
    }

    /**
     * 刷新数据提供者
     */
    public void refreshDataProvider() {
        if (mDataScanner == null) {
            mMainHandler.post(()-> {
                initDataProvider();
                if (mDataScanner != null) {
                    mDataScanner.resume();
                }
            });
        }
    }

    /**
     * 刷新媒体数据
     */
    public void refreshMediaData() {
        if (mMediaDataAdapter != null) {
            if (mMediaDataAdapter.getItemCount() == 0) {
                mMediaDataAdapter.notifyDataSetChanged();
            } else {
                refreshVisibleItemChange();
            }
        }
    }

    /**
     * 刷新可见的列表
     */
    private void refreshVisibleItemChange() {
        if (mMediaDataListView != null && mMediaDataListView.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager manager = (GridLayoutManager) mMediaDataListView.getLayoutManager();
            int start = manager.findFirstVisibleItemPosition();
            int end = manager.findLastVisibleItemPosition();
            mMediaDataAdapter.notifyItemRangeChanged(start, (end - start + 1));
        }
    }

    /**
     * 媒体数据加载完成
     * @param mediaDataList
     */
    @Override
    public void onMediaDataObserve(@NonNull List<MediaData> mediaDataList) {
        if (mMediaDataAdapter != null) {
            mMediaDataAdapter.setMediaData(mediaDataList);
            mMediaDataListView.post(() -> {
                mMediaDataAdapter.notifyDataSetChanged();
            });
        }
        checkBlankView();
    }

    @Override
    public int getSelectedIndex(@NonNull MediaData mediaData) {
        return mPresenter.getSelectedIndex(mediaData);
    }

    @Override
    public void onMediaPreview(@NonNull MediaData mediaData) {
        if (mSelectedChangeListener != null) {
            mSelectedChangeListener.onMediaDataPreview(mediaData);
        }
    }

    @Override
    public void onMediaSelectedChange(@NonNull MediaData mediaData) {
        if (mPresenter.getSelectedIndex(mediaData) >= 0) {
            mPresenter.removeSelectedMedia(mediaData);
        } else {
            mPresenter.addSelectedMedia(mediaData);
        }
        checkSelectedButton();
        refreshMediaData();
    }

    public void checkSelectedButton() {
        int selectSize = mPresenter.getSelectedMediaDataList().size();
        if (mSelectedChangeListener != null) {
            String text = "";
            if (selectSize > 0) {
                text = String.format(Locale.getDefault(), "確定(%d)", selectSize);
            }
            if (getMediaType() == TypeImage && selectSize > 1) {
                text = String.format(Locale.getDefault(), "照片电影(%d)", selectSize);
            }
            mSelectedChangeListener.onSelectedChange(text);
        }
    }

    /**
     * 检查是否显示空白页
     */
    private void checkBlankView() {
        if (getMediaDataList().size() > 0) {
            mMediaDataListView.setVisibility(View.VISIBLE);
            mLayoutBlank.setVisibility(View.GONE);
        } else {
            mMediaDataListView.setVisibility(View.GONE);
            mLayoutBlank.setVisibility(View.VISIBLE);
        }
    }

    public interface OnSelectedChangeListener {

        void onMediaDataPreview(MediaData mediaData);

        void onSelectedChange(String text);
    }

    public void addOnSelectedChangeListener(OnSelectedChangeListener listener) {
        mSelectedChangeListener = listener;
    }
}
