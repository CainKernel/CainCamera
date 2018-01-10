precision highp float;
varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D curveTexture;//curve

void main()
{ 
	lowp vec4 textureColor;
	lowp vec4 textureColorOri;
	
	float xCoordinate = textureCoordinate.x;
	float yCoordinate = textureCoordinate.y;
	
	highp float redCurveValue;
	highp float greenCurveValue;
	highp float blueCurveValue;
	
	textureColor = texture2D( inputTexture, vec2(xCoordinate, yCoordinate));
	textureColorOri = textureColor;

	redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).r;
	greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).g;
	blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).b;

	redCurveValue = texture2D(curveTexture, vec2(redCurveValue, 0.0)).a;
	greenCurveValue = texture2D(curveTexture, vec2(greenCurveValue, 0.0)).a;
	blueCurveValue = texture2D(curveTexture, vec2(blueCurveValue, 0.0)).a;

	redCurveValue = redCurveValue * 1.25 - 0.12549;
	greenCurveValue = greenCurveValue * 1.25 - 0.12549; 
	blueCurveValue = blueCurveValue * 1.25 - 0.12549;

	textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0);
	textureColor = (textureColorOri - textureColor) * 0.549 + textureColor;
	
	gl_FragColor = vec4(textureColor.r, textureColor.g, textureColor.b, 1.0);
} 
  