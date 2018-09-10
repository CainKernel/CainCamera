precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

uniform float repeat; // 画面重复的次数

void main() {
    vec2 uv = textureCoordinate;
    // 反向UV坐标
    vec2 invertedUV = 1.0 - uv;
    // 计算重复次数之后的uv值以及偏移值
    vec2 fiter = floor(uv * repeat * 2.0);
    vec2 riter = floor(invertedUV * repeat * 2.0);
    vec2 iter = min(fiter, riter);
    float minOffset = min(iter.x, iter.y);
    // 偏移值
    vec2 offset = (vec2(0.5, 0.5) / repeat) * minOffset;
    // 当前实际的偏移值
    vec2 currenOffset = 1.0 / (vec2(1.0, 1.0) - offset * 2.0);
    // 计算出当前的实际UV坐标
    vec2 currentUV = (uv - offset) * currenOffset;

    gl_FragColor = texture2D(inputTexture, fract(currentUV));
}
