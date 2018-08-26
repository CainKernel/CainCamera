package com.cgfay.filterlibrary.multimedia;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MediaAudioEncoder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class MediaAudioEncoder extends MediaEncoder {
	private static final boolean DEBUG = false;	// TODO set false on release
	private static final String TAG = "MediaAudioEncoder";

	private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;	// 44.1[KHz] is only setting guaranteed to be available on all devices.
    private static final int BIT_RATE = 64000;
	public static final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
	public static final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

    private AudioThread mAudioThread = null;

	public MediaAudioEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener) {
		super(muxer, listener, false);
	}

	@Override
	protected void prepare() throws IOException {
		if (DEBUG) Log.d(TAG, "prepare:");
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        // prepare MediaCodec for AAC encoding of audio data from inernal mic.
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
		if (DEBUG) Log.i(TAG, "selected codec: " + audioCodecInfo.getName());

        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, 1);
		audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
		audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
		audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
		if (DEBUG) Log.i(TAG, "format: " + audioFormat);
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        if (DEBUG) Log.i(TAG, "prepare finishing");
        if (mListener != null) {
        	try {
        		mListener.onPrepared(this);
        	} catch (final Exception e) {
        		Log.e(TAG, "prepare:", e);
        	}
        }
	}

    @Override
	protected void startRecording() {
		super.startRecording();
		// create and execute audio capturing thread using internal mic
		if (mAudioThread == null) {
	        mAudioThread = new AudioThread();
			mAudioThread.start();
		}
		if (mListener != null) {
			try {
				mListener.onStarted(this);
			} catch (final Exception e) {
				Log.e(TAG, "prepare:", e);
			}
		}
	}

	@Override
	void pauseRecording(boolean isPause) {
		super.pauseRecording(isPause);
		if (mAudioThread != null) {
			if (isPause) {
				mAudioThread.stopRecording();
			} else {
				mAudioThread.startRecording();
			}
		}
	}

	@Override
    protected void release() {
		if (mAudioThread != null) {
			mAudioThread.stopRecording();
			mAudioThread = null;
		}
		super.release();
    }

	private static final int[] AUDIO_SOURCES = new int[] {
		MediaRecorder.AudioSource.MIC,
		MediaRecorder.AudioSource.DEFAULT,
		MediaRecorder.AudioSource.CAMCORDER,
		MediaRecorder.AudioSource.VOICE_COMMUNICATION,
		MediaRecorder.AudioSource.VOICE_RECOGNITION,
	};

	/**
	 * Thread to capture audio data from internal mic as uncompressed 16bit PCM data
	 * and write them to the MediaCodec encoder
	 */
    private class AudioThread extends Thread {

    	private Object mSync = new Object();
    	private volatile boolean mAudioStarted = false;
    	private WeakReference<AudioRecord> mWeakRecorder;

    	@Override
    	public void run() {
    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    		try {
				final int min_buffer_size = AudioRecord.getMinBufferSize(
						SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
				if (buffer_size < min_buffer_size)
					buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

				AudioRecord audioRecord = null;
				for (final int source : AUDIO_SOURCES) {
					try {
						audioRecord = new AudioRecord(
							source, SAMPLE_RATE,
							AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
	    	            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
	    	            	audioRecord = null;
					} catch (final Exception e) {
						audioRecord = null;
					}
					if (audioRecord != null) break;
				}
				if (audioRecord != null) {
					mWeakRecorder = new WeakReference<AudioRecord>(audioRecord);
		            try {
						if (mIsCapturing) {
		    				if (DEBUG) {
		    					Log.d(TAG, "AudioThread:start audio recording");
							}
							final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
			                int readBytes;
			                synchronized (mSync) {
								audioRecord.startRecording();
								mAudioStarted = true;
							}
			                try {
					    		for (; mIsCapturing && !mRequestStop && !mIsEOS && mAudioStarted;) {
					    			// read audio data from internal mic
									if(isPause){
										continue;
									}
									buf.clear();
					    			readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
					    			if (readBytes > 0 ) {
					    			    // set audio data to encoder
										buf.position(readBytes);
										buf.flip();
					    				encode(buf, readBytes, getPTSUs());
					    				frameAvailableSoon();
					    			}
					    		}
			    				frameAvailableSoon();
			                } finally {
			                	synchronized (mSync) {
									mAudioStarted = false;
									audioRecord.stop();
								}
			                }
		            	}
		            } finally {
		            	synchronized (mSync) {
							mAudioStarted = false;
							audioRecord.release();
							if (mWeakRecorder != null) {
								mWeakRecorder.clear();
								mWeakRecorder = null;
							}
						}
		            }
				} else {
					Log.e(TAG, "failed to initialize AudioRecord");
				}
    		} catch (final Exception e) {
    			Log.e(TAG, "AudioThread#run", e);
    		}
			if (DEBUG) Log.d(TAG, "AudioThread:finished");
    	}

		/**
		 * 开始录音
		 */
		public void startRecording() {
			if (mAudioStarted) {
				return;
			}
			synchronized (mSync) {
				if (mWeakRecorder != null && mWeakRecorder.get() != null) {
					mWeakRecorder.get().startRecording();
					mAudioStarted = true;
				}
			}
		}

		/**
		 * 停止录音
		 */
		public void stopRecording() {
			if (!mAudioStarted) {
				return;
			}
			synchronized (mSync) {
				if (mWeakRecorder != null && mWeakRecorder.get() != null) {
					mWeakRecorder.get().stop();
					mAudioStarted = false;
				}
			}
		}

    }

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
    	if (DEBUG) Log.d(TAG, "selectAudioCodec:");

    	MediaCodecInfo result = null;
    	// get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
LOOP:	for (int i = 0; i < numCodecs; i++) {
        	final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
            	if (DEBUG) Log.i(TAG, "supportedType:" + codecInfo.getName() + ",MIME=" + types[j]);
                if (types[j].equalsIgnoreCase(mimeType)) {
                	if (result == null) {
                		result = codecInfo;
               			break LOOP;
                	}
                }
            }
        }
   		return result;
    }

}
