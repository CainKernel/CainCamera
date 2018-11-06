 precision mediump float;
 
 varying mediump vec2 textureCoordinate;
 
 uniform sampler2D inputTexture;
 uniform sampler2D edgeBurnTexture;  //edgeBurn
 uniform sampler2D mapTexture;  //hefeMap
 uniform sampler2D gradientMapTexture;  //hefeGradientMap
 uniform sampler2D softLightTexture;  //hefeSoftLight
 uniform sampler2D metalTexture;  //hefeMetal
 
 uniform float strength;

 void main() {
    vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
    vec3 textureColor = sourceColor.rgb;
    vec3 edgeColor = texture2D(edgeBurnTexture, textureCoordinate).rgb;
    textureColor = textureColor * edgeColor;
    
    textureColor = vec3(
                 texture2D(mapTexture, vec2(textureColor.r, .16666)).r,
                 texture2D(mapTexture, vec2(textureColor.g, .5)).g,
                 texture2D(mapTexture, vec2(textureColor.b, .83333)).b);
    
    vec3 luma = vec3(.30, .59, .11);
    vec3 gradSample = texture2D(gradientMapTexture, vec2(dot(luma, textureColor), .5)).rgb;
    vec3 final = vec3(
                      texture2D(softLightTexture, vec2(gradSample.r, textureColor.r)).r,
                      texture2D(softLightTexture, vec2(gradSample.g, textureColor.g)).g,
                      texture2D(softLightTexture, vec2(gradSample.b, textureColor.b)).b
                      );
    
    vec3 metal = texture2D(metalTexture, textureCoordinate).rgb;
    vec3 metaled = vec3(
                        texture2D(softLightTexture, vec2(metal.r, textureColor.r)).r,
                        texture2D(softLightTexture, vec2(metal.g, textureColor.g)).g,
                        texture2D(softLightTexture, vec2(metal.b, textureColor.b)).b
                        );
    
    metaled.rgb = mix(sourceColor.rgb, metaled.rgb, strength);

    gl_FragColor = vec4(metaled, 1.0);
 }