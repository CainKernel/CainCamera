precision mediump float;
varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;         // 输入图像纹理
uniform sampler2D lookupTableTexture;   // lut纹理

uniform lowp float strength;            // 滤镜强度值，0 ~ 1.0f

void main() {
    lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);

    mediump float blueColor = textureColor.b * 15.0;

    mediump vec2 quad1;
    quad1.y = floor(blueColor / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);

    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

    lowp vec4 newColor1 = texture2D(lookupTableTexture, texPos1);
    lowp vec4 newColor2 = texture2D(lookupTableTexture, texPos2);

    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
    gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), strength);
}