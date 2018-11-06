 precision mediump float;

 varying mediump vec2 textureCoordinate;

 uniform sampler2D inputTexture;
 uniform sampler2D curveTexture; //earlyBirdCurves
 uniform sampler2D overlayTexture; //earlyBirdOverlay
 uniform sampler2D vignetteTexture; //vig
 uniform sampler2D blowoutTexture; //earlyBirdBlowout
 uniform sampler2D mapTexture; //earlyBirdMap

uniform float strength;

 const mat3 saturate = mat3(1.210300,  -0.089700, -0.091000,
                            -0.176100,  1.123900, -0.177400,
                            -0.034200, -0.034200, 1.265800);
 
 const vec3 rgbPrime = vec3(0.25098, 0.14640522, 0.0);
 const vec3 desaturate = vec3(.3, .59, .11);

 void main()
 {

     vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
     vec3 textureColor = sourceColor.rgb;


     vec2 lookup;
     lookup.y = 0.5;

     lookup.x = textureColor.r;
     textureColor.r = texture2D(curveTexture, lookup).r;

     lookup.x = textureColor.g;
     textureColor.g = texture2D(curveTexture, lookup).g;

     lookup.x = textureColor.b;
     textureColor.b = texture2D(curveTexture, lookup).b;

     float desaturatedColor;
     vec3 result;
     desaturatedColor = dot(desaturate, textureColor);


     lookup.x = desaturatedColor;
     result.r = texture2D(overlayTexture, lookup).r;
     lookup.x = desaturatedColor;
     result.g = texture2D(overlayTexture, lookup).g;
     lookup.x = desaturatedColor;
     result.b = texture2D(overlayTexture, lookup).b;

     textureColor = saturate * mix(textureColor, result, .5);

     vec2 tc = (2.0 * textureCoordinate) - 1.0;
     float d = dot(tc, tc);

     vec3 sampled;
     lookup.y = .5;

     lookup = vec2(d, textureColor.r);
     textureColor.r = texture2D(vignetteTexture, lookup).r;
     lookup.y = textureColor.g;
     textureColor.g = texture2D(vignetteTexture, lookup).g;
     lookup.y = textureColor.b;
     textureColor.b	= texture2D(vignetteTexture, lookup).b;
     float value = smoothstep(0.0, 1.25, pow(d, 1.35)/1.65);

     

     lookup.x = textureColor.r;
     sampled.r = texture2D(blowoutTexture, lookup).r;
     lookup.x = textureColor.g;
     sampled.g = texture2D(blowoutTexture, lookup).g;
     lookup.x = textureColor.b;
     sampled.b = texture2D(blowoutTexture, lookup).b;
     textureColor = mix(sampled, textureColor, value);


     lookup.x = textureColor.r;
     textureColor.r = texture2D(mapTexture, lookup).r;
     lookup.x = textureColor.g;
     textureColor.g = texture2D(mapTexture, lookup).g;
     lookup.x = textureColor.b;
     textureColor.b = texture2D(mapTexture, lookup).b;

     gl_FragColor = mix(sourceColor, vec4(textureColor, 1.0), strength);
 }
