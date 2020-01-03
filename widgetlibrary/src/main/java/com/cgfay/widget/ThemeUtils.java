package com.cgfay.widget;

import android.content.Context;
import android.content.res.TypedArray;

import com.cgfay.design.R;

/**
 * @author CainHuang
 * 2019-09-28
 */
final class ThemeUtils {

    private static final int[] APPCOMPAT_CHECK_ATTRS = {
            R.attr.colorPrimary
    };

    static void checkAppCompatTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS);
        final boolean failed = !a.hasValue(0);
        a.recycle();
        if (failed) {
            throw new IllegalArgumentException("You need to use a Theme.AppCompat theme "
                    + "(or descendant) with the design library.");
        }
    }
}
