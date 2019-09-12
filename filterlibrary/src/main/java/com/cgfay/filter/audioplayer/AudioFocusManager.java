package com.cgfay.filter.audioplayer;

import android.content.Context;
import android.media.AudioManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 音频对焦管理器
 */
public class AudioFocusManager {

    // 音频管理器
    private AudioManager mAudioManager;

    // 音频管理器状态
    private int mState = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;

    // 监听器集合
    private Set<AudioFocusChangeListener> mListenerSet = new HashSet<>();

    private static class ManagerHolder {
        public static AudioFocusManager instance = new AudioFocusManager();
    }

    private AudioFocusManager() {
    }

    public static AudioFocusManager getInstance() {
        return ManagerHolder.instance;
    }

    /**
     * 初始化管理器
     * @param context
     */
    public void init(Context context) {
        mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 释放管理器
     */
    public void release() {
        mState = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        if (mListenerSet != null) {
            mListenerSet.clear();
        }
        mAudioManager.abandonAudioFocus(mFocusChangeListener);
        mAudioManager = null;
    }

    /**
     * 添加音频状态监听器
     * @param listener
     * @return
     */
    public AudioFocusManager addAudioFocusChangeListener(AudioFocusChangeListener listener) {
        mListenerSet.add(listener);
        return this;
    }

    /**
     * 移除音频状态监听器
     * @param listener
     * @return
     */
    public AudioFocusManager removeAudioFocusChangeListener(AudioFocusChangeListener listener) {
        mListenerSet.remove(listener);
        return this;
    }

    /**
     * 是否处于对焦装填
     * @return
     */
    public boolean isFocused() {
        return mState == AudioManager.AUDIOFOCUS_GAIN;
    }

    /**
     * 请求音频对焦
     * @return
     */
    public synchronized AudioFocusManager requestAudioFocus() {
        if (mState != 1) {
            mState = mAudioManager.requestAudioFocus(mFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        onFocusChanged();
        return this;
    }

    /**
     * 对焦状态发生变化时调用
     */
    private void onFocusChanged() {
        HashSet<AudioFocusChangeListener> hashSet;
        synchronized (this) {
            hashSet = new HashSet<>(mListenerSet);
        }
        for (Iterator iterator = hashSet.iterator(); iterator.hasNext();) {
            AudioFocusChangeListener listener = (AudioFocusChangeListener) iterator.next();
            listener.onFocusChange(mState);
        }
    }

    /**
     * 对焦状态监听器
     */
    private AudioManager.OnAudioFocusChangeListener mFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAudioManager.abandonAudioFocus(mFocusChangeListener);
            }
            onFocusChanged();
        }
    };

}
