attribute vec4 aPosition;
attribute vec2 aTextureCoord;

varying vec2 textureCoordinate;

void main() {
    gl_Position = aPosition;
    textureCoordinate = aTextureCoord.xy;
}
