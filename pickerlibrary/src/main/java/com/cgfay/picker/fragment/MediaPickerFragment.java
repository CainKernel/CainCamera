package com.cgfay.picker.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.picker.utils.MediaMetadataUtils;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.picker.MediaPicker;
import com.cgfay.picker.MediaPickerParam;
import com.cgfay.picker.model.AlbumData;
import com.cgfay.picker.model.MediaData;
import com.cgfay.picker.selector.OnMediaSelector;
import com.cgfay.scan.R;
import com.cgfay.picker.adapter.AlbumItemDecoration;
import com.cgfay.picker.adapter.AlbumDataAdapter;
import com.cgfay.picker.adapter.MediaDataPagerAdapter;
import com.cgfay.picker.scanner.AlbumDataScanner;
import com.cgfay.uitls.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体选择页面
 */
public class MediaPickerFragment extends AppCompatDialogFragment implements MediaDataFragment.OnSelectedChangeListener {

    public static final String TAG = "MediaPickerFragment";

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    private static final int MIN_DURATION = 50;
    private static final int ANIMATION_DURATION = 400;

    private Handler mMainHandler;
    private FragmentActivity mActivity;
    private MediaPickerParam mPickerParam;

    private View mContentView;

    private TextView mTvAlbumView;
    private ImageView mIvAlbumIndicator;
    private TextView mBtnSelect;
    private LinearLayout mLayoutAlbumList;
    private RecyclerView mAlbumListView;
    private AlbumDataAdapter mAlbumDataAdapter;
    private boolean mShowingAlbumList;
    private volatile boolean mAlbumAnimating;
    private AlbumDataScanner mAlbumDataScanner;

    private TabLayout mTabLayout;

    private MediaDataFragment mImageDataFragment;
    private MediaDataFragment mVideoDataFragment;
    private List<MediaDataFragment> mMediaDataFragments = new ArrayList<>();
    private ViewPager mMediaListViewPager;
    private MediaDataPagerAdapter mMediaDataPagerAdapter;

    private OnMediaSelector mMediaSelector;

    public MediaPickerFragment() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActivity) {
            mActivity = (FragmentActivity) context;
        } else {
            mActivity = getActivity();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.PickerDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_picker, container, false);
        initData();
        initView(view);
        mContentView = view;
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            if (PermissionUtils.permissionChecking(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                initAlbumController(mActivity);
            } else {
                PermissionUtils.requestStoragePermission(this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAlbumDataScanner != null) {
            mAlbumDataScanner.resume();
        }
        initDialogKeyBackListener();
        mMainHandler.postDelayed(this::setDialogNoAnimation, ANIMATION_DURATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAlbumDataScanner != null) {
            mAlbumDataScanner.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mAlbumDataScanner != null) {
            mAlbumDataScanner.destroy();
            mAlbumDataScanner = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initAlbumController(mActivity);
        }
    }

    private void setDialogAnimation() {
        mMainHandler.post(()-> {
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setWindowAnimations(R.style.PickerDialogAnimation);
            }
        });
    }

    private void setDialogNoAnimation() {
        mMainHandler.post(() -> {
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setWindowAnimations(R.style.PickerDialogNoAnimation);
            }
        });
    }

    /**
     * 初始化对话框返回监听
     */
    private void initDialogKeyBackListener() {
        if (getDialog() != null) {
            getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                    if (fragment != null) {
                        getChildFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commitNowAllowingStateLoss();
                        return true;
                    }
                    animateCloseFragment();
                    return true;
                }
                return false;
            });
        }
    }

    private void animateCloseFragment() {
        setDialogAnimation();
        mMainHandler.postDelayed(this::closeFragment, MIN_DURATION);
    }

    /**
     * 关闭当前页面
     */
    private void closeFragment() {
        if (getParentFragment() != null) {
            getParentFragment()
                    .getChildFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
        } else if (mActivity != null) {
            mActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commitAllowingStateLoss();
        }
    }

    private void initData() {
        if (mActivity != null && getArguments() != null) {
            mPickerParam = (MediaPickerParam) getArguments().getSerializable(MediaPicker.PICKER_PARAMS);
        }
        if (mPickerParam == null) {
            mPickerParam = new MediaPickerParam();
        }
    }

    /**
     * 初始化列表
     * @param rootView
     */
    private void initView(@NonNull View rootView) {
        initAlbumSelectView(rootView);
        initMediaListView(rootView);
        initTabView(rootView);
    }

    /**
     * 初始化相册控制器
     * @param context
     */
    private void initAlbumController(@NonNull Context context) {
        if (mAlbumDataScanner == null) {
            mAlbumDataScanner = new AlbumDataScanner(context, LoaderManager.getInstance(this), mPickerParam);
            mAlbumDataScanner.setAlbumDataReceiver(new AlbumDataScanner.AlbumDataReceiver() {
                @Override
                public void onAlbumDataObserve(List<AlbumData> albumDataList) {
                    if (mAlbumDataAdapter != null) {
                        mAlbumDataAdapter.setAlbumDataList(albumDataList);
                    }
                }

                @Override
                public void onAlbumDataReset() {
                    if (mAlbumDataAdapter != null) {
                        mAlbumDataAdapter.reset();
                    }
                }
            });
        }
    }

    /**
     * 初始化相册选择控件
     * @param rootView
     */
    private void initAlbumSelectView(@NonNull View rootView) {

        rootView.findViewById(R.id.iv_album_back).setOnClickListener(v -> {
            animateCloseFragment();
        });

        mBtnSelect = rootView.findViewById(R.id.btn_select);
        mBtnSelect.setOnClickListener(v -> {
            if (mMediaSelector != null) {
                int index = mMediaListViewPager.getCurrentItem();
                mMediaSelector.onMediaSelect(mActivity, mMediaDataFragments.get(index).getSelectedMediaDataList());
                if (mPickerParam.isAutoDismiss()) {
                    animateCloseFragment();
                }
            }
        });

        mShowingAlbumList = false;
        mTvAlbumView = rootView.findViewById(R.id.tv_album_selected);
        mTvAlbumView.setText(getText(R.string.album_all));
        mIvAlbumIndicator = rootView.findViewById(R.id.iv_album_indicator);
        mLayoutAlbumList = rootView.findViewById(R.id.ll_album_list);
        mAlbumListView = rootView.findViewById(R.id.rv_album_list);
        mAlbumListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAlbumListView.addItemDecoration(new AlbumItemDecoration());

        mAlbumDataAdapter = new AlbumDataAdapter();
        mAlbumListView.setAdapter(mAlbumDataAdapter);
        mAlbumDataAdapter.addOnAlbumSelectedListener((album) -> {
            if (!mAlbumAnimating) {
                mTvAlbumView.setText(album.getDisplayName());
                showAlbumListView(false);
                mAlbumListView.post(() -> {
                    for (MediaDataFragment fragment : mMediaDataFragments) {
                        fragment.loadAlbumMedia(album);
                    }
                });
            }
        });

        rootView.findViewById(R.id.layout_album_select).setOnClickListener(v -> {
            mAlbumListView.post(() -> {
                mShowingAlbumList = !mShowingAlbumList;
                showAlbumListView(mShowingAlbumList);
            });
        });
    }

    /**
     * 是否显示相册列表页
     * @param show
     */
    private void showAlbumListView(boolean show) {
        mAlbumAnimating = true;
        if (show) {
            mLayoutAlbumList.setVisibility(View.VISIBLE);
            mAlbumListView.setVisibility(View.VISIBLE);
        }
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, show ? -1.0f : 0.0f, Animation.RELATIVE_TO_PARENT,
                show ? 0.0f : -1.0f);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnimation.setDuration(ANIMATION_DURATION);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAlbumListView.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mShowingAlbumList = show;
                mLayoutAlbumList.setVisibility(show ? View.VISIBLE : View.GONE);
                mAlbumListView.setVisibility(show ? View.VISIBLE : View.GONE);
                if (mAlbumListView.getAdapter() != null) {
                    mAlbumListView.getAdapter().notifyDataSetChanged();
                }
                mAlbumAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAlbumListView.startAnimation(translateAnimation);

        mIvAlbumIndicator.setPivotX(mIvAlbumIndicator.getWidth() / 2f);
        mIvAlbumIndicator.setPivotY(mIvAlbumIndicator.getHeight() / 2f);
        RotateAnimation rotateAnimation = new RotateAnimation(show ? 0 : 180, show ? 180 : 360,
                mIvAlbumIndicator.getWidth() / 2f, mIvAlbumIndicator.getHeight() / 2f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(ANIMATION_DURATION);
        mIvAlbumIndicator.startAnimation(rotateAnimation);
    }

    /**
     * 初始化媒体列表
     * @param rootView
     */
    private void initMediaListView(@NonNull View rootView) {
        mMediaDataFragments.clear();

        if (!mPickerParam.showImageOnly()) {
            if (mVideoDataFragment == null) {
                mVideoDataFragment = new VideoDataFragment();
            }
            mVideoDataFragment.setMediaPickerParam(mPickerParam);
            mVideoDataFragment.addOnSelectedChangeListener(this);
            mMediaDataFragments.add(mVideoDataFragment);
        }

        if (!mPickerParam.showVideoOnly()) {
            if (mImageDataFragment == null) {
                mImageDataFragment = new ImageDataFragment();
            }
            mImageDataFragment.setMediaPickerParam(mPickerParam);
            mImageDataFragment.addOnSelectedChangeListener(this);
            mMediaDataFragments.add(mImageDataFragment);
        }
        mMediaListViewPager = rootView.findViewById(R.id.vp_media_thumbnail);
        mMediaDataPagerAdapter = new MediaDataPagerAdapter(getChildFragmentManager(), mMediaDataFragments);
        mMediaListViewPager.setAdapter(mMediaDataPagerAdapter);
    }

    /**
     * 初始化媒体类型TabLayout
     * @param rootView
     */
    private void initTabView(@NonNull View rootView) {
        mTabLayout = rootView.findViewById(R.id.tl_media_title);
        mTabLayout.setupWithViewPager(mMediaListViewPager);
        mTabLayout.setVisibility(mMediaDataFragments.size() > 1 ? View.VISIBLE : View.GONE);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                MediaDataFragment fragment = mMediaDataFragments.get(tab.getPosition());
                fragment.checkSelectedButton();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onMediaDataPreview(MediaData mediaData) {
        if (mediaData.isVideo()) {
            if (mVideoDataFragment.isMultiSelect()) {
                parseVideoOrientation(mediaData);
                onPreviewMedia(mediaData);
            } else {
                if (mMediaSelector != null) {
                    List<MediaData> mediaDataList = new ArrayList<>();
                    parseVideoOrientation(mediaData);
                    mediaDataList.add(mediaData);
                    mMediaSelector.onMediaSelect(mActivity, mediaDataList);
                    if (mPickerParam.isAutoDismiss()) {
                        animateCloseFragment();
                    }
                }
            }
        } else {
            onPreviewMedia(mediaData);
        }
    }

    /**
     * 解析视频旋转角度
     */
    private void parseVideoOrientation(@NonNull MediaData mediaData) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(MediaMetadataUtils.getPath(requireContext(), mediaData.getContentUri()));
        String orientation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if ("90".equals(orientation)) {
            mediaData.setOrientation(90);
        } else if ("180".equals(orientation)) {
            mediaData.setOrientation(180);
        } else if ("270".equals(orientation)) {
            mediaData.setOrientation(270);
        } else {
            mediaData.setOrientation(0);
        }
    }

    @Override
    public void onSelectedChange(String text) {
        if (TextUtils.isEmpty(text)) {
            mBtnSelect.setVisibility(View.GONE);
        } else {
            mBtnSelect.setVisibility(View.VISIBLE);
            mBtnSelect.setText(text);
        }
    }

    /**
     * 预览媒体数据
     * @param mediaData
     */
    private void onPreviewMedia(@NonNull MediaData mediaData) {
        MediaPreviewFragment fragment = MediaPreviewFragment.newInstance(mediaData);
        getChildFragmentManager()
                .beginTransaction()
                .add(fragment, FRAGMENT_TAG)
                .commitNowAllowingStateLoss();
    }

    /**
     * 设置媒体选择器
     * @param selector
     */
    public void setOnMediaSelector(OnMediaSelector selector) {
        mMediaSelector = selector;
    }
}
