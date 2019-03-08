// 仿抖音黑白三屏特效
precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

uniform float scale;          // 黑白部分缩放倍数

void main() {
    highp vec2 uv = textureCoordinate;
    vec4 color;
    if (uv.y < 1.0 / 3.0) {
        // 缩放
        vec2 center = vec2(0.5, 0.5);
        uv -= center;
        uv = uv / scale;
        uv += center;
        color = texture2D(inputTexture, uv);
        // 黑白图片
        float gray = 0.3 * color.r + 0.59 * color.g + 0.11 * color.b;
        color = vec4(gray, gray, gray, 1.0);
    } else if (uv.y > 2.0 / 3.0) {
        color = texture2D(inputTexture, uv);
        // 缩放
        vec2 center = vec2(0.5, 0.5);
        uv -= center;
        uv = uv / scale;
        uv += center;
        color = texture2D(inputTexture, uv);
        // 黑白图片
        float gray = 0.3 * color.r + 0.59 * color.g + 0.11 * color.b;
        color = vec4(gray, gray, gray, 1.0);
    } else {
        color = texture2D(inputTexture, uv);
    }
    gl_FragColor = color;
}