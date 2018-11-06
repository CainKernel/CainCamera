// 美肤滤镜
varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture; // 图像texture
uniform sampler2D grayTexture;  // 灰度查找表
uniform sampler2D lookupTexture; // LUT

uniform highp float levelRangeInv; // 范围
uniform lowp float levelBlack; // 灰度level 
uniform lowp float alpha; // 肤色成都 

void main() {
    lowp vec3 textureColor = texture2D(inputTexture, textureCoordinate).rgb;

    textureColor = clamp((textureColor - vec3(levelBlack, levelBlack, levelBlack)) * levelRangeInv, 0.0, 1.0);
    textureColor.r = texture2D(grayTexture, vec2(textureColor.r, 0.5)).r;
    textureColor.g = texture2D(grayTexture, vec2(textureColor.g, 0.5)).g;
    textureColor.b = texture2D(grayTexture, vec2(textureColor.b, 0.5)).b;

    mediump float blueColor = textureColor.b * 15.0;

    mediump vec2 quad1;
    quad1.y = floor(blueColor / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);

    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);

    lowp vec4 newColor1 = texture2D(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture2D(lookupTexture, texPos2);

    lowp vec3 newColor = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));

    textureColor = mix(textureColor, newColor, alpha);

    gl_FragColor = vec4(textureColor, 1.0); 
}