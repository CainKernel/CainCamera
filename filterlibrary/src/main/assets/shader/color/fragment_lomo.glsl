precision mediump float;
 
 varying mediump vec2 textureCoordinate;
 
 uniform sampler2D inputTexture;
 uniform sampler2D mapTexture;
 uniform sampler2D vignetteTexture;
 
 uniform float strength;

 void main()
 {
     vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
     vec3 textureColor = sourceColor.rgb;

     vec2 red = vec2(textureColor.r, 0.16666);
     vec2 green = vec2(textureColor.g, 0.5);
     vec2 blue = vec2(textureColor.b, 0.83333);

     textureColor.rgb = vec3(
                      texture2D(mapTexture, red).r,
                      texture2D(mapTexture, green).g,
                      texture2D(mapTexture, blue).b);

     vec2 tc = (2.0 * textureCoordinate) - 1.0;
     float d = dot(tc, tc);
     vec2 lookup = vec2(d, textureColor.r);
     textureColor.r = texture2D(vignetteTexture, lookup).r;
     lookup.y = textureColor.g;
     textureColor.g = texture2D(vignetteTexture, lookup).g;
     lookup.y = textureColor.b;
     textureColor.b	= texture2D(vignetteTexture, lookup).b;

     textureColor.rgb = mix(sourceColor.rgb, textureColor.rgb, strength);

     gl_FragColor = vec4(textureColor, 1.0);
 }