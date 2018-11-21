package com.cgfay.filterlibrary.glfilter.effect;

import android.content.Context;
import android.text.TextUtils;

import com.cgfay.filterlibrary.glfilter.base.GLImageGroupFilter;
import com.cgfay.filterlibrary.glfilter.effect.bean.DynamicEffect;

/**
 * 动态特效滤镜
 */
public class GLImageDynamicEffectFilter extends GLImageGroupFilter {

    public GLImageDynamicEffectFilter(Context context, DynamicEffect dynamicEffect) {
        super(context);
        if (dynamicEffect == null || dynamicEffect.effectList == null
                || TextUtils.isEmpty(dynamicEffect.unzipPath)) {
            return;
        }
        // 添加滤镜
        for (int i = 0; i < dynamicEffect.effectList.size(); i++) {
            mFilters.add(new DynamicEffectFilter(context,
                    dynamicEffect.effectList.get(i), dynamicEffect.unzipPath));
        }
    }

    /**
     * 设置时间戳
     * @param timeStamp 单位秒
     */
    public void setTimeStamp(float timeStamp) {
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i) != null && mFilters.get(i) instanceof DynamicEffectBaseFilter) {
                ((DynamicEffectBaseFilter) mFilters.get(i)).setTimeStamp(timeStamp);
            }
        }
    }

}
