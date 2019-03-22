precision highp float;

varying highp vec2 textureCoordinate;
varying highp vec2 leftTextureCoordinate;
varying highp vec2 rightTextureCoordinate;
varying highp vec2 topTextureCoordinate;
varying highp vec2 bottomTextureCoordinate;

varying highp float centerMultiplier;
varying highp float edgeMultiplier;

uniform sampler2D inputTexture;

void main()
{
    mediump vec3 textureColor = texture2D(inputTexture, textureCoordinate).rgb;
    mediump vec3 leftTextureColor = texture2D(inputTexture, leftTextureCoordinate).rgb;
    mediump vec3 rightTextureColor = texture2D(inputTexture, rightTextureCoordinate).rgb;
    mediump vec3 topTextureColor = texture2D(inputTexture, topTextureCoordinate).rgb;
    mediump vec3 bottomTextureColor = texture2D(inputTexture, bottomTextureCoordinate).rgb;

    gl_FragColor = vec4((textureColor * centerMultiplier
               - (leftTextureColor * edgeMultiplier
               + rightTextureColor * edgeMultiplier
               + topTextureColor * edgeMultiplier
               + bottomTextureColor * edgeMultiplier)),
               texture2D(inputTexture, bottomTextureCoordinate).w);
}
