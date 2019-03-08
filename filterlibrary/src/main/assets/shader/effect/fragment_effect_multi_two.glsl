// 仿抖音两屏特效
precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    // 纹理坐标
    vec2 uv = textureCoordinate.xy;
    float y;
    if (uv.y >= 0.0 && uv.y <= 0.5) {
        y = uv.y + 0.25;
    } else {
        y = uv.y - 0.25;
    }

    gl_FragColor = texture2D(inputTexture, vec2(uv.x, y));
}
