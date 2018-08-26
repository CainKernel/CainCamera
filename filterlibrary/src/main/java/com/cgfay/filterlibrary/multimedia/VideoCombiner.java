package com.cgfay.filterlibrary.multimedia;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

@TargetApi(18)
public class VideoCombiner {

    private static final String TAG = "VideoCombiner";
    private static final boolean VERBOSE = true;

    private static final int MAX_BUFF_SIZE = 1048576;

    private List<String> mVideoList;
    private String mDestPath;

    private MediaMuxer mMuxer;
    private ByteBuffer mReadBuf;
    private int mOutAudioTrackIndex;
    private int mOutVideoTrackIndex;
    private MediaFormat mAudioFormat;
    private MediaFormat mVideoFormat;

    private CombineListener mCombineListener;

    public interface CombineListener {

        /**
         * 合并开始
         */
        void onCombineStart();

        /**
         * 合并过程
         * @param current 当前合并的视频
         * @param sum   合并视频总数
         */
        void onCombineProcessing(int current, int sum);

        /**
         * 合并结束
         * @param success   是否合并成功
         */
        void onCombineFinished(boolean success);
    }



    public VideoCombiner(List<String> videoList, String destPath, CombineListener listener) {
        mVideoList = videoList;
        mDestPath = destPath;
        mCombineListener = listener;
        mReadBuf = ByteBuffer.allocate(MAX_BUFF_SIZE);
    }

    /**
     * 合并视频
     * @return
     */
    @SuppressLint("WrongConstant")
    public void combineVideo() {
        boolean hasAudioFormat = false;
        boolean hasVideoFormat = false;
        Iterator videoIterator = mVideoList.iterator();

        // 开始合并
        if (mCombineListener != null) {
            mCombineListener.onCombineStart();
        }

        // MediaExtractor拿到多媒体信息，用于MediaMuxer创建文件
        while (videoIterator.hasNext()) {
            String videoPath = (String) videoIterator.next();
            MediaExtractor extractor = new MediaExtractor();
            try {
                extractor.setDataSource(videoPath);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            int trackIndex;
            if (!hasVideoFormat) {
                trackIndex = selectTrack(extractor, "video/");
                if(trackIndex < 0) {
                    Log.e(TAG, "No video track found in " + videoPath);
                } else {
                    extractor.selectTrack(trackIndex);
                    mVideoFormat = extractor.getTrackFormat(trackIndex);
                    hasVideoFormat = true;
                }
            }

            if (!hasAudioFormat) {
                trackIndex = selectTrack(extractor, "audio/");
                if(trackIndex < 0) {
                    Log.e(TAG, "No audio track found in " + videoPath);
                } else {
                    extractor.selectTrack(trackIndex);
                    mAudioFormat = extractor.getTrackFormat(trackIndex);
                    hasAudioFormat = true;
                }
            }

            extractor.release();

            if (hasVideoFormat && hasAudioFormat) {
                break;
            }
        }

        try {
            mMuxer = new MediaMuxer(mDestPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hasVideoFormat) {
            mOutVideoTrackIndex = mMuxer.addTrack(mVideoFormat);
        }

        if (hasAudioFormat) {
            mOutAudioTrackIndex = mMuxer.addTrack(mAudioFormat);
        }
        mMuxer.start();

        // MediaExtractor遍历读取帧，MediaMuxer写入帧，并记录帧信息
        long ptsOffset = 0L;
        Iterator trackIndex = mVideoList.iterator();
        int currentVideo = 0;
        boolean combineResult = true;
        while (trackIndex.hasNext()) {
            // 监听当前合并第几个视频
            currentVideo++;
            if (mCombineListener != null) {
                mCombineListener.onCombineProcessing(currentVideo, mVideoList.size());
            }

            String videoPath = (String) trackIndex.next();
            boolean hasVideo = true;
            boolean hasAudio = true;

            // 选择视频轨道
            MediaExtractor videoExtractor = new MediaExtractor();
            try {
                videoExtractor.setDataSource(videoPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int inVideoTrackIndex = selectTrack(videoExtractor, "video/");
            if(inVideoTrackIndex < 0) {
                hasVideo = false;
            }
            videoExtractor.selectTrack(inVideoTrackIndex);

            // 选择音频轨道
            MediaExtractor audioExtractor = new MediaExtractor();
            try {
                audioExtractor.setDataSource(videoPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int inAudioTrackIndex = selectTrack(audioExtractor, "audio/");
            if (inAudioTrackIndex < 0) {
                hasAudio = false;
            }
            audioExtractor.selectTrack(inAudioTrackIndex);

            // 如果存在视频轨道和音频轨道都不存在，则合并失败，文件出错
            if (!hasVideo && !hasAudio) {
                combineResult = false;
                videoExtractor.release();
                audioExtractor.release();
                break;
            }

            boolean bMediaDone = false;
            long presentationTimeUs = 0L;
            long audioPts = 0L;
            long videoPts = 0L;

            while (!bMediaDone) {
                // 判断是否存在音视频
                if(!hasVideo && !hasAudio) {
                    break;
                }

                int outTrackIndex;
                MediaExtractor extractor;
                int currentTrackIndex;
                if ((!hasVideo || audioPts - videoPts <= 50000L) && hasAudio) {
                    currentTrackIndex = inAudioTrackIndex;
                    outTrackIndex = mOutAudioTrackIndex;
                    extractor = audioExtractor;
                } else {
                    currentTrackIndex = inVideoTrackIndex;
                    outTrackIndex = mOutVideoTrackIndex;
                    extractor = videoExtractor;
                }

                if (VERBOSE) {
                    Log.d(TAG, "currentTrackIndex： " + currentTrackIndex
                            + ", outTrackIndex: " + outTrackIndex);
                }

                mReadBuf.rewind();
                // 读取数据帧
                int frameSize = extractor.readSampleData(mReadBuf, 0);
                if (frameSize < 0) {
                    if (currentTrackIndex == inVideoTrackIndex) {
                        hasVideo = false;
                    } else if (currentTrackIndex == inAudioTrackIndex) {
                        hasAudio = false;
                    }
                } else {
                    if (extractor.getSampleTrackIndex() != currentTrackIndex) {
                        Log.e(TAG, "got sample from track "
                                + extractor.getSampleTrackIndex()
                                + ", expected " + currentTrackIndex);
                    }

                    // 读取帧的pts
                    presentationTimeUs = extractor.getSampleTime();
                    if (currentTrackIndex == inVideoTrackIndex) {
                        videoPts = presentationTimeUs;
                    } else {
                        audioPts = presentationTimeUs;
                    }

                    // 帧信息
                    BufferInfo info = new BufferInfo();
                    info.offset = 0;
                    info.size = frameSize;
                    info.presentationTimeUs = ptsOffset + presentationTimeUs;

                    if ((extractor.getSampleFlags() & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                        info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                    }
                    mReadBuf.rewind();
                    if (VERBOSE) {
                        Log.d(TAG, String.format("write sample track %d, size %d, pts %d flag %d",
                                Integer.valueOf(outTrackIndex),
                                Integer.valueOf(info.size),
                                Long.valueOf(info.presentationTimeUs),
                                Integer.valueOf(info.flags))
                        );
                    }
                    // 将读取到的数据写入文件
                    mMuxer.writeSampleData(outTrackIndex, mReadBuf, info);
                    extractor.advance();
                }
            }

            // 当前文件最后一帧的PTS，用作下一个视频的pts
            ptsOffset += videoPts > audioPts ? videoPts : audioPts;
            // 当前文件最后一帧和下一帧的间隔差40ms，默认录制25fps的视频，帧间隔时间就是40ms
            // 但由于使用MediaCodec录制完之后，后面又写入了一个OES的帧，导致前面解析的时候会有时间差
            // 这里设置10ms效果比40ms的要好些。
            ptsOffset += 10000L;

            if (VERBOSE) {
                Log.d(TAG, "finish one file, ptsOffset " + ptsOffset);
            }

            // 释放资源
            videoExtractor.release();
            audioExtractor.release();
        }

        // 释放复用器
        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
            } catch (Exception e) {
                Log.e(TAG, "Muxer close error. No data was written");
            } finally {
                mMuxer = null;
            }
        }

        if (VERBOSE) {
            Log.d(TAG, "video combine finished");
        }

        // 合并结束
        if (mCombineListener != null) {
            mCombineListener.onCombineFinished(combineResult);
        }
    }

    /**
     *  选择轨道
     * @param extractor     MediaExtractor
     * @param mimePrefix    音轨还是视轨
     * @return
     */
    private int selectTrack(MediaExtractor extractor, String mimePrefix) {
        // 获取轨道总数
        int numTracks = extractor.getTrackCount();
        // 遍历查找包含mimePrefix的轨道
        for(int i = 0; i < numTracks; ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString("mime");
            if (mime.startsWith(mimePrefix)) {
                return i;
            }
        }

        return -1;
    }
}
