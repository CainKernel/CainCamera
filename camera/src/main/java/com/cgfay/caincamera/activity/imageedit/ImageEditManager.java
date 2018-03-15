package com.cgfay.caincamera.activity.imageedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.cgfay.cainfilter.camerarender.ParamsManager;
import com.cgfay.cainfilter.type.GLFilterGroupType;
import com.cgfay.cainfilter.type.GLFilterType;
import com.cgfay.utilslibrary.BitmapUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 图片编辑管理器
 * Created by Administrator on 2018/3/13.
 */

public final class ImageEditManager implements OnImageEditListener {
    
    private static final String TAG = "ImageEditManager";
    private static final boolean VERBOSE = true;

    private Context mContext;
    private WeakReference<ImageView> mWeakImageView;

    private Handler mMainHandler;
    private ImageEditHandler mHandler;
    private ImageEditThread mThread;

    private Bitmap mSourceBitmap;
    private Bitmap mCurrentBitmap;
    private String mImagePath;

    private int mScreenWidth;
    private int mScreenHeight;

    public ImageEditManager(Context context, ImageView imageView) {
        mContext = context;
        mWeakImageView = new WeakReference<ImageView>(imageView);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenHeight = wm.getDefaultDisplay().getHeight();
        initEGLRenderThread();
    }

    /**
     * 初始化后台EGL渲染线程
     */
    public void initEGLRenderThread() {
        mThread = new ImageEditThread("RenderThread", this);
        mThread.start();
        mHandler = new ImageEditHandler(mThread.getLooper(), mThread);
        mThread.setImageEditHandler(mHandler);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 销毁后台EGL渲染线程
     */
    public void destroyEGLRenderThread() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (mThread != null) {
            mThread.quitSafely();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }

        mThread = null;
        mHandler = null;
        mMainHandler = null;
    }

    @Override
    public void onTextureCreated() {
        Log.d(TAG, "onTextureCreated: ");
    }

    @Override
    public void onSaveImageListener(final ByteBuffer buffer, final int width, final int height) {
        if (mWeakImageView != null && mWeakImageView.get() != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWeakImageView.get().setImageBitmap(BitmapUtils
                            .getBitmapFromBuffer(buffer, width, height, false, true));
                }
            });
        }
    }

    /**
     * 设置图片路径
     * @param path
     */
    public void setImagePath(String path) {
        mImagePath = path;
        if (mSourceBitmap != null && !mSourceBitmap.isRecycled()) {
            mSourceBitmap.recycle();
        }
        Bitmap bitmap = BitmapUtils.getBitmapFromFile(new File(path), mScreenWidth, mScreenHeight);
        if (bitmap.getWidth() < mScreenWidth / 2 || bitmap.getHeight() < mScreenHeight / 2.0) {
            mSourceBitmap = BitmapUtils.zoomBitmap(bitmap, mScreenWidth, mScreenHeight, true);
        } else {
            mSourceBitmap = bitmap;
        }
        showImageBitmap(mSourceBitmap);
        createBitmapTexture(mSourceBitmap);
    }

    /**
     * 显示图片
     * @param bitmap
     */
    public void showImageBitmap(final Bitmap bitmap) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mWeakImageView != null && mWeakImageView.get() != null) {
                    mWeakImageView.get().setImageBitmap(bitmap);
                }
            }
        });
    }

    /**
     * 使用Bitmap创建Texture
     * @param bitmap
     */
    public void createBitmapTexture(Bitmap bitmap) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_CREATE_TEXTURE, bitmap));
        }
    }

    /**
     * 更新修改后的图像纹理
     */
    public void updateBitmapTexture() {
        if (mCurrentBitmap != null && !mCurrentBitmap.isRecycled()) {
            createBitmapTexture(mCurrentBitmap);
        } else {
            createBitmapTexture(mSourceBitmap);
        }
    }

    /**
     * 更新当前的图片
     */
    public void updateImageBitmap() {
        if (mWeakImageView != null && mWeakImageView.get() != null) {
            mWeakImageView.get().setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mWeakImageView.get().getDrawingCache());
            mWeakImageView.get().setDrawingCacheEnabled(false);
            // TODO 将保存的图片缓存起来，用于实现编辑撤销回删功能
            if (mCurrentBitmap != null) {
                mCurrentBitmap.recycle();
            }
            mCurrentBitmap = bitmap;
        }
    }

    /**
     * 从ImageView中保存图片
     */
    public boolean saveImageFromImageView() {
        if (mWeakImageView != null && mWeakImageView.get() != null) {
            mWeakImageView.get().setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mWeakImageView.get().getDrawingCache());
            mWeakImageView.get().setDrawingCacheEnabled(false);
            String path = ParamsManager.AlbumPath + "CainCamera_" + System.currentTimeMillis() + ".jpg";
            BitmapUtils.saveBitmap(mContext, path, bitmap);
            bitmap.recycle();
            return true;
        }
        return false;
    }

    /**
     * 初始化EGLSurface
     * @param width
     * @param height
     */
    public void initEGLSurface(int width, int height) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_INIT_SURFACE, width, height));
        }
    }

    /**
     * 销毁EGLSurface
     */
    public void destroyEGLSurface() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_DESTROY_SURFACE));
        }
    }

    /**
     * 切换滤镜
     * @param type
     */
    public void changeFilter(GLFilterType type) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_CHANGE_FILTER, type));
        }
    }

    /**
     * 切换滤镜组
     * @param type
     */
    public void changeFilterGroup(GLFilterGroupType type) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_CHANGE_FILTER_GROUP, type));
        }
    }

    /**
     * 设置亮度
     * @param brightness
     */
    public void setBrightness(float brightness) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_SET_BRIGHTNESS, brightness));
        }
    }

    /**
     * 设置对比度
     * @param contrast
     */
    public void setContrast(float contrast) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_SET_CONTRAST, contrast));
        }
    }

    /**
     * 设置曝光
     * @param exposure
     */
    public void setExposure(float exposure) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_SET_EXPOSURE, exposure));
        }
    }

    /**
     * 设置色调 0 ~ 360度
     * @param hue
     */
    public void setHue(float hue) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_SET_HUE, hue));
        }
    }

    /**
     * 设置饱和度 0.0 ~ 2.0之间
     * @param saturation
     */
    public void setSaturation(float saturation) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_SET_SATURATION, saturation));
        }
    }

    /**
     * 设置锐度
     * @param sharpness
     */
    public void setSharpness(float sharpness) {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler
                    .obtainMessage(ImageEditHandler.MSG_SET_SHARPNESS, sharpness));
        }
    }

    /**
     * 重置调节滤镜
     */
    public void resetAdjustFilter() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(ImageEditHandler.MSG_RESET_ADJUST));
        }
    }

    /**
     * 旋转图片
     * @param rotate 0 ~ 360
     */
    public void rotateBitmap(final int rotate) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentBitmap != null) {
                        mCurrentBitmap = BitmapUtils.rotateBitmap(mCurrentBitmap, rotate, true);
                    } else {
                        mCurrentBitmap = BitmapUtils.rotateBitmap(mSourceBitmap, rotate,false);
                    }
                    showImageBitmap(mCurrentBitmap);
                }
            });
        }
    }

    /**
     * 镜像翻转
     */
    public void flipBitmap() {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentBitmap != null) {
                        mCurrentBitmap = BitmapUtils.flipBitmap(mCurrentBitmap, true);
                    } else {
                        mCurrentBitmap = BitmapUtils.flipBitmap(mSourceBitmap, false);
                    }
                    showImageBitmap(mCurrentBitmap);
                }
            });
        }
    }

    /**
     * 裁剪图片
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void cropBitmap(final int x, final int y, final int width, final int height) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCurrentBitmap != null) {
                        mCurrentBitmap = BitmapUtils.cropBitmap(mCurrentBitmap, x, y, width, height, true);
                    } else {
                        mCurrentBitmap = BitmapUtils.cropBitmap(mSourceBitmap, x, y, width, height, false);
                    }
                    showImageBitmap(mCurrentBitmap);
                }
            });
        }
    }
}
