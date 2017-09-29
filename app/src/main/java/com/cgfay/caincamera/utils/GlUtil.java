package com.cgfay.caincamera.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class GlUtil {

    public static final String TAG = "GlUtil";

    // 从初始化失败
    public static final int GL_NOT_INIT = -1;

    // 单位矩阵
    public static final float[] IDENTITY_MATRIX;
    static {
        IDENTITY_MATRIX = new float[16];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

    private static final int SIZEOF_FLOAT = 4;

    private GlUtil() {}

    /**
     * 创建program
     * @param vertexSource
     * @param fragmentSource
     * @return
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES30.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES30.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES30.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES30.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * 加载Shader
     * @param shaderType
     * @param source
     * @return
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES30.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * 检查是否出错
     * @param op
     */
    public static void checkGlError(String op) {
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * 创建FloatBuffer
     * @param coords
     * @return
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     * 创建FloatBuffer
     * @param data
     * @return
     */
    public static FloatBuffer createFloatBuffer(ArrayList<Float> data) {
        float[] coords = new float[data.size()];
        for (int i = 0; i < coords.length; i++){
            coords[i] = data.get(i);
        }
        return createFloatBuffer(coords);
    }

    /**
     * 创建Texture对象
     * @param textureType
     * @return
     */
    public static int createTextureObject(int textureType) {
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");
        int textureId = textures[0];
        GLES30.glBindTexture(textureType, textureId);
        GlUtil.checkGlError("glBindTexture " + textureId);
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");
        return textureId;
    }

    /**
     * 创建Sampler2D的Framebuffer 和 Texture
     * @param frameBuffer
     * @param frameBufferTex
     * @param width
     * @param height
     */
    public static void createSampler2DFrameBuff(int[] frameBuffer, int[] frameBufferTex,
                                                int width, int height) {
        GLES30.glGenFramebuffers(1, frameBuffer, 0);
        GLES30.glGenTextures(1, frameBufferTex, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameBufferTex[0]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, frameBufferTex[0], 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        checkGlError("createCamFrameBuff");
    }

    /**
     * 加载mipmap纹理
     * @param bitmap bitmap图片
     * @return
     */
    public static int createTexture(Bitmap bitmap) {
        int[] texture = new int[1];
        if (bitmap != null && !bitmap.isRecycled()) {
            //生成纹理
            GLES30.glGenTextures(1, texture, 0);
            checkGlError("glGenTexture");
            //生成纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER,GLES30.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S,GLES30.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T,GLES30.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
            return texture[0];
        }
        return 0;
    }

    /**
     * 创建OES 类型的Texture
     * @return
     */
    public static int createTextureOES() {
        return GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    /**
     * 加载mipmap纹理
     * @param context
     * @param name
     * @return
     */
    public static int loadMipmapTextureFromAssets(Context context, String name) {
        int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0) {
            Bitmap bitmap = getImageFromAssetsFile(context, name);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    /**
     * 加载Assets文件夹下的图片
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager manager = context.getResources().getAssets();
        try {
            InputStream is = manager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 底部框
     * @param line
     * @param offsetX
     * @param offsetY
     * @param pitch
     * @param yaw
     * @param roll
     * @param orientation
     * @return
     */
    public static FloatBuffer drawBottomShowRect(float line, float offsetX, float offsetY,
                                                 float pitch, float yaw, float roll,
                                                 int orientation) {
        double real_roll = roll + Math.PI + (orientation / 180.0f) * Math.PI;
        while (real_roll > 2 * Math.PI)
            real_roll -= 2 * Math.PI;
        roll = (float)(real_roll - Math.PI);

        float a = line / 2.0f;

        float offset_z = 0f;
        float[] all_points = new float[] {
                0, 0, 0,
                1, 0, 0,
                0, -1, 0,
                0, 0, 1
        };

        for (int i = 0; i < all_points.length / 3; ++i) {

            rotatePoint3f(all_points, i * 3, yaw, 2, 0);
            rotatePoint3f(all_points, i * 3, pitch, 2, 1);
            rotatePoint3f(all_points, i * 3, roll, 0, 1);

            all_points[i * 3] = all_points[i * 3] * a + offsetX;
            all_points[i * 3 + 1] = all_points[i * 3 + 1] * a + offsetY;
            all_points[i * 3 + 2] = all_points[i * 3 + 2] * a + offset_z;
        }

        FloatBuffer all_pointsBuffer = floatBufferUtil(all_points);

        return all_pointsBuffer;
    }

    /**
     * 旋转
     * @param points
     * @param offset
     * @param angle
     * @param x_axis
     * @param y_axis
     */
    public static void rotatePoint3f(float points[], int offset, float angle/*radis*/,
                                     int x_axis, int y_axis) {
        float x = points[offset + x_axis], y = points[offset + y_axis];
        float alpha_x = (float)Math.cos(angle), alpha_y = (float)Math.sin(angle);

        points[offset + x_axis] = x * alpha_x - y * alpha_y;
        points[offset + y_axis] = x * alpha_y + y * alpha_x;
    }

    /**
     * 定义一个工具方法，将float[]数组转换为OpenGL ES所需的FloatBuffer
     * @param arr
     * @return
     */
    public static FloatBuffer floatBufferUtil(float[] arr) {
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        FloatBuffer mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }

    /**
     * 画绿方框
     * @param isBackCamera
     * @param width
     * @param height
     * @param roi_ratio
     * @return
     */
    public static ArrayList<FloatBuffer> drawCenterShowRect(boolean isBackCamera,
                                                            float width, float height,
                                                            float roi_ratio) {
        RectF rectF = new RectF();

        float showRectHeight = height * roi_ratio;
        float _x_offset = 0, _y_offset = 0;
        float max_len = height;
        if (width > height) {
            max_len = width;
            _y_offset = (width - height) / 2;
        } else
            _x_offset = (height - width) / 2;
        // 把框固定在中间以最短边的0.8倍大小
        rectF.left = ((width - showRectHeight) / 2.0f + _x_offset) / max_len;
        rectF.top = ((height - showRectHeight) / 2.0f + _y_offset) / max_len;
        rectF.right = ((width - showRectHeight) / 2.0f + showRectHeight + _x_offset) / max_len;
        rectF.bottom = ((height - showRectHeight) / 2.0f + showRectHeight + _y_offset) / max_len;

        float _left = rectF.left * 2 - 1;
        float _right = rectF.right * 2 - 1;
        float _top = 1 - rectF.top * 2;
        float _bottom = 1 - rectF.bottom * 2;
        if (isBackCamera) {
            _left = -_left;
            _right = -_right;
        }
        float delta_x = 3 / height, delta_y = 3 / height;
        // 4个点分别是// top_left bottom_left bottom_right
        // top_right
        float rectangle_left[] = { _left, _top, 0, _left, _bottom,
                0, _left + delta_x, _bottom, 0, _left + delta_x, _top, 0 };
        float rectangle_top[] = { _left, _top, 0, _left, _top - delta_y,
                0, _right, _top - delta_y, 0, _right, _top, 0 };
        float rectangle_right[] = { _right - delta_x, _top, 0, _right - delta_x, _bottom,
                0, _right, _bottom, 0, _right, _top, 0 };
        float rectangle_bottom[] = { _left, _bottom + delta_y, 0, _left, _bottom, 0, _right,
                _bottom, 0, _right, _bottom + delta_y, 0 };

        FloatBuffer fb_left = floatBufferUtil(rectangle_left);
        FloatBuffer fb_top = floatBufferUtil(rectangle_top);
        FloatBuffer fb_right = floatBufferUtil(rectangle_right);
        FloatBuffer fb_bottom = floatBufferUtil(rectangle_bottom);
        ArrayList<FloatBuffer> vertexBuffersOpengl = new ArrayList<FloatBuffer>();
        vertexBuffersOpengl.add(fb_left);
        vertexBuffersOpengl.add(fb_top);
        vertexBuffersOpengl.add(fb_right);
        vertexBuffersOpengl.add(fb_bottom);
        return vertexBuffersOpengl;
    }
}
