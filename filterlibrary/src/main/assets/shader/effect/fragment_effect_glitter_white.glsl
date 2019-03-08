// 仿抖音闪白特效
precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;

uniform float color; // 增强的颜色值

void main() {
    vec2 uv = textureCoordinate.xy;
    vec4 textureColor = texture2D(inputTexture, uv);

    // 计算出最后的rgb颜色，并限定范围到0.0 ~ 1.0的范围
    float rColor = clamp(textureColor.r + color, 0.0, 1.0);
    float gColor = clamp(textureColor.g + color, 0.0, 1.0);
    float bColor = clamp(textureColor.b + color, 0.0, 1.0);

    // 输出结果
    gl_FragColor = vec4(rColor, gColor, bColor, textureColor.a);
}