precision mediump float;

varying highp vec2 textureCoordinate;

uniform sampler2D inputTexture;
uniform float texelWidthOffset;
uniform float texelHeightOffset;

uniform float strength;

const highp vec3 W = vec3(0.299,0.587,0.114);

void main()
{
    float threshold = 0.0;
    //pic1
    vec4 sourceColor = texture2D(inputTexture, textureCoordinate);

    //pic2
    vec3 maxValue = vec3(0.0, 0.0, 0.0);

    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);

    for(int i = -2; i<=2; i++) {
        for(int j = -2; j<=2; j++) {
            vec4 tempColor = texture2D(inputTexture,
                                       textureCoordinate + singleStepOffset * vec2(i, j));
            maxValue.r = max(maxValue.r, tempColor.r);
            maxValue.g = max(maxValue.g, tempColor.g);
            maxValue.b = max(maxValue.b, tempColor.b);
            threshold += dot(tempColor.rgb, W);
        }
    }
    //pic3
    float gray1 = dot(sourceColor.rgb, W);

    //pic4
    float gray2 = dot(maxValue, W);

    //pic5
    float contour = gray1 / gray2;

    threshold = threshold / 25.;
    float alpha = max(1.0,gray1>threshold?1.0:(gray1/threshold));

    float result = contour * alpha + (1.0-alpha)*gray1;

    gl_FragColor = mix(sourceColor, vec4(vec3(result, result, result), sourceColor.w), strength);
}