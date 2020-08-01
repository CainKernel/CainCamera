package com.cgfay.cavfoundation;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频混音处理类
 */
public class AVAudioMix {

    /**
     * 音频输入参数对象
     */
    private List<AVAudioMixInputParameters> mInputParameters = new ArrayList<>();

    public AVAudioMix() {

    }

    /**
     * 获取音频输入参数列表
     */
    public List<AVAudioMixInputParameters> getInputParameters() {
        return mInputParameters;
    }

    /**
     * 设置音频输入参数列表
     */
    public void setInputParameters(List<AVAudioMixInputParameters> inputParameters) {
        mInputParameters = inputParameters;
    }
}
