package com.cgfay.caincamera.filter.beauty;

import com.cgfay.caincamera.filter.base.BaseImageFilter;

/**
 * Created by cain.huang on 2017/9/25.
 */

public class RealtimeBeauty extends BaseImageFilter {

    private static String FRAGMENT_SHADER = "\n" +
            "#define x_a 960.0\n" +
            "#define y_a 1280.0\n" +
            "\n" +
            "precision lowp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying lowp vec2 textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    vec3 centralColor;\n" +
            "    float mul_x = 1.6 / x_a;\n" +
            "    float mul_y = 1.6 / y_a;\n" +

            "    vec2 blurCoordinates0 = textureCoordinate + vec2(0.0 * mul_x,  -10.0 * mul_y);\n" +
            "    vec2 blurCoordinates1 = textureCoordinate + vec2(5.0 * mul_x,   -8.0 * mul_y);\n" +
            "    vec2 blurCoordinates2 = textureCoordinate + vec2(8.0 * mul_x,   -5.0 * mul_y);\n" +
            "    vec2 blurCoordinates3 = textureCoordinate + vec2(10.0 * mul_x,   0.0 * mul_y);\n" +
            "    vec2 blurCoordinates4 = textureCoordinate + vec2(8.0 * mul_x,    5.0 * mul_y);\n" +
            "    vec2 blurCoordinates5 = textureCoordinate + vec2(5.0 * mul_x,    8.0 * mul_y);\n" +
            "    vec2 blurCoordinates6 = textureCoordinate + vec2(0.0 * mul_x,   10.0 * mul_y);\n" +
            "    vec2 blurCoordinates7 = textureCoordinate + vec2(-5.0 * mul_x,   8.0 * mul_y);\n" +
            "    vec2 blurCoordinates8 = textureCoordinate + vec2(-8.0 * mul_x,   5.0 * mul_y);\n" +
            "    vec2 blurCoordinates9 = textureCoordinate + vec2(-10.0 * mul_x,  0.0 * mul_y);\n" +
            "    vec2 blurCoordinates10 = textureCoordinate + vec2(-8.0 * mul_x, -5.0 * mul_y);\n" +
            "    vec2 blurCoordinates11 = textureCoordinate + vec2(-5.0 * mul_x, -8.0 * mul_y);\n" +
            "\n" +
            "    float central;  // 中心颜色\n" +
            "    float gaussianWeightTotal; // 高斯总权重\n" +
            "    float sumColor; // 总颜色\n" +
            "    float colorGreen; // 绿色\n" +
            "    float distanceFromCentralColor; // 与中心颜色的汉明距离\n" +
            "    float gaussianWeight;\n" +
            "    float distanceNormalizationFactor = 9.0;\n" +
            "\n" +
            "    central = texture2D(inputTexture, textureCoordinate).g;\n" +
            "    gaussianWeightTotal = 0.2;\n" +
            "    sumColor = central * 0.2;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates0).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates1).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates2).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates3).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates4).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates5).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates6).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates7).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates8).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates9).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates10).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    colorGreen = texture2D(inputTexture, blurCoordinates11).g;\n" +
            "    distanceFromCentralColor = min(abs(central - colorGreen) * distanceNormalizationFactor, 1.0);\n" +
            "    gaussianWeight = 0.08 * (1.0 - distanceFromCentralColor);\n" +
            "    gaussianWeightTotal += gaussianWeight;\n" +
            "    sumColor += colorGreen * gaussianWeight;\n" +
            "\n" +
            "    sumColor = sumColor / gaussianWeightTotal;\n" +
            "    centralColor = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "    colorGreen = centralColor.g - sumColor + 0.5;\n" +
            "\n" +
            "    // 高反差保留\n" +
            "    for(int i = 0; i < 5; ++i) {\n" +
            "        if(colorGreen <= 0.5) {\n" +
            "            colorGreen = colorGreen * colorGreen * 2.0;\n" +
            "        } else {\n" +
            "            colorGreen = 1.0 - ((1.0 - colorGreen)*(1.0 - colorGreen) * 2.0);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    float aa = 1.0 + pow(sumColor, 0.3) * 0.07;\n" +
            "    vec3 smoothColor = centralColor * aa - vec3(colorGreen) * (aa - 1.0); // 获取平滑颜色\n" +
            "    smoothColor = clamp(smoothColor, vec3(0.0), vec3(1.0));// 规整颜色值\n" +
            "    // 混合颜色\n" +
            "    smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.33));\n" +
            "    smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.39));\n" +
            "    smoothColor = mix(centralColor, smoothColor, 0.99);\n" +
            "\n" +
            "    gl_FragColor = vec4(pow(smoothColor, vec3(0.96)), 1.0);\n" +
            "}";

    public RealtimeBeauty() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public RealtimeBeauty(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
}
