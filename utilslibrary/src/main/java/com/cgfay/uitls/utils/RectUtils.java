package com.cgfay.uitls.utils;

import android.graphics.RectF;

public class RectUtils {

    /**
     * 矩形缩放
     * @param rectF
     * @param scale
     */
    public static void scale(RectF rectF, float scale) {
        float width = rectF.width();
        float height = rectF.height();

        float newWidth = scale * width;
        float newHeight = scale * height;

        float dx = (newWidth - width) / 2;
        float dy = (newHeight - height) / 2;

        rectF.left -= dx;
        rectF.top -= dy;
        rectF.right += dx;
        rectF.bottom += dy;
    }

    /**
     * 旋转
     * @param rect
     * @param centerX
     * @param centerY
     * @param rotateAngle
     */
    public static void rotate(RectF rect, float centerX, float centerY, float rotateAngle) {
        float x = rect.centerX();
        float y = rect.centerY();
        float sinA = (float) Math.sin(Math.toRadians(rotateAngle));
        float cosA = (float) Math.cos(Math.toRadians(rotateAngle));
        // 使用三角形公式计算新的位置
        float newX = centerX + (x - centerX) * cosA - (y - centerY) * sinA;
        float newY = centerY + (y - centerY) * cosA + (x - centerX) * sinA;

        float dx = newX - x;
        float dy = newY - y;

        rect.offset(dx, dy);
    }

}
