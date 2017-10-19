package com.cgfay.caincamera.multimedia;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.cgfay.caincamera.core.VideoRecordDrawer;
import com.cgfay.caincamera.gles.EglCore;

import java.io.IOException;

/**
 * 视频编码器
 * Created by cain on 2017/10/14.
 */

public class VideoMediaEncoder extends MediaEncoder {

    private static final boolean VERBOSE = false;
    private static final String TAG = "VideoMediaEncoder";

    private static final String MIME_TYPE = "video/avc";

    // 帧率
    private static final int FRAME_RATE = 25;
    // 影响视频质量
    private static final float BPP = 0.25f;
    // I 帧间隔
    private static final int I_FRAME_INTERVAL = 10;

    // 视频宽高
    private final int mWidth;
    private final int mHeight;

    private Surface mSurface;

    private RecordDrawer mRenderDrawer;

    public VideoMediaEncoder(MediaEncoderMuxer muxer, EncoderListener listener,
                             int width, int height) {
        super(muxer, listener);
        mWidth = width;
        mHeight = height;
        mRenderDrawer = new VideoRecordDrawer(TAG, mWidth, mHeight);
    }

    @Override
    public boolean frameAvailable() {
        boolean result = super.frameAvailable();
        if (result) {
            // 绘制
            videoDraw();
        }
        return result;
    }

    /**
     * 绘制
     */
    private void videoDraw() {
        mRenderDrawer.sendDraw();
    }


    /**
     * 设置OpenGLES 共享上下文
     * @param eglCore
     * @param tex_id
     */
    public void setEglContext(final EglCore eglCore, final int tex_id) {
        mRenderDrawer.setEglContext(eglCore, tex_id, mSurface, true);
    }

    @Override
    protected void prepare() throws IOException {
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;

        MediaCodecInfo info = getVideoCodecInfo(MIME_TYPE);
        if (info == null) {
            return;
        }

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, calculateBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
        if (mListener != null) {
            mListener.onPrepared(this);
        }
    }

    @Override
    protected void release() {

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }

        if (mRenderDrawer != null) {
            mRenderDrawer.release();
            mRenderDrawer = null;
        }
        super.release();
    }

    /**
     * 计算比特率
     * @return
     */
    private int calculateBitRate() {
        int bitRate = (int)(BPP * FRAME_RATE * mWidth * mHeight);
        return bitRate;
    }


    /**
     * 获取信息
     * @param mimeType
     * @return
     */
    protected static final MediaCodecInfo getVideoCodecInfo(final String mimeType) {
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            // 跳过解码的
            if (!codecInfo.isEncoder()){
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (getColorFormat(codecInfo, mimeType) > 0) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 或者颜色
     * @param info
     * @param mimeType
     * @return
     */
    protected static final int getColorFormat(MediaCodecInfo info, String mimeType) {
        int result = 0;
        MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = info.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int format;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            format = caps.colorFormats[i];
            if (isRecognizedVideoFormat(format)) {
                result = format;
                break;
            }
        }
        if (result == 0) {
            Log.e(TAG, "error color format for " + info.getName() + " / " + mimeType);
        }
        return result;
    }

    protected static int[] mRecognizeFormats;

    static {
        mRecognizeFormats = new int[] {
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static final boolean isRecognizedVideoFormat(final int format) {
        int n = mRecognizeFormats != null ? mRecognizeFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (format == mRecognizeFormats[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void endOfInputStream() {
        mMediaCodec.signalEndOfInputStream();
        mIsEOS = true;
    }

    /**
     * 获取视频录制绘制器
     * @return
     */
    public RecordDrawer getVideoDrawer() {
        return mRenderDrawer;
    }
}
