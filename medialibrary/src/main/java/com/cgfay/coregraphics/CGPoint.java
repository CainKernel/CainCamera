package com.cgfay.coregraphics;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * 点对象
 */
public class CGPoint implements Serializable, Parcelable {

    public static final Creator<CGPoint> CREATOR = new Creator<CGPoint>() {
        @Override
        public CGPoint createFromParcel(Parcel source) {
            return new CGPoint(source);
        }

        @Override
        public CGPoint[] newArray(int size) {
            return new CGPoint[size];
        }
    };

    public int x;
    public int y;

    public CGPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private CGPoint(Parcel source) {
        x = source.readInt();
        y = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof CGPoint) {
            CGPoint other = (CGPoint) obj;
            return x == other.x && y == other.y;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }

    @Override
    public int hashCode() {
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return y ^ ((x << (Integer.SIZE / 2)) | (x >>> (Integer.SIZE / 2)));
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
