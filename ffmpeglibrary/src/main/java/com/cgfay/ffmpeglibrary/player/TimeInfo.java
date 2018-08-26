package com.cgfay.ffmpeglibrary.player;

public class TimeInfo {
    private int current;    // 当前播放时间
    private int duration;   // 总时长

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "time info: current = " + current + ", duraion = " + duration;
    }
}
