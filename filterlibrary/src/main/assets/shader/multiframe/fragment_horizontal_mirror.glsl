varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;

void main() {
    vec2 uv = textureCoordinate;
    if (uv.x > 0.5) {
        uv.x = 1.0 - uv.x;
    }
    gl_FragColor = texture2D(inputTexture, fract(uv));
}