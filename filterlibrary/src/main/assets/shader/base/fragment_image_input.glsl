// 图像输入滤镜，倒过来
precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
void main() {
    vec2 coordinate = vec2(textureCoordinate.x, 1.0 - textureCoordinate.y);
    gl_FragColor = texture2D(inputTexture, coordinate);
}