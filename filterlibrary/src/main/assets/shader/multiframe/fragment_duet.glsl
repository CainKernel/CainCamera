precision mediump float;
varying vec2 textureCoordinate;
uniform sampler2D inputTexture;
// x轴偏移量，上下同框时才会生效，当视频比例比9:8大时，需要左右挪动(9:16的上下半区)
uniform float offset_dx;
// y轴偏移量, 上下同框时才会生效，当视频比例比9:8小时，需要上下挪动(9:16的上下半区)
uniform float offset_dy;
// 同框绘制区域区分，上下同框时，0标志直接绘制，1表示上下同框，其他值绘制黑色
uniform float type;
// 限定值，处理type的浮点偏差
const float eps = 0.01;

void main() {
    // 左右同框、画中画模式都直接绘制，调整顶点坐标、纹理坐标、viewport即可实现
    if (abs(type) < eps) {
        gl_FragColor = texture2D(inputTexture, textureCoordinate);
    } else if (abs(type - 1.0) < eps) {
        // 上下同框的上下半区纹理绘制逻辑一致，区别在于外部参的x、y偏移量区别而已
        vec2 uv = textureCoordinate.xy;
        uv.x = uv.x + offset_dx;
        uv.y = uv.y + offset_dy;
        gl_FragColor = texture2D(inputTexture, uv);
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}