precision mediump float;
varying vec2 textureCoordinate;    // texture的uv坐标
varying vec2 mipmapCoordinate;     // mipmap经过透视变换到屏幕上的uv坐标

uniform sampler2D inputTexture;    // 原始Texture
uniform sampler2D mipmapTexture;   // 贴图Texture

void main()
{
    lowp vec4 sourceColor = texture2D(inputTexture, 1.0 - textureCoordinate);
    lowp vec4 mipmapColor = texture2D(mipmapTexture, mipmapCoordinate);
    // 混合处理，此时可以做各种混合处理，比如变换透明度，变换颜色等等
    gl_FragColor = mipmapColor * mipmapColor.a + sourceColor * (1.0 - mipmapColor.a);
}