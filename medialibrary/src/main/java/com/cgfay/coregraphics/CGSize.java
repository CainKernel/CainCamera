package com.cgfay.coregraphics;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Size对象
 */
public final class CGSize implements Serializable, Parcelable {

    public static final CGSize kSizeZero = new CGSize(0, 0);

    public static final Creator<CGSize> CREATOR = new Creator<CGSize>() {
        @Override
        public CGSize createFromParcel(Parcel source) {
            return new CGSize(source);
        }

        @Override
        public CGSize[] newArray(int size) {
            return new CGSize[size];
        }
    };

    public int width;
    public int height;

    public CGSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private CGSize(Parcel source) {
        width = source.readInt();
        height = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Check if this size is equal to another size.
     * <p>
     * Two sizes are equal if and only if both their widths and heights are equal.
     * </p>
     * <p>
     * A size object is never equal to any other type of object.
     * </p>
     * @return {@code true} if the objects were equal, @{code false} otherwise
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof CGSize) {
            CGSize other = (CGSize) obj;
            return width == other.width && height == other.height;
        }
        return false;
    }

    /**
     * Return the size represented as a string with the format {@code "WxH"}
     * @return string representation of the size
     */
    @NonNull
    @Override
    public String toString() {
        return width + "x" + height;
    }

    private static NumberFormatException invalidSize(String s) {
        throw new NumberFormatException("Invalid Size: \"" + s + "\"");
    }

    /**
     * Parses the specified string as a size value.
     */
    public static CGSize parseSize(String string) {
        if (TextUtils.isEmpty(string)) {
            throw new NullPointerException("string must not be null");
        }
        int sep_ix = string.indexOf('*');
        if (sep_ix < 0) {
            sep_ix = string.indexOf('x');
        }
        if (sep_ix < 0) {
            throw invalidSize(string);
        }
        try {
            return new CGSize(Integer.parseInt(string.substring(0, sep_ix)),
                    Integer.parseInt(string.substring(sep_ix + 1)));
        } catch (NumberFormatException e) {
            throw invalidSize(string);
        }
    }

    @Override
    public int hashCode() {
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return height ^ ((width << (Integer.SIZE / 2)) | (width >>> (Integer.SIZE / 2)));
    }
}
