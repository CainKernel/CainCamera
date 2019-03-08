// 仿抖音三屏特效
precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    highp vec2 uv = textureCoordinate;
    if (uv.y < 1.0 / 3.0) {
        uv.y = uv.y + 1.0 / 3.0;
    } else if (uv.y > 2.0 / 3.0) {
        uv.y = uv.y - 1.0 / 3.0;
    }
    gl_FragColor = texture2D(inputTexture, uv);
}