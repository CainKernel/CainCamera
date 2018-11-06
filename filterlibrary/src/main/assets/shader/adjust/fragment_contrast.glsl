varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform lowp float contrast;

void main()
{
     lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
     gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
}