attribute vec4 aPosition;
attribute vec4 aTextureCoord;

uniform float imageWidthFactor;
uniform float imageHeightFactor;
uniform float sharpness;

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;
varying vec2 topTextureCoordinate;
varying vec2 bottomTextureCoordinate;

varying float centerMultiplier;
varying float edgeMultiplier;
void main()
{
    gl_Position = aPosition;

    mediump vec2 widthStep = vec2(imageWidthFactor, 0.0);
    mediump vec2 heightStep = vec2(0.0, imageHeightFactor);

    textureCoordinate = aTextureCoord.xy;
    leftTextureCoordinate = aTextureCoord.xy - widthStep;
    rightTextureCoordinate = aTextureCoord.xy + widthStep;
    topTextureCoordinate = aTextureCoord.xy + heightStep;
    bottomTextureCoordinate = aTextureCoord.xy - heightStep;

    centerMultiplier = 1.0 + 4.0 * sharpness;
    edgeMultiplier = sharpness;
}