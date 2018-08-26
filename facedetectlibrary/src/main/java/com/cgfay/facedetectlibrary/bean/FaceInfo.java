package com.cgfay.facedetectlibrary.bean;

/**
 * 人脸关键点信息
 */
public class FaceInfo {
    // 性别
    public static final int GENDER_MAN = 0;
    public static final int GENDER_WOMAN = 1;
    // 置信度
    public float confidence;
    // 俯仰角(绕x轴旋转)
    public float pitch;
    // 偏航角(绕y轴旋转)
    public float yaw;
    // 翻滚角(绕z轴旋转)
    public float roll;
    // 年龄
    public float age;
    // 性别
    public int gender;
    // 顶点坐标
    public float[] vertexPoints;
    // 纹理坐标
    public float[] texturePoints;
    // 笛卡尔坐标
    public float[] cartesianPoint;
}
