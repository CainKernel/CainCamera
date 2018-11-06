
precision highp float;

varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D maskTexture;  //grey1Frame
uniform sampler2D maskTexture1;  //grey2Frame
uniform sampler2D curveTexture;  //curve

uniform float strength;

const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

void main()
{
	lowp float satura = 0.5;
	
	highp float redCurveValue;
	highp float greenCurveValue;
	highp float blueCurveValue;

	lowp vec4 sourceColor = texture2D( inputTexture, textureCoordinate.xy);
	lowp vec4 textureColor = sourceColor;
	
	vec4 grey1Color = texture2D(maskTexture, textureCoordinate.xy);
	vec4 grey2Color = texture2D(maskTexture1, textureCoordinate.xy);

	lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
	lowp vec3 greyScaleColor = vec3(luminance);
	
	textureColor = vec4(mix(greyScaleColor, textureColor.rgb, satura), textureColor.w); 

	redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).r;
	redCurveValue = texture2D(curveTexture, vec2(redCurveValue, 1.0/2.0)).r;
	
	greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).g;
	greenCurveValue = texture2D(curveTexture, vec2(greenCurveValue, 1.0/2.0)).g;
	
	blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).b;
	blueCurveValue = texture2D(curveTexture, vec2(blueCurveValue, 1.0/2.0)).b;
	blueCurveValue = texture2D(curveTexture, vec2(blueCurveValue, 1.0/2.0)).g;
	
	lowp vec4 base = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	
	redCurveValue = texture2D(curveTexture, vec2(redCurveValue, 1.0)).r;
	greenCurveValue = texture2D(curveTexture, vec2(greenCurveValue, 1.0)).r;
	blueCurveValue = texture2D(curveTexture, vec2(blueCurveValue, 1.0)).r;
	lowp vec4 overlayer = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	base = (base - overlayer) * (1.0 - grey1Color.r) + overlayer;
	
	redCurveValue = texture2D(curveTexture, vec2(base.r, 1.0)).g;
	greenCurveValue = texture2D(curveTexture, vec2(base.g, 1.0)).g;
	blueCurveValue = texture2D(curveTexture, vec2(base.b, 1.0)).g;
	overlayer = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	
	textureColor = (base - overlayer) * (1.0 - grey2Color.r) + overlayer;

	gl_FragColor = mix(sourceColor, textureColor, strength);;
}