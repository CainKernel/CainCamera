precision mediump float;

varying vec2 textureCoordinate;     // 图像纹理坐标

varying vec2 maskCoordinate;        // 遮罩纹理坐标

uniform sampler2D inputTexture;     // 图像纹理, 原图、素材等

uniform sampler2D materialTexture;  // 素材纹理, 对于唇彩来说，这里存放的是lut纹理

uniform sampler2D maskTexture;      // 遮罩纹理, 唇彩或者眼睛的遮罩纹理

uniform float strength;             // 彩妆强度

uniform int makeupType;             // 彩妆类型, 0表示原图，1比表示绘制没有遮罩的素材，2主要表示美瞳裁剪，3表示绘制唇彩

void main() {
    if (makeupType == 0) { // 直接绘制输入的原图像纹理

        lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
        gl_FragColor = sourceColor;

    } else if (makeupType == 1) { // 绘制不带遮罩的彩妆素材，此时inputTexture是素材纹理

        lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate.xy);
        gl_FragColor = textureColor * strength;

    } else if (makeupType == 2) { // 素材裁剪
        // 这部分主要是美瞳在用，实现过程如下：
        // 首先将美瞳素材使绘制到FBO中，将绘制了美瞳的图像作为素材纹理传进来，跟眼睛的遮罩图进行裁剪混合
        // 输入原图
        lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate.xy);

        // 绘制了素材的图像
        lowp vec4 matetialColor = texture2D(materialTexture, textureCoordinate.xy);

        // 遮罩图像
        lowp vec4 maskColor = texture2D(maskTexture, maskCoordinate.xy);

        // 线性混合，裁掉超出遮罩部分
        textureColor = mix(textureColor, matetialColor, maskColor.r);

        gl_FragColor = textureColor;
    } else if (makeupType == 3) { // 映射唇彩

        lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate.xy);

        lowp vec4 lipMaskColor = texture2D(maskTexture, maskCoordinate.xy);

        if (lipMaskColor.r > 0.005) {
            mediump vec2 quad1;
            mediump vec2 quad2;
            mediump vec2 texPos1;
            mediump vec2 texPos2;

            mediump float blueColor = textureColor.b * 15.0;

            quad1.y = floor(floor(blueColor) / 4.0);
            quad1.x = floor(blueColor) - (quad1.y * 4.0);

            quad2.y = floor(ceil(blueColor) / 4.0);
            quad2.x = ceil(blueColor) - (quad2.y * 4.0);

            texPos1.xy = (quad1.xy * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.rg);
            texPos2.xy = (quad2.xy * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.rg);

            lowp vec3 newColor1 = texture2D(materialTexture, texPos1).rgb;
            lowp vec3 newColor2 = texture2D(materialTexture, texPos2).rgb;

            lowp vec3 newColor = mix(newColor1, newColor2, fract(blueColor));

            textureColor = vec4(newColor, 1.0) * (lipMaskColor.r * strength);
        } else {
            textureColor = vec4(0.0, 0.0, 0.0, 0.0);
        }
        gl_FragColor = textureColor;

    } else {
        gl_FragColor = texture2D(inputTexture, textureCoordinate.xy);
    }
}
