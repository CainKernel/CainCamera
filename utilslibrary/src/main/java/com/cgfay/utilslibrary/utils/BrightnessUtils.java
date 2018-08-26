package com.cgfay.utilslibrary.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

/**
 * 亮度调节工具
 */
public class BrightnessUtils {

    private static final String TAG = "BrightnessUtils";

    // 最大亮度值
    public static final int MAX_BRIGHTNESS = 255;

    private BrightnessUtils() {}

    /**
     * 获得系统当前的亮度模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * @return
     */
    public static int getSystemBrightnessMode(Context context) {
        int brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        try {
            brightnessMode = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            Log.e(TAG, "getSystemBrightnessMode: ", e);
        }
        return brightnessMode;
    }

    /**
     * 设置系统当前的亮度模式 需要权限
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1 为自动调节屏幕亮度, 0 为手动调节
     * @param context
     * @param brightnessMode
     */
    public static void setSystemBrightnessMode(Context context, int brightnessMode) {
        try {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, brightnessMode);
        } catch (Exception e) {
            Log.e(TAG, "setSystemBrightnessMode: ", e);
        }
    }

    /**
     * 获得系统当前的亮度值
     * @param context
     * @return
     */
    public static int getSystemBrightness(Context context) {
        int screenBrightness = MAX_BRIGHTNESS;
        try {
            screenBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            Log.e(TAG, "getSystemBrightness: ", e);
        }
        return screenBrightness;
    }

    /**
     * 设置系统当前的亮度值 需要权限
     * @param context
     * @param brightness 0~255
     */
    public static void setSystemBrightness(Context context, int brightness) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Settings.System
                    .getUriFor(Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            // 实时通知改变
            resolver.notifyChange(uri, null);
        } catch (Exception e) {
            Log.e(TAG, "setSystemBrightness: ", e);;
        }
    }

    /**
     * 设置当前窗口屏幕亮度
     * @param activity
     * @param brightness 0~255, -1 时表示自动亮度
     */
    public static void setWindowBrightness(Activity activity, int brightness) {
        final WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        // 自动亮度时，还原会系统默认值就行
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = brightness / (float) MAX_BRIGHTNESS;
        }
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 恢复亮度模式和亮度值的设置 需要权限
     * @param activity
     * @param brightnessMode
     * @param brightness
     */
    public static void restoreSystemBrightness(Activity activity, int brightnessMode, int brightness) {
        setSystemBrightnessMode(activity, brightnessMode);
        setSystemBrightness(activity, brightness);
        setWindowBrightness(activity, -MAX_BRIGHTNESS);
    }

}
