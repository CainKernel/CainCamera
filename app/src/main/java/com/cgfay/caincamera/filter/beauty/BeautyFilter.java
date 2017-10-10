package com.cgfay.caincamera.filter.beauty;

import android.opengl.GLES30;

import com.cgfay.caincamera.filter.base.BaseImageFilter;

/**
 * 实时美颜
 * Created by cain on 2017/7/30.
 */
public class BeautyFilter extends BaseImageFilter {

    private static final String FRAGMENT_SHADER =
             "precision mediump float;\n" +
                    "\n" +
                    "varying mediump vec2 textureCoordinate;\n" +
                    "\n" +
                    "uniform sampler2D inputTexture;\n" +
                    "uniform vec2 singleStepOffset;\n" +
                    "uniform mediump float params;\n" +
                    "\n" +
                    "const highp vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n" +
                    "vec2 blurCoordinates[20];\n" +
                    "\n" +
                    "float hardLight(float color)\n" +
                    "{\n" +
                    "\tif(color <= 0.5)\n" +
                    "\t\tcolor = color * color * 2.0;\n" +
                    "\telse\n" +
                    "\t\tcolor = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
                    "\treturn color;\n" +
                    "}\n" +
                    "\n" +
                    "void main(){\n" +
                    "\n" +
                    "    vec3 centralColor = texture2D(inputTexture, textureCoordinate).rgb;\n" +
                    "    if(params != 0.0){\n" +
                    "\n" +
                    "        blurCoordinates[0] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);\n" +
                    "        blurCoordinates[1] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
                    "        blurCoordinates[2] = textureCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
                    "        blurCoordinates[3] = textureCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
                    "        blurCoordinates[4] = textureCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
                    "        blurCoordinates[5] = textureCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
                    "        blurCoordinates[6] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
                    "        blurCoordinates[7] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
                    "        blurCoordinates[8] = textureCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
                    "        blurCoordinates[9] = textureCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
                    "        blurCoordinates[10] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
                    "        blurCoordinates[11] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
                    "        blurCoordinates[12] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
                    "        blurCoordinates[13] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
                    "        blurCoordinates[14] = textureCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
                    "        blurCoordinates[15] = textureCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
                    "        blurCoordinates[16] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
                    "        blurCoordinates[17] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
                    "        blurCoordinates[18] = textureCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
                    "        blurCoordinates[19] = textureCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
                    "\n" +
                    "        float sampleColor = centralColor.g * 20.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[0]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[1]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[2]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[3]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[4]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[5]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[6]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[7]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[8]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[9]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[10]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[11]).g;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[12]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[13]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[14]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[15]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[16]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[17]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[18]).g * 2.0;\n" +
                    "        sampleColor += texture2D(inputTexture, blurCoordinates[19]).g * 2.0;\n" +
                    "\n" +
                    "        sampleColor = sampleColor / 48.0;\n" +
                    "\n" +
                    "        float highPass = centralColor.g - sampleColor + 0.5;\n" +
                    "\n" +
                    "        for(int i = 0; i < 5;i++)\n" +
                    "        {\n" +
                    "            highPass = hardLight(highPass);\n" +
                    "        }\n" +
                    "        float luminance = dot(centralColor, luminanceWeighting);\n" +
                    "\n" +
                    "        float alpha = pow(luminance, params);\n" +
                    "\n" +
                    "        vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;\n" +
                    "\n" +
                    "        gl_FragColor = vec4(mix(smoothColor.rgb, max(smoothColor, centralColor), alpha), 1.0);\n" +
                    "    }else{\n" +
                    "        gl_FragColor = vec4(centralColor.rgb,1.0);\n" +
                    "    }\n" +
                    "}";

    private int mSingleStepOffsetLoc;
    private int mParamsLoc;

    public BeautyFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public BeautyFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        mSingleStepOffsetLoc = GLES30.glGetUniformLocation(mProgramHandle, "singleStepOffset");
        mParamsLoc = GLES30.glGetUniformLocation(mProgramHandle, "params");
        setBeautyLevel(5);
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    private void setTexelSize(float width, float height) {
        setFloatVec2(mSingleStepOffsetLoc, new float[]{ 2.0f / width, 2.0f / height });
    }

    public void setBeautyLevel(int level) {
        switch (level) {
            case 0:
                setFloat(mParamsLoc, 0.0f);
                break;

            case 1:
                setFloat(mParamsLoc, 3.0f);
                break;

            case 2:
                setFloat(mParamsLoc, 2.3f);
                break;

            case 3:
                setFloat(mParamsLoc, 1.7f);
                break;

            case 4:
                setFloat(mParamsLoc, 1.3f);
                break;

            case 5:
                setFloat(mParamsLoc, 1.0f);
                break;
            default:
                break;
        }
    }
}
