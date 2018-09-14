//// 高斯模糊(GPUImage中的shader)
//precision mediump float;
//varying highp vec2 textureCoordinate;
//uniform sampler2D inputTexture;
//const lowp int GAUSSIAN_SAMPLES = 9;
//varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];
//
//void main()
//{
//	lowp vec3 sum = vec3(0.0);
//   lowp vec4 fragColor=texture2D(inputTexture,textureCoordinate);
//
//    sum += texture2D(inputTexture, blurCoordinates[0]).rgb * 0.05;
//    sum += texture2D(inputTexture, blurCoordinates[1]).rgb * 0.09;
//    sum += texture2D(inputTexture, blurCoordinates[2]).rgb * 0.12;
//    sum += texture2D(inputTexture, blurCoordinates[3]).rgb * 0.15;
//    sum += texture2D(inputTexture, blurCoordinates[4]).rgb * 0.18;
//    sum += texture2D(inputTexture, blurCoordinates[5]).rgb * 0.15;
//    sum += texture2D(inputTexture, blurCoordinates[6]).rgb * 0.12;
//    sum += texture2D(inputTexture, blurCoordinates[7]).rgb * 0.09;
//    sum += texture2D(inputTexture, blurCoordinates[8]).rgb * 0.05;
//
//	gl_FragColor = vec4(sum, fragColor.a);
//}

// 优化后的高斯模糊
precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5
const int SHIFT_SIZE = 2;
varying vec4 blurShiftCoordinates[SHIFT_SIZE];
void main() {
    // 计算当前坐标的颜色值
    vec4 currentColor = texture2D(inputTexture, textureCoordinate);
    mediump vec3 sum = currentColor.rgb;
    // 计算偏移坐标的颜色值总和
    for (int i = 0; i < SHIFT_SIZE; i++) {
        sum += texture2D(inputTexture, blurShiftCoordinates[i].xy).rgb;
        sum += texture2D(inputTexture, blurShiftCoordinates[i].zw).rgb;
    }
    // 求出平均值
    gl_FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);
}