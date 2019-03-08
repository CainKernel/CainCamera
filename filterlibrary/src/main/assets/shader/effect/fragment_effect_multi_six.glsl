// 仿抖音六屏特效
precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    highp vec2 uv = textureCoordinate;
    // 左右分三屏
    if (uv.x <= 1.0 / 3.0) {
        uv.x = uv.x + 1.0 / 3.0;
    } else if (uv.x >= 2.0 / 3.0) {
        uv.x = uv.x - 1.0 / 3.0;
    }
    // 上下分两屏，保留 0.25 ~ 0.75部分
    if (uv.y <= 0.5) {
        uv.y = uv.y + 0.25;
    } else {
        uv.y = uv.y - 0.25;
    }
    gl_FragColor = texture2D(inputTexture, uv);
}