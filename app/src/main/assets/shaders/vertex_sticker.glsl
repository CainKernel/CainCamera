uniform mat4 uMVPMatrix;        // 变换矩阵
attribute vec4 aPosition;       // 位置坐标
attribute vec2 aTextureCoord;   // 原始纹理坐标
attribute vec2 aMipmapCoord;    // 贴图坐标

varying vec2 textureCoordinate; // 输出texture坐标
varying vec2 mipmapCoordinate;  // 输出mipmap坐标

void main() {
    // texture的坐标
    textureCoordinate = aTextureCoord;
    mipmapCoordinate = aMipmapCoord;
    gl_Position = uMVPMatrix * aPosition;
}