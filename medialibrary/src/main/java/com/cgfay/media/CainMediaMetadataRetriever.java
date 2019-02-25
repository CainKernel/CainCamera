package com.cgfay.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.cgfay.media.annotations.AccessedByNative;
import com.cgfay.utilslibrary.utils.BitmapUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于FFmpeg实现的MediaMetadataRetriever
 * 实现仿照MediaMetadataRetriever 的实现逻辑
 * 详情请参考 android.media.MediaMetadataRetriever.java 和 android_media_MediaMetadataRetriever.cpp
 */
public class CainMediaMetadataRetriever {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("metadata_retriever");
        native_init();
    }

    // The field below is accessed by native methods
    @AccessedByNative
    private long mNativeContext;

    private static final int EMBEDDED_PICTURE_TYPE_ANY = 0xFFFF;

    public CainMediaMetadataRetriever() {
        native_setup();
    }

    /**
     * Sets the data source (file pathname) to use. Call this
     * method before the rest of the methods in this class. This method may be
     * time-consuming.
     *
     * @param path The path of the input media file.
     * @throws IllegalArgumentException If the path is invalid.
     */
    public native void setDataSource(String path) throws IllegalArgumentException;

    /**
     * Sets the data source (URI) to use. Call this
     * method before the rest of the methods in this class. This method may be
     * time-consuming.
     *
     * @param uri The URI of the input media.
     * @param headers the headers to be sent together with the request for the data
     * @throws IllegalArgumentException If the URI is invalid.
     */
    public void setDataSource(String uri, Map<String, String> headers)
            throws IllegalArgumentException {
        int i = 0;
        String[] keys = new String[headers.size()];
        String[] values = new String[headers.size()];
        for (Map.Entry<String, String> entry: headers.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            ++i;
        }
        _setDataSource(uri, keys, values);
    }

    private native void _setDataSource(
            String uri, String[] keys, String[] values)
            throws IllegalArgumentException;

    /**
     * Sets the data source (FileDescriptor) to use.  It is the caller's
     * responsibility to close the data source descriptor. It is safe to do so as soon
     * as this call returns. Call this method before the rest of the methods in
     * this class. This method may be time-consuming.
     *
     * @param fd the FileDescriptor for the data source you want to play
     * @param offset the offset into the data source where the data to be played starts,
     * in bytes. It must be non-negative
     * @param length the length in bytes of the data to be played. It must be
     * non-negative.
     * @throws IllegalArgumentException if the arguments are invalid
     */
    public native void setDataSource(FileDescriptor fd, long offset, long length)
            throws IllegalArgumentException;

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's
     * responsibility to close the data source descriptor. It is safe to do so as soon
     * as this call returns. Call this method before the rest of the methods in
     * this class. This method may be time-consuming.
     *
     * @param fd the FileDescriptor for the data source you want to play
     * @throws IllegalArgumentException if the FileDescriptor is invalid
     */
    public void setDataSource(FileDescriptor fd)
            throws IllegalArgumentException {
        // intentionally less than LONG_MAX
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }

    /**
     * Sets the data source as a content Uri. Call this method before
     * the rest of the methods in this class. This method may be time-consuming.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to play
     * @throws IllegalArgumentException if the Uri is invalid
     * @throws SecurityException if the Uri cannot be used due to lack of
     * permission.
     */
    public void setDataSource(Context context, Uri uri)
            throws IllegalArgumentException, SecurityException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }

        String scheme = uri.getScheme();
        if(scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }

        AssetFileDescriptor fd = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            try {
                fd = resolver.openAssetFileDescriptor(uri, "r");
            } catch(FileNotFoundException e) {
                throw new IllegalArgumentException();
            }
            if (fd == null) {
                throw new IllegalArgumentException();
            }
            FileDescriptor descriptor = fd.getFileDescriptor();
            if (!descriptor.valid()) {
                throw new IllegalArgumentException();
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            if (fd.getDeclaredLength() < 0) {
                setDataSource(descriptor);
            } else {
                setDataSource(descriptor, fd.getStartOffset(), fd.getDeclaredLength());
            }
            return;
        } catch (SecurityException ex) {
        } finally {
            try {
                if (fd != null) {
                    fd.close();
                }
            } catch(IOException ioEx) {
            }
        }
        setDataSource(uri.toString());
    }

    /**
     * Call this method after setDataSource(). This method retrieves the
     * meta data value associated with the keyCode.
     *
     * The keyCode currently supported is listed below as METADATA_XXX
     * constants. With any other value, it returns a null pointer.
     *
     * @param keyCode One of the constants listed below at the end of the class.
     * @return The meta data value associate with the given keyCode on success;
     * null on failure.
     */
    public native String extractMetadata(String keyCode);


    /**
     * Call this method after setDataSource(). This method retrieves the
     * meta data value associated with the keyCode.
     *
     * The keyCode currently supported is listed below as METADATA_XXX
     * constants. With any other value, it returns a null pointer.
     *
     * @param keyCode One of the constants listed below at the end of the class.
     * @param chapter The chapter from where the metadata will be retrieved.
     * @return The meta data value associate with the given keyCode on success;
     * null on failure.
     */
    public native String extractMetadataFromChapter(String keyCode, int chapter);

    /**
     * Gets the media metadata.
     * @return
     */
    public CainMetadata getMetadata() {
        HashMap<String, String> hashMap = _getAllMetadata();
        if (hashMap != null) {
            CainMetadata mediaMetadata = new CainMetadata(hashMap);
            return mediaMetadata;
        }
        return null;
    }

    private native HashMap<String, String> _getAllMetadata();

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position by considering
     * the given option if possible, and returns it as a bitmap. This is
     * useful for generating a thumbnail for an input data source or just
     * obtain and display a frame at the given time position.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarantee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @param option a hint on how the frame is found. Use
     * {@link #OPTION_PREVIOUS_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp earlier than or the same as timeUs. Use
     * {@link #OPTION_NEXT_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp later than or the same as timeUs. Use
     * {@link #OPTION_CLOSEST_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp closest to or the same as timeUs. Use
     * {@link #OPTION_CLOSEST} if one wants to retrieve a frame that may
     * or may not be a sync frame but is closest to or the same as timeUs.
     * {@link #OPTION_CLOSEST} often has larger performance overhead compared
     * to the other options if there is no sync frame located at timeUs.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     */
    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option < OPTION_PREVIOUS_SYNC ||
                option > OPTION_CLOSEST) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }

        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;

        byte [] picture = _getFrameAtTime(timeUs, option);

        if (picture != null) {
            b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }

        return b;
    }

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position if possible,
     * and returns it as a bitmap. This is useful for generating a thumbnail
     * for an input data source. Call this method if one does not care
     * how the frame is found as long as it is close to the given time;
     * otherwise, please call {@link #getFrameAtTime(long, int)}.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarentee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     *
     * @see #getFrameAtTime(long, int)
     */
    public Bitmap getFrameAtTime(long timeUs) {
        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;

        byte [] picture = _getFrameAtTime(timeUs, OPTION_CLOSEST_SYNC);

        if (picture != null) {
            b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
        }

        return b;
    }

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame at any time position if possible,
     * and returns it as a bitmap. This is useful for generating a thumbnail
     * for an input data source. Call this method if one does not
     * care about where the frame is located; otherwise, please call
     * {@link #getFrameAtTime(long)} or {@link #getFrameAtTime(long, int)}
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     *
     * @see #getFrameAtTime(long)
     * @see #getFrameAtTime(long, int)
     */
    public Bitmap getFrameAtTime() {
        return getFrameAtTime(-1, OPTION_CLOSEST_SYNC);
    }

    private native byte[] _getFrameAtTime(long timeUs, int option);

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position by considering
     * the given option if possible, and returns it as a bitmap. This is
     * useful for generating a thumbnail for an input data source or just
     * obtain and display a frame at the given time position.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarantee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @param option a hint on how the frame is found. Use
     * {@link #OPTION_PREVIOUS_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp earlier than or the same as timeUs. Use
     * {@link #OPTION_NEXT_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp later than or the same as timeUs. Use
     * {@link #OPTION_CLOSEST_SYNC} if one wants to retrieve a sync frame
     * that has a timestamp closest to or the same as timeUs. Use
     * {@link #OPTION_CLOSEST} if one wants to retrieve a frame that may
     * or may not be a sync frame but is closest to or the same as timeUs.
     * {@link #OPTION_CLOSEST} often has larger performance overhead compared
     * to the other options if there is no sync frame located at timeUs.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     */
    public Bitmap getScaledFrameAtTime(long timeUs, int option, int width, int height) {
        if (option < OPTION_PREVIOUS_SYNC ||
                option > OPTION_CLOSEST) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }

        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;

        int rotate = Integer.valueOf(extractMetadata(METADATA_KEY_ROTAE));
        byte [] picture = _getScaledFrameAtTime(timeUs, option, width, height);

        if (picture != null) {

            if (rotate % 90 != 0) {
                return BitmapUtils.rotateBitmap(picture, rotate);
            } else {
                b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
            }
        }

        return b;
    }

    /**
     * Call this method after setDataSource(). This method finds a
     * representative frame close to the given time position if possible,
     * and returns it as a bitmap. This is useful for generating a thumbnail
     * for an input data source. Call this method if one does not care
     * how the frame is found as long as it is close to the given time;
     * otherwise, please call {@link #getFrameAtTime(long, int)}.
     *
     * @param timeUs The time position where the frame will be retrieved.
     * When retrieving the frame at the given time position, there is no
     * guarentee that the data source has a frame located at the position.
     * When this happens, a frame nearby will be returned. If timeUs is
     * negative, time position and option will ignored, and any frame
     * that the implementation considers as representative may be returned.
     *
     * @return A Bitmap containing a representative video frame, which
     *         can be null, if such a frame cannot be retrieved.
     *
     * @see #getFrameAtTime(long, int)
     */
    public Bitmap getScaledFrameAtTime(long timeUs, int width, int height) {
        Bitmap b = null;

        BitmapFactory.Options bitmapOptionsCache = new BitmapFactory.Options();
        //bitmapOptionsCache.inPreferredConfig = getInPreferredConfig();
        bitmapOptionsCache.inDither = false;

        int rotate = Integer.valueOf(extractMetadata(METADATA_KEY_ROTAE));
        byte [] picture = _getScaledFrameAtTime(timeUs, OPTION_CLOSEST_SYNC, width, height);
        if (picture != null) {
            if (rotate != 0) {
                return BitmapUtils.rotateBitmap(picture, rotate);
            } else {
                b = BitmapFactory.decodeByteArray(picture, 0, picture.length, bitmapOptionsCache);
            }
        }
        return b;
    }

    private native byte[] _getScaledFrameAtTime(long timeUs, int option, int width, int height);

    /**
     * Call this method after setDataSource(). This method finds the optional
     * graphic or album/cover art associated associated with the data source. If
     * there are more than one pictures, (any) one of them is returned.
     *
     * @return null if no such graphic is found.
     */
    public byte[] getEmbeddedPicture() {
        return getEmbeddedPicture(EMBEDDED_PICTURE_TYPE_ANY);
    }

    private native byte[] getEmbeddedPicture(int pictureType);

    /**
     * Call it when one is done with the object. This method releases the memory
     * allocated internally.
     */
    public native void release();
    private native void native_setup();
    private static native void native_init();

    private native final void native_finalize();

    @Override
    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }


    /**
     * Option used in method {@link #getFrameAtTime(long, int)} to get a
     * frame at a specified location.
     *
     * @see #getFrameAtTime(long, int)
     */
    /* Do not change these option values without updating their counterparts
     * in include/media/stagefright/MediaSource.h!
     */
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a sync (or key) frame associated with a data source that is located
     * right before or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_PREVIOUS_SYNC    = 0x00;
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a sync (or key) frame associated with a data source that is located
     * right after or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_NEXT_SYNC        = 0x01;
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a sync (or key) frame associated with a data source that is located
     * closest to (in time) or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_CLOSEST_SYNC     = 0x02;
    /**
     * This option is used with {@link #getFrameAtTime(long, int)} to retrieve
     * a frame (not necessarily a key frame) associated with a data source that
     * is located closest to or at the given time.
     *
     * @see #getFrameAtTime(long, int)
     */
    public static final int OPTION_CLOSEST          = 0x03;


    /**
     * reference from FFmpeg Metadata API:
     * http://ffmpeg.org/doxygen/trunk/group__metadata__api.html
     *
     * https://blog.csdn.net/weiyuefei/article/details/70171489
     */

    /**
     * The metadata key to retrieve the information about the album title
     * of the data source.
     */
    public static final String METADATA_KEY_ALBUM = "album";

    /**
     * The metadata key to retrieve the information about the performers or
     * artist associated with the data source.
     */
    public static final String METADATA_KEY_ALBUMARTIST = "album_artist";

    /**
     * The metadata key to retrieve the information about the artist of
     * the data source.
     */
    public static final String METADATA_KEY_ARTIST = "artist";

    /**
     * The metadata key to retrieve the any additional description of
     * the data source.
     */
    public static final String METADATA_KEY_COMMENT = "comment";

    /**
     * The metadata key to retrieve the information about the composer of
     * the data source.
     */
    public static final String METADATA_KEY_COMPOSER = "composer";

    /**
     * The metadata key to retrieve the name of copyright holder.
     */
    public static final String METADATA_KEY_COPYRIGHT = "copyright";

    /**
     * The metadata key to retrieve the date when the data source was created,
     * preferably in ISO 8601.
     */
    public static final String METADATA_KEY_CREATION_TIME = "creation_time";

    /**
     * The metadata key to retrieve the date when the data source was created
     * or modified.
     */
    public static final String METADATA_KEY_DATE = "date";

    /**
     * The metadata key to retrieve the numberic string that describes which
     * part of a set the audio data source comes from.
     */
    public static final String METADATA_KEY_DISC_NUMBER = "disc";

    /**
     * The metadata key to retrieve the name/settings of the software/hardware that produced
     * the data source.
     */
    public static final String METADATA_KEY_ENCODER = "encoder";

    /**
     * The metadata key to retrieve the person/group who created the data source.
     */
    public static final String METADATA_KEY_ENCODED_BY = "encoded_by";

    /**
     * The metadata key to retrieve the original name of the data source.
     */
    public static final String METADATA_KEY_FILENAME = "filename";

    /**
     * The metadata key to retrieve the content type or genre of the data
     * source.
     */
    public static final String METADATA_KEY_GENRE = "genre";

    /**
     * The metadata key to retrieve the language code of text tracks, if available.
     * If multiple text tracks present, the return value will look like:
     * "eng:chi"
     */
    public static final String METADATA_KEY_LANGUAGE = "language";

    /**
     * The metadata key to retrieve the artist who performed the work, if different from artist.
     * E.g for "Also sprach Zarathustra", artist would be "Richard Strauss" and performer "London
     * Philharmonic Orchestra".
     */
    public static final String METADATA_KEY_PERFORMER = "performer";

    /**
     * The metadata key to retrieve the name of the label/publisher.
     */
    public static final String METADATA_KEY_PUBLISHER = "publisher";

    /**
     * The metadata key to retrieve the name of the service in broadcasting (channel name).
     */
    public static final String METADATA_KEY_SERVICE_NAME = "service_name";

    /**
     * The metadata key to retrieve the name of the service provider in broadcasting.
     */
    public static final String METADATA_KEY_SERVICE_PROVIDER = "service_provider";

    /**
     * The metadata key to retrieve the data source title.
     */
    public static final String METADATA_KEY_TITLE = "title";

    /**
     * The metadata key to retrieve the number of this work in the set, can be in form current/total.
     */
    public static final String METADATA_KEY_TRACK = "track";

    /**
     * The metadata key to retrieve the average bitrate (in bits/sec), if available.
     */
    public static final String METADATA_KEY_BITRATE = "bitrate";

    /**
     * The metadata key to retrieve the description when the data source.
     */
    public static final String METADATA_KEY_DESCRIPTION = "description";
    /**
     * The metadata key to retrieve the year when the data source was created
     * or modified.
     */
    public static final String METADATA_KEY_YEAR = "year";

    /**
     * The metadata key to retrieve the playback duration of the data source.
     */
    public static final String METADATA_KEY_DURATION = "duration";

    /**
     * The metadata key to retrieve the audio codec of the work.
     */
    public static final String METADATA_KEY_AUDIO_CODEC = "audio_codec";

    /**
     * The metadata key to retrieve the video codec of the work.
     */
    public static final String METADATA_KEY_VIDEO_CODEC = "video_codec";

    /**
     * The metadata key to retrieve the main creator of the work.
     */
    public static final String METADATA_KEY_ICY_METADATA = "icy_metadata";

    /**
     * The metadata key to retrieve the video rotation angle in degrees, if available.
     * The video rotation angle may be 0, 90, 180, or 270 degrees.
     */
    public static final String METADATA_KEY_ROTAE = "rotate";

    /**
     * The metadata key to retrieve the average framerate (in frames/sec), if available.
     */
    public static final String METADATA_KEY_FRAME_RATE = "frame_rate";

    /**
     * The metadata key to retrieve the chapter start time in milliseconds.
     */
    public static final String METADATA_KEY_CHAPTER_START = "chapter_start";

    /**
     * The metadata key to retrieve the chapter end time in milliseconds.
     */
    public static final String METADATA_KEY_CHAPTER_END = "chapter_end";

    /**
     * The metadata key to retrieve the chapter count.
     */
    public static final String METADATA_KEY_CHAPTER_COUNT = "chapter_count";

    /**
     * The metadata key to retrieve the file size in bytes.
     */
    public static final String METADATA_KEY_FILE_SIZE = "file_size";

    /**
     * If the media contains video, this key retrieves its width.
     */
    public static final String METADATA_KEY_VIDEO_WIDTH = "video_width";

    /**
     * If the media contains video, this key retrieves its height.
     */
    public static final String METADATA_KEY_VIDEO_HEIGHT = "video_height";

}
