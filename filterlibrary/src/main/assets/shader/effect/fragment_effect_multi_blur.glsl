// 仿抖音模糊分屏
precision mediump float;
uniform sampler2D inputTexture; // 原始图像
uniform sampler2D blurTexture;  // 经过高斯模糊的图像
varying vec2 textureCoordinate;

uniform float blurOffsetY;  // y轴边框模糊偏移值
uniform float scale;        // 模糊部分的缩放倍数

void main() {
    // uv坐标
    vec2 uv = textureCoordinate.xy;
    vec4 color;
    // 中间为原图部分
    if (uv.y >= blurOffsetY && uv.y <= 1.0 - blurOffsetY) {
        color = texture2D(inputTexture, uv);
    } else { // 边框部分使用高斯模糊的图像
        vec2 center = vec2(0.5, 0.5);
        uv -= center;
        uv = uv / scale;
        uv += center;
        color = texture2D(blurTexture, uv);
    }
    gl_FragColor = color;
}