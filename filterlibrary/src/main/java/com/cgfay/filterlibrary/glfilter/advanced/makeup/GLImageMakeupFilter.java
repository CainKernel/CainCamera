package com.cgfay.filterlibrary.glfilter.advanced.makeup;

import android.content.Context;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.model.FacePoints;
import com.cgfay.filterlibrary.glfilter.model.IFacePoints;
import com.cgfay.filterlibrary.glfilter.model.IMakeup;
import com.cgfay.filterlibrary.glfilter.model.Makeup;

import java.util.List;

/**
 * 彩妆滤镜
 */
public class GLImageMakeupFilter extends GLImageFilter implements IMakeup, IFacePoints {

    protected FacePoints mFacePoints;

    public GLImageMakeupFilter(Context context) {
        super(context);
    }

    public GLImageMakeupFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void onMakeup(Makeup makeup) {

    }

    @Override
    public void onFacePoints(FacePoints facePoints) {
        mFacePoints = facePoints;
    }
}
