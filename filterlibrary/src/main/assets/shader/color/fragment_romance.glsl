precision highp float;
varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform sampler2D curveTexture;//curve

uniform float strength;

void main()
{
	mediump float satVal = 115.0 / 100.0;

    lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
	lowp vec4 textureColor = sourceColor;

	// step1. screen blending 
	textureColor = 1.0 - ((1.0 - sourceColor) * (1.0 - sourceColor));
	textureColor = (textureColor - sourceColor) + sourceColor;

	// step2. curve 
	highp float redCurveValue = texture2D(curveTexture, vec2(textureColor.r, 0.0)).r;
	highp float greenCurveValue = texture2D(curveTexture, vec2(textureColor.g, 0.0)).g;
    highp float blueCurveValue = texture2D(curveTexture, vec2(textureColor.b, 0.0)).b;

	// step3. saturation 
	highp float G = (redCurveValue + greenCurveValue + blueCurveValue); 
	G = G / 3.0; 

    redCurveValue = ((1.0 - satVal) * G + satVal * redCurveValue); 
	greenCurveValue = ((1.0 - satVal) * G + satVal * greenCurveValue); 
	blueCurveValue = ((1.0 - satVal) * G + satVal * blueCurveValue); 

	textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); 

    gl_FragColor = mix(sourceColor, textureColor, strength);
}
