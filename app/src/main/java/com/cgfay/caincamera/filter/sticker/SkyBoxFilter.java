package com.cgfay.caincamera.filter.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.caincamera.utils.BitmapUtils;
import com.cgfay.caincamera.utils.GlUtil;
import com.cgfay.caincamera.utils.MatrixHelper;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * 天空滤镜效果
 * Created by cain.huang on 2017/8/24.
 */
public class SkyBoxFilter {

    private static final String VERTEX_SHADER =
            "uniform mat4 uProjMatrix;                                                  \n" +
            "uniform mat4 uViewMatrix;                                                  \n" +
            "uniform mat4 uModelMatrix;                                                 \n" +
            "uniform mat4 uRotateMatrix;                                                \n" +
            "attribute vec3 aPosition;                                                  \n" +
            "attribute vec2 aCoordinate;                                                \n" +
            "varying vec2 vCoordinate;                                                  \n" +
            "void main(){                                                               \n" +
            "    gl_Position = uProjMatrix * uRotateMatrix * uViewMatrix * uModelMatrix \n" +
            "        * vec4(aPosition,1);                                               \n" +
            "    vCoordinate=aCoordinate;                                               \n" +
            "}";

    private static final String FRAGMENT_SHADER =
            "precision highp float;                         \n" +
            "uniform sampler2D uTexture;                    \n" +
            "varying vec2 vCoordinate;                      \n" +
            "void main(){                                   \n" +
            "   vec4 color=texture2D(uTexture,vCoordinate); \n" +
            "   gl_FragColor=color;                         \n" +
            "}";

    private static final float UNIT_SIZE = 1f;// 单位尺寸
    private float r = 2f; // 球的半径

    private float radius = 2f;

    final double angleSpan = Math.PI / 90f; // 将球进行单位切分的角度
    int vCount = 0; // 顶点个数，先初始化为0

    private int mHProgram;
    private int mHUTexture;
    private int mHProjMatrix;
    private int mHViewMatrix;
    private int mHModelMatrix;
    private int mHRotateMatrix;
    private int mHPosition;
    private int mHCoordinate;

    private int mMipmapTextureId;

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mModelMatrix=new float[16];
    private float[] mRotateMatrix=new float[16];

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    private Bitmap mBitmap;

    public SkyBoxFilter(Context context, String path) {
        mBitmap = BitmapUtils.getImageFromAssetsFile(context, path);
    }

    public void createProgram() {
        mHProgram = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        mHProjMatrix=GLES30.glGetUniformLocation(mHProgram,"uProjMatrix");
        mHViewMatrix=GLES30.glGetUniformLocation(mHProgram,"uViewMatrix");
        mHModelMatrix=GLES30.glGetUniformLocation(mHProgram,"uModelMatrix");
        mHRotateMatrix=GLES30.glGetUniformLocation(mHProgram,"uRotateMatrix");
        mHUTexture=GLES30.glGetUniformLocation(mHProgram,"uTexture");
        mHPosition=GLES30.glGetAttribLocation(mHProgram,"aPosition");
        mHCoordinate=GLES30.glGetAttribLocation(mHProgram,"aCoordinate");
        mMipmapTextureId = GlUtil.createTexture(mBitmap);
        calculateAttribute();
    }

    /**
     *  设置视图宽高
     * @param width
     * @param height
     */
    public void setViewSize(int width, int height) {
        //计算宽高比
        float ratio = (float)width / height;
        //透视投影矩阵/视锥
        MatrixHelper.perspectiveM(mProjectMatrix, 0, 45, ratio, 1f, 300);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0f, 1.0f, 0.0f);
        //模型矩阵
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    /**
     * 设置旋转矩阵
     * @param matrix
     */
    public void setRotationMatrix(float[] matrix) {
        System.arraycopy(matrix, 0, mRotateMatrix, 0, 16);
    }


    /**
     * 绘制
     */
    public void draw() {
        if (mMipmapTextureId == GlUtil.GL_NOT_INIT) {
            return;
        }
        GLES30.glUseProgram(mHProgram);
        GLES30.glUniformMatrix4fv(mHProjMatrix,1,false,mProjectMatrix,0);
        GLES30.glUniformMatrix4fv(mHViewMatrix,1,false,mViewMatrix,0);
        GLES30.glUniformMatrix4fv(mHModelMatrix,1,false,mModelMatrix,0);
        GLES30.glUniformMatrix4fv(mHRotateMatrix,1,false,mRotateMatrix,0);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mMipmapTextureId);
        GLES30.glUniform1i(mHUTexture, 0);
        GLES30.glEnableVertexAttribArray(mHPosition);
        GLES30.glVertexAttribPointer(mHPosition,3,GLES30.GL_FLOAT,false,0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(mHCoordinate);
        GLES30.glVertexAttribPointer(mHCoordinate,2,GLES30.GL_FLOAT,false,0, mTextureBuffer);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);

        GLES30.glDisableVertexAttribArray(mHPosition);

    }


    /**
     * 计算坐标
     */
    private void calculateAttribute()  {
        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Float> textureVertices = new ArrayList<>();
        for (double vAngle = 0; vAngle < Math.PI; vAngle = vAngle + angleSpan) {
            for (double hAngle = 0; hAngle < 2 * Math.PI; hAngle = hAngle + angleSpan){

                float x0 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle));
                float y0 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle));
                float z0 = (float) (radius * Math.cos((vAngle)));

                float x1 = (float) (radius* Math.sin(vAngle) * Math.cos(hAngle + angleSpan));
                float y1 = (float) (radius* Math.sin(vAngle) * Math.sin(hAngle + angleSpan));
                float z1 = (float) (radius * Math.cos(vAngle));

                float x2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan));
                float y2 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan));
                float z2 = (float) (radius * Math.cos(vAngle + angleSpan));

                float x3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.cos(hAngle));
                float y3 = (float) (radius* Math.sin(vAngle + angleSpan) * Math.sin(hAngle));
                float z3 = (float) (radius * Math.cos(vAngle + angleSpan));

                vertices.add(x1);
                vertices.add(y1);
                vertices.add(z1);

                vertices.add(x0);
                vertices.add(y0);
                vertices.add(z0);

                vertices.add(x3);
                vertices.add(y3);
                vertices.add(z3);

                vertices.add(x1);
                vertices.add(y1);
                vertices.add(z1);

                vertices.add(x3);
                vertices.add(y3);
                vertices.add(z3);

                vertices.add(x2);
                vertices.add(y2);
                vertices.add(z2);

                float s0 = (float) (hAngle / Math.PI / 2);
                float s1 = (float) ((hAngle + angleSpan) / Math.PI / 2);
                float t0 = (float) (vAngle / Math.PI);
                float t1 = (float) ((vAngle + angleSpan) / Math.PI);

                textureVertices.add(s1);// x1 y1对应纹理坐标
                textureVertices.add(t0);

                textureVertices.add(s0);// x0 y0对应纹理坐标
                textureVertices.add(t0);

                textureVertices.add(s0);// x3 y3对应纹理坐标
                textureVertices.add(t1);

                textureVertices.add(s1);// x1 y1对应纹理坐标
                textureVertices.add(t0);

                textureVertices.add(s0);// x3 y3对应纹理坐标
                textureVertices.add(t1);

                textureVertices.add(s1);// x2 y3对应纹理坐标
                textureVertices.add(t1);
            }
        }
        vCount = vertices.size() / 3;
        mVertexBuffer = GlUtil.createFloatBuffer(vertices);
        mTextureBuffer = GlUtil.createFloatBuffer(textureVertices);
    }
}
