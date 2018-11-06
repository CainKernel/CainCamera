precision mediump float;
varying mediump vec2 textureCoordinate;
uniform sampler2D inputTexture;
uniform float Angle;
uniform float MirrorX;
uniform float MirrorY;

mediump vec2 getCenterRotateXY(mediump float x, mediump float y, mediump float angle) {
    mediump float cx, cy;
    mediump vec2 xy;
    mediump float rotateRadians = radians(angle);
    mediump float fZoom = 1.0;
    cx = x - 0.5;
    cy = y - 0.5;
    xy.x = cx * cos(rotateRadians) - cy * sin(rotateRadians);
    xy.y = cx * sin(rotateRadians) + cy * cos(rotateRadians);
    xy *= fZoom;
    xy += vec2(0.5);
    if (xy.x > 1.0) {
        xy.x = 2.0 - xy.x;
    } else if (xy.x < 0.0) {
        xy.x = abs(xy.x);
    }
    if (xy.y > 1.0) {
        xy.y = 2.0 - xy.y;
    } else if (xy.y < 0.0) {
        xy.y = abs(xy.y);
    }

    xy = clamp(xy, 0.0, 1.0);
    return xy;
}

void main() {
    vec3 color;
    vec2 textPos = getCenterRotateXY(textureCoordinate.x, textureCoordinate.y, Angle);
    if(MirrorX > 0.001) {
        textPos = vec2(1.0-textPos.x, textPos.y);
    }
    if(MirrorY > 0.001) {
        textPos = vec2(textPos.x, 1.0-textPos.y);
    }
    color =texture2D(inputTexture, textPos).rgb;
    gl_FragColor = vec4(color, 1.0);
}