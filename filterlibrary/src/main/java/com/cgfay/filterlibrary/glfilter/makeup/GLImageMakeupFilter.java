package com.cgfay.filterlibrary.glfilter.makeup;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.makeup.bean.IMakeup;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupParam;

/**
 * 彩妆滤镜
 */
public class GLImageMakeupFilter extends GLImageFilter implements IMakeup {

    public GLImageMakeupFilter(Context context) {
        super(context);
    }

    public GLImageMakeupFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void onMakeup(MakeupParam makeup) {

    }

}
