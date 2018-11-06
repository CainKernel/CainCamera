varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;

void main() {
    vec2 uv = textureCoordinate;
    if (uv.y < 0.5) {
        uv.y = 1.0 - uv.y;
    }
    gl_FragColor = texture2D(inputTexture, fract(uv));
}
