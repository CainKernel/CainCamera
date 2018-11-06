attribute vec4 aPosition;
attribute vec4 aTextureCoord;

uniform highp float texelWidth; 
uniform highp float texelHeight; 

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
    gl_Position = aPosition;

    vec2 widthStep = vec2(texelWidth, 0.0);
    vec2 heightStep = vec2(0.0, texelHeight);
    vec2 widthHeightStep = vec2(texelWidth, texelHeight);
    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);

    textureCoordinate = aTextureCoord.xy;
    leftTextureCoordinate = aTextureCoord.xy - widthStep;
    rightTextureCoordinate = aTextureCoord.xy + widthStep;

    topTextureCoordinate = aTextureCoord.xy - heightStep;
    topLeftTextureCoordinate = aTextureCoord.xy - widthHeightStep;
    topRightTextureCoordinate = aTextureCoord.xy + widthNegativeHeightStep;

    bottomTextureCoordinate = aTextureCoord.xy + heightStep;
    bottomLeftTextureCoordinate = aTextureCoord.xy - widthNegativeHeightStep;
    bottomRightTextureCoordinate = aTextureCoord.xy + widthHeightStep;
}
