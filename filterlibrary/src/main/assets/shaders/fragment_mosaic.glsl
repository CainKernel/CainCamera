precision highp float;
uniform sampler2D inputTexture;
varying vec2 textureCoordinate;

uniform float imageWidthFactor;
uniform float imageHeightFactor;

uniform float mosaicSize;

void main()
{
    vec2 uv  = textureCoordinate.xy;
    float dx = mosaicSize * imageWidthFactor;
    float dy = mosaicSize * imageHeightFactor;
    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));
    vec3 tc = texture2D(inputTexture, coord).xyz;
    gl_FragColor = vec4(tc, 1.0);
}