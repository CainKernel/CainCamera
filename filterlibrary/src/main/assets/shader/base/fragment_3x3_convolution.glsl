precision highp float;

uniform sampler2D inputTexture;

uniform mediump mat3 convolutionMatrix;

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;

varying vec2 topTextureCoordinate;
varying vec2 topLeftTextureCoordinate;
varying vec2 topRightTextureCoordinate;

varying vec2 bottomTextureCoordinate;
varying vec2 bottomLeftTextureCoordinate;
varying vec2 bottomRightTextureCoordinate;

void main()
{
    mediump vec4 bottomColor = texture2D(inputTexture, bottomTextureCoordinate);
    mediump vec4 bottomLeftColor = texture2D(inputTexture, bottomLeftTextureCoordinate);
    mediump vec4 bottomRightColor = texture2D(inputTexture, bottomRightTextureCoordinate);
    mediump vec4 centerColor = texture2D(inputTexture, textureCoordinate);
    mediump vec4 leftColor = texture2D(inputTexture, leftTextureCoordinate);
    mediump vec4 rightColor = texture2D(inputTexture, rightTextureCoordinate);
    mediump vec4 topColor = texture2D(inputTexture, topTextureCoordinate);
    mediump vec4 topRightColor = texture2D(inputTexture, topRightTextureCoordinate);
    mediump vec4 topLeftColor = texture2D(inputTexture, topLeftTextureCoordinate);

    mediump vec4 resultColor = topLeftColor * convolutionMatrix[0][0] + topColor * convolutionMatrix[0][1] + topRightColor * convolutionMatrix[0][2];
    resultColor += leftColor * convolutionMatrix[1][0] + centerColor * convolutionMatrix[1][1] + rightColor * convolutionMatrix[1][2];
    resultColor += bottomLeftColor * convolutionMatrix[2][0] + bottomColor * convolutionMatrix[2][1] + bottomRightColor * convolutionMatrix[2][2];

    gl_FragColor = resultColor;
}
