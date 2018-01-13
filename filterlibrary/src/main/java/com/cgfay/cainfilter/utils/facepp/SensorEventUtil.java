package com.cgfay.cainfilter.utils.facepp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 传感器
 * Created by cain.huang on 2017/8/15.
 */

public class SensorEventUtil implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    public int orientation = 0;

    public SensorEventUtil(Context context) {
        if (context != null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            // TYPE_GRAVITY
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // 参数三，检测的精准度
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final double G = 9.81;
        final double SQRT2 = 1.414213;
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (z >= G / SQRT2) { //screen is more likely lying on the table
                if (x >= G / 2) {
                    orientation = 1;
                } else if (x <= -G / 2) {
                    orientation = 2;
                } else if (y <= -G / 2) {
                    orientation = 3;
                } else {
                    orientation = 0;
                }
            } else {
                if (x >= G / SQRT2) {
                    orientation = 1;
                } else if (x <= -G / SQRT2) {
                    orientation = 2;
                } else if (y <= -G / SQRT2) {
                    orientation = 3;
                } else {
                    orientation = 0;
                }
            }
        }
    }
}
