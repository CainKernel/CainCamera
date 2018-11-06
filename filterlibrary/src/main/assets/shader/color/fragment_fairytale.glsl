precision highp float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputTexture;
uniform sampler2D lookupTexture; // lookup texture

uniform float strength;


void main()
{
    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
    mediump float blueColor = sourceColor.b * 63.0;
    mediump vec2 quad1;
    quad1.y = floor(floor(blueColor) / 8.0);
    quad1.x = floor(blueColor) - (quad1.y * 8.0);
    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 8.0);
    quad2.x = ceil(blueColor) - (quad2.y * 8.0);
    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * sourceColor.r);
    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * sourceColor.g);
    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * sourceColor.r);
    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * sourceColor.g);
    lowp vec4 newColor1 = texture2D(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture2D(lookupTexture, texPos2);
    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));

    gl_FragColor = mix(sourceColor, newColor, strength);
}