package com.cgfay.cainfilter.glfilter.beauty;

import android.opengl.GLES30;

import com.cgfay.cainfilter.glfilter.base.BaseImageFilter;

/**
 * 实时磨皮滤镜
 * Created by cain.huang on 2017/10/12.
 */
public class RealtimeBeautify extends BaseImageFilter {

    private static String FRAGMENT_SHADER =
            "precision lowp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying lowp vec2 textureCoordinate;\n" +
            "\n" +
            "uniform int width;\n" +
            "uniform int height;\n" +
            "\n" +
            "// 磨皮程度(由低到高: 0.5 ~ 0.99)\n" +
            "uniform float opacity;\n" +
            "\n" +
            "void main() {\n" +
            "    vec3 centralColor;\n" +
            "\n" +
            "    centralColor = texture2D(inputTexture, textureCoordinate).rgb;\n" +
            "\n" +
            "    if(opacity < 0.01) {\n" +
            "        gl_FragColor = vec4(centralColor, 1.0);\n" +
            "    } else {\n" +
            "        float x_a = float(width);\n" +
            "        float y_a = float(height);\n" +
            "\n" +
            "        float mul_x = 2.0 / x_a;\n" +
            "        float mul_y = 2.0 / y_a;\n" +
            "        vec2 blurCoordinates0 = textureCoordinate + vec2(0.0 * mul_x, -10.0 * mul_y);\n" +
            "        vec2 blurCoordinates2 = textureCoordinate + vec2(8.0 * mul_x, -5.0 * mul_y);\n" +
            "        vec2 blurCoordinates4 = textureCoordinate + vec2(8.0 * mul_x, 5.0 * mul_y);\n" +
            "        vec2 blurCoordinates6 = textureCoordinate + vec2(0.0 * mul_x, 10.0 * mul_y);\n" +
            "        vec2 blurCoordinates8 = textureCoordinate + vec2(-8.0 * mul_x, 5.0 * mul_y);\n" +
            "        vec2 blurCoordinates10 = textureCoordinate + vec2(-8.0 * mul_x, -5.0 * mul_y);\n" +
            "\n" +
            "        mul_x = 1.8 / x_a;\n" +
            "        mul_y = 1.8 / y_a;\n" +
            "        vec2 blurCoordinates1 = textureCoordinate + vec2(5.0 * mul_x, -8.0 * mul_y);\n" +
            "        vec2 blurCoordinates3 = textureCoordinate + vec2(10.0 * mul_x, 0.0 * mul_y);\n" +
            "        vec2 blurCoordinates5 = textureCoordinate + vec2(5.0 * mul_x, 8.0 * mul_y);\n" +
            "        vec2 blurCoordinates7 = textureCoordinate + vec2(-5.0 * mul_x, 8.0 * mul_y);\n" +
            "        vec2 blurCoordinates9 = textureCoordinate + vec2(-10.0 * mul_x, 0.0 * mul_y);\n" +
            "        vec2 blurCoordinates11 = textureCoordinate + vec2(-5.0 * mul_x, -8.0 * mul_y);\n" +
            "\n" +
            "        mul_x = 1.6 / x_a;\n" +
            "        mul_y = 1.6 / y_a;\n" +
            "        vec2 blurCoordinates12 = textureCoordinate + vec2(0.0 * mul_x,-6.0 * mul_y);\n" +
            "        vec2 blurCoordinates14 = textureCoordinate + vec2(-6.0 * mul_x,0.0 * mul_y);\n" +
            "        vec2 blurCoordinates16 = textureCoordinate + vec2(0.0 * mul_x,6.0 * mul_y);\n" +
            "        vec2 blurCoordinates18 = textureCoordinate + vec2(6.0 * mul_x,0.0 * mul_y);\n" +
            "\n" +
            "        mul_x = 1.4 / x_a;\n" +
            "        mul_y = 1.4 / y_a;\n" +
            "        vec2 blurCoordinates13 = textureCoordinate + vec2(-4.0 * mul_x,-4.0 * mul_y);\n" +
            "        vec2 blurCoordinates15 = textureCoordinate + vec2(-4.0 * mul_x,4.0 * mul_y);\n" +
            "        vec2 blurCoordinates17 = textureCoordinate + vec2(4.0 * mul_x,4.0 * mul_y);\n" +
            "        vec2 blurCoordinates19 = textureCoordinate + vec2(4.0 * mul_x,-4.0 * mul_y);\n" +
            "\n" +
            "        float central;\n" +
            "        float gaussianWeightTotal;\n" +
            "        float sum;\n" +
            "        float sampler;\n" +
            "        float distanceFromCentralColor;\n" +
            "        float gaussianWeight;\n" +
            "\n" +
            "        float distanceNormalizationFactor = 3.6;\n" +
            "\n" +
            "        central = texture2D(inputTexture, textureCoordinate).g;\n" +
            "        gaussianWeightTotal = 0.2;\n" +
            "        sum = central * 0.2;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates0).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates1).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates2).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates3).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates4).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates5).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates6).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates7).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates8).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates9).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates10).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates11).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.09 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates12).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates13).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates14).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates15).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates16).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates17).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates18).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sampler = texture2D(inputTexture, blurCoordinates19).g;\n" +
            "        distanceFromCentralColor = min(abs(central - sampler) * distanceNormalizationFactor, 1.0);\n" +
            "        gaussianWeight = 0.1 * (1.0 - distanceFromCentralColor);\n" +
            "        gaussianWeightTotal += gaussianWeight;\n" +
            "        sum += sampler * gaussianWeight;\n" +
            "\n" +
            "        sum = sum/gaussianWeightTotal;\n" +
            "\n" +
            "        sampler = centralColor.g - sum + 0.5;\n" +
            "\n" +
            "        // 高反差保留\n" +
            "        for(int i = 0; i < 5; ++i) {\n" +
            "            if(sampler <= 0.5) {\n" +
            "                sampler = sampler * sampler * 2.0;\n" +
            "            } else {\n" +
            "                sampler = 1.0 - ((1.0 - sampler)*(1.0 - sampler) * 2.0);\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        float aa = 1.0 + pow(sum, 0.3) * 0.09;\n" +
            "        vec3 smoothColor = centralColor * aa - vec3(sampler) * (aa - 1.0);\n" +
            "        smoothColor = clamp(smoothColor, vec3(0.0), vec3(1.0));\n" +
            "\n" +
            "        smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.33));\n" +
            "        smoothColor = mix(centralColor, smoothColor, pow(centralColor.g, 0.39));\n" +
            "\n" +
            "        smoothColor = mix(centralColor, smoothColor, opacity);\n" +
            "\n" +
            "        gl_FragColor = vec4(pow(smoothColor, vec3(0.96)), 1.0);\n" +
            "    }\n" +
            " }";


    private int mWidthLoc;
    private int mHeightLoc;
    private int mOpacityLoc;

    public RealtimeBeautify() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public RealtimeBeautify(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mWidthLoc = GLES30.glGetUniformLocation(mProgramHandle, "width");
        mHeightLoc = GLES30.glGetUniformLocation(mProgramHandle, "height");
        mOpacityLoc = GLES30.glGetUniformLocation(mProgramHandle, "opacity");
        setSmoothOpacity(1.0f);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        // 宽高变更时需要重新设置宽高值
        setInteger(mWidthLoc, width);
        setInteger(mHeightLoc, height);
    }

    /**
     * 设置磨皮程度
     * @param percent 0.0 ~ 1.0
     */
    public void setSmoothOpacity(float percent) {
        float opacity;
        if (percent <= 0) {
            opacity = 0.0f;
        } else {
            opacity = calculateOpacity(percent);
        }
        setFloat(mOpacityLoc, opacity);
    }

    /**
     * 根据百分比计算出实际的磨皮程度
     * @param percent 0% ~ 100%
     * @return
     */
    private float calculateOpacity(float percent) {
        if (percent > 1.0f) {
            percent = 1.0f;
        }
        float result = 0.0f;
        result = (float) (1.0f - (1.0f - percent + 0.02) / 2.0f);

        return result;
    }
}

