attribute vec4 aPosition;       // 图像顶点坐标
attribute vec4 aTextureCoord;   // 遮罩纹理坐标，这里是复用了原来的图像纹理坐标

varying vec2 textureCoordinate;
varying vec2 maskCoordinate;

void main() {
    gl_Position = aPosition;
    maskCoordinate = aTextureCoord.xy;
    // 用顶点坐标来处理纹理坐标
    textureCoordinate = aPosition.xy * 0.5 + 0.5;
}