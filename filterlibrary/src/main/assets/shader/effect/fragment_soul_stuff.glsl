// 灵魂出窍滤镜
precision highp float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;

uniform float scale;

void main() {
     vec2 uv = textureCoordinate.xy;
     // 输入纹理
     vec4 sourceColor = texture2D(inputTexture, fract(uv));
     // 将纹理坐标中心转成(0.0, 0.0)再做缩放
     vec2 center = vec2(0.5, 0.5);
     uv -= center;
     uv = uv / scale;
     uv += center;
     // 缩放纹理
     vec4 scaleColor = texture2D(inputTexture, fract(uv));
     // 线性混合
     gl_FragColor = mix(sourceColor, scaleColor, 0.5 * (0.6 - fract(scale)));
}