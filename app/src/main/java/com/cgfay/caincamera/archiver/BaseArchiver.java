package com.cgfay.caincamera.archiver;

import android.os.Handler;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 压缩解压基类
 * Created by cain on 2017/11/16.
 */

public abstract class BaseArchiver {

    protected WeakReference<Handler> mWeakHandler;

    public BaseArchiver(Handler handler) {
        mWeakHandler = new WeakReference<Handler>(handler);
    }

    /**
     * 压缩文件
     * @param files     // 需要压缩的文件
     * @param destPath  // 压缩路径
     */
    public abstract void onArchiving(File[] files, String destPath);

    /**
     * 解压文件
     * @param srcFile       // 源文件路径
     * @param unArchivePath // 解压路径
     * @param password      // 解压密码，可以为空
     * @param listener      // 解压回调
     */
    public abstract void onUnArchiving(String srcFile, String unArchivePath,
                                       String password, ArchiverListener listener);


}
