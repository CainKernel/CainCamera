precision highp float;
varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D curveTexture;//curve

uniform float strength;

void main()
{
	lowp vec4 sourceColor = texture2D( inputTexture, textureCoordinate.xy);
	lowp vec4 textureColor = sourceColor;

	highp float redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).r;
	highp float greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).g;
	highp float blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).b;

	redCurveValue = texture2D(curveTexture, vec2(redCurveValue, 0.0)).a;
	greenCurveValue = texture2D(curveTexture, vec2(greenCurveValue, 0.0)).a;
	blueCurveValue = texture2D(curveTexture, vec2(blueCurveValue, 0.0)).a;

	redCurveValue = redCurveValue * 1.25 - 0.12549;
	greenCurveValue = greenCurveValue * 1.25 - 0.12549; 
	blueCurveValue = blueCurveValue * 1.25 - 0.12549;

	textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	textureColor = (sourceColor - textureColor) * 0.549 + textureColor;
	
	gl_FragColor = mix(sourceColor, textureColor, strength);
} 
  