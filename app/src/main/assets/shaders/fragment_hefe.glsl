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
    vec4 originColor = texture2D(inputTexture, textureCoordinate);
    vec3 texel = texture2D(inputTexture, textureCoordinate).rgb;
    vec3 edge = texture2D(edgeBurnTexture, textureCoordinate).rgb;
    texel = texel * edge;
    
    texel = vec3(
                 texture2D(mapTexture, vec2(texel.r, .16666)).r,
                 texture2D(mapTexture, vec2(texel.g, .5)).g,
                 texture2D(mapTexture, vec2(texel.b, .83333)).b);
    
    vec3 luma = vec3(.30, .59, .11);
    vec3 gradSample = texture2D(gradientMapTexture, vec2(dot(luma, texel), .5)).rgb;
    vec3 final = vec3(
                      texture2D(softLightTexture, vec2(gradSample.r, texel.r)).r,
                      texture2D(softLightTexture, vec2(gradSample.g, texel.g)).g,
                      texture2D(softLightTexture, vec2(gradSample.b, texel.b)).b
                      );
    
    vec3 metal = texture2D(metalTexture, textureCoordinate).rgb;
    vec3 metaled = vec3(
                        texture2D(softLightTexture, vec2(metal.r, texel.r)).r,
                        texture2D(softLightTexture, vec2(metal.g, texel.g)).g,
                        texture2D(softLightTexture, vec2(metal.b, texel.b)).b
                        );
    
    metaled.rgb = mix(originColor.rgb, metaled.rgb, strength);

    gl_FragColor = vec4(metaled, 1.0);
 }