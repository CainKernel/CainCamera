package com.cgfay.landmarklibrary;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

/**
 * 关键点工具类
 */
public final class LandmarkUtils {

    /**
     * 绘制人脸关键点
     * @param canvas
     * @param points
     * @param width
     * @param height
     * @param frontCamera
     */
    public static void drawFacePoints(Canvas canvas, PointF[] points, int width, int height, boolean frontCamera) {
        if (canvas == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 0,0));
        int strokeWidth = Math.max(width / 240, 5);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);

        for (PointF point : points) {
            canvas.drawPoint(frontCamera ? width - point.x : point.x, point.y, paint);
        }
    }

}
