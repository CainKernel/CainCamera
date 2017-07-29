#extension GL_OES_EGL_image_external : require
precision highp float;
varying mediump vec2 textureCoordinate;
uniform samplerExternalOES inputTexture;
uniform float redden;
uniform float whitening;
uniform float pinking;
void main () {
    lowp vec4 softColor;
    softColor.xyz = texture2D (inputTexture, textureCoordinate).xyz;
    softColor.w = 1.0;
    if ((whitening != 0.0)) {
        softColor.xyz = clamp (mix (softColor.xyz, (vec3(1.0, 1.0, 1.0) -
        ((vec3(1.0, 1.0, 1.0) - softColor.xyz) * (vec3(1.0, 1.0, 1.0) - softColor.xyz))),
        (whitening * dot (vec3(0.299, 0.587, 0.114), softColor.xyz))), 0.0, 1.0);
    };

    if ((redden != 0.0)) {
        lowp vec3 tmpvar_2;
        tmpvar_2 = mix (softColor.xyz, (vec3(1.0, 1.0, 1.0) -
            ((vec3(1.0, 1.0, 1.0) - softColor.xyz) * (vec3(1.0, 1.0, 1.0) - softColor.xyz))),
        (0.2 * redden));

        lowp vec3 tmpvar_3 = mix (vec3(dot (tmpvar_2, vec3(0.299, 0.587, 0.114))),
            tmpvar_2, (1.0 + redden));
        lowp vec3 tmpvar_4 = mix (tmpvar_3.xyy, tmpvar_3, 0.5);
        lowp float tmpvar_5 = dot (tmpvar_4, vec3(0.299, 0.587, 0.114));

        softColor.xyz = clamp (mix (tmpvar_3, mix (tmpvar_4, sqrt(tmpvar_4), tmpvar_5),
                (redden * tmpvar_5)), 0.0, 1.0);
    };
    if ((pinking != 0.0)) {
        lowp vec3 tmpvar_6;
        tmpvar_6.x = ((sqrt(softColor.x) * 0.41) + (0.59 * softColor.x));
        tmpvar_6.y = ((sqrt(softColor.y) * 0.568) + (0.432 * softColor.y));
        tmpvar_6.z = ((sqrt(softColor.z) * 0.7640001) + (0.2359999 * softColor.z));
        softColor.xyz = clamp (mix (softColor.xyz, tmpvar_6,
            (pinking * dot (vec3(0.299, 0.587, 0.114), softColor.xyz))), 0.0, 1.0);
    };
    gl_FragColor = softColor;
}