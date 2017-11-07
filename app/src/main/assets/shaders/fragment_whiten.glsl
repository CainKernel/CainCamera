precision highp float;
varying mediump vec2 textureCoordinate;
uniform sampler2D inputTexture;
uniform float redden;
uniform float whitening;
uniform float pinking;
void main () {

    lowp vec4 fragColor = vec4(texture2D (inputTexture, textureCoordinate).xyz, 1.0);

    if ((whitening != 0.0)) {
        fragColor.xyz = clamp (mix (fragColor.xyz, (vec3(1.0, 1.0, 1.0) -
        ((vec3(1.0, 1.0, 1.0) - fragColor.xyz) * (vec3(1.0, 1.0, 1.0) - fragColor.xyz))),
        (whitening * dot (vec3(0.299, 0.587, 0.114), fragColor.xyz))), 0.0, 1.0);
    };

    if ((redden != 0.0)) {
        lowp vec3 redColor = mix (fragColor.xyz, (vec3(1.0, 1.0, 1.0) -
            ((vec3(1.0, 1.0, 1.0) - fragColor.xyz) * (vec3(1.0, 1.0, 1.0) - fragColor.xyz))),
        (0.2 * redden));

        lowp vec3 tmpvar_3 = mix (vec3(dot (redColor, vec3(0.299, 0.587, 0.114))),
            redColor, (1.0 + redden));
        lowp vec3 tmpvar_4 = mix (tmpvar_3.xyy, tmpvar_3, 0.5);
        lowp float tmpvar_5 = dot (tmpvar_4, vec3(0.299, 0.587, 0.114));

        fragColor.xyz = clamp (mix (tmpvar_3, mix (tmpvar_4, sqrt(tmpvar_4), tmpvar_5),
                (redden * tmpvar_5)), 0.0, 1.0);
    };

    if ((pinking != 0.0)) {
        lowp vec3 pinkColor;
        pinkColor.x = ((sqrt(fragColor.x) * 0.41) + (0.59 * fragColor.x));
        pinkColor.y = ((sqrt(fragColor.y) * 0.568) + (0.432 * fragColor.y));
        pinkColor.z = ((sqrt(fragColor.z) * 0.7640001) + (0.2359999 * fragColor.z));
        fragColor.xyz = clamp (mix (fragColor.xyz, pinkColor,
            (pinking * dot (vec3(0.299, 0.587, 0.114), fragColor.xyz))), 0.0, 1.0);
    };
    gl_FragColor = fragColor;
}