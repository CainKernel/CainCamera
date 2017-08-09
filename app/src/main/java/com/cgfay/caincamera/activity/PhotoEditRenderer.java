package com.cgfay.caincamera.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;

import com.cgfay.caincamera.bean.ImageMeta;
import com.cgfay.caincamera.core.FilterManager;
import com.cgfay.caincamera.core.FilterType;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.utils.GlUtil;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 图片编辑渲染部分
 * Created by cain.huang on 2017/8/9.
 */
public class PhotoEditRenderer implements GLSurfaceView.Renderer {

    private Context mContext;
    // 图片元数据
    private ImageMeta mImageMeta;
    // 滤镜
    private BaseImageFilter mFilter;
    // 原始图片的textureId
    private int mPhotoTexture;

    private WeakReference<GLSurfaceView> mWeakGLSurfaceView;

    public PhotoEditRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    /**
     * 设置GLSurfaceView
     * @param view
     */
    public void setGLView(GLSurfaceView view) {
        if (mWeakGLSurfaceView != null) {
            throw new RuntimeException("it has bind a GLSurfaceView");
        } else {
            mWeakGLSurfaceView = new WeakReference<GLSurfaceView>(view);
        }
    }

    /**
     * 设置图片数据
     * @param imageMeta
     */
    public void setImageMeta(ImageMeta imageMeta) {
        mImageMeta = imageMeta;
        new AsyncLoadBitmapTexture().execute();
    }

    /**
     * 更新filter
     * @param type Filter类型
     */
    private void setFilter(FilterType type) {
        if (mFilter != null) {
            mFilter.release();
        }
        mFilter = FilterManager.getFilter(type);
    }


    private class AsyncLoadBitmapTexture extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (mImageMeta != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mImageMeta.getPath(), options);
                mPhotoTexture = GlUtil.createTexture(bitmap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // 完成后，进行绘制
            if (mWeakGLSurfaceView != null) {
                mWeakGLSurfaceView.get().requestRender();
            }
        }
    }
}
