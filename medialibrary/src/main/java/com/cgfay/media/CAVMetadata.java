package com.cgfay.media;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * metadata数据对象
 */
public class CAVMetadata {

    private HashMap<String, String> mParcelMetadata;

    public CAVMetadata(HashMap<String, String> metadata) {
        mParcelMetadata = metadata;
    }

    /**
     * 是否包含key
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return mParcelMetadata.containsKey(key);
    }

    public String getString(String key) {
        if (containsKey(key)) {
            return String.valueOf(mParcelMetadata.get(key));
        }
        return null;
    }

    public int getInt(String key) {
        if (containsKey(key)) {
            return Integer.valueOf(mParcelMetadata.get(key));
        }
        return 0;
    }

    public long getLong(String key) {
        if (containsKey(key)) {
            return Long.valueOf(mParcelMetadata.get(key));
        }
        return 0;
    }

    public double getDouble(String key) {
        if (containsKey(key)) {
            return Double.valueOf(mParcelMetadata.get(key));
        }
        return 0;
    }

    public byte[] getByteArray(String key) {
        if (containsKey(key)) {
            return mParcelMetadata.get(key).getBytes();
        }
        return null;
    }

    public Date getDate(String key) {
        if (containsKey(key)) {
            final long timeDefault = Long.valueOf(mParcelMetadata.get(key));
            final String timeZoneStr = mParcelMetadata.get(key);

            if (timeZoneStr.length() == 0) {
                return new Date(timeDefault);
            } else {
                TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
                Calendar cal = Calendar.getInstance(timeZone);
                cal.setTimeInMillis(timeDefault);
                return cal.getTime();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "MediaMetadata{ \n" +
                "mParcelMetadata = " + mParcelMetadata.toString() +
                "\n}";
    }
}
