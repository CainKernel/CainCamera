// 边框模糊
precision mediump float;
uniform sampler2D inputTexture; // 原始图像
uniform sampler2D blurTexture;  // 经过高斯模糊的图像
varying vec2 textureCoordinate;

uniform float blurOffsetX;  // x轴边框模糊偏移值
uniform float blurOffsetY;  // y轴边框模糊偏移值

void main() {
    // uv坐标
    vec2 uv = textureCoordinate.xy;
    vec4 color;
    // 中间为原图，需要缩小
    if (uv.x >= blurOffsetX && uv.x <= 1.0 - blurOffsetX
        && uv.y >= blurOffsetY && uv.y <= 1.0 - blurOffsetY) {
        // 内部UV缩放值
        float scaleX = 1.0 / (1.0 - 2.0 * blurOffsetX);
        float scaleY = 1.0 / (1.0 - 2.0 * blurOffsetY);
        // 计算出内部新的UV坐标
        vec2 newUV = vec2((uv.x - blurOffsetX) * scaleX, (uv.y - blurOffsetY) * scaleY);
        color = texture2D(inputTexture, newUV);
    } else { // 边框部分使用高斯模糊的图像
        color = texture2D(blurTexture, uv);
    }
    gl_FragColor = color;
}
