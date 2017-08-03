precision mediump float;
varying vec2 textureCoordinate;    // texture坐标
varying vec2 mipmapCoordinate;     // mipmap坐标

uniform sampler2D inputTexture;    // 原始Texture
uniform sampler2D mipmapTexture;   // 贴图Texture

void main()
{
    lowp vec4 sourceColor = texture2D(inputTexture, 1.0 - textureCoordinate);
    lowp vec4 mipmapColor = texture2D(mipmapTexture, mipmapCoordinate);
    gl_FragColor = mipmapColor * mipmapColor.a + sourceColor * (1.0 - mipmapColor.a);
}