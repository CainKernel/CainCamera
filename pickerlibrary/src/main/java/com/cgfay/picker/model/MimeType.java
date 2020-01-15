package com.cgfay.picker.model;

import androidx.collection.ArraySet;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * 项目支持的媒体类型
 */
public enum MimeType {

    // 图片类型
    JPEG("image/jpeg", arraySetOf(
            "jpeg"
    )),
    JPG("image/jpg", arraySetOf(
            "jpg"
    )),
    BMP("image/bmp", arraySetOf(
            "bmp"
    )),
    PNG("image/png", arraySetOf(
            "png"
    )),
    GIF("image/gif", arraySetOf(
            "gif"
    )),

    // 视频类型
    MPEG("video/mpeg", arraySetOf(
            "mpeg",
            "mpg"
    )),
    MP4("video/mp4", arraySetOf(
            "mp4",
            "m4v"
    )),
    GPP("video/3gpp", arraySetOf(
            "3gpp"
    )),
    MKV("video/x-matroska", arraySetOf(
            "mkv"
    )),
    AVI("video/avi", arraySetOf(
            "avi"
    ));

    private final String mMimeType;
    private final Set<String> mExtensions;

    MimeType(String mimeType, Set<String> extensions) {
        mMimeType = mimeType;
        mExtensions = extensions;
    }

    public static Set<MimeType> ofAll() {
        return EnumSet.allOf(MimeType.class);
    }

    public static Set<MimeType> of(MimeType type, MimeType...rest) {
        return EnumSet.of(type, rest);
    }

    // 图片
    public static Set<MimeType> ofImage() {
        return EnumSet.of(JPEG, JPG, PNG, BMP, GIF);
    }

    // 视频
    public static Set<MimeType> ofVideo() {
        return EnumSet.of(MPEG, MP4, GPP, MKV, AVI);
    }

    public static boolean isImage(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.startsWith("image");
    }

    public static boolean isVideo(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.startsWith("video");
    }

    private static Set<String> arraySetOf(String... suffixes) {
        return new ArraySet<>(Arrays.asList(suffixes));
    }

    public Set<String> getExtensions() {
        return mExtensions;
    }

    public String getMimeType() {
        return mMimeType;
    }
}
