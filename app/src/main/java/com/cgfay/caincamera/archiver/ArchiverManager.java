package com.cgfay.caincamera.archiver;

import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
/**
 * 压缩/解压管理器
 * Created by cain on 2017/11/16.
 */

public final class ArchiverManager {
    
    private static ArchiverManager mInstance;

    // 当前执行压缩/解压任务
    private BaseArchiver mCurrentArchiver;

    // 线程池
    private Executor mThreadPool;

    public static ArchiverManager getInstance() {
        if (mInstance == null) {
            mInstance = new ArchiverManager();
        }
        return mInstance;
    }

    private ArchiverManager() {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * 执行压缩
     * @param handler   // 需要回调的handler
     * @param files     // 需要压缩的文件
     * @param destPath  // 压缩路径
     */
    public void startArchiving(Handler handler, File[] files, String destPath) {

    }


    /**
     * 执行解压
     * @param handler   // 需要回调的handler
     * @param srcFile           // 压缩包文件路径
     * @param unArchivingPath   // 解压路径
     * @param password          // 解压密码
     * @param listener          // 解压回调监听
     */
    synchronized public void startUnArchiving(Handler handler, final String srcFile,
                                              final String unArchivingPath, final String password,
                                              final ArchiverListener listener) {

        mCurrentArchiver = getArchiver(handler, getArchiverType(srcFile));
        if (mCurrentArchiver != null) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mCurrentArchiver.onUnArchiving(srcFile, unArchivingPath, password, listener);
                }
            });
        }
    }


    /**
     * 获取压缩包类型
     * @param fileName
     * @return
     */
    private String getArchiverType(String fileName) {
        String type;
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String[] paths = fileName.split("\\.");
        type = paths[paths.length - 1];
        return type;
    }

    /**
     * 根据类型获取当前的压缩/解压器类型
     * TODO：7z 格式的解压暂时不做
     * @param archiveType   类型
     * @return
     */
    private BaseArchiver getArchiver(Handler handler, String archiveType) {

        if (archiveType.equalsIgnoreCase("zip")) {
            return new ZipArchiver(handler);
        } else if (archiveType.equalsIgnoreCase("rar")) {
            return new RarArchiver(handler);
        } else {
            return null;
        }
    }
}
