package com.cgfay.filter.glfilter.effect.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态特效数据
 * 备注：特效是跟时间有关的滤镜
 */
public class DynamicEffectData {

    public String name;                             // 特效名称
    public String vertexShader;                     // vertex shader 名称
    public String fragmentShader;                   // fragment shader 名称
    public List<UniformData> uniformDataList;       // 特效的统一变量列表
    public List<UniformSampler> uniformSamplerList; // 特效的统一变量纹理列表
    public boolean texelSize;                       // 是否存在纹理宽高的统一变量
    public int duration;                            // 特效刷新时间间隔

    public DynamicEffectData() {
        uniformDataList = new ArrayList<>();
        uniformSamplerList = new ArrayList<>();
    }

    /**
     * 特效统一变量数值对象
     * 绑定统一变量以及统一变量数值列表
     */
    public static class UniformData {
        public String uniform;  // 统一变量名称
        public float[] value;   // 与统一变量绑定的数值

        public UniformData(String uniform, float[] value) {
            this.uniform = uniform;
            this.value = value;
        }
    }

    /**
     * 特效统一变量采样对象
     * 绑定的统一变量以及统一变量纹理图片
     */
    public static class UniformSampler {

        public String uniform;  // 统一变量字段
        public String value;    // 与统一变量绑定的纹理图片

        public UniformSampler(String uniform, String value) {
            this.uniform = uniform;
            this.value = value;
        }
    }
}
