package com.cgfay.filterlibrary.glfilter.stickers;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.badlogic.gdx.math.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.stickers.bean.StaticStickerNormalData;

import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;


import java.nio.FloatBuffer;

/**
 * 实现静态或者动态贴纸，并且实现其旋转，移动，缩放
 * by ferrisXu
 */
public class StaticStickerNormalFilter extends DynamicStickerBaseFilter {


    // 变换矩阵句柄
    private int mMVPMatrixHandle;
    // 贴纸变换矩阵
    // 贴纸变换矩阵
    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();
    public OrthographicCamera camera;

    // 贴纸坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVideoVertexBuffer;
    public StaticStickerNormalFilter(Context context, DynamicSticker sticker) {
        super(context, sticker, OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_normal.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_normal.glsl"));


        // 前景贴纸加载器
        if (mDynamicSticker != null && mDynamicSticker.dataList != null) {
            for (int i = 0; i < mDynamicSticker.dataList.size(); i++) {
                if (mDynamicSticker.dataList.get(i) instanceof StaticStickerNormalData) {
                    String path = mDynamicSticker.unzipPath + "/" + mDynamicSticker.dataList.get(i).stickerName;
                    mStickerLoaderList.add(new DynamicStickerLoader(true,this, mDynamicSticker.dataList.get(i), path));
                }
            }
        }
        camera=new OrthographicCamera();
        initBuffer();
    }


    /**
     * 初始化缓冲
     */
    private void initBuffer() {
        releaseBuffer();
        mVideoVertexBuffer = OpenGLUtils.createFloatBuffer(mVideoVertices);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(mStickerVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    /**
     * 释放缓冲
     */
    private void releaseBuffer() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mVideoVertexBuffer != null) {
            mVideoVertexBuffer.clear();
            mVideoVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        } else {
            mMVPMatrixHandle = OpenGLUtils.GL_NOT_INIT;
        }
    }

    // 长宽比


    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if(camera!=null) {
            camera.setToOrtho(false, width, height);
            camera.update();
            projectionMatrix.set(camera.combined);
        }
        updateVideoVertexBuffer(width,height);
    }

    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        if(camera!=null) {
            camera.setGdxGraphicsSize(width, height);
        }
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 绘制到FBO
        int stickerTexture = drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
        // 绘制显示
        return super.drawFrame(stickerTexture, vertexBuffer, textureBuffer);
    }


    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 1、先将图像绘制到FBO中
        //绘制前景不需要旋转等
        transformMatrix.idt();
        super.drawFrameBuffer(textureId, mVideoVertexBuffer, textureBuffer);
        // 2、将贴纸逐个绘制到FBO中
        if (mStickerLoaderList.size() > 0) {
            for (int stickerIndex = 0; stickerIndex < mStickerLoaderList.size(); stickerIndex++) {
                synchronized (this) {
                    mStickerLoaderList.get(stickerIndex).updateStickerTexture();
                    calculateStickerVertices((StaticStickerNormalData) mStickerLoaderList.get(stickerIndex).getStickerData());
                    super.drawFrameBuffer(mStickerLoaderList.get(stickerIndex).getStickerTexture(), mVertexBuffer, mTextureBuffer);
                }
            }
        }
        return mFrameBufferTextures[0];
    }
    public  final float mStickerVertices[] = {
            -1.0f, -1.0f,  // 0 bottom left
            1.0f,  -1.0f,  // 1 bottom right
            -1.0f,  1.0f,  // 2 top left
            1.0f,   1.0f,  // 3 top right
    };

    public  final float mVideoVertices[] = {
            -1.0f, -1.0f,  // 0 bottom left
            1.0f,  -1.0f,  // 1 bottom right
            -1.0f,  1.0f,  // 2 top left
            1.0f,   1.0f,  // 3 top right
    };

    public void updateVideoVertexBuffer(int width,int height){
        float stickerHeight=height;
        float stickerWidth= width;
        float stickerX=0;
        float stickerY=0;
        mVideoVertices[0] = stickerX ; mVideoVertices[1] = stickerY;
        mVideoVertices[2] = stickerX + stickerWidth; mVideoVertices[3] = stickerY;
        mVideoVertices[4] = stickerX; mVideoVertices[5] = stickerY + stickerHeight;
        mVideoVertices[6] = stickerX + stickerWidth; mVideoVertices[7] = stickerY + stickerHeight;
        mVideoVertexBuffer.clear();
        mVideoVertexBuffer.position(0);
        mVideoVertexBuffer.put(mVideoVertices);
    }
    private void calculateStickerVertices(StaticStickerNormalData stickerData) {
        //贴纸宽高
        swidth=stickerData.width;
        sheight=stickerData.height;
        float stickerHeight=stickerData.height;
        float stickerWidth= stickerData.width;
        float stickerX=x;//0
        float stickerY=y;//0
        mStickerVertices[0] = stickerX ; mStickerVertices[1] = stickerY;
        mStickerVertices[2] = stickerX + stickerWidth; mStickerVertices[3] = stickerY;
        mStickerVertices[4] = stickerX; mStickerVertices[5] = stickerY + stickerHeight;
        mStickerVertices[6] = stickerX + stickerWidth; mStickerVertices[7] = stickerY + stickerHeight;
        mVertexBuffer.clear();
        mVertexBuffer.position(0);
        mVertexBuffer.put(mStickerVertices);

        transformMatrix.idt();

        //左下角为坐标中心
        float centerX=stickerX+stickerWidth/2f;
        float centerY=stickerY+stickerHeight/2f;


        transformMatrix.translate(centerX,centerY,0);
        transformMatrix.rotate(Vector3.Z,rotation);

        transformMatrix.scale(scale,scale,scale);
        transformMatrix.translate(-centerX,-centerY,0);


        rotation+=1;
        //test
        if(scale>=1.2f){
            flipScale=-1;
        }
        if(scale<=0.8f){
            flipScale=1;
        }
        scale+=0.01f*flipScale;

    }
    int flipScale=1;

    int rotation=0;
    float scale=1f;
    float x;
    float y;
    float swidth;
    float sheight;

    /**
     * 其中x,y为onInputSizeChanged中图片的坐标，左下角为坐标原点
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        this.x=x;
        this.y=y;
    }

    public void setRotate(int rotate){
        this.rotation=rotate;
    }

    public void scale(float scale){
        this.scale=scale;
    }
    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();

        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, combinedMatrix.val, 0);
        }
        // 绘制到FBO中，需要开启混合模式
        // 绘制到FBO中，需要开启混合模式
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE, GLES30.GL_ONE);
    }

    @Override
    public void onDrawFrameAfter() {
        super.onDrawFrameAfter();
        GLES30.glDisable(GLES30.GL_BLEND);
    }



    @Override
    public void release() {
        super.release();
        for (int i = 0; i < mStickerLoaderList.size(); i++) {
            if (mStickerLoaderList.get(i) != null) {
                mStickerLoaderList.get(i).release();
            }
        }
        mStickerLoaderList.clear();
    }

    public Vector3 parentToLocalCoordinates (Vector3 parentCoords) {
        final float rotation = this.rotation;
        final float scaleX = this.scale;
        final float scaleY = this.scale;
        final float childX = x;
        final float childY = y;
        float originX=swidth/2;
        float originY=sheight/2;
        if (rotation == 0) {
            if (scaleX == 1 && scaleY == 1) {
                parentCoords.x -= childX;
                parentCoords.y -= childY;
            } else {
                parentCoords.x = (parentCoords.x - childX - originX) / scaleX + originX;
                parentCoords.y = (parentCoords.y - childY - originY) / scaleY + originY;
            }
        } else {
            final float cos = (float)Math.cos(rotation * MathUtils.degreesToRadians);
            final float sin = (float)Math.sin(rotation * MathUtils.degreesToRadians);
            final float tox = parentCoords.x - childX - originX;
            final float toy = parentCoords.y - childY - originY;
            parentCoords.x = (tox * cos + toy * sin) / scaleX + originX;
            parentCoords.y = (tox * -sin + toy * cos) / scaleY + originY;
        }
        return parentCoords;
    }
    public StaticStickerNormalFilter hit(Vector3 target) {
        parentToLocalCoordinates(target);
        return target.x >= 0 && target.x < swidth && target.y >= 0 && target.y < sheight ? this : null;
    }

    static final Vector3 tempVec=new Vector3();
    static final Vector3 tmpCoords3 = new Vector3();
    public void onScroll(float distanceX, float distanceY) {
        stageToLocalAmount(tempVec.set(distanceX, distanceY,0));
        Log.d("sticker","onscrollx="+tempVec.x+",onscrolly="+tempVec.y);
        setPosition(x-tempVec.x,y-tempVec.y);
    }

    private void stageToLocalAmount (Vector3 amount) {
        camera.unproject(amount);
        amount.sub(camera.unproject(tmpCoords3.set(0, 0,0)));
    }
}
