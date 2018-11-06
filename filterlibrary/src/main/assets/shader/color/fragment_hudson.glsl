 precision mediump float;

 varying mediump vec2 textureCoordinate;
 
 uniform sampler2D inputTexture;
 uniform sampler2D blowoutTexture; //blowout;
 uniform sampler2D overlayTexture; //overlay;
 uniform sampler2D mapTexture; //map
 
 uniform float strength;
 
 void main()
 {
     vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
     
     vec4 textureColor = sourceColor;
     
     vec3 blowoutColor = texture2D(blowoutTexture, textureCoordinate).rgb;
     
     textureColor.r = texture2D(overlayTexture, vec2(blowoutColor.r, textureColor.r)).r;
     textureColor.g = texture2D(overlayTexture, vec2(blowoutColor.g, textureColor.g)).g;
     textureColor.b = texture2D(overlayTexture, vec2(blowoutColor.b, textureColor.b)).b;
     
     vec4 mapColor;
     mapColor.r = texture2D(mapTexture, vec2(textureColor.r, .16666)).r;
     mapColor.g = texture2D(mapTexture, vec2(textureColor.g, .5)).g;
     mapColor.b = texture2D(mapTexture, vec2(textureColor.b, .83333)).b;
     mapColor.a = 1.0;
     
     mapColor.rgb = mix(sourceColor.rgb, mapColor.rgb, strength);

     gl_FragColor = mapColor;
 }