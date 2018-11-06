 precision highp float;
 varying mediump vec2 textureCoordinate;
 
 uniform sampler2D inputTexture;
 uniform sampler2D randTexture;
 
 uniform float texelWidthOffset;
 uniform float texelHeightOffset;
 
 float texture2Size = 1024.0;
 
 uniform float strength;

 float colorGray(vec4 color) {
    float gray = 0.2125 * color.r + 0.7154 * color.g + 0.0721 * color.b;
    
    return gray;
 }
 

 vec4 toneMapping(vec4 color) {
    
    vec4 mapped;
    mapped.r = texture2D(randTexture, vec2(color.r, 0.0)).r;
    mapped.g = texture2D(randTexture, vec2(color.g, 0.0)).g;
    mapped.b = texture2D(randTexture, vec2(color.b, 0.0)).b;
    mapped.a = color.a;
    
    return mapped;
 }
 
 vec4 colorControl(vec4 color, float saturation, float brightness, float contrast) {
    float gray = colorGray(color);
    
    color.rgb = vec3(saturation) * color.rgb + vec3(1.0-saturation) * vec3(gray);
    color.r = clamp(color.r, 0.0, 1.0);
    color.g = clamp(color.g, 0.0, 1.0);
    color.b = clamp(color.b, 0.0, 1.0);
    
    color.rgb = vec3(contrast) * (color.rgb - vec3(0.5)) + vec3(0.5);
    color.r = clamp(color.r, 0.0, 1.0);
    color.g = clamp(color.g, 0.0, 1.0);
    color.b = clamp(color.b, 0.0, 1.0);
    
    color.rgb = color.rgb + vec3(brightness);
    color.r = clamp(color.r, 0.0, 1.0);
    color.g = clamp(color.g, 0.0, 1.0);
    color.b = clamp(color.b, 0.0, 1.0);
    
    return color;
 }

 vec4 hueAdjust(vec4 color, float hueAdjust) {
    vec3 kRGBToYPrime = vec3(0.299, 0.587, 0.114);
    vec3 kRGBToI = vec3(0.595716, -0.274453, -0.321263);
    vec3 kRGBToQ = vec3(0.211456, -0.522591, 0.31135);
    
    vec3 kYIQToR   = vec3(1.0, 0.9563, 0.6210);
    vec3 kYIQToG   = vec3(1.0, -0.2721, -0.6474);
    vec3 kYIQToB   = vec3(1.0, -1.1070, 1.7046);
    
    float yPrime = dot(color.rgb, kRGBToYPrime);
    float I = dot(color.rgb, kRGBToI);
    float Q = dot(color.rgb, kRGBToQ);
    
    float hue = atan(Q, I);
    float chroma  = sqrt (I * I + Q * Q);
    
    hue -= hueAdjust;
    
    Q = chroma * sin (hue);
    I = chroma * cos (hue);
    
    color.r = dot(vec3(yPrime, I, Q), kYIQToR);
    color.g = dot(vec3(yPrime, I, Q), kYIQToG);
    color.b = dot(vec3(yPrime, I, Q), kYIQToB);
    
    return color;
 }
 
 vec4 colorMatrix(vec4 color, float red, float green, float blue, float alpha, vec4 bias) {
    color = color * vec4(red, green, blue, alpha) + bias;
    
    return color;
}
 

 vec4 multiplyBlend(vec4 overlay, vec4 base) {
    vec4 outputColor;
    
    float a = overlay.a + base.a * (1.0 - overlay.a);

    outputColor.rgb = ((1.0-base.a) * overlay.rgb * overlay.a + (1.0-overlay.a) * base.rgb * base.a + overlay.a * base.a * overlay.rgb * base.rgb) / a;
    
    outputColor.a = a;
    
    return outputColor;
 }
 

 float pseudoRandom(vec2 co) {
    mediump float a = 12.9898;
    mediump float b = 78.233;
    mediump float c = 43758.5453;
    mediump float dt= dot(co.xy ,vec2(a,b));
    mediump float sn= mod(dt,3.14);
    return fract(sin(sn) * c);
 }
 
 void main() {
    vec4 sourceColor = texture2D(inputTexture, textureCoordinate.xy);
    vec4 textureColor = sourceColor;
    
    textureColor.a = 1.0;

    textureColor = colorControl(textureColor, 0.5, 0.1, 0.9);

	float x = textureCoordinate.x * texelWidthOffset / texture2Size;
    float y = textureCoordinate.y * texelHeightOffset / texture2Size;

    vec4 rd = texture2D(randTexture, textureCoordinate);
    rd = colorControl(rd, 1.0, 0.4, 1.2);

    textureColor = multiplyBlend(rd, textureColor);

    textureColor = colorMatrix(textureColor, 1.0, 1.0, 1.0, 1.0, vec4(-0.15, -0.15, -0.15, 0));
    
    textureColor.rgb = mix(sourceColor.rgb, textureColor.rgb, strength);
    gl_FragColor = textureColor;
}