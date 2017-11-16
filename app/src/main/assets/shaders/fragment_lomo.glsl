precision mediump float;
 
 varying mediump vec2 textureCoordinate;
 
 uniform sampler2D inputTexture;
 uniform sampler2D mapTexture;
 uniform sampler2D vignetteTexture;
 
 uniform float strength;

 void main()
 {
     vec4 originColor = texture2D(inputTexture, textureCoordinate);
     vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;

     vec2 red = vec2(texel.r, 0.16666);
     vec2 green = vec2(texel.g, 0.5);
     vec2 blue = vec2(texel.b, 0.83333);

     texel.rgb = vec3(
                      texture2D(mapTexture, red).r,
                      texture2D(mapTexture, green).g,
                      texture2D(mapTexture, blue).b);

     vec2 tc = (2.0 * textureCoordinate) - 1.0;
     float d = dot(tc, tc);
     vec2 lookup = vec2(d, texel.r);
     texel.r = texture2D(vignetteTexture, lookup).r;
     lookup.y = texel.g;
     texel.g = texture2D(vignetteTexture, lookup).g;
     lookup.y = texel.b;
     texel.b	= texture2D(vignetteTexture, lookup).b;

     texel.rgb = mix(originColor.rgb, texel.rgb, strength);

     gl_FragColor = vec4(texel,1.0);
 }