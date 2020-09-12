uniform mat4 uMVPMatrix;        // 变换矩阵，左右同框需要用来处理缩放平移等变换
attribute vec4 aPosition;       // 输入图像顶点坐标

attribute vec4 aTextureCoord;   // 输入图像纹理坐标
varying vec2 textureCoordinate; // 输出图像纹理坐标

void main() {
    gl_Position = uMVPMatrix * aPosition;
    textureCoordinate = aTextureCoord.xy;
}