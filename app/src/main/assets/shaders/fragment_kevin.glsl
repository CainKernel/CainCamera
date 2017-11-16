 precision mediump float;

 varying mediump vec2 textureCoordinate;

 uniform sampler2D inputTexture;
 uniform sampler2D mapTexture;

 void main()
 {
     vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;

     vec2 lookup;
     lookup.y = .5;

     lookup.x = texel.r;
     texel.r = texture2D(mapTexture, lookup).r;

     lookup.x = texel.g;
     texel.g = texture2D(mapTexture, lookup).g;

     lookup.x = texel.b;
     texel.b = texture2D(mapTexture, lookup).b;

     gl_FragColor = vec4(texel, 1.0);
 }
