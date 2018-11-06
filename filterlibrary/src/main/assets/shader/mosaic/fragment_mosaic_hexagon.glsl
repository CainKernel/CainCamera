// 六边形马赛克
// 参考文章：https://blog.csdn.net/simpledrunk/article/details/17095821

precision highp float;
uniform sampler2D inputTexture;
varying vec2 textureCoordinate;

uniform float mosaicSize;   // 马赛克大小

void main (void)
{
    float length = mosaicSize;
    float TR = 0.866025;
    float x = textureCoordinate.x;
    float y = textureCoordinate.y;
    int wx = int(x / 1.5 / length);
    int wy = int(y / TR / length);
    vec2 v1, v2, vn;
    if (wx/2 * 2 == wx) {
        if (wy/2 * 2 == wy) {
            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy));
            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy + 1));
        } else {
            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy + 1));
            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy));
        }
    } else {
        if (wy/2 * 2 == wy) {
            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy + 1));
            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy));
        } else {
            v1 = vec2(length * 1.5 * float(wx), length * TR * float(wy));
            v2 = vec2(length * 1.5 * float(wx + 1), length * TR * float(wy + 1));
        }
    }
    float s1 = sqrt(pow(v1.x - x, 2.0) + pow(v1.y - y, 2.0));
    float s2 = sqrt(pow(v2.x - x, 2.0) + pow(v2.y - y, 2.0));
    if (s1 < s2) {
        vn = v1;
    } else {
        vn = v2;
    }
    vec4  color = texture2D(inputTexture, vn);
    gl_FragColor = color;
}