//// 高斯模糊（GPUImage中的shader）
//uniform mat4 uMVPMatrix;
//attribute vec4 aPosition;
//attribute vec4 aTextureCoord;
//
//// 高斯算子大小(3 x 3)
//const int GAUSSIAN_SAMPLES = 9;
//
//uniform float texelWidthOffset;
//uniform float texelHeightOffset;
//
//varying vec2 textureCoordinate;
//varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];
//
//void main()
//{
//    gl_Position = uMVPMatrix * aPosition;
//    textureCoordinate = aTextureCoord.xy;
//
//    int multiplier = 0;
//    vec2 blurStep;
//    vec2 singleStepOffset = vec2(texelHeightOffset, texelWidthOffset);
//
//    for (int i = 0; i < GAUSSIAN_SAMPLES; i++) {
//        multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));
//        blurStep = float(multiplier) * singleStepOffset;
//        blurCoordinates[i] = aTextureCoord.xy + blurStep;
//    }
//}

// 优化后的高斯模糊
uniform mat4 uMVPMatrix;
attribute vec4 aPosition;
attribute vec4 aTextureCoord;

// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5
const int SHIFT_SIZE = 2;

uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;

varying vec2 textureCoordinate;
varying vec4 blurShiftCoordinates[SHIFT_SIZE];

void main() {
    gl_Position = uMVPMatrix * aPosition;
    textureCoordinate = aTextureCoord.xy;
    // 偏移步距
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    // 记录偏移坐标
    for (int i = 0; i < SHIFT_SIZE; i++) {
        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,
                                       textureCoordinate.xy + float(i + 1) * singleStepOffset);
    }
}