package com.cgfay.filterlibrary.glfilter.model;

/**
 * 人脸关键点
 */
public class FacePoints {
    // 关键点索引是否发生了改变
    public boolean indexChanged;
    // 关键点个数
    public int points;
    // 索引坐标
    public short[] indices;
    // 顶点坐标
    public float[] vertices;
    // 纹理坐标
    public float[] textures;

    public FacePoints() {
        indexChanged = false;
        points = 0;
        indices = null;
        vertices = null;
        textures = null;
    }
}
