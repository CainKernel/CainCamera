package com.cgfay.coregraphics;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 矩形对象
 */
public class CGRect implements Serializable, Parcelable {

    public static final Creator<CGRect> CREATOR = new Creator<CGRect>() {
        @Override
        public CGRect createFromParcel(Parcel source) {
            return new CGRect(source);
        }

        @Override
        public CGRect[] newArray(int size) {
            return new CGRect[size];
        }
    };

    public CGPoint origin;
    public CGSize size;

    public CGRect(CGPoint origin, CGSize size) {
        this.origin = origin;
        this.size = size;
    }

    public CGRect(int left, int top, int width, int height) {
        origin = new CGPoint(left, top);
        size = new CGSize(width, height);
    }

    private CGRect(Parcel source) {
        origin = source.readParcelable(CGPoint.class.getClassLoader());
        size = source.readParcelable(CGSize.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(origin, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(size, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
    }

    /**
     * 判断矩形是否为空
     */
    public boolean isEmpty() {
        return size.width == 0 && size.height == 0;
    }

    public CGPoint getOrigin() {
        return origin;
    }

    public CGSize getSize() {
        return size;
    }

    public int getMinX() {
        return origin.x;
    }

    public int getMidX() {
        return (origin.x + size.getWidth()) / 2;
    }

    public int getMaxX() {
        return (origin.x + size.getWidth());
    }

    public int getMinY() {
        return origin.y;
    }

    public int getMidY() {
        return (origin.y + size.getHeight()) / 2;
    }

    public int getMaxY() {
        return (origin.y + size.getHeight());
    }
}
