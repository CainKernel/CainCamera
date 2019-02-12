package com.cgfay.scan.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.v4.util.ArraySet;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.cgfay.scan.utils.MediaMetadataUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * 项目支持的媒体类型
 */
public enum MimeType {

    // 图片类型
    JPEG("image/jpeg", arraySetOf(
            "jpg",
            "jpeg"
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
    MKV("video/x-matroska", arraySetOf(
            "mkv"
    )),
    AVI("video/avi", arraySetOf(
            "avi"
    ));

    private final String mMimeTypeName;
    private final Set<String> mExtensions;

    MimeType(String mimeTypeName, Set<String> extensions) {
        mMimeTypeName = mimeTypeName;
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
        return EnumSet.of(JPEG, PNG, GIF);
    }

    // 视频
    public static Set<MimeType> ofVideo() {
        return EnumSet.of(MPEG, MP4, MKV, AVI);
    }

    private static Set<String> arraySetOf(String... suffixes) {
        return new ArraySet<>(Arrays.asList(suffixes));
    }

    @Override
    public String toString() {
        return mMimeTypeName;
    }

    /**
     * 检查类型
     * @param resolver
     * @param uri
     * @return
     */
    public boolean checkType(ContentResolver resolver, Uri uri) {
        MimeTypeMap map = MimeTypeMap.getSingleton();
        if (uri == null) {
            return false;
        }

        String type = map.getExtensionFromMimeType(resolver.getType(uri));
        String path = null;
        boolean pathParsed = false;
        for (String extension : mExtensions) {
            if (extension.equals(type)) {
                return true;
            }
            if (!pathParsed) {
                path = MediaMetadataUtils.getPath(resolver, uri);
                if (!TextUtils.isEmpty(path)) {
                    path = path.toLowerCase(Locale.US);
                }
                pathParsed = true;
            }
            if (path != null && path.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}
