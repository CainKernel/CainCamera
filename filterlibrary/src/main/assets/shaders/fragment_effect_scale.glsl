// 缩放滤镜
precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;

uniform float scale;

void main() {
    vec2 uv = textureCoordinate.xy;
    // 将纹理坐标中心转成(0.0, 0.0)再做缩放
    vec2 center = vec2(0.5, 0.5);
    uv -= center;
    uv = uv / scale;
    uv += center;

    gl_FragColor = texture2D(inputTexture, uv);
}
