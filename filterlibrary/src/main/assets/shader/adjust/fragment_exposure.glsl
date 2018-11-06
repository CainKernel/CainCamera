varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform highp float exposure;

void main()
{
   highp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
   gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);
}