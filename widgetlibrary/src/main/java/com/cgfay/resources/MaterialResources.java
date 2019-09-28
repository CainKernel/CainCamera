package com.cgfay.resources;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleableRes;
import android.support.design.resources.TextAppearance;
import android.support.v7.content.res.AppCompatResources;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public final class MaterialResources {
    private MaterialResources() {
    }

    @Nullable
    public static ColorStateList getColorStateList(Context context, TypedArray attributes, @StyleableRes int index) {
        if (attributes.hasValue(index)) {
            int resourceId = attributes.getResourceId(index, 0);
            if (resourceId != 0) {
                ColorStateList value = AppCompatResources.getColorStateList(context, resourceId);
                if (value != null) {
                    return value;
                }
            }
        }

        return attributes.getColorStateList(index);
    }

    @Nullable
    public static Drawable getDrawable(Context context, TypedArray attributes, @StyleableRes int index) {
        if (attributes.hasValue(index)) {
            int resourceId = attributes.getResourceId(index, 0);
            if (resourceId != 0) {
                Drawable value = AppCompatResources.getDrawable(context, resourceId);
                if (value != null) {
                    return value;
                }
            }
        }

        return attributes.getDrawable(index);
    }

    @SuppressLint("RestrictedApi")
    @Nullable
    public static TextAppearance getTextAppearance(Context context, TypedArray attributes, @StyleableRes int index) {
        if (attributes.hasValue(index)) {
            int resourceId = attributes.getResourceId(index, 0);
            if (resourceId != 0) {
                return new TextAppearance(context, resourceId);
            }
        }

        return null;
    }

    @StyleableRes
    static int getIndexWithValue(TypedArray attributes, @StyleableRes int a, @StyleableRes int b) {
        return attributes.hasValue(a) ? a : b;
    }
}
