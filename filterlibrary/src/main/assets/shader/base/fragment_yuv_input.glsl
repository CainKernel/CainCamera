// YUV/BGRA 渲染
precision mediump float;
varying vec2 textureCoordinate;

uniform int renderYUV; // 是否渲染YUV，renderYUV为1时，渲染YUV，为0时渲染BGRA

// 当渲染为YUV时，inputTexture 为 Y纹理，inputTextureU为U纹理， inputTextureV 为V纹理
// 当渲染BGRA时，仅适用inputTexture纹理
uniform sampler2D inputTexture;
uniform sampler2D inputTextureU;
uniform sampler2D inputTextureV;

void main() {
    if (renderYUV == 1) {
        vec3 yuv;
        vec3 rgb;
        yuv.r = texture2D(inputTexture, textureCoordinate).r - (16.0 / 255.0);
        yuv.g = texture2D(inputTextureU, textureCoordinate).r - 0.5;
        yuv.b = texture2D(inputTextureV, textureCoordinate).r - 0.5;
        rgb = mat3(1.164,  1.164,  1.164,
                   0.0,   -0.213,  2.112,
                   1.793, -0.533,    0.0) * yuv;
        gl_FragColor = vec4(rgb, 1.0);
    } else {
        vec4 abgr = texture2D(inputTexture, textureCoordinate);
        gl_FragColor = abgr;
        gl_FragColor.r = abgr.b;
        gl_FragColor.b = abgr.r;
    }
}
