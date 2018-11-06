precision highp float;

varying highp vec2 textureCoordinate;
uniform sampler2D inputTexture;

uniform highp float scaleRatio; // 缩放系数，0无缩放，大于0则放大
uniform highp float radius; // 缩放算法的作用域半径
uniform highp vec2 leftEyeCenterPosition; // 左眼控制点，越远变形越小
uniform highp vec2 rightEyeCenterPosition; // 右眼控制点
uniform float aspectRatio; // 所处理图像的宽高比

// 大眼算法
highp vec2 enlargeEye(vec2 centerPostion, vec2 currentPosition, float radius, float scaleRatio, float aspectRatio)
{
    vec2 positionToUse = currentPosition;
    vec2 currentPositionToUse = vec2(currentPosition.x, currentPosition.y * aspectRatio + 0.5 - 0.5 * aspectRatio);
    vec2 centerPostionToUse = vec2(centerPostion.x, centerPostion.y * aspectRatio + 0.5 - 0.5 * aspectRatio);
    float r = distance(currentPositionToUse, centerPostionToUse);

    if (r < radius) {
        float alpha = 1.0 - scaleRatio * (r / radius - 1.0) * (r / radius - 1.0);
        positionToUse = centerPostion + alpha * (currentPosition - centerPostion);
    }

    return positionToUse;
}
 
void main()
{
    vec2 curCoord = enlargeEye(leftEyeCenterPosition, textureCoordinate, radius, scaleRatio, aspectRatio);

    curCoord = enlargeEye(rightEyeCenterPosition, curCoord, radius, scaleRatio, aspectRatio);

    gl_FragColor = texture2D(inputTexture, curCoord);
}