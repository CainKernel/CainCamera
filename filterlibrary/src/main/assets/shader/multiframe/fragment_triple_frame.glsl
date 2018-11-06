varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D lookupTable1; // 上层lookupTable
uniform sampler2D lookupTable2; // 中层lookupTable
uniform sampler2D lookupTable3; // 下层lookupTable

// textureColor:输入纹理颜色，lookupTexture：lut纹理
vec4 getLutColor(vec4 textureColor, sampler2D lookupTexture) {
    mediump float blueColor = textureColor.b * 63.0;

    mediump vec2 quad1;
    quad1.y = floor(floor(blueColor) / 8.0);
    quad1.x = floor(blueColor) - (quad1.y * 8.0);

    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 8.0);
    quad2.x = ceil(blueColor) - (quad2.y * 8.0);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);

    lowp vec4 newColor1 = texture2D(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture2D(lookupTexture, texPos2);

    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
    vec4 color = vec4(newColor.rgb, textureColor.w);
    return color;
}

void main() 
{
    highp vec2 uv = textureCoordinate;
    vec4 color;
    if (uv.y >= 0.0 && uv.y <= 0.33) { // 上层
        vec2 coordinate = vec2(uv.x, uv.y + 0.33);
        vec4 textureColor = texture2D(inputTexture, coordinate);
        color = getLutColor(textureColor, lookupTable1);
    } else if (uv.y > 0.33 && uv.y <= 0.67) {   // 中间层
        vec4 textureColor = texture2D(inputTexture, uv);
        color = getLutColor(textureColor, lookupTable2);
    } else {    // 下层
        vec2 coordinate = vec2(uv.x, uv.y - 0.33);
        vec4 textureColor = texture2D(inputTexture, coordinate);
        color = getLutColor(textureColor, lookupTable3);
    }
    gl_FragColor = color;
}