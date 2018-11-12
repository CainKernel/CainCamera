attribute vec4 aPosition;           // 图像顶点坐标
attribute vec4 aTextureCoord;       // 图像纹理坐标

varying vec2 textureCoordinate;     // 图像纹理坐标
varying vec2 maskCoordinate;        // 遮罩纹理坐标

void main() {
    gl_Position = aPosition;
    // 原图纹理坐标，用顶点来计算
    textureCoordinate = aPosition.xy * 0.5 + 0.5;
    // 遮罩纹理坐标，用传进来的坐标值计算
    maskCoordinate = aTextureCoord.xy;
}