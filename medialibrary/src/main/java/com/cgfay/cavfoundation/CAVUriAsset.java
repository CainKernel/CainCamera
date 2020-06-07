package com.cgfay.cavfoundation;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coregraphics.CGSize;
import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeRange;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用Uri
 */
public class CAVUriAsset extends AVAsset<AVAssetTrack> {

    /**
     * 轨道ID集合，key为轨道索引，value为trackID
     */
    private final Map<Integer, Integer> mTrackIDGroups;

    /**
     * 旋转角度
     */
    private int mRotation;

    /**
     * 轨道数量
     */
    private int mTrackCount;

    public CAVUriAsset() {
        super();
        mTrackIDGroups = new HashMap<>();
        native_setup();
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri     the Content URI of the data you want to extract from.
     * @param headers the headers to be sent together with the request for the data
     */
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers) throws IOException {
        mUri = uri;
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }

        AssetFileDescriptor fd = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            fd = resolver.openAssetFileDescriptor(uri, "r");
            if (fd == null) {
                return;
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            if (fd.getDeclaredLength() < 0) {
                setDataSource(fd.getFileDescriptor());
            } else {
                setDataSource(
                        fd.getFileDescriptor(),
                        fd.getStartOffset(),
                        fd.getDeclaredLength());
            }
            return;
        } catch (SecurityException ex) {
        } catch (IOException ex) {
        } finally {
            if (fd != null) {
                fd.close();
            }
        }

        setDataSource(uri.toString(), headers);
    }

    /**
     * Sets the data source (file-path or http URL) to use.
     *
     * @param path    the path of the file, or the http URL
     * @param headers the headers associated with the http request for the stream you want to play
     */
    public void setDataSource(@NonNull String path, @Nullable Map<String, String> headers) throws IOException {
        String[] keys = null;
        String[] values = null;

        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];

            int i = 0;
            for (Map.Entry<String, String> entry: headers.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                ++i;
            }
        }
        _setDataSource(path, keys, values);
    }

    private native void _setDataSource(
            String uri, String[] keys, String[] values)
            throws IOException;

    /**
     * Sets the data source (file-path or http URL) to use.
     *
     * @param path the path of the file, or the http URL of the stream
     *
     *             <p>When <code>path</code> refers to a local file, the file may actually be opened by a
     *             process other than the calling application.  This implies that the pathname
     *             should be an absolute path (as any other process runs with unspecified current working
     *             directory), and that the pathname should reference a world-readable file.
     *             As an alternative, the application could first open the file for reading,
     *             and then use the file descriptor form {@link #setDataSource(FileDescriptor)}.
     */
    public native void setDataSource(@NonNull String path) throws IOException;

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to extract from.
     */
    public void setDataSource(@NonNull FileDescriptor fd) throws IOException {
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }

    /**
     * Sets the data source (FileDescriptor) to use.  The FileDescriptor must be
     * seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd     the FileDescriptor for the file you want to extract from.
     * @param offset the offset into the file where the data to be extracted starts, in bytes
     * @param length the length in bytes of the data to be extracted
     */
    public void setDataSource(@NonNull FileDescriptor fd, long offset, long length) throws IOException {
        _setDataSource(fd, offset, length);
    }

    private native void _setDataSource(FileDescriptor fd, long offset, long length) throws IOException;

    /**
     * 绑定轨道索引和轨道ID，native层调用
     * @param index     轨道索引
     * @param trackID   轨道ID
     */
    private void putTrack(int index, int trackID) {
        mTrackIDGroups.put(index, trackID);
    }

    /**
     * 设置视频分辨率，native层调用
     */
    private void putVideoSize(int width, int height) {
        mNaturalSize = new CGSize(width, height);
    }

    /**
     * 设置时间，native层调用
     * @param value
     * @param timescale
     */
    private void putDuration(long value, int timescale) {
        mDuration = new AVTime(value, timescale);
    }

    /**
     * 设置旋转角度
     * @param rotation 旋转角度
     */
    private void putRotation(int rotation) {
        mRotation = rotation;
        mPreferredTransform.setRotation(rotation);
    }

    /**
     * 获取当前轨道索引的媒体类型
     * @param index 索引位置
     */
    private AVMediaType getTrackType(int index) {
        int mediaType = _getTrackType(index);
        AVMediaType type = AVMediaType.AVMediaTypeUnknown;
        if (mediaType == MEDIA_TYPE_AUDIO) {
            type = AVMediaType.AVMediaTypeAudio;
        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            type = AVMediaType.AVMediaTypeVideo;
        }
        return type;
    }

    private static final int MEDIA_TYPE_VIDEO  = 0;
    private static final int MEDIA_TYPE_AUDIO = 1;
    @IntDef({
            MEDIA_TYPE_AUDIO,
            MEDIA_TYPE_VIDEO,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface MediaType {}
    private @MediaType native int _getTrackType(int index);

    /**
     * 创建轨道信息
     */
    void createAssetTrack(@NonNull Uri uri) {
        for (int i = 0; i < mTrackCount; i++) {
            AVMediaType type = getTrackType(i);
            int trackID = mTrackIDGroups.get(i);
            if (type == AVMediaType.AVMediaTypeAudio || type == AVMediaType.AVMediaTypeVideo) {
                AVTimeRange timeRange = new AVTimeRange(AVTime.kAVTimeZero, getDuration());
                if (type == AVMediaType.AVMediaTypeVideo) {
                    AVAssetTrack track = new AVAssetTrack(this, uri, trackID, type, timeRange, mNaturalSize);
                    track.mNaturalTimeScale = AVTime.DEFAULT_TIME_SCALE;
                    mTracks.add(track);
                } else {
                    AVAssetTrack track = new AVAssetTrack(this, uri, trackID, type, timeRange);
                    track.mNaturalTimeScale = 44100;
                    mTracks.add(track);
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        native_finalize();
    }

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("cavfoundation");
        native_init();
    }

    private long mNativeContext;

    private static native final void native_init();
    private native final void native_setup();
    private native final void native_finalize();
}
