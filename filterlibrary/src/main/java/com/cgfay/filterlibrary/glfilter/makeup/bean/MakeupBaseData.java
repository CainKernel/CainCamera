package com.cgfay.filterlibrary.glfilter.makeup.bean;

/**
 * 彩妆数据基类
 */
public class MakeupBaseData {
    // 彩妆类型
    public MakeupType makeupType;
    // 彩妆名称
    public String name;
    // 彩妆id，彩妆id主要是方便预设一组彩妆以及提供单独更改某个彩妆的功能，方便添加彩妆保存等功能
    public String id;
    // 彩妆默认强度
    public float strength;

}
