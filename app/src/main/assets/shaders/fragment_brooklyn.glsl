 precision mediump float;
 varying mediump vec2 textureCoordinate;
 
 uniform sampler2D inputTexture;
 uniform sampler2D curveTexture;
 uniform sampler2D mapTexture;
 uniform sampler2D curveTexture1;
 
 uniform float strength;

 /**
  * 灰度
  */
 float colorGray(vec4 color) {
    float gray = 0.2125 * color.r + 0.7154 * color.g + 0.0721 * color.b;
    
    return gray;
 }
 
 /**
  * 色调映射
  */
 vec4 toneMapping(vec4 color) {
    vec4 mapped;
    mapped.r = texture2D(curveTexture, vec2(color.r, 0.0)).r;
    mapped.g = texture2D(curveTexture, vec2(color.g, 0.0)).g;
    mapped.b = texture2D(curveTexture, vec2(color.b, 0.0)).b;
    mapped.a = color.a;
    
    return mapped;
 }
 
 /**
  * 颜色控制
  */
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
 
 /**
  * 色调调整
  */
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
 
 /**
  * 颜色矩阵
  */
 vec4 colorMatrix(vec4 color, float red, float green, float blue, float alpha, vec4 bias) {
    color = color * vec4(red, green, blue, alpha) + bias;
    
    return color;
 }
 
 /**
  * 混合
  */
 vec4 multiplyBlend(vec4 overlay, vec4 base){
    vec4 outputColor;
    
    float a = overlay.a + base.a * (1.0 - overlay.a);

    outputColor.rgb = ((1.0-base.a) * overlay.rgb * overlay.a +
                      (1.0-overlay.a) * base.rgb * base.a +
                      overlay.a * base.a * overlay.rgb * base.rgb) / a;

    outputColor.a = a;
    
    return outputColor;
 }
 
 void main() {
    vec4 originColor = texture2D(inputTexture, textureCoordinate);

    vec4 color = texture2D(inputTexture, textureCoordinate);
    color.a = 1.0;
    // 色调映射
    color = toneMapping(color);
    // 颜色控制
    color = colorControl(color, 0.88, 0.03, 0.85);
    // 色调调整
    color = hueAdjust(color, -0.0444);
    // 混合
    vec4 bg = vec4(0.5647, 0.1961, 0.0157, 0.14);
    color = multiplyBlend(bg, color);
    vec4 bg2 = texture2D(mapTexture, textureCoordinate);
    bg2.a *= 0.9;
    color = multiplyBlend(bg2, color);
    
    // 色调映射
    color = toneMapping(color);
    
    color.rgb = mix(originColor.rgb, color.rgb, strength);

    gl_FragColor = color;
 }