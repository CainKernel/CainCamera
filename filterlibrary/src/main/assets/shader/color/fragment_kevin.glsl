 precision mediump float;

 varying mediump vec2 textureCoordinate;

 uniform sampler2D inputTexture;
 uniform sampler2D mapTexture;

uniform float strength;

 void main()
 {
     vec4 sourceColor = texture2D(inputTexture, textureCoordinate);
     vec3 textureColor = sourceColor.rgb;

     vec2 lookup;
     lookup.y = .5;

     lookup.x = textureColor.r;
     textureColor.r = texture2D(mapTexture, lookup).r;

     lookup.x = textureColor.g;
     textureColor.g = texture2D(mapTexture, lookup).g;

     lookup.x = textureColor.b;
     textureColor.b = texture2D(mapTexture, lookup).b;

     gl_FragColor = mix(sourceColor, vec4(textureColor, 1.0), strength);
 }
