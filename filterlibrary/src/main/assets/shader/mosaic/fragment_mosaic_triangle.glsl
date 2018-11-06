// 三角形马赛克效果
// 参考文章：https://blog.csdn.net/simpledrunk/article/details/17170965

precision highp float;
uniform sampler2D inputTexture;
varying vec2 textureCoordinate;

// len 是六边形的边长
uniform float mosaicSize;

void main (void){
    const float TR = 0.866025;  // .5*(3)^.5
    const float PI6 = 0.523599; // PI/6

    float x = textureCoordinate.x;
    float y = textureCoordinate.y;

    // 1.5*len 是矩形矩阵的长，TR*len 是宽
    // 计算矩形矩阵的顶点坐标 (0,0)(0,1)(1,0)(1,1)
    int wx = int(x/(1.5 * mosaicSize));
    int wy = int(y/(TR * mosaicSize));

    vec2 v1, v2, vn;

    // 判断是矩形的哪个顶点，上半部还是下半部
    if (wx / 2 * 2 == wx) {
        if (wy/2 * 2 == wy) {
            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy));
            v2 = vec2(mosaicSize * 1.5 * float(wx + 1), mosaicSize * TR * float(wy + 1));
        } else {
            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy + 1));
            v2 = vec2(mosaicSize * 1.5 * float(wx + 1), mosaicSize * TR * float(wy));
        }
    } else {
        if (wy/2 * 2 == wy) {
            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy + 1));
            v2 = vec2(mosaicSize * 1.5 * float(wx+1), mosaicSize * TR * float(wy));
        } else {
            v1 = vec2(mosaicSize * 1.5 * float(wx), mosaicSize * TR * float(wy));
            v2 = vec2(mosaicSize * 1.5 * float(wx + 1), mosaicSize * TR * float(wy+1));
        }
    }
    // 计算参考点与当前纹素的距离
    float s1 = sqrt(pow(v1.x - x, 2.0) + pow(v1.y - y, 2.0));
    float s2 = sqrt(pow(v2.x - x, 2.0) + pow(v2.y - y, 2.0));
    // 选择距离小的参考点
    if (s1 < s2) {
        vn = v1;
    } else {
        vn = v2;
    }

    vec4 mid = texture2D(inputTexture, vn);
    float a = atan((x - vn.x)/(y - vn.y)); // 计算夹角
    // 分别计算六个三角形的中心点坐标，之后将作为参考点
    vec2 area1 = vec2(vn.x, vn.y - mosaicSize * TR / 2.0);
    vec2 area2 = vec2(vn.x + mosaicSize / 2.0, vn.y - mosaicSize * TR / 2.0);
    vec2 area3 = vec2(vn.x + mosaicSize / 2.0, vn.y + mosaicSize * TR / 2.0);
    vec2 area4 = vec2(vn.x, vn.y + mosaicSize * TR / 2.0);
    vec2 area5 = vec2(vn.x - mosaicSize / 2.0, vn.y + mosaicSize * TR / 2.0);
    vec2 area6 = vec2(vn.x - mosaicSize / 2.0, vn.y - mosaicSize * TR / 2.0);

    // 根据夹角判断是哪个三角形
    if (a >= PI6 && a < PI6 * 3.0) {
        vn = area1;
    } else if (a >= PI6 * 3.0 && a < PI6 * 5.0) {
        vn = area2;
    } else if ((a >= PI6 * 5.0 && a <= PI6 * 6.0) || (a < -PI6 * 5.0 && a > -PI6 * 6.0)) {
        vn = area3;
    } else if (a < -PI6 * 3.0 && a >= -PI6 * 5.0) {
        vn = area4;
    } else if(a <= -PI6 && a> -PI6 * 3.0) {
        vn = area5;
    } else if (a > -PI6 && a < PI6) {
        vn = area6;
    }

    vec4 color = texture2D(inputTexture, vn);
    gl_FragColor = color;
}

