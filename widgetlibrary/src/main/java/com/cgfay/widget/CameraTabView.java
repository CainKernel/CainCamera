package com.cgfay.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pools;
import androidx.core.view.GravityCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cgfay.design.R;
import com.cgfay.resources.MaterialResources;
import com.cgfay.internal.ViewUtils;

import java.lang.CharSequence;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * CameraTabLayout provides a horizontal layout to display tabs.
 *
 * <p>Population of the tabs to display is
 * done through {@link Tab} instances. You create tabs via {@link #newTab()}. From there you can
 * change the tab's label or icon via {@link Tab#setText(int)} and {@link Tab#setIcon(int)}
 * respectively. To display the tab, you need to add it to the layout via one of the
 * {@link #addTab(Tab)} methods. For example:
 * <pre>
 * CameraTabLayout tabLayout = ...;
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
 * </pre>
 * You should set a listener via {@link #setOnTabSelectedListener(OnTabSelectedListener)} to be
 * notified when any tab's selection state has been changed.
 *
 * <p>You can also add items to CameraTabLayout in your layout through the use of {@link TabItem}.
 * An example usage is like so:</p>
 *
 * <pre>
 * &lt;com.cgfay.design.CameraTabLayout
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_width=&quot;match_parent&quot;&gt;
 *
 *     &lt;com.cgfay.design.CameraTabItem
 *             android:text=&quot;@string/tab_text&quot;/&gt;
 *
 *     &lt;com.cgfay.design.CameraTabItem
 *             android:icon=&quot;@drawable/ic_android&quot;/&gt;
 *
 * &lt;/com.cgfay.design.CameraTabLayout&gt;
 * </pre>
 *
 * <h3>ViewPager integration</h3>
 * <p>
 * If you're using a {@link ViewPager} together
 * with this layout, you can call {@link #setupWithViewPager(ViewPager)} to link the two together.
 * This layout will be automatically populated from the {@link PagerAdapter}'s page titles.</p>
 *
 * <p>
 * This view also supports being used as part of a ViewPager's decor, and can be added
 * directly to the ViewPager in a layout resource file like so:</p>
 *
 * <pre>
 * &lt;android.support.v4.view.ViewPager
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;com.cgfay.design.CameraTabLayout
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_gravity=&quot;top&quot; /&gt;
 *
 * &lt;/android.support.v4.view.ViewPager&gt;
 * </pre>
 *
 * @see <a href="http://www.google.com/design/spec/components/tabs.html">Tabs</a>
 *
 * @attr ref R.styleable#TabLayout_tabPadding
 * @attr ref R.styleable#TabLayout_tabPaddingStart
 * @attr ref R.styleable#TabLayout_tabPaddingTop
 * @attr ref R.styleable#TabLayout_tabPaddingEnd
 * @attr ref R.styleable#TabLayout_tabPaddingBottom
 * @attr ref R.styleable#TabLayout_tabContentStart
 * @attr ref R.styleable#TabLayout_tabBackground
 * @attr ref R.styleable#TabLayout_tabMinWidth
 * @attr ref R.styleable#TabLayout_tabMaxWidth
 * @attr ref R.styleable#TabLayout_tabTextAppearance
 */
@ViewPager.DecorView
public class CameraTabView extends HorizontalScrollView {

    private static final Boolean VERBOSE = true;
    private static final String TAG = "CameraTabView";

    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72; // dps
    static final int DEFAULT_GAP_TEXT_ICON = 8; // dps
    private static final int INVALID_WIDTH = -1;
    private static final int DEFAULT_HEIGHT = 48; // dps
    private static final int TAB_MIN_WIDTH_MARGIN = 56; //dps
    static final int FIXED_WRAP_GUTTER_MIN = 16; //dps
    static final int MOTION_NON_ADJACENT_OFFSET = 24;

    private static final int ANIMATION_DURATION = 300;

    private static final Pools.Pool<Tab> sTabPool = new Pools.SynchronizedPool<>(16);

    /**
     * Scrollable tabs display a subset of tabs at any given moment, and can contain longer tab
     * labels and a larger number of tabs. They are best used for browsing contexts in touch
     * interfaces when users don’t need to directly compare the tab labels.
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_SCROLLABLE = 0;

    /**
     * Fixed tabs display all tabs concurrently and are best used with content that benefits from
     * quick pivots between tabs. The maximum number of tabs is limited by the view’s width.
     * Fixed tabs have equal width, based on the widest tab label.
     *
     * @see #setTabMode(int)
     * @see #getTabMode()
     */
    public static final int MODE_FIXED = 1;

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {}

    /**
     * Gravity used to fill the {@link CameraTabView} as much as possible. This option only takes effect
     * when used with {@link #MODE_FIXED}.
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_FILL = 0;

    /**
     * Gravity used to lay out the tabs in the center of the {@link CameraTabView}.
     *
     * @see #setTabGravity(int)
     * @see #getTabGravity()
     */
    public static final int GRAVITY_CENTER = 1;

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(flag = true, value = {GRAVITY_FILL, GRAVITY_CENTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {}


    public static final int INDICATOR_GRAVITY_BOTTOM = 0;

    public static final int INDICATOR_GRAVITY_CENTER = 1;

    public static final int INDICATOR_GRAVITY_TOP = 2;

    public static final int INDICATOR_GRAVITY_STRETCH = 3;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = {INDICATOR_GRAVITY_BOTTOM, INDICATOR_GRAVITY_CENTER, INDICATOR_GRAVITY_TOP, INDICATOR_GRAVITY_STRETCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorGravity {}



    /**
     * Callback interface invoked when a tab's selection state changes.
     */
    public interface OnTabSelectedListener {

        /**
         * Called when a tab enters the selected state.
         *
         * @param tab The tab that was selected
         */
        void onTabSelected(Tab tab);

        /**
         * Called when a tab exits the selected state.
         *
         * @param tab The tab that was unselected
         */
        void onTabUnselected(Tab tab);

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param tab The tab that was reselected.
         */
        void onTabReselected(Tab tab);
    }
    private final ArrayList<Tab> mTabs = new ArrayList<>();
    private Tab mSelectedTab;
    private final RectF mTabViewContentBounds = new RectF();
    private final SlidingTabIndicator mSlidingTabIndicator;

    boolean mIndicatorCenter;       // 指示器是否居中，默认为false
    boolean mScrollTabAutoSelect;     // 滚动到居中位置时，是否需要切换tab的选中颜色，配合mIndicatorCenter使用

    int mTabPaddingStart;
    int mTabPaddingTop;
    int mTabPaddingEnd;
    int mTabPaddingBottom;

    int mTabTextAppearance;
    ColorStateList mTabTextColors;
    ColorStateList mTabIconTint;
    ColorStateList mTabRippleColorStateList;
    @Nullable
    Drawable mTabSelectedIndicator;
    android.graphics.PorterDuff.Mode mTabIconTintMode;
    float mTabTextSize;
    float mTabTextMultiLineSize;

    final int mTabBackgroundResId;

    int mTabMaxWidth = Integer.MAX_VALUE;
    private final int mRequestedTabMinWidth;
    private final int mRequestedTabMaxWidth;
    private final int mScrollableTabMinWidth;

    private int mContentInsetStart;

    int mTabGravity;
    int mTabIndicatorAnimationDuration;
    int mTabIndicatorGravity;
    int mMode;

    boolean mInlineLabel;
    boolean mTabIndicatorFullWidth;

    private OnTabSelectedListener mSelectedListener;
    private final ArrayList<OnTabSelectedListener> mSelectedListeners = new ArrayList<>();
    private OnTabSelectedListener mCurrentVpSelectedListener;

    private ValueAnimator mScrollAnimator;

    ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private DataSetObserver mPagerAdapterObserver;
    private TabLayoutOnPageChangeListener mPageChangeListener;
    private AdapterChangeListener mAdapterChangeListener;
    private boolean mSetupViewPagerImplicitly;

    // Pool we use as a simple RecyclerBin
    private final Pools.Pool<TabView> mTabViewPool = new Pools.SimplePool<>(12);

    public CameraTabView(Context context) {
        this(context, null);
    }

    public CameraTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ThemeUtils.checkAppCompatTheme(context);

        mIndicatorCenter = false;
        mScrollTabAutoSelect = false;

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);

        // Add the TabStrip
        mSlidingTabIndicator = new SlidingTabIndicator(context);
        super.addView(mSlidingTabIndicator, 0, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout,
                defStyleAttr, R.style.Widget_Design_TabLayout);

        mSlidingTabIndicator.setSelectedIndicatorHeight(
                a.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, 0));
        mSlidingTabIndicator.setSelectedIndicatorColor(a.getColor(R.styleable.TabLayout_tabIndicatorColor, 0));

        setTabIndicatorFullWidth(a.getBoolean(R.styleable.TabLayout_tabIndicatorFullWidth, true));

        mTabPaddingStart = mTabPaddingTop = mTabPaddingEnd = mTabPaddingBottom = a
                .getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
        mTabPaddingStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart,
                mTabPaddingStart);
        mTabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop,
                mTabPaddingTop);
        mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd,
                mTabPaddingEnd);
        mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom,
                mTabPaddingBottom);

        mTabTextAppearance = a.getResourceId(R.styleable.TabLayout_tabTextAppearance,
                R.style.TextAppearance_Design_Tab);

        // Text colors/sizes come from the text appearance first
        final TypedArray ta = context.obtainStyledAttributes(mTabTextAppearance,
                R.styleable.TextAppearance);
        try {
            mTabTextSize = ta.getDimensionPixelSize(
                    R.styleable.TextAppearance_android_textSize, 0);
            mTabTextColors = ta.getColorStateList(
                    R.styleable.TextAppearance_android_textColor);
        } finally {
            ta.recycle();
        }

        if (a.hasValue(R.styleable.TabLayout_tabTextColor)) {
            // If we have an explicit text color set, use it instead
            mTabTextColors = a.getColorStateList(R.styleable.TabLayout_tabTextColor);
        }

        if (a.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
            // We have an explicit selected text color set, so we need to make merge it with the
            // current colors. This is exposed so that developers can use theme attributes to set
            // this (theme attrs in ColorStateLists are Lollipop+)
            final int selected = a.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0);
            mTabTextColors = createColorStateList(mTabTextColors.getDefaultColor(), selected);
        }

        mTabIconTint = MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabIconTint);
        mTabIconTintMode = ViewUtils.parseTintMode(a.getInt(R.styleable.TabLayout_tabIconTintMode, -1), (android.graphics.PorterDuff.Mode)null);
        mTabRippleColorStateList = MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabRippleColor);
        mTabIndicatorAnimationDuration = a.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);
        mRequestedTabMinWidth = a.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth,
                INVALID_WIDTH);
        mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth,
                INVALID_WIDTH);
        mTabBackgroundResId = a.getResourceId(R.styleable.TabLayout_tabBackground, 0);
        mContentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
        mMode = a.getInt(R.styleable.TabLayout_tabMode, MODE_FIXED);
        mTabGravity = a.getInt(R.styleable.TabLayout_tabGravity, GRAVITY_FILL);
        mInlineLabel = a.getBoolean(R.styleable.TabLayout_tabInlineLabel, false);
        a.recycle();

        // TODO add attr for these
        final Resources res = getResources();
        mTabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
        mScrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);

        // Now apply the tab mode and gravity
        applyModeAndGravity();
    }

    /**
     * Sets the tab indicator's color for the currently selected tab.
     *
     * @param color color to use for the indicator
     *
     * @attr ref R.styleable#TabLayout_tabIndicatorColor
     */
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        mSlidingTabIndicator.setSelectedIndicatorColor(color);
    }

    /**
     * Sets the tab indicator's height for the currently selected tab.
     *
     * @param height height to use for the indicator in pixels
     *
     * @attr ref R.styleable#TabLayout_tabIndicatorHeight
     */
    @Deprecated
    public void setSelectedTabIndicatorHeight(int height) {
        mSlidingTabIndicator.setSelectedIndicatorHeight(height);
    }

    /**
     * 设置指示器是否默认居中
     * @param center
     */
    public void setIndicateCenter(boolean center) {
        mIndicatorCenter = center;
    }

    /**
     * 设置tab滚动到居中位置是否切换选中颜色
     * @param autoSelect
     */
    public void setScrollAutoSelected(boolean autoSelect) {
        mScrollTabAutoSelect = autoSelect;
    }

    /**
     * Set the scroll position of the tabs. This is useful for when the tabs are being displayed as
     * part of a scrolling container such as {@link ViewPager}.
     * <p>
     * Calling this method does not update the selected tab, it is only used for drawing purposes.
     *
     * @param position current scroll position
     * @param positionOffset Value from [0, 1) indicating the offset from {@code position}.
     * @param updateSelectedText Whether to update the text's selected state.
     */
    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        setScrollPosition(position, positionOffset, updateSelectedText, true);
    }

    void setScrollPosition(int position, float positionOffset, boolean updateSelectedText,
                           boolean updateIndicatorPosition) {
        final int roundedPosition = Math.round(position + positionOffset);
        if (roundedPosition < 0 || roundedPosition >= mSlidingTabIndicator.getChildCount()) {
            return;
        }

        // Set the indicator position, if enabled
        if (updateIndicatorPosition) {
            mSlidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
        }

        // Now update the scroll position, canceling any running animation
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            mScrollAnimator.cancel();
        }
        scrollTo(calculateScrollXForTab(position, positionOffset), 0);

        // Update the 'selected state' view as we scroll, if enabled
        if (updateSelectedText) {
            setSelectedTabView(roundedPosition);
        }
    }

    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     * If this is the first tab to be added it will become the selected tab.
     *
     * @param tab Tab to add
     */
    public void addTab(@NonNull Tab tab) {
        addTab(tab, mTabs.isEmpty());
    }

    /**
     * Add a tab to this layout. The tab will be inserted at <code>position</code>.
     * If this is the first tab to be added it will become the selected tab.
     *
     * @param tab The tab to add
     * @param position The new position of the tab
     */
    public void addTab(@NonNull Tab tab, int position) {
        addTab(tab, position, mTabs.isEmpty());
    }

    /**
     * Add a tab to this layout. The tab will be added at the end of the list.
     *
     * @param tab Tab to add
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        addTab(tab, mTabs.size(), setSelected);
    }

    /**
     * Add a tab to this layout. The tab will be inserted at <code>position</code>.
     *
     * @param tab The tab to add
     * @param position The new position of the tab
     * @param setSelected True if the added tab should become the selected tab.
     */
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different ");
        }
        configureTab(tab, position);
        addTabView(tab);

        if (setSelected) {
            tab.select();
        }
        updateAllTabMargin();
    }

    private void addTabFromItemView(@NonNull TabItem item) {
        final Tab tab = newTab();
        if (item.mText != null) {
            tab.setText(item.mText);
        }
        if (item.mIcon != null) {
            tab.setIcon(item.mIcon);
        }
        if (item.mCustomLayout != 0) {
            tab.setCustomView(item.mCustomLayout);
        }
        if (!TextUtils.isEmpty(item.getContentDescription())) {
            tab.setContentDescription(item.getContentDescription());
        }
        addTab(tab);
    }

    /**
     * 更新所有的tab的margin，只有在已经measure完成阶段才需要设置，就是显示之后动态添加tab的时候需要
     */
    private void updateAllTabMargin() {
        if (mIndicatorCenter && getMeasuredWidth() > 0) {
            for (int i = 0; i < mSlidingTabIndicator.getChildCount(); i++) {
                View child = mSlidingTabIndicator.getChildAt(i);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                if (i == 0) {
                    params.setMarginStart((getWidth() - child.getWidth()) / 2);
                    params.setMarginEnd(0);
                } else if (i == mSlidingTabIndicator.getChildCount() - 1) {
                    params.setMarginStart(0);
                    params.setMarginEnd((getWidth() - child.getWidth()) / 2);
                } else {
                    params.setMarginStart(0);
                    params.setMarginEnd(0);
                }
                child.setLayoutParams(params);
            }
        }
    }

    /**
     * @deprecated Use {@link #addOnTabSelectedListener(OnTabSelectedListener)} and
     * {@link #removeOnTabSelectedListener(OnTabSelectedListener)}.
     */
    @Deprecated
    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        // The logic in this method emulates what we had before support for multiple
        // registered listeners.
        if (mSelectedListener != null) {
            removeOnTabSelectedListener(mSelectedListener);
        }
        // Update the deprecated field so that we can remove the passed listener the next
        // time we're called
        mSelectedListener = listener;
        if (listener != null) {
            addOnTabSelectedListener(listener);
        }
    }

    /**
     * Add a {@link OnTabSelectedListener} that will be invoked when tab selection
     * changes.
     *
     * <p>Components that add a listener should take care to remove it when finished via
     * {@link #removeOnTabSelectedListener(OnTabSelectedListener)}.</p>
     *
     * @param listener listener to add
     */
    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    /**
     * Remove the given {@link OnTabSelectedListener} that was previously added via
     * {@link #addOnTabSelectedListener(OnTabSelectedListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }

    /**
     * Remove all previously added {@link OnTabSelectedListener}s.
     */
    public void clearOnTabSelectedListeners() {
        mSelectedListeners.clear();
    }

    /**
     * Create and return a new {@link Tab}. You need to manually add this using
     * {@link #addTab(Tab)} or a related method.
     *
     * @return A new Tab
     * @see #addTab(Tab)
     */
    @NonNull
    public Tab newTab() {
        Tab tab = createTabFromPool();
        tab.mParent = this;
        tab.mView = createTabView(tab);
        return tab;
    }

    protected Tab createTabFromPool() {
        Tab tab = (Tab) sTabPool.acquire();
        if (tab == null) {
            tab = new Tab();
        }

        return tab;
    }

    protected boolean releaseFromTabPool(Tab tab) {
        return sTabPool.release(tab);
    }
    /**
     * Returns the number of tabs currently registered with the action bar.
     *
     * @return Tab count
     */
    public int getTabCount() {
        return mTabs.size();
    }

    /**
     * Returns the tab at the specified index.
     */
    @Nullable
    public Tab getTabAt(int index) {
        return (index < 0 || index >= getTabCount()) ? null : mTabs.get(index);
    }

    /**
     * Returns the position of the current selected tab.
     *
     * @return selected tab position, or {@code -1} if there isn't a selected tab.
     */
    public int getSelectedTabPosition() {
        return mSelectedTab != null ? mSelectedTab.getPosition() : -1;
    }

    /**
     * set the position to find selected tab.
     * @param position selected tab position
     */
    public void setSelectedTabPosition(int position) {
        if (position >= 0 && position < getTabCount()) {
            selectTab(getTabAt(position));
        }
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected
     * and another tab will be selected if present.
     *
     * @param tab The tab to remove
     */
    public void removeTab(Tab tab) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab does not belong to this ");
        }

        removeTabAt(tab.getPosition());
    }

    /**
     * Remove a tab from the layout. If the removed tab was selected it will be deselected
     * and another tab will be selected if present.
     *
     * @param position Position of the tab to remove
     */
    public void removeTabAt(int position) {
        final int selectedTabPosition = mSelectedTab != null ? mSelectedTab.getPosition() : 0;
        removeTabViewAt(position);

        final Tab removedTab = mTabs.remove(position);
        if (removedTab != null) {
            removedTab.reset();
            releaseFromTabPool(removedTab);
        }

        final int newTabCount = mTabs.size();
        for (int i = position; i < newTabCount; i++) {
            mTabs.get(i).setPosition(i);
        }

        if (selectedTabPosition == position) {
            selectTab(mTabs.isEmpty() ? null : mTabs.get(Math.max(0, position - 1)));
        }

        updateAllTabMargin();
    }

    /**
     * Remove all tabs from the action bar and deselect the current tab.
     */
    public void removeAllTabs() {
        // Remove all the views
        for (int i = mSlidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
            removeTabViewAt(i);
        }

        for (final Iterator<Tab> i = mTabs.iterator(); i.hasNext();) {
            final Tab tab = i.next();
            i.remove();
            tab.reset();
            releaseFromTabPool(tab);
        }

        mSelectedTab = null;
    }

    /**
     * Set the behavior mode for the Tabs in this layout. The valid input options are:
     * <ul>
     * <li>{@link #MODE_FIXED}: Fixed tabs display all tabs concurrently and are best used
     * with content that benefits from quick pivots between tabs.</li>
     * <li>{@link #MODE_SCROLLABLE}: Scrollable tabs display a subset of tabs at any given moment,
     * and can contain longer tab labels and a larger number of tabs. They are best used for
     * browsing contexts in touch interfaces when users don’t need to directly compare the tab
     * labels. This mode is commonly used with a {@link ViewPager}.</li>
     * </ul>
     *
     * @param mode one of {@link #MODE_FIXED} or {@link #MODE_SCROLLABLE}.
     *
     * @attr ref R.styleable#TabLayout_tabMode
     */
    public void setTabMode(@Mode int mode) {
        if (mode != mMode) {
            mMode = mode;
            applyModeAndGravity();
        }
    }

    /**
     * Returns the current mode used by this {@link CameraTabView}.
     *
     * @see #setTabMode(int)
     */
    @Mode
    public int getTabMode() {
        return mMode;
    }

    /**
     * Set the gravity to use when laying out the tabs.
     *
     * @param gravity one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     *
     * @attr ref R.styleable#TabLayout_tabGravity
     */
    public void setTabGravity(@TabGravity int gravity) {
        if (mTabGravity != gravity) {
            mTabGravity = gravity;
            applyModeAndGravity();
        }
    }

    /**
     * The current gravity used for laying out tabs.
     *
     * @return one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
     */
    @TabGravity
    public int getTabGravity() {
        return mTabGravity;
    }

    public void setSelectedTabIndicatorGravity(@IndicatorGravity int indicatorGravity) {
        if (mTabIndicatorGravity != indicatorGravity) {
            mTabIndicatorGravity = indicatorGravity;
            ViewCompat.postInvalidateOnAnimation(mSlidingTabIndicator);
        }

    }

    public int getTabIndicatorGravity() {
        return mTabIndicatorGravity;
    }

    public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
        mTabIndicatorFullWidth = tabIndicatorFullWidth;
        ViewCompat.postInvalidateOnAnimation(mSlidingTabIndicator);
    }

    public boolean isTabIndicatorFullWidth() {
        return mTabIndicatorFullWidth;
    }

    public void setInlineLabel(boolean inline) {
        if (mInlineLabel != inline) {
            mInlineLabel = inline;

            for(int i = 0; i < mSlidingTabIndicator.getChildCount(); ++i) {
                View child = mSlidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView)child).updateOrientation();
                }
            }

            applyModeAndGravity();
        }

    }

    public void setInlineLabelResource(@BoolRes int inlineResourceId) {
        setInlineLabel(getResources().getBoolean(inlineResourceId));
    }

    public boolean isInlineLabel() {
        return mInlineLabel;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @see #getTabTextColors()
     */
    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (mTabTextColors != textColor) {
            mTabTextColors = textColor;
            updateAllTabs();
        }
    }

    /**
     * Gets the text colors for the different states (normal, selected) used for the tabs.
     */
    @Nullable
    public ColorStateList getTabTextColors() {
        return mTabTextColors;
    }

    /**
     * Sets the text colors for the different states (normal, selected) used for the tabs.
     *
     * @attr ref R.styleable#TabLayout_tabTextColor
     * @attr ref R.styleable#TabLayout_tabSelectedTextColor
     */
    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    public void setTabIconTint(@Nullable ColorStateList iconTint) {
        if (mTabIconTint != iconTint) {
            mTabIconTint = iconTint;
            updateAllTabs();
        }

    }

    public void setTabIconTintResource(@ColorRes int iconTintResourceId) {
        setTabIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
    }

    @Nullable
    public ColorStateList getTabIconTint() {
        return mTabIconTint;
    }

    @Nullable
    public ColorStateList getTabRippleColor() {
        return mTabRippleColorStateList;
    }

    public void setTabRippleColor(@Nullable ColorStateList color) {
        if (mTabRippleColorStateList != color) {
            mTabRippleColorStateList = color;

            for(int i = 0; i < mSlidingTabIndicator.getChildCount(); ++i) {
                View child = mSlidingTabIndicator.getChildAt(i);
                if (child instanceof TabView) {
                    ((TabView)child).updateBackgroundDrawable(getContext());
                }
            }
        }

    }

    public void setTabRippleColorResource(@ColorRes int tabRippleColorResourceId) {
        setTabRippleColor(AppCompatResources.getColorStateList(getContext(), tabRippleColorResourceId));
    }

    @Nullable
    public Drawable getTabSelectedIndicator() {
        return mTabSelectedIndicator;
    }

    public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
        if (mTabSelectedIndicator != tabSelectedIndicator) {
            mTabSelectedIndicator = tabSelectedIndicator;
            ViewCompat.postInvalidateOnAnimation(mSlidingTabIndicator);
        }

    }

    public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
        if (tabSelectedIndicatorResourceId != 0) {
            setSelectedTabIndicator(AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
        } else {
            setSelectedTabIndicator((Drawable)null);
        }

    }

    /**
     * The one-stop shop for setting up this {@link CameraTabView} with a {@link ViewPager}.
     *
     * <p>This is the same as calling {@link #setupWithViewPager(ViewPager, boolean)} with
     * auto-refresh enabled.</p>
     *
     * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
     */
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    /**
     * The one-stop shop for setting up this {@link CameraTabView} with a {@link ViewPager}.
     *
     * <p>This method will link the given ViewPager and this TabLayout together so that
     * changes in one are automatically reflected in the other. This includes scroll state changes
     * and clicks. The tabs displayed in this layout will be populated
     * from the ViewPager adapter's page titles.</p>
     *
     * <p>If {@code autoRefresh} is {@code true}, any changes in the {@link PagerAdapter} will
     * trigger this layout to re-populate itself from the adapter's titles.</p>
     *
     * <p>If the given ViewPager is non-null, it needs to already have a
     * {@link PagerAdapter} set.</p>
     *
     * @param viewPager   the ViewPager to link to, or {@code null} to clear any previous link
     * @param autoRefresh whether this layout should refresh its contents if the given ViewPager's
     *                    content changes
     */
    public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
        setupWithViewPager(viewPager, autoRefresh, false);
    }

    private void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh,
                                    boolean implicitSetup) {
        if (mViewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (mPageChangeListener != null) {
                mViewPager.removeOnPageChangeListener(mPageChangeListener);
            }
            if (mAdapterChangeListener != null) {
                mViewPager.removeOnAdapterChangeListener(mAdapterChangeListener);
            }
        }

        if (mCurrentVpSelectedListener != null) {
            // If we already have a tab selected listener for the ViewPager, remove it
            removeOnTabSelectedListener(mCurrentVpSelectedListener);
            mCurrentVpSelectedListener = null;
        }

        if (viewPager != null) {
            mViewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            mPageChangeListener.reset();
            viewPager.addOnPageChangeListener(mPageChangeListener);

            // Now we'll add a tab selected listener to set ViewPager's current item
            mCurrentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
            addOnTabSelectedListener(mCurrentVpSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (mAdapterChangeListener == null) {
                mAdapterChangeListener = new AdapterChangeListener();
            }
            mAdapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(mAdapterChangeListener);

            // Now update the scroll position to match the ViewPager's current item
            setScrollPosition(viewPager.getCurrentItem(), 0f, true);
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            mViewPager = null;
            setPagerAdapter(null, false);
        }

        mSetupViewPagerImplicitly = implicitSetup;
    }

    /**
     * @deprecated Use {@link #setupWithViewPager(ViewPager)} to link a TabLayout with a ViewPager
     *             together. When that method is used, the TabLayout will be automatically updated
     *             when the {@link PagerAdapter} is changed.
     */
    @Deprecated
    public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
        setPagerAdapter(adapter, false);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        // Only delay the pressed state if the tabs can scroll
        return getTabScrollRange() > 0;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager == null) {
            // If we don't have a ViewPager already, check if our parent is a ViewPager to
            // setup with it automatically
            final ViewParent vp = getParent();
            if (vp instanceof ViewPager) {
                // If we have a ViewPager parent and we've been added as part of its decor, let's
                // assume that we should automatically setup to display any titles
                setupWithViewPager((ViewPager) vp, true, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mSetupViewPagerImplicitly) {
            // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
            setupWithViewPager(null);
            mSetupViewPagerImplicitly = false;
        }
    }

    private int getTabScrollRange() {
        return Math.max(0, mSlidingTabIndicator.getWidth() - getWidth() - getPaddingLeft()
                - getPaddingRight());
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (mPagerAdapter != null && mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter.unregisterDataSetObserver(mPagerAdapterObserver);
        }

        mPagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        removeAllTabs();

        if (mPagerAdapter != null) {
            final int adapterCount = mPagerAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                addTab(newTab().setText(mPagerAdapter.getPageTitle(i)), false);
            }

            // Make sure we reflect the currently set ViewPager item
            if (mViewPager != null && adapterCount > 0) {
                final int curItem = mViewPager.getCurrentItem();
                if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
                    selectTab(getTabAt(curItem));
                }
            }
        }
    }

    private void updateAllTabs() {
        for (int i = 0, z = mTabs.size(); i < z; i++) {
            mTabs.get(i).updateView();
        }
    }

    private TabView createTabView(@NonNull final Tab tab) {
        TabView tabView = mTabViewPool != null ? mTabViewPool.acquire() : null;
        if (tabView == null) {
            tabView = new TabView(getContext());
        }
        tabView.setTab(tab);
        tabView.setFocusable(true);
        tabView.setMinimumWidth(getTabMinWidth());
        if (TextUtils.isEmpty(tab.mContentDesc)) {
            tabView.setContentDescription(tab.mText);
        } else {
            tabView.setContentDescription(tab.mContentDesc);
        }
        return tabView;
    }

    private void configureTab(Tab tab, int position) {
        tab.setPosition(position);
        mTabs.add(position, tab);

        final int count = mTabs.size();
        for (int i = position + 1; i < count; i++) {
            mTabs.get(i).setPosition(i);
        }
    }

    private void addTabView(Tab tab) {
        final TabView tabView = tab.mView;
        mSlidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
    }

    @Override
    public void addView(View child) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    private void addViewInternal(final View child) {
        if (child instanceof TabItem) {
            addTabFromItemView((TabItem) child);
        } else {
            throw new IllegalArgumentException("Only CameraTabItem instances can be added to TabLayout");
        }
    }

    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
        if (mMode == MODE_FIXED && mTabGravity == GRAVITY_FILL) {
            lp.width = 0;
            lp.weight = 1;
        } else {
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lp.weight = 0;
        }
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for(int i = 0; i < mSlidingTabIndicator.getChildCount(); ++i) {
            View tabView = mSlidingTabIndicator.getChildAt(i);
            if (tabView instanceof TabView) {
                ((TabView)tabView).drawBackground(canvas);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If we have a MeasureSpec which allows us to decide our height, try and use the default
        // height
        final int idealHeight = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)),
                        MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, MeasureSpec.EXACTLY);
                break;
        }

        final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            // If we don't have an unspecified width spec, use the given size to calculate
            // the max tab width
            mTabMaxWidth = mRequestedTabMaxWidth > 0
                    ? mRequestedTabMaxWidth
                    : specWidth - dpToPx(TAB_MIN_WIDTH_MARGIN);
        }

        // Now super measure itself using the (possibly) modified height spec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 1) {
            // If we're in fixed mode then we need to make the tab strip is the same width as us
            // so we don't scroll
            final View child = getChildAt(0);
            boolean remeasure = false;

            switch (mMode) {
                case MODE_SCROLLABLE:
                    // We only need to resize the child if it's smaller than us. This is similar
                    // to fillViewport
                    remeasure = child.getMeasuredWidth() < getMeasuredWidth();
                    break;
                case MODE_FIXED:
                    // Resize the child so that it doesn't scroll
                    remeasure = child.getMeasuredWidth() != getMeasuredWidth();
                    break;
            }

            if (remeasure) {
                // Re-measure the child with a widthSpec set to be exactly our measure width
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop()
                        + getPaddingBottom(), child.getLayoutParams().height);
                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                        getMeasuredWidth(), MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 布局阶段自动滚动到选中的位置上
        if (mIndicatorCenter) {
            scrollTo(calculateScrollXForTab(mSelectedTab.getPosition(), 0), 0);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mIndicatorCenter) {
            mSlidingTabIndicator.invalidate();
            // 滚动过程中是否需要改变tab的选中颜色
            if (mScrollTabAutoSelect) {
                onScrollTab();
            }
        }
    }

    /**
     * 滚动居中tab选中态切换
     */
    private void onScrollTab() {
        int scrollX = getScrollX();
        for (int i = 0; i < mTabs.size(); i++) {
            Tab tab = mTabs.get(i);
            tab.mView.setSelected(tab.mView.getLeft() - scrollX <= getWidth()/2 && getWidth()/2 < tab.mView.getRight() - scrollX);
        }
    }

    private static final int DELAY_INTERVAL = 30;
    private boolean mTouching;
    private int mScrollX;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouching = false;
                if (mIndicatorCenter && mScrollTabAutoSelect) {
                    mScrollX = getScrollX();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mScrollX == getScrollX()) {
                                scrollSelectedTab();
                            } else {
                                mScrollX = getScrollX();
                                postDelayed(this, DELAY_INTERVAL);
                            }
                        }
                    }, DELAY_INTERVAL);
                }
                break;

            case MotionEvent.ACTION_DOWN:
                mTouching = true;
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 滚动完成计算选中哪个tab
     */
    private void scrollSelectedTab() {
        if (mTouching) {
            return;
        }
        int scrollX = getScrollX();
        for (int i = 0; i < mTabs.size(); i++) {
            Tab tab = mTabs.get(i);
            if (tab.mView.getLeft() - scrollX <= getWidth()/2
                    && getWidth()/2 < tab.mView.getRight() - scrollX) {
                if (VERBOSE) {
                    Log.d(TAG, "scrollSelectedTab: " + tab.getPosition());
                }
                selectTab(tab);
                break;
            }
        }
    }

    private void removeTabViewAt(int position) {
        final TabView view = (TabView) mSlidingTabIndicator.getChildAt(position);
        mSlidingTabIndicator.removeViewAt(position);
        if (view != null) {
            view.reset();
            mTabViewPool.release(view);
        }
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition == Tab.INVALID_POSITION) {
            return;
        }

        if (getWindowToken() == null || !ViewCompat.isLaidOut(this)
                || mSlidingTabIndicator.childrenNeedLayout()) {
            // If we don't have a window token, or we haven't been laid out yet just draw the new
            // position now
            setScrollPosition(newPosition, 0f, true);
            return;
        }

        final int startScrollX = getScrollX();
        final int targetScrollX = calculateScrollXForTab(newPosition, 0);

        if (startScrollX != targetScrollX) {
            ensureScrollAnimator();

            mScrollAnimator.setIntValues(startScrollX, targetScrollX);
            mScrollAnimator.start();
        }

        // Now animate the indicator
        mSlidingTabIndicator.animateIndicatorToPosition(newPosition, mTabIndicatorAnimationDuration);
    }

    private void ensureScrollAnimator() {
        if (mScrollAnimator == null) {
            mScrollAnimator = new ValueAnimator();
            mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            mScrollAnimator.setDuration(mTabIndicatorAnimationDuration);
            mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    scrollTo((int) animator.getAnimatedValue(), 0);
                }
            });
        }
    }

    void setScrollAnimatorListener(Animator.AnimatorListener listener) {
        ensureScrollAnimator();
        mScrollAnimator.addListener(listener);
    }

    private void setSelectedTabView(int position) {
        final int tabCount = mSlidingTabIndicator.getChildCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                final View child = mSlidingTabIndicator.getChildAt(i);
                child.setSelected(i == position);
            }
        }
    }

    void selectTab(Tab tab) {
        selectTab(tab, true);
    }

    void selectTab(final Tab tab, boolean updateIndicator) {
        final Tab currentTab = mSelectedTab;

        if (currentTab == tab) {
            if (currentTab != null) {
                dispatchTabReselected(tab);
                animateToTab(tab.getPosition());
            }
        } else {
            final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
            if (updateIndicator) {
                if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION)
                        && newPosition != Tab.INVALID_POSITION) {
                    // If we don't currently have a tab, just draw the indicator
                    setScrollPosition(newPosition, 0f, true);
                } else {
                    animateToTab(newPosition);
                }
                if (newPosition != Tab.INVALID_POSITION) {
                    setSelectedTabView(newPosition);
                }
            }

            mSelectedTab = tab;
            if (currentTab != null) {
                dispatchTabUnselected(currentTab);
            }

            if (tab != null) {
                dispatchTabSelected(tab);
            }
        }
    }

    private void dispatchTabSelected(@NonNull final Tab tab) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabSelected(tab);
        }
    }

    private void dispatchTabUnselected(@NonNull final Tab tab) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabUnselected(tab);
        }
    }

    private void dispatchTabReselected(@NonNull final Tab tab) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabReselected(tab);
        }
    }

    /**
     * 计算滚动的位置
     * @param position
     * @param positionOffset
     * @return
     */
    private int calculateScrollXForTab(int position, float positionOffset) {
        if (mMode == MODE_SCROLLABLE) {
            final View selectedChild = mSlidingTabIndicator.getChildAt(position);
            final View nextChild = position + 1 < mSlidingTabIndicator.getChildCount()
                    ? mSlidingTabIndicator.getChildAt(position + 1)
                    : null;
            final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
            final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

            // base scroll amount: places center of tab in center of parent
            int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
            // offset amount: fraction of the distance between centers of tabs
            int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

            return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
                    ? scrollBase + scrollOffset
                    : scrollBase - scrollOffset;
        }
        return 0;
    }

    private void applyModeAndGravity() {
        int paddingStart = 0;
        if (mMode == MODE_SCROLLABLE) {
            // If we're scrollable, or fixed at start, inset using padding
            paddingStart = Math.max(0, mContentInsetStart - mTabPaddingStart);
        }
        ViewCompat.setPaddingRelative(mSlidingTabIndicator, paddingStart, 0, 0, 0);

        switch (mMode) {
            case MODE_FIXED:
                mSlidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case MODE_SCROLLABLE:
                mSlidingTabIndicator.setGravity(GravityCompat.START);
                break;
        }

        updateTabViews(true);
    }

    void updateTabViews(final boolean requestLayout) {
        for (int i = 0; i < mSlidingTabIndicator.getChildCount(); i++) {
            View child = mSlidingTabIndicator.getChildAt(i);
            child.setMinimumWidth(getTabMinWidth());
            updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
            if (requestLayout) {
                child.requestLayout();
            }
        }
    }

    /**
     * A tab in this layout. Instances can be created via {@link #newTab()}.
     */
    public static final class Tab {

        /**
         * An invalid position for a tab.
         *
         * @see #getPosition()
         */
        public static final int INVALID_POSITION = -1;

        private Object mTag;
        private Drawable mIcon;
        private CharSequence mText;
        private CharSequence mContentDesc;
        private int mPosition = INVALID_POSITION;
        private View mCustomView;

        CameraTabView mParent;
        TabView mView;

        Tab() {
            // Private constructor
        }

        /**
         * @return This Tab's tag object.
         */
        @Nullable
        public Object getTag() {
            return mTag;
        }

        /**
         * Give this Tab an arbitrary object to hold for later use.
         *
         * @param tag Object to store
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setTag(@Nullable Object tag) {
            mTag = tag;
            return this;
        }


        /**
         * Returns the custom view used for this tab.
         *
         * @see #setCustomView(View)
         * @see #setCustomView(int)
         */
        @Nullable
        public View getCustomView() {
            return mCustomView;
        }

        /**
         * Set a custom view to be used for this tab.
         * <p>
         * If the provided view contains a {@link TextView} with an ID of
         * {@link //android.R.id#text1} then that will be updated with the value given
         * to {@link #setText(CharSequence)}. Similarly, if this layout contains an
         * {@link ImageView} with ID {@link //android.R.id#icon} then it will be updated with
         * the value given to {@link #setIcon(Drawable)}.
         * </p>
         *
         * @param view Custom view to be used as a tab.
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@Nullable View view) {
            mCustomView = view;
            updateView();
            return this;
        }

        /**
         * Set a custom view to be used for this tab.
         * <p>
         * If the inflated layout contains a {@link TextView} with an ID of
         * {@link //android.R.id#text1} then that will be updated with the value given
         * to {@link #setText(CharSequence)}. Similarly, if this layout contains an
         * {@link ImageView} with ID {@link //android.R.id#icon} then it will be updated with
         * the value given to {@link #setIcon(Drawable)}.
         * </p>
         *
         * @param resId A layout resource to inflate and use as a custom tab view
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setCustomView(@LayoutRes int resId) {
            final LayoutInflater inflater = LayoutInflater.from(mView.getContext());
            return setCustomView(inflater.inflate(resId, mView, false));
        }

        /**
         * Return the icon associated with this tab.
         *
         * @return The tab's icon
         */
        @Nullable
        public Drawable getIcon() {
            return mIcon;
        }

        /**
         * Return the current position of this tab in the action bar.
         *
         * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in
         * the action bar.
         */
        public int getPosition() {
            return mPosition;
        }

        void setPosition(int position) {
            mPosition = position;
        }

        /**
         * Return the text of this tab.
         *
         * @return The tab's text
         */
        @Nullable
        public CharSequence getText() {
            return mText;
        }

        /**
         * Set the icon displayed on this tab.
         *
         * @param icon The drawable to use as an icon
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@Nullable Drawable icon) {
            mIcon = icon;
            updateView();
            return this;
        }

        /**
         * Set the icon displayed on this tab.
         *
         * @param resId A resource ID referring to the icon that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setIcon(@DrawableRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setIcon(AppCompatResources.getDrawable(mParent.getContext(), resId));
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display
         * the entire string.
         *
         * @param text The text to display
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            mText = text;
            updateView();
            return this;
        }

        /**
         * Set the text displayed on this tab. Text may be truncated if there is not room to display
         * the entire string.
         *
         * @param resId A resource ID referring to the text that should be displayed
         * @return The current instance for call chaining
         */
        @NonNull
        public Tab setText(@StringRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setText(mParent.getResources().getText(resId));
        }

        /**
         * Select this tab. Only valid if the tab has been added to the action bar.
         */
        public void select() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            mParent.selectTab(this);
        }

        /**
         * Returns true if this tab is currently selected.
         */
        public boolean isSelected() {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return mParent.getSelectedTabPosition() == mPosition;
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param resId A resource ID referring to the description text
         * @return The current instance for call chaining
         * @see #setContentDescription(CharSequence)
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@StringRes int resId) {
            if (mParent == null) {
                throw new IllegalArgumentException("Tab not attached to a TabLayout");
            }
            return setContentDescription(mParent.getResources().getText(resId));
        }

        /**
         * Set a description of this tab's content for use in accessibility support. If no content
         * description is provided the title will be used.
         *
         * @param contentDesc Description of this tab's content
         * @return The current instance for call chaining
         * @see #setContentDescription(int)
         * @see #getContentDescription()
         */
        @NonNull
        public Tab setContentDescription(@Nullable CharSequence contentDesc) {
            mContentDesc = contentDesc;
            updateView();
            return this;
        }

        /**
         * Gets a brief description of this tab's content for use in accessibility support.
         *
         * @return Description of this tab's content
         * @see #setContentDescription(CharSequence)
         * @see #setContentDescription(int)
         */
        @Nullable
        public CharSequence getContentDescription() {
            return mContentDesc;
        }

        void updateView() {
            if (mView != null) {
                mView.update();
            }
        }

        void reset() {
            mParent = null;
            mView = null;
            mTag = null;
            mIcon = null;
            mText = null;
            mContentDesc = null;
            mPosition = INVALID_POSITION;
            mCustomView = null;
        }
    }

    /**
     * Tab控件view处理
     */
    class TabView extends LinearLayout {
        private Tab mTab;
        private TextView mTextView;
        private ImageView mIconView;

        private View mCustomView;
        private TextView mCustomTextView;
        private ImageView mCustomIconView;

        @Nullable
        private Drawable mBaseBackgroundDrawable;
        private int mDefaultMaxLines = 2;

        public TabView(Context context) {
            super(context);
            updateBackgroundDrawable(context);
            ViewCompat.setPaddingRelative(this, mTabPaddingStart, mTabPaddingTop,
                    mTabPaddingEnd, mTabPaddingBottom);
            setGravity(Gravity.CENTER);
            setOrientation(VERTICAL);
            setClickable(true);
            ViewCompat.setPointerIcon(this,
                    PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
        }

        private void updateBackgroundDrawable(Context context) {
            if (mTabBackgroundResId != 0) {
                mBaseBackgroundDrawable = AppCompatResources.getDrawable(context, mTabBackgroundResId);
                if (mBaseBackgroundDrawable != null && mBaseBackgroundDrawable.isStateful()) {
                    mBaseBackgroundDrawable.setState(getDrawableState());
                }
            } else {
                mBaseBackgroundDrawable = null;
            }
        }

        private void drawBackground(Canvas canvas) {
            if (mBaseBackgroundDrawable != null) {
                mBaseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
                mBaseBackgroundDrawable.draw(canvas);
            }

        }

        protected void drawableStateChanged() {
            super.drawableStateChanged();
            boolean changed = false;
            int[] state = getDrawableState();
            if (mBaseBackgroundDrawable != null && mBaseBackgroundDrawable.isStateful()) {
                changed |= mBaseBackgroundDrawable.setState(state);
            }

            if (changed) {
                invalidate();
                CameraTabView.this.invalidate();
            }

        }

        @Override
        public boolean performClick() {
            final boolean handled = super.performClick();

            if (mTab != null) {
                if (!handled) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                mTab.select();
                return true;
            } else {
                return handled;
            }
        }

        @Override
        public void setSelected(final boolean selected) {
            final boolean changed = isSelected() != selected;

            super.setSelected(selected);

            if (changed && selected && Build.VERSION.SDK_INT < 16) {
                // Pre-JB we need to manually send the TYPE_VIEW_SELECTED event
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            }

            // Always dispatch this to the child views, regardless of whether the value has
            // changed
            if (mTextView != null) {
                mTextView.setSelected(selected);
            }
            if (mIconView != null) {
                mIconView.setSelected(selected);
            }
            if (mCustomView != null) {
                mCustomView.setSelected(selected);
            }
        }

        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            // This view masquerades as an action bar tab.
            event.setClassName(ActionBar.Tab.class.getName());
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            // This view masquerades as an action bar tab.
            info.setClassName(ActionBar.Tab.class.getName());
        }

        @Override
        public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
            final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
            final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
            final int maxWidth = getTabMaxWidth();

            final int widthMeasureSpec;
            final int heightMeasureSpec = origHeightMeasureSpec;

            if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED
                    || specWidthSize > maxWidth)) {
                // If we have a max width and a given spec which is either unspecified or
                // larger than the max width, update the width spec using the same mode
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mTabMaxWidth, MeasureSpec.AT_MOST);
            } else {
                // Else, use the original width spec
                widthMeasureSpec = origWidthMeasureSpec;
            }

            // Now lets measure
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // We need to switch the text size based on whether the text is spanning 2 lines or not
            if (mTextView != null) {
                final Resources res = getResources();
                float textSize = mTabTextSize;
                int maxLines = mDefaultMaxLines;

                if (mIconView != null && mIconView.getVisibility() == VISIBLE) {
                    // If the icon view is being displayed, we limit the text to 1 line
                    maxLines = 1;
                } else if (mTextView != null && mTextView.getLineCount() > 1) {
                    // Otherwise when we have text which wraps we reduce the text size
                    textSize = mTabTextMultiLineSize;
                }

                final float curTextSize = mTextView.getTextSize();
                final int curLineCount = mTextView.getLineCount();
                final int curMaxLines = TextViewCompat.getMaxLines(mTextView);

                if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
                    // We've got a new text size and/or max lines...
                    boolean updateTextView = true;

                    if (mMode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
                        // If we're in fixed mode, going up in text size and currently have 1 line
                        // then it's very easy to get into an infinite recursion.
                        // To combat that we check to see if the change in text size
                        // will cause a line count change. If so, abort the size change and stick
                        // to the smaller size.
                        final Layout layout = mTextView.getLayout();
                        if (layout == null || approximateLineWidth(layout, 0, textSize)
                                > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
                            updateTextView = false;
                        }
                    }

                    if (updateTextView) {
                        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        mTextView.setMaxLines(maxLines);
                        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    }
                }
            }
        }

        void setTab(@Nullable final Tab tab) {
            if (tab != mTab) {
                mTab = tab;
                update();
            }
        }

        void reset() {
            setTab(null);
            setSelected(false);
        }

        final void update() {
            final Tab tab = mTab;
            final View custom = tab != null ? tab.getCustomView() : null;
            if (custom != null) {
                final ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                mCustomView = custom;
                if (mTextView != null) {
                    mTextView.setVisibility(GONE);
                }
                if (mIconView != null) {
                    mIconView.setVisibility(GONE);
                    mIconView.setImageDrawable(null);
                }

                mCustomTextView = (TextView) custom.findViewById(android.R.id.text1);
                if (mCustomTextView != null) {
                    mDefaultMaxLines = TextViewCompat.getMaxLines(mCustomTextView);
                }
                mCustomIconView = (ImageView) custom.findViewById(android.R.id.icon);
            } else {
                // We do not have a custom view. Remove one if it already exists
                if (mCustomView != null) {
                    removeView(mCustomView);
                    mCustomView = null;
                }
                mCustomTextView = null;
                mCustomIconView = null;
            }

            if (mCustomView == null) {
                // If there isn't a custom view, we'll us our own in-built layouts
                if (mIconView == null) {
                    ImageView iconView = (ImageView) LayoutInflater.from(getContext())
                            .inflate(R.layout.design_layout_tab_icon, this, false);
                    addView(iconView, 0);
                    mIconView = iconView;
                }

                Drawable icon = tab != null && tab.getIcon() != null ? DrawableCompat.wrap(tab.getIcon()).mutate() : null;
                if (icon != null) {
                    DrawableCompat.setTintList(icon, mTabIconTint);
                    if (mTabIconTintMode != null) {
                        DrawableCompat.setTintMode(icon, mTabIconTintMode);
                    }
                }


                if (mTextView == null) {
                    TextView textView = (TextView) LayoutInflater.from(getContext())
                            .inflate(R.layout.design_layout_tab_text, this, false);
                    addView(textView);
                    mTextView = textView;
                    mDefaultMaxLines = TextViewCompat.getMaxLines(mTextView);
                }
                TextViewCompat.setTextAppearance(mTextView, mTabTextAppearance);
                if (mTabTextColors != null) {
                    mTextView.setTextColor(mTabTextColors);
                }
                updateTextAndIcon(mTextView, mIconView);
            } else {
                // Else, we'll see if there is a TextView or ImageView present and update them
                if (mCustomTextView != null || mCustomIconView != null) {
                    updateTextAndIcon(mCustomTextView, mCustomIconView);
                }
            }

            // Finally update our selected state
            setSelected(tab != null && tab.isSelected());
        }

        final void updateOrientation() {
            setOrientation(mInlineLabel ? HORIZONTAL : VERTICAL);
            if (mCustomTextView == null && mCustomIconView == null) {
                this.updateTextAndIcon(mTextView, mIconView);
            } else {
                this.updateTextAndIcon(mCustomTextView, mCustomIconView);
            }
        }

        private void updateTextAndIcon(@Nullable final TextView textView,
                                       @Nullable final ImageView iconView) {
            final Drawable icon = mTab != null ? mTab.getIcon() : null;
            final CharSequence text = mTab != null ? mTab.getText() : null;
            final CharSequence contentDesc = mTab != null ? mTab.getContentDescription() : null;

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
                iconView.setContentDescription(contentDesc);
            }

            final boolean hasText = !TextUtils.isEmpty(text);
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    textView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                    textView.setText(null);
                }
                textView.setContentDescription(contentDesc);
            }

            if (iconView != null) {
                MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
                int bottomMargin = 0;
                if (hasText && iconView.getVisibility() == VISIBLE) {
                    // If we're showing both text and icon, add some margin bottom to the icon
                    bottomMargin = dpToPx(DEFAULT_GAP_TEXT_ICON);
                }
                if (bottomMargin != lp.bottomMargin) {
                    lp.bottomMargin = bottomMargin;
                    iconView.requestLayout();
                }
            }
            TooltipCompat.setTooltipText(this, hasText ? null : contentDesc);
        }

        private int getContentWidth() {
            boolean initialized = false;
            int left = 0;
            int right = 0;
            View[] views = new View[]{mTextView, mIconView, mCustomView};
            int length = views.length;

            for(int i = 0; i < length; ++i) {
                View view = views[i];
                if (view != null && view.getVisibility() == VISIBLE) {
                    left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
                    right = initialized ? Math.max(right, view.getRight()) : view.getRight();
                    initialized = true;
                }
            }

            return right - left;
        }

        public Tab getTab() {
            return mTab;
        }

        /**
         * Approximates a given lines width with the new provided text size.
         */
        private float approximateLineWidth(Layout layout, int line, float textSize) {
            return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
        }
    }

    /**
     * 滑动指示器
     */
    private class SlidingTabIndicator extends LinearLayout {
        private int mSelectedIndicatorHeight;
        private final Paint mSelectedIndicatorPaint;
        private final GradientDrawable mDefaultSelectionIndicator;

        int mSelectedPosition = -1;
        float mSelectionOffset;

        private int mLayoutDirection = -1;

        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;

        private ValueAnimator mIndicatorAnimator;

        SlidingTabIndicator(Context context) {
            super(context);
            setWillNotDraw(false);
            mSelectedIndicatorPaint = new Paint();
            mDefaultSelectionIndicator = new GradientDrawable();
        }

        /**
         * 设置指示器颜色
         * @param color
         */
        void setSelectedIndicatorColor(int color) {
            if (mSelectedIndicatorPaint.getColor() != color) {
                mSelectedIndicatorPaint.setColor(color);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /**
         * 设置指示器高度
         * @param height
         */
        void setSelectedIndicatorHeight(int height) {
            if (mSelectedIndicatorHeight != height) {
                mSelectedIndicatorHeight = height;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /**
         * 判断子控件是否需要重新布局
         * @return
         */
        boolean childrenNeedLayout() {
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (child.getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 设置指示器从哪里开始滚动
         * @param position
         * @param positionOffset
         */
        void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            mSelectedPosition = position;
            mSelectionOffset = positionOffset;
            updateIndicatorPosition();
        }

        @Override
        public void onRtlPropertiesChanged(int layoutDirection) {
            super.onRtlPropertiesChanged(layoutDirection);

            // Workaround for a bug before Android M where LinearLayout did not relayout itself when
            // layout direction changed.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //noinspection WrongConstant
                if (mLayoutDirection != layoutDirection) {
                    requestLayout();
                    mLayoutDirection = layoutDirection;
                }
            }
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
                // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
                return;
            }

            if (mMode == MODE_FIXED && mTabGravity == GRAVITY_CENTER) {
                final int count = getChildCount();

                // First we'll find the widest tab
                int largestTabWidth = 0;
                for (int i = 0, z = count; i < z; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == VISIBLE) {
                        largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
                    }
                }

                if (largestTabWidth <= 0) {
                    // If we don't have a largest child yet, skip until the next measure pass
                    return;
                }

                final int gutter = dpToPx(FIXED_WRAP_GUTTER_MIN);
                boolean remeasure = false;

                if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
                    // If the tabs fit within our width minus gutters, we will set all tabs to have
                    // the same width
                    for (int i = 0; i < count; i++) {
                        final LayoutParams lp =
                                (LayoutParams) getChildAt(i).getLayoutParams();
                        if (lp.width != largestTabWidth || lp.weight != 0) {
                            lp.width = largestTabWidth;
                            lp.weight = 0;
                            remeasure = true;
                        }
                    }
                } else {
                    // If the tabs will wrap to be larger than the width minus gutters, we need
                    // to switch to GRAVITY_FILL
                    mTabGravity = GRAVITY_FILL;
                    updateTabViews(false);
                    remeasure = true;
                }

                if (remeasure) {
                    // Now re-measure after our changes
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }

            // measure阶段设置margin，解决初次进来默认不居中文件
            if (mIndicatorCenter) {
                layoutMarginForAllTabs();
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            if (mIndicatorCenter) {
                updateIndicatorPosition();
            } else {
                if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                    // If we're currently running an animation, lets cancel it and start a
                    // new animation with the remaining duration
                    mIndicatorAnimator.cancel();
                    final long duration = mIndicatorAnimator.getDuration();
                    animateIndicatorToPosition(mSelectedPosition,
                            Math.round((1f - mIndicatorAnimator.getAnimatedFraction()) * duration));
                } else {
                    // If we've been layed out, update the indicator position
                    updateIndicatorPosition();
                }
            }
        }

        /**
         * 设置各个tab的顶层的margin
         */
        private void layoutMarginForAllTabs() {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                if (i == 0) {
                    int margin = (CameraTabView.this.getMeasuredWidth() - child.getMeasuredWidth()) / 2;
                    params.setMarginStart(margin);
                    params.setMarginEnd(0);
                } else if (i == childCount - 1) {
                    int margin = (CameraTabView.this.getMeasuredWidth() - child.getMeasuredWidth()) / 2;
                    params.setMarginStart(0);
                    params.setMarginEnd(margin);
                } else {
                    params.setMarginStart(0);
                    params.setMarginEnd(0);
                }
                child.setLayoutParams(params);
            }
            requestLayout();
        }

        /**
         * 更新指示器的位置
         */
        private void updateIndicatorPosition() {
            final View selectedTitle = getChildAt(mSelectedPosition);
            int left, right;

            if (selectedTitle != null && selectedTitle.getWidth() > 0) {
                left = selectedTitle.getLeft();
                right = selectedTitle.getRight();

                if (!mTabIndicatorFullWidth && selectedTitle instanceof TabView) {
                    calculateTabViewContentBounds((TabView)selectedTitle, mTabViewContentBounds);
                    left = (int) mTabViewContentBounds.left;
                    right = (int) mTabViewContentBounds.right;
                }

                if (mSelectionOffset > 0f && mSelectedPosition < getChildCount() - 1) {
                    // Draw the selection partway between the tabs
                    View nextTitle = getChildAt(mSelectedPosition + 1);
                    int nextTitleLeft = nextTitle.getLeft();
                    int nextTitleRight = nextTitle.getRight();
                    if (!mTabIndicatorFullWidth && nextTitle instanceof TabView) {
                        calculateTabViewContentBounds((TabView) nextTitle, mTabViewContentBounds);
                        nextTitleLeft = (int)mTabViewContentBounds.left;
                        nextTitleRight = (int) mTabViewContentBounds.right;
                    }

                    left = (int) (mSelectionOffset * nextTitleLeft +
                            (1.0f - mSelectionOffset) * left);
                    right = (int) (mSelectionOffset * nextTitleRight +
                            (1.0f - mSelectionOffset) * right);
                }
            } else {
                left = right = -1;
            }

            setIndicatorPosition(left, right);
        }

        /**
         * 设置指示器的位置
         * @param left
         * @param right
         */
        void setIndicatorPosition(int left, int right) {
            if (left != mIndicatorLeft || right != mIndicatorRight) {
                // If the indicator's left/right has changed, invalidate
                mIndicatorLeft = left;
                mIndicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        /**
         * 指示器滚动到某个位置
         * @param position
         * @param duration
         */
        void animateIndicatorToPosition(final int position, int duration) {
            if (mIndicatorAnimator != null && mIndicatorAnimator.isRunning()) {
                mIndicatorAnimator.cancel();
            }

            final boolean isRtl = ViewCompat.getLayoutDirection(this)
                    == ViewCompat.LAYOUT_DIRECTION_RTL;

            final View targetView = getChildAt(position);
            if (targetView == null) {
                // If we don't have a view, just update the position now and return
                updateIndicatorPosition();
                return;
            }

            int left = targetView.getLeft();
            int right = targetView.getRight();

            if (!mTabIndicatorFullWidth && targetView instanceof TabView) {
                calculateTabViewContentBounds((TabView)targetView, mTabViewContentBounds);
                left = (int)mTabViewContentBounds.left;
                right = (int) mTabViewContentBounds.right;
            }

            final int targetLeft = left;
            final int targetRight = right;
            final int startLeft = mIndicatorLeft;
            final int startRight = mIndicatorRight;

            if (startLeft != targetLeft || startRight != targetRight) {
                ValueAnimator animator = mIndicatorAnimator = new ValueAnimator();
                animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration(duration);
                animator.setFloatValues(0, 1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        final float fraction = animator.getAnimatedFraction();
                        if (mIndicatorCenter) {
                            setIndicatorPosition(
                                    AnimationUtils.lerp(mIndicatorLeft, targetLeft, fraction),
                                    AnimationUtils.lerp(mIndicatorRight, targetRight, fraction));
                        } else {
                            setIndicatorPosition(
                                    AnimationUtils.lerp(startLeft, targetLeft, fraction),
                                    AnimationUtils.lerp(startRight, targetRight, fraction));
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mSelectedPosition = position;
                        mSelectionOffset = 0f;
                    }
                });
                animator.start();
            }
        }

        private void calculateTabViewContentBounds(TabView tabView, RectF contentBounds) {
            int tabViewContentWidth = tabView.getContentWidth();
            if (tabViewContentWidth < dpToPx(MOTION_NON_ADJACENT_OFFSET)) {
                tabViewContentWidth = dpToPx(MOTION_NON_ADJACENT_OFFSET);
            }

            int tabViewCenter = (tabView.getLeft() + tabView.getRight()) / 2;
            int contentLeftBounds = tabViewCenter - tabViewContentWidth / 2;
            int contentRightBounds = tabViewCenter + tabViewContentWidth / 2;
            contentBounds.set((float)contentLeftBounds, 0.0F, (float)contentRightBounds, 0.0F);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            int indicatorHeight = 0;
            if (mTabSelectedIndicator != null) {
                indicatorHeight = mTabSelectedIndicator.getIntrinsicHeight();
            }
            if (mSelectedIndicatorHeight >= 0) {
                indicatorHeight = mSelectedIndicatorHeight;
            }

            int indicatorTop = 0;
            int indicatorBottom = 0;
            switch (mTabIndicatorGravity) {
                case INDICATOR_GRAVITY_BOTTOM:
                    indicatorTop = getHeight() - indicatorHeight;
                    indicatorBottom = getHeight();
                    break;

                case INDICATOR_GRAVITY_CENTER:
                    indicatorTop = (getHeight() - indicatorHeight) / 2;
                    indicatorBottom = (getHeight() + indicatorHeight) / 2;
                    break;

                case INDICATOR_GRAVITY_TOP:
                    indicatorTop = 0;
                    indicatorBottom = indicatorHeight;
                    break;

                case INDICATOR_GRAVITY_STRETCH:
                    indicatorTop = 0;
                    indicatorBottom = getHeight();
                    break;
            }

            // Thick colored underline below the current selection
            if (mIndicatorCenter) { // 指示器居中显示
                Drawable selectedIndicator = DrawableCompat.wrap((Drawable)(mTabSelectedIndicator != null ? mTabSelectedIndicator : mDefaultSelectionIndicator));
                selectedIndicator.setBounds(
                        (int)(CameraTabView.this.getScrollX() + (CameraTabView.this.getMeasuredWidth() - Math.abs(mIndicatorRight - mIndicatorLeft))/2.0f),
                        indicatorTop,
                        (int)(CameraTabView.this.getScrollX() + (CameraTabView.this.getMeasuredWidth() + Math.abs(mIndicatorRight - mIndicatorLeft))/2.0f),
                        indicatorBottom);
                if (mSelectedIndicatorPaint != null) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                        selectedIndicator.setColorFilter(mSelectedIndicatorPaint.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        DrawableCompat.setTint(selectedIndicator, mSelectedIndicatorPaint.getColor());
                    }
                }

                selectedIndicator.draw(canvas);

            } else {
                if (mIndicatorLeft >= 0 && mIndicatorRight > mIndicatorLeft) {
                    Drawable selectedIndicator = DrawableCompat.wrap((Drawable)(mTabSelectedIndicator != null ? mTabSelectedIndicator : mDefaultSelectionIndicator));
                    selectedIndicator.setBounds(mIndicatorLeft, indicatorTop, mIndicatorRight, indicatorBottom);
                    if (mSelectedIndicatorPaint != null) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                            selectedIndicator.setColorFilter(this.mSelectedIndicatorPaint.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            DrawableCompat.setTint(selectedIndicator, this.mSelectedIndicatorPaint.getColor());
                        }
                    }

                    selectedIndicator.draw(canvas);
                }
            }
        }
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;

        return new ColorStateList(states, colors);
    }

    private int getDefaultHeight() {
        boolean hasIconAndText = false;
        for (int i = 0, count = mTabs.size(); i < count; i++) {
            Tab tab = mTabs.get(i);
            if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
                hasIconAndText = true;
                break;
            }
        }
        return hasIconAndText ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
    }

    private int getTabMinWidth() {
        if (mRequestedTabMinWidth != INVALID_WIDTH) {
            // If we have been given a min width, use it
            return mRequestedTabMinWidth;
        }
        // Else, we'll use the default value
        return mMode == MODE_SCROLLABLE ? mScrollableTabMinWidth : 0;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        // We don't care about the layout params of any views added to us, since we don't actually
        // add them. The only view we add is the SlidingTabStrip, which is done manually.
        // We return the default layout params so that we don't blow up if we're given a CameraTabItem
        // without android:layout_* values.
        return generateDefaultLayoutParams();
    }

    int getTabMaxWidth() {
        return mTabMaxWidth;
    }

    /**
     * A {@link ViewPager.OnPageChangeListener} class which contains the
     * necessary calls back to the provided {@link CameraTabView} so that the tab position is
     * kept in sync.
     *
     * <p>This class stores the provided TabLayout weakly, meaning that you can use
     * {@link ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and
     * not cause a leak.
     */
    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<CameraTabView> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        public TabLayoutOnPageChangeListener(CameraTabView tabLayout) {
            mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset,
                                   final int positionOffsetPixels) {
            final CameraTabView tabLayout = mTabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                final boolean updateText = mScrollState != ViewPager.SCROLL_STATE_SETTLING ||
                        mPreviousScrollState == ViewPager.SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                final boolean updateIndicator = !(mScrollState == ViewPager.SCROLL_STATE_SETTLING
                        && mPreviousScrollState == ViewPager.SCROLL_STATE_IDLE);
                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final CameraTabView tabLayout = mTabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                final boolean updateIndicator = mScrollState == ViewPager.SCROLL_STATE_IDLE
                        || (mScrollState == ViewPager.SCROLL_STATE_SETTLING
                        && mPreviousScrollState == ViewPager.SCROLL_STATE_IDLE);
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
            }
        }

        void reset() {
            mPreviousScrollState = mScrollState = ViewPager.SCROLL_STATE_IDLE;
        }
    }

    /**
     * A {@link OnTabSelectedListener} class which contains the necessary calls back
     * to the provided {@link ViewPager} so that the tab position is kept in sync.
     */
    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onTabSelected(Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(Tab tab) {
            // No-op
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager,
                                     @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (mViewPager == viewPager) {
                setPagerAdapter(newAdapter, mAutoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            mAutoRefresh = autoRefresh;
        }
    }
}
