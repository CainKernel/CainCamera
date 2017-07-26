// mls(Image Deformation Using Moving Least Squares)方法计算人脸变形
attribute vec4 aPosition;
attribute vec4 aTextureCoord;

#define MAX_ASSIGN_POINT 106
#define M_PI_HALF 1.57079632679
#define imageWidth fDst[101].x
#define imageHeight fDst[101].y
#define xCenter fSrc[64].x
#define yCenter fSrc[64].y

uniform int mPoint; // 点数
uniform vec2 fSrc[MAX_ASSIGN_POINT]; // 原始点
uniform vec2 fDst[MAX_ASSIGN_POINT]; // 目标点

uniform mat4 uMVPMatrix;  // MVP 的变换矩阵（整体变形）
uniform mat4 uTexMatrix;  // Texture 的变换矩阵 （只对texture变形）
varying vec2 textureCoord;
void main() {
    // 如果点数小于0，则直接输出
    if (mPoint < 0) {
        gl_Position = uMVPMatrix * aPosition;
        textureCoord = (uTexMatrix * aTextureCoord).xy;
        return;
    }
    vec4 rawPosition = aPosition;
    rawPosition.x = (aPosition.x + 1.0) * imageWidth / 2.0;
    rawPosition.y = (aPosition.y + 1.0) * imageHeight / 2.0;

    vec2 swq;
    vec2 qstar;
    vec2 newP;
    vec2 tempP;
    float sw;
    float w[MAX_ASSIGN_POINT];

    vec2 swp;
    vec2 pstar;
    vec2 curV;
    vec2 curVJ;
    vec2 Pi;
    vec2 PiJ;
    float miu_s;

    float i = rawPosition.x;
    float j = rawPosition.y;
    sw = 0.0;
    swp.x = swp.y = 0.0;
    swq.x = swq.y = 0.0;
    newP.x = newP.y = 0.0;
    curV.x = i;
    curV.y = j;
    // 1、计算平方
    for(int k = 0; k < mPoint - 1; k++) {
        w[k] = 1.0 / ((i - fSrc[k].x) * (i - fSrc[k].x)
        + (j - fSrc[k].y) * (j - fSrc[k].y));
        sw = sw + w[k];
        swp = swp + w[k] * fSrc[k];
        swq = swq + w[k] * fDst[k];
    }
    pstar = 1.0 / sw * swp;
    qstar = 1.0 / sw * swq;

    miu_s = 0.0;
    for (int k = 0; k < mPoint - 1; k++) {
        Pi = fSrc[k] - pstar;
        miu_s += w[k] * dot(Pi, Pi);
    }

    curV -= pstar;
    curVJ.x = - curV.y;
    curVJ.y = curV.x;

    for (int k = 0; k < mPoint - 1; k++) {
        Pi = fSrc[k] - pstar;
        PiJ.x = -Pi.y;
        PiJ.y = Pi.x;

        tempP.x = dot(Pi, curV) * fDst[k].x -
         dot(PiJ, curV) * fDst[k].y;
         tempP.y = -dot(Pi, curVJ) * fDst[k].x +
         dot(PiJ, curVJ) * fDst[k].y;
         tempP *= w[k] / miu_s;
         newP += tempP;
    }
    newP += qstar;

    vec4 nPosition = rawPosition;
    nPosition.x = newP.x;
    nPosition.y = newP.y;

    i = nPosition.x - rawPosition.x;
    j = nPosition.y - rawPosition.y;

    if (rawPosition.x < xCenter) {
        i = sin(rawPosition.x * M_PI_HALF / xCenter) * i;
    } else {
        i = (i * sin((imageWidth - rawPosition.x) * M_PI_HALF / (imageWidth - xCenter)));
    }
    if (rawPosition.y < yCenter) {
        j = sin(rawPosition.y * M_PI_HALF / yCenter) * j;
    } else {
        j = (j * sin((imageHeight - rawPosition.y) * M_PI_HALF / (imageHeight - yCenter)));
    }
    nPosition.x = rawPosition.x + i;
    nPosition.y = rawPosition.y + j;

    if (rawPosition.x <= 0.1) {
        nPosition.x = 0.0;
    }
    if (rawPosition.x >= imageWidth - 0.1) {
        nPosition.x = imageWidth;
    }
    if (rawPosition.y <= 0.1) {
        nPosition.y = 0.0;
    }
    if (rawPosition.y >= imageHeight - 0.1) {
        nPosition.y = imageHeight;
    }
    nPosition.x = (nPosition.x * 2.0 / imageWidth) - 1.0;
    nPosition.y = (nPosition.y * 2.0 / imageHeight) - 1.0;
    gl_Position = nPosition;
    textureCoord = (uTexMatrix * aTextureCoord).xy;
}