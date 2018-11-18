// 人脸美化处理
precision highp float;

varying vec2 textureCoordinate;
varying vec2 maskCoordinate;

uniform sampler2D inputTexture;         // 输入图像纹理
uniform sampler2D blurTexture;          // 经过高斯模糊处理的图像纹理
uniform sampler2D blurTexture2;         // 经过高斯模糊处理的图像纹理2
uniform sampler2D maskTexture;          // 遮罩图像纹理
uniform sampler2D teethLookupTexture;   // 美牙的lookup table 纹理

uniform float brightEyeStrength;        // 亮眼程度
uniform float teethStrength;            // 美牙程度
uniform float nasolabialStrength;       // 法令纹处理程度
uniform float furrowStrength;           // 卧蚕处理程度
uniform float eyeBagStrength;           // 眼袋处理程度

uniform int processType;                // 处理类型, 1表示亮眼处理，2表示美牙处理，3表示消除法令纹，4表示消除卧蚕眼袋，其他类型则直接绘制原图

void main()
{
    vec4 sourceColor = texture2D(inputTexture, textureCoordinate);
    vec4 color = sourceColor;
    if (processType == 1) { // 亮眼处理
        // 如果遮罩纹理存在
        vec4 maskColor = texture2D(maskTexture, maskCoordinate);
        if (maskColor.r > 0.01) {
            // 高斯模糊的图像颜色值
            vec4 blurColor = texture2D(blurTexture2, textureCoordinate);
            // 统计颜色
            vec3 sumColor = vec3(0.0, 0.0, 0.0);
            // 将RGB颜色差值放大。突出眼睛明亮部分
            sumColor = clamp((sourceColor.rgb - blurColor.rgb) * 3.3, 0.0, 1.0);
            sumColor = max(sourceColor.rgb, sumColor);
            // 用原图和最终得到的明亮部分进行线性混合处理
            color = mix(sourceColor, vec4(sumColor, 1.0), brightEyeStrength * maskColor.r);
        }
    } else if (processType == 2) { // 美牙处理
        vec4 maskColor = texture2D(maskTexture, maskCoordinate.xy);
        if (maskColor.r > 0.001) {
            mediump float blueColor = sourceColor.b * 15.0;

            vec2 quad1;
            vec2 quad2;

            quad1.y = floor(floor(blueColor) * 0.25);
            quad1.x = floor(blueColor) - (quad1.y * 4.0);

            quad2.y = floor(ceil(blueColor) * 0.25);
            quad2.x = ceil(blueColor) - (quad2.y * 4.0);

            vec2 texPos1;
            vec2 texPos2;

            texPos1.x = (quad1.x * 0.25) + 0.0078125 + (0.234375 * sourceColor.r);
            texPos1.y = (quad1.y * 0.25) + 0.0078125 + (0.234375 * sourceColor.g);

            texPos2.x = (quad2.x * 0.25) + 0.0078125 + (0.234375 * sourceColor.r);
            texPos2.y = (quad2.y * 0.25) + 0.0078125 + (0.234375 * sourceColor.g);

            lowp vec3 newColor1 = texture2D(teethLookupTexture, texPos1).rgb;
            lowp vec3 newColor2 = texture2D(teethLookupTexture, texPos2).rgb;
            lowp vec3 newColor = mix(newColor1, newColor2, fract(blueColor));

            color = vec4(mix(sourceColor.rgb, newColor, teethStrength * maskColor.r), 1.0);
        }
    } else if (processType == 3) { // 消除法令纹
        vec3 maskColor = texture2D(maskTexture, maskCoordinate.xy).rgb;
        // 去除法令纹原理，用两张不同程度的高斯模糊图像差值比较，得到鼻唇沟附近的颜色差值比较，配合法令纹遮罩图像去除法令纹
        if (maskColor.r > 0.01) {
            vec3 blurColor1 = texture2D(blurTexture, textureCoordinate.xy).rgb;
            vec3 blurColor2 = texture2D(blurTexture2, textureCoordinate.xy).rgb;
            vec3 diffColor = clamp((blurColor2 - blurColor1) * 1.8 + 0.1 * blurColor2, 0.0, 0.5);
            color = vec4(min(sourceColor.rgb + diffColor, 1.0), 1.0) * nasolabialStrength * maskColor.r;
        }
    } else if (processType == 4) {
        vec4 maskColor = texture2D(maskTexture, maskCoordinate.xy);
        if (maskColor.r > 0.005) {  // 消除眼袋，用红色表示
            vec3 blurColor1 = texture2D(blurTexture, textureCoordinate.xy).rgb;
            vec3 blurColor2 = texture2D(blurTexture2, textureCoordinate.xy).rgb;
            // 放大差值，用输入的图像加上差值，消除差值所带来的影响
            vec3 diffColor = clamp((blurColor2 - blurColor1) * 2.0 + 0.05 * blurColor2, 0.0, 0.3);
            color.rgb = mix(color.rgb, min(color.rgb + diffColor, 1.0), eyeBagStrength * maskColor.r);
        } else if (maskColor.g > 0.005) { // 消除卧蚕，蓝色部分为卧蚕遮罩
            color.rgb = mix(color.rgb, pow(color.rgb, vec3(0.5, 0.5, 0.5)), furrowStrength * maskColor.g);
        }
    }
    gl_FragColor = color;
}
