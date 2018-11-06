// 圆形马赛克
precision highp float;
uniform sampler2D inputTexture;
varying vec2 textureCoordinate;

uniform float imageWidth;     // 图片宽度
uniform float imageHeight;    // 图片高度
uniform float mosaicSize;

void main(void)
{
    vec2 texSize = vec2(imageWidth, imageHeight);
    // 计算实际图像位置
    vec2 xy = vec2(textureCoordinate.x * texSize.x, textureCoordinate.y * texSize.y);
    // 计算某一个小mosaic的中心坐标
    vec2 mosaicCenter = vec2(floor(xy.x / mosaicSize) * mosaicSize + 0.5 * mosaicSize,
                         floor(xy.y / mosaicSize) * mosaicSize + 0.5 * mosaicSize);
    // 计算距离中心的长度
    vec2 delXY = mosaicCenter - xy;
    float delLength = length(delXY);
    // 换算回纹理坐标系
    vec2 uvMosaic = vec2(mosaicCenter.x / texSize.x, mosaicCenter.y / texSize.y);

    vec4 color;
    if (delLength < 0.5 * mosaicSize) {
        color = texture2D(inputTexture, uvMosaic);
    } else {
        color = texture2D(inputTexture, textureCoordinate);
    }
    gl_FragColor = color;
}
