package com.cgfay.caincamera.activity.facetrack;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * 屏幕大小管理器
 * Created by cain.huang on 2017/7/27.
 */
public class ScreenManager {

	public static float LEFTMENU_UI_PERCENT = 0.15f;
	public static int mNotificationBarHeight;

	public static int mScreenWidth;
	public static int mScreenHeight;
	public static int mWidth;
	public static int mHeight;
	public static float densityDpi;
	public static float density;

	public static float drawWidth;
	public static float drawHeight;

	private static final int PADDING_L = 30;
	private static final int PADDING_R = 30;
	private static final int PADDING_T = 50;
	private static final int PADDING_B = 40;

	public static float drawPaddingLeft;
	public static float drawPaddingRight;
	public static float drawPaddingTop;
	public static float drawPaddingBottom;

	public static int drawRows;
	public static float lineHeight;
	public static float line_space = 0;
	public static float charHeight;

	public static void initialize(Context context) {
		if (drawWidth == 0 || drawHeight == 0 || mWidth == 0 || mHeight == 0
				|| density == 0) {
			Resources res = context.getResources();
			DisplayMetrics metrics = res.getDisplayMetrics();
			// TODO // - 50
			density = metrics.density;
			mNotificationBarHeight = (int) (35 * density);
			mWidth = metrics.widthPixels;// - (int)(50 * density)
			mHeight = metrics.heightPixels/* - mNotificationBarHeight */;// -
			// (int)(50
			// *
			// density)
			mScreenWidth = metrics.widthPixels;
			mScreenHeight = metrics.heightPixels;

			densityDpi = metrics.densityDpi;

			drawPaddingLeft = density * PADDING_L;
			drawPaddingRight = density * PADDING_R;
			drawPaddingTop = density * PADDING_T;
			drawPaddingBottom = density * PADDING_B;

			drawWidth = mWidth - drawPaddingLeft - drawPaddingRight;
			// TODO 如果非全屏，减去标题栏的高度
			drawHeight = mHeight - drawPaddingTop - drawPaddingBottom;
		}
	}
}