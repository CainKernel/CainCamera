precision mediump float;
 
varying mediump vec2 textureCoordinate;
 
uniform sampler2D inputTexture;
uniform sampler2D blowoutTexture; //blowout;
uniform sampler2D overlayTexture; //overlay;
uniform sampler2D mapTexture; //map
 
uniform float strength;

void main()
{
    vec4 originColor = texture2D(inputTexture, textureCoordinate.xy);
    vec4 texel = texture2D(inputTexture, textureCoordinate.xy);
    vec3 bbTexel = texture2D(blowoutTexture, textureCoordinate.xy).rgb;

    texel.r = texture2D(overlayTexture, vec2(bbTexel.r, texel.r)).r;
    texel.g = texture2D(overlayTexture, vec2(bbTexel.g, texel.g)).g;
    texel.b = texture2D(overlayTexture, vec2(bbTexel.b, texel.b)).b;

    vec4 mapped;
    mapped.r = texture2D(mapTexture, vec2(texel.r, 0.16666)).r;
    mapped.g = texture2D(mapTexture, vec2(texel.g, 0.5)).g;
    mapped.b = texture2D(mapTexture, vec2(texel.b, 0.83333)).b;
    mapped.a = 1.0;

    mapped.rgb = mix(originColor.rgb, mapped.rgb, strength);

    gl_FragColor = mapped;
 }