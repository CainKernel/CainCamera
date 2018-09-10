package com.cgfay.filterlibrary.glfilter.advanced.multi;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 四分镜滤镜
 */
public class GLImageMultiFourthFilter extends GLImageFilter {

    private static final String FRAGMENT_SHADER = ""
            + "precision highp float;\n" +
            "uniform sampler2D inputTexture;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    vec2 uv = textureCoordinate;\n" +
            "    if (uv.x <= 0.5) {\n" +
            "        uv.x = uv.x * 2.0;\n" +
            "    } else {\n" +
            "        uv.x = (uv.x - 0.5) * 2.0;\n" +
            "    }\n" +
            "    if (uv.y <= 0.5) {\n" +
            "        uv.y = uv.y * 2.0;\n" +
            "    } else {\n" +
            "        uv.y = (uv.y - 0.5) * 2.0;\n" +
            "    }\n" +
            "    gl_FragColor = texture2D(inputTexture, fract(uv));\n" +
            "}";

    public GLImageMultiFourthFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public GLImageMultiFourthFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }
}
