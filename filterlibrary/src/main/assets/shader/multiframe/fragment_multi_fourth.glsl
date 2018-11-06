// 四分镜
precision highp float;
uniform sampler2D inputTexture;
varying highp vec2 textureCoordinate;

void main() {
    vec2 uv = textureCoordinate;
    if (uv.x <= 0.5) {
        uv.x = uv.x * 2.0;
    } else {
        uv.x = (uv.x - 0.5) * 2.0;
    }
    if (uv.y <= 0.5) {
        uv.y = uv.y * 2.0;
    } else {
        uv.y = (uv.y - 0.5) * 2.0;
    }
    gl_FragColor = texture2D(inputTexture, fract(uv));
}
