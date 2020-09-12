// 仿抖音两屏特效
precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    // 纹理坐标
    vec2 uv = textureCoordinate.xy;
    float y;
    float middle = step(uv.y, 0.5);
    y = uv.y + (middle - 0.5) * 0.5;
    vec4 textureColor = texture2D(inputTexture, vec2(uv.x, y));
    gl_FragColor = middle * textureColor + (1.0 - middle) * textureColor;
}
