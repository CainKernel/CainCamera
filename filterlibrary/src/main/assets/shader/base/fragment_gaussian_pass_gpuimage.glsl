// 高斯模糊(GPUImage中的shader)
precision mediump float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputTexture;
const lowp int GAUSSIAN_SAMPLES = 9;
varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];

void main()
{
	lowp vec3 sum = vec3(0.0);
   lowp vec4 fragColor=texture2D(inputTexture,textureCoordinate);

    sum += texture2D(inputTexture, blurCoordinates[0]).rgb * 0.05;
    sum += texture2D(inputTexture, blurCoordinates[1]).rgb * 0.09;
    sum += texture2D(inputTexture, blurCoordinates[2]).rgb * 0.12;
    sum += texture2D(inputTexture, blurCoordinates[3]).rgb * 0.15;
    sum += texture2D(inputTexture, blurCoordinates[4]).rgb * 0.18;
    sum += texture2D(inputTexture, blurCoordinates[5]).rgb * 0.15;
    sum += texture2D(inputTexture, blurCoordinates[6]).rgb * 0.12;
    sum += texture2D(inputTexture, blurCoordinates[7]).rgb * 0.09;
    sum += texture2D(inputTexture, blurCoordinates[8]).rgb * 0.05;

	gl_FragColor = vec4(sum, fragColor.a);
}