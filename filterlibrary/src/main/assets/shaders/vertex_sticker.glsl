uniform mat4 uMVPMatrix;        // 总变换矩阵
uniform mat4 uTexMatrix;        // 输入图像的缩放矩阵
attribute vec4 aPosition;       // 输入图像的位置坐标
attribute vec4 aTextureCoord;   // 输入图像纹理坐标

attribute vec4 aMipmapCoord;    // 贴纸在视锥体空间中的垂直于z轴的假想坐标

uniform int centerX;          // 贴纸处于屏幕的中心位置x
uniform int centerY;          // 贴纸处于屏幕的中心位置y

varying vec2 textureCoordinate; // 输出texture坐标
varying vec2 mipmapCoordinate;  // 输出mipmap坐标

/**
 * 计算贴纸投影到屏幕的UV坐标
 */
vec2 calculateUVPosition(vec4 modelPosition, mat4 mvpMatrix) {
    vec4 tmp = vec4(modelPosition);
    tmp = mvpMatrix * tmp; // gl_Position
    tmp /= tmp.w; // 经过这个步骤，tmp就是归一化标准坐标了.
    tmp = tmp * 0.5 + vec4(0.5f, 0.5f, 0.5f, 0.5f); // NDC坐标
    tmp += vec4(centerx, centerY, 0.5f, 0.5f);// 平移到贴纸中心
    return vec2(tmp.x, tmp.y); // 屏幕的UV坐标
}

void main() {
    // texture的坐标
    textureCoordinate = (uTexMatrix * aTextureCoord).xy;;
    // 变换矩阵
    mipmapCoordinate = calculateUVPosition(aMipmapCoord, uMVPMatrix);
    gl_Position = aPosition;
}