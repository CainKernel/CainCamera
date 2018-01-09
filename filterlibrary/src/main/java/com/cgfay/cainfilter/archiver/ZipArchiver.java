package com.cgfay.cainfilter.archiver;

import android.os.Handler;
import android.text.TextUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;

/**
 * ZIP压缩/解压器
 * Created by cain on 2017/11/16.
 */
public class ZipArchiver extends BaseArchiver {

    private static final String TAG = "ZipArchiver";

    public ZipArchiver(Handler handler) {
        super(handler);
    }

    @Override
    public void onArchiving(File[] files, String destPath) {

    }

    @Override
    public void onUnArchiving(String srcFile, String unArchivePath,
                              String password, final ArchiverListener listener) {
        if (TextUtils.isEmpty(srcFile) || TextUtils.isEmpty(unArchivePath))
            return;
        File src = new File(srcFile);
        if (!src.exists())
            return;
        try {
            ZipFile zFile = new ZipFile(srcFile);
            zFile.setFileNameCharset("GBK");

            if (!zFile.isValidZipFile()) {
                throw new ZipException("zip file is illegal!");
            }

            File destDir = new File(unArchivePath);
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdirs();
            }

            if (zFile.isEncrypted()) {
                zFile.setPassword(password.toCharArray());
            }

            if (listener != null && mWeakHandler != null && mWeakHandler.get() != null) {
                mWeakHandler.get().post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStartArchiving();
                    }
                });
            }

            FileHeader fileHeader;
            final int sum = zFile.getFileHeaders().size();
            for (int i = 0; i < sum; i++) {
                fileHeader = (FileHeader) zFile.getFileHeaders().get(i);

                zFile.extractFile(fileHeader, unArchivePath);
                if (listener != null && mWeakHandler != null && mWeakHandler.get() != null) {
                    final int current = i  + 1;
                    mWeakHandler.get().post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProgressArchiving(current, sum);
                        }
                    });
                }
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
        if (listener != null && mWeakHandler != null && mWeakHandler.get() != null) {
            mWeakHandler.get().post(new Runnable() {
                @Override
                public void run() {
                    listener.onFinishArchiving();
                }
            });

        }
    }
}