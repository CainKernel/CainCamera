package com.cgfay.camera.render;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.EGLContext;

import com.cgfay.filter.gles.EglCore;
import com.cgfay.filter.gles.WindowSurface;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * 从GPU中读取读取纹理数据
 */
public class GLImageReader {

    // 最大存储图像数
    private static final int MAX_IMAGE_NUMBER = 1;
    private WindowSurface mWindowSurface;
    private EglCore mEglCore;
    private ImageReader mImageReader;
    private ImageReceiveListener mListener;

    private GLImageFilter mImageFilter;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    public GLImageReader(EGLContext context, ImageReceiveListener listener) {
        mListener = listener;
        mEglCore = new EglCore(context, EglCore.FLAG_RECORDABLE);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);

    }

    public void init(int width, int height) {
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, MAX_IMAGE_NUMBER);
            mImageReader.setOnImageAvailableListener(new ImageAvailable(), null);
            mWindowSurface = new WindowSurface(mEglCore, mImageReader.getSurface(), true);
        }
        if (mImageFilter == null) {
            // 创建录制用的滤镜
            mImageFilter = new GLImageFilter(null);
            mImageFilter.onInputSizeChanged(width, height);
            mImageFilter.onDisplaySizeChanged(width, height);
        }
    }

    public void drawFrame(int texture) {
        makeCurrent();
        if (mImageFilter != null) {
            mImageFilter.drawFrame(texture, mVertexBuffer, mTextureBuffer);
        }
        swapBuffers();
    }

    private void makeCurrent() {
        if (mWindowSurface != null) {
            mWindowSurface.makeCurrent();
        }
    }

    private void swapBuffers() {
        if (mWindowSurface != null) {
            mWindowSurface.swapBuffers();
        }
    }

    public void release() {
        makeCurrent();
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        if (mImageFilter != null) {
            mImageFilter.release();
            mImageFilter = null;
        }
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    private class ImageAvailable implements ImageReader.OnImageAvailableListener {


        public ImageAvailable() {
        }

        @Override
        public void onImageAvailable(ImageReader reader) {
            if (mListener == null) {
                return;
            }
            Image image = reader.acquireNextImage();
            Image.Plane[] planes = image.getPlanes();
            int width = image.getWidth();//设置的宽
            int height = image.getHeight();//设置的高
            int pixelStride = planes[0].getPixelStride();//像素个数，RGBA为4
            int rowStride = planes[0].getRowStride();//这里除pixelStride就是真实宽度
            int rowPadding = rowStride - pixelStride * width;//计算多余宽度

            byte[] data = new byte[rowStride * height];
            ByteBuffer buffer = planes[0].getBuffer();
            buffer.get(data);

            int[] pixelData = new int[width * height];

            int offset = 0;
            int index = 0;
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    int pixel = 0;
                    pixel |= (data[offset] & 0xff) << 16;     // R
                    pixel |= (data[offset + 1] & 0xff) << 8;  // G
                    pixel |= (data[offset + 2] & 0xff);       // B
                    pixel |= (data[offset + 3] & 0xff) << 24; // A
                    pixelData[index++] = pixel;
                    offset += pixelStride;
                }
                offset += rowPadding;
            }
            Bitmap bitmap = Bitmap.createBitmap(pixelData,
                    width, height,
                    Bitmap.Config.ARGB_8888);

            image.close();
            mListener.onImageReceive(bitmap);
        }
    }

    /**
     * 图片接受监听器
     */
    public interface ImageReceiveListener {

        void onImageReceive(Bitmap bitmap);
    }

}
