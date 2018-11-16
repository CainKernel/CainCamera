precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;

// 图像笛卡尔坐标系的关键点，也就是纹理坐标乘以宽高得到
uniform vec2 cartesianPoints[106];

#define INDEX_FACE_LIFT     0   // 瘦脸
#define INDEX_FACE_SHAVE    1   // 削脸
#define INDEX_FACE_NARROW   2   // 小脸
#define INDEX_CHIN          3   // 下巴
#define INDEX_FOREHEAD      4   // 额头
#define INDEX_EYE_ENLARGE   5   // 大眼
#define INDEX_EYE_DISTANCE  6   // 眼距
#define INDEX_EYE_CORNER    7   // 眼角
#define INDEX_NOSE_THIN     8   // 瘦鼻
#define INDEX_ALAE          9   // 鼻翼
#define INDEX_PROBOSCIS    10   // 长鼻
#define INDEX_MOUTH        11   // 嘴型
#define INDEX_SIZE 12           // 索引大小

// 美型程度参数列表
uniform float reshapeIntensity[INDEX_SIZE];

// 纹理宽度
uniform int textureWidth;
// 纹理高度
uniform int textureHeight;

// 是否允许美型处理，存在人脸时为1，没有人脸时为0
uniform int enableReshape;

// 曲线形变处理
vec2 curveWarp(vec2 textureCoord, vec2 originPosition, vec2 targetPosition, float radius)
{
    vec2 offset = vec2(0.0);
    vec2 result = vec2(0.0);

    vec2 direction = targetPosition - originPosition;

    float infect = distance(textureCoord, originPosition)/radius;

    infect = 1.0 - infect;
    infect = clamp(infect, 0.0, 1.0);
    offset = direction * infect;

    result = textureCoord - offset;

    return result;
}

// 大眼处理
vec2 enlargeEye(vec2 currentCoordinate, vec2 circleCenter, float radius, float intensity)
{
    float currentDistance = distance(currentCoordinate, circleCenter);
    float weight = currentDistance / radius;
    weight = 1.0 - intensity * (1.0 - weight * weight);
    weight = clamp(weight, 0.0, 1.0);
    currentCoordinate = circleCenter + (currentCoordinate - circleCenter) * weight;

    return currentCoordinate;
}

// 瘦脸
vec2 faceLift(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float faceLiftScale = reshapeIntensity[INDEX_FACE_LIFT] * 0.05;
    float radius = faceLength;

    currentPoint = cartesianPoints[3];
    destPoint = currentPoint + (cartesianPoints[44] - currentPoint) * faceLiftScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[29];
    destPoint = currentPoint + (cartesianPoints[44] - currentPoint) * faceLiftScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    radius = faceLength * 0.8;
    currentPoint = cartesianPoints[10];
    destPoint = currentPoint + (cartesianPoints[46] - currentPoint) * (faceLiftScale * 0.6);
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[22];
    destPoint = currentPoint + (cartesianPoints[46] - currentPoint) * (faceLiftScale * 0.6);
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

// 削脸
vec2 faceShave(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float faceShaveScale = reshapeIntensity[INDEX_FACE_SHAVE] * 0.12;
    float radius = faceLength * 1.0;

    // 下巴中心
    vec2 chinCenter = (cartesianPoints[16] + cartesianPoints[93]) * 0.5;
    currentPoint = cartesianPoints[13];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[19];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

// 处理下巴
vec2 chinChange(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float chinScale = reshapeIntensity[INDEX_CHIN] * 0.08;
    float radius = faceLength * 1.25;
    currentPoint = cartesianPoints[16];
    destPoint = currentPoint + (cartesianPoints[46] - currentPoint) * chinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

void main()
{
    vec2 coordinate = textureCoordinate.xy;
    // 禁用美型处理或者鼻子不在图像中，则直接绘制
    if (enableReshape == 0 || (cartesianPoints[46].x / float(textureWidth) <= 0.03)
        || (cartesianPoints[46].y / float(textureHeight)) <= 0.03) {
        gl_FragColor = texture2D(inputTexture, coordinate);
        return;
    }

    // 将坐标转成图像大小，这里是为了方便计算
    coordinate = textureCoordinate * vec2(float(textureWidth), float(textureHeight));

    float eyeDistance = distance(cartesianPoints[74], cartesianPoints[77]); // 两个瞳孔的距离

    // 瘦脸
    coordinate = faceLift(coordinate, eyeDistance);

    // 削脸
    coordinate = faceShave(coordinate, eyeDistance);

    // 小脸 TODO 眼睛到下巴图像线性缩小

    // 下巴
    coordinate = chinChange(coordinate, eyeDistance);

    // 额头

    // 大眼
    float eyeEnlarge = reshapeIntensity[INDEX_EYE_ENLARGE] * 0.12; // 放大倍数
    if (eyeEnlarge > 0.0) {
        float radius = eyeDistance * 0.33; // 眼睛放大半径
        coordinate = enlargeEye(coordinate, cartesianPoints[74] + (cartesianPoints[77] - cartesianPoints[74]) * 0.05, radius, eyeEnlarge);
        coordinate = enlargeEye(coordinate, cartesianPoints[77] + (cartesianPoints[74] - cartesianPoints[77]) * 0.05, radius, eyeEnlarge);
    }

    // 眼距

    // 眼角

    // 瘦鼻

    // 鼻翼

    // 长鼻

    // 嘴型

    // 转变回原来的纹理坐标系
    coordinate = coordinate / vec2(float(textureWidth), float(textureHeight));
    // 输出图像
    gl_FragColor = texture2D(inputTexture, coordinate);
}