// rgb抖动滤镜
precision highp float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;

uniform float scale;

void main()
{
    vec2 uv = textureCoordinate.xy;
    vec2 scaleCoordinate = vec2((scale - 1.0) * 0.5 + uv.x / scale ,
                                (scale - 1.0) * 0.5 + uv.y / scale);
    vec4 smoothColor = texture2D(inputTexture, scaleCoordinate);

    // 计算红色通道偏移值
    vec4 shiftRedColor = texture2D(inputTexture,
         scaleCoordinate + vec2(-0.1 * (scale - 1.0), - 0.1 *(scale - 1.0)));

    // 计算绿色通道偏移值
    vec4 shiftGreenColor = texture2D(inputTexture,
         scaleCoordinate + vec2(-0.075 * (scale - 1.0), - 0.075 *(scale - 1.0)));

    // 计算蓝色偏移值
    vec4 shiftBlueColor = texture2D(inputTexture,
         scaleCoordinate + vec2(-0.05 * (scale - 1.0), - 0.05 *(scale - 1.0)));

    vec3 resultColor = vec3(shiftRedColor.r, shiftGreenColor.g, shiftBlueColor.b);

    gl_FragColor = vec4(resultColor, smoothColor.a);
}