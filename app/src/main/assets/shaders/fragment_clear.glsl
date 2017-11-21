// 原图，啥也没做
precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
void main() {
    gl_FragColor = texture2D(inputTexture, textureCoordinate);
}
