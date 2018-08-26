package com.cgfay.filterlibrary.glfilter.advanced.colors;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 黑白
 * Created by cain on 2017/11/15.
 */

public class GLImageBlackWhiteFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "vec4 calVignette(vec2 coord,vec4 color,float texture_width,float texture_height) {\n" +
            "float shade = 0.6;\n" +
            "float slope = 20.0;\n" +
            "float range = 1.30 - sqrt(0.8) * 0.7;\n" +
            "vec2 scale;\n" +
            "if(texture_width > texture_height) {\n" +
            "scale.x = 1.0;\n" +
            "scale.y = texture_height / texture_width;\n" +
            "} else {\n" +
            "scale.x = texture_width / texture_height;\n" +
            "scale.y = 1.0;\n" +
            "}\n" +
            "float inv_max_dist = 2.0 / length(scale);\n" +
            "float dist = length((coord - vec2(0.5, 0.5)) * scale);\n" +
            "float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);\n" +
            "return vec4(color.rgb * lumen,color.a);\n" +
            "}\n" +
            "vec4 calNewVignette(vec2 coord,vec4 color,float texture_width,float texture_height,float value) {\n" +
            "float shade = 0.85;\n" +
            "float slope = 20.0;\n" +
            "float range = 1.30 - sqrt(value) * 0.7;\n" +
            "vec2 scale;\n" +
            "if(texture_width > texture_height) {\n" +
            "scale.x = 1.0;\n" +
            "scale.y = texture_height / texture_width;\n" +
            "} else {\n" +
            "scale.x = texture_width / texture_height;\n" +
            "scale.y = 1.0;\n" +
            "}\n" +
            "float inv_max_dist = 2.0 / length(scale);\n" +
            "float dist = length((coord - vec2(0.5, 0.5)) * scale);\n" +
            "float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);\n" +
            "return vec4(color.rgb * lumen,color.a);\n" +
            "}\n" +
            "vec4 calVignette2(vec4 color, vec2 coord, float strength) {\n" +
            "float distance = (coord.x - 0.5) * (coord.x - 0.5) + (coord.y - 0.5) * (coord.y - 0.5);\n" +
            "float scale = distance / 0.5 * strength;\n" +
            "color.r =  color.r - scale;\n" +
            "color.g = color.g - scale;\n" +
            "color.b = color.b - scale;\n" +
            "return color;\n" +
            "}\n" +
            "vec4 calBrightnessContract(vec4 color,float brightness, float contrast,float threshold) {\n" +
            "float cv = contrast <= -255.0 ? -1.0 : contrast / 255.0;\n" +
            "if (contrast > 0.0 && contrast < 255.0) {\n" +
            "cv = 1.0 / (1.0 - cv) - 1.0;\n" +
            "}\n" +
            "float r  = color.r + brightness / 255.0;\n" +
            "float g = color.g + brightness / 255.0;\n" +
            "float b = color.b + brightness / 255.0;\n" +
            "if (contrast >= 255.0) {\n" +
            "r = r >= threshold / 255.0 ? 1.0 : 0.0;\n" +
            "g = g >= threshold / 255.0 ? 1.0 : 0.0;\n" +
            " b = b >= threshold / 255.0 ? 1.0 : 0.0;\n" +
            "} else {\n" +
            "r =  r + (r - threshold / 255.0) * cv;\n" +
            "g = g + (g - threshold / 255.0) * cv;\n" +
            "b = b + (b - threshold / 255.0) * cv;\n" +
            "}\n" +
            "color.r = r;\n" +
            "color.g = g;\n" +
            "color.b = b;\n" +
            "return color;\n" +
            "}\n" +
            "void main() {\n" +
            "vec4 color = texture2D(inputTexture, textureCoordinate);\n" +
            "float gray = dot(color.rgb, vec3(0.229, 0.587, 0.114));\n" +
            "float exposure = gray * 1.33;\n" +
            "color.r = exposure;\n" +
            "color.g = exposure;\n" +
            "color.b = exposure;\n" +
            "color = calVignette2(color, textureCoordinate, 0.5);\n" +
            "color = calBrightnessContract(color, 0.0, 16.0, 128.0);\n" +
            "gl_FragColor = color;\n" +
            "}\n" +
            "\n";

    public GLImageBlackWhiteFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageBlackWhiteFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
