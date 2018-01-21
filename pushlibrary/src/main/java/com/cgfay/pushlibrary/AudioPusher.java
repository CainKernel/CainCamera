package com.cgfay.pushlibrary;

import java.lang.ref.WeakReference;

/**
 * 音频推流器
 * Created by cain on 2018/1/21.
 */

public class AudioPusher implements IAudioCallback {
    static final int SampleRate = 44100;
    private static final int channel = 1;

    private AudioRecorder mAudioRecorder;

    private final WeakReference<RtmpPusher> mWeakPusher;

    public AudioPusher(RtmpPusher pusher) {
        mWeakPusher = new WeakReference<RtmpPusher>(pusher);
    }

    public void start() {
        if (mWeakPusher != null && mWeakPusher.get() != null) {
            mWeakPusher.get().initAudio(SampleRate, channel);
        }
        mAudioRecorder = new AudioRecorder(this);
        new Thread(mAudioRecorder).start();
    }

    public void stop() {
        if (mAudioRecorder != null) {
            mAudioRecorder.stopRecord();
        }
        mWeakPusher.clear();
    }

    @Override
    public void onAudioError(int what, String message) {

    }

    @Override
    public void receiveAudioData(byte[] sampleBuffer, int len) {
        if (mWeakPusher.get() != null) {
            mWeakPusher.get().pushPCM(sampleBuffer);
        }
    }
}
