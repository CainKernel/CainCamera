package com.cgfay.cainfilter.archiver;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * RAR压缩/解压器
 * Created by cain on 2017/11/16.
 */

public class RarArchiver extends BaseArchiver {

    private static final String TAG = "RarArchiver";

    private boolean isDebug = false;

    public RarArchiver(Handler handler) {
        super(handler);
    }

    @Override
    public void onArchiving(File[] files, String destPath) {
        // 压缩
    }

    @Override
    public void onUnArchiving(String srcPath, String unArchivePath,
                              String password, final ArchiverListener listener) {
        File srcFile = new File(srcPath);
        if (null == unArchivePath || "".equals(unArchivePath)) {
            unArchivePath = srcFile.getParentFile().getPath();
        }
        // 保证文件夹路径最后是"/"或者"\"
        char lastChar = unArchivePath.charAt(unArchivePath.length() - 1);
        if (lastChar != '/' && lastChar != '\\') {
            unArchivePath += File.separator;
        }
        if (isDebug) {
            Log.d(TAG, "unrar file to :" + unArchivePath);
        }

        if (listener != null && mWeakHandler != null && mWeakHandler.get() != null) {
            mWeakHandler.get().post(new Runnable() {
                @Override
                public void run() {
                    listener.onStartArchiving();
                }
            });
        }

        FileOutputStream fileOut = null;
        Archive rarfile = null;

        try {
            rarfile = new Archive(srcFile,password,false);
            FileHeader fh = null;
            final int total = rarfile.getFileHeaders().size();
            for (int i = 0; i < rarfile.getFileHeaders().size(); i++) {
                fh = rarfile.getFileHeaders().get(i);
                String entrypath;

                if (fh.isUnicode()) {
                    entrypath = fh.getFileNameW().trim();
                } else {
                    entrypath = fh.getFileNameString().trim();
                }

                entrypath = entrypath.replaceAll("\\\\", "/");

                File file = new File(unArchivePath + entrypath);
                if (isDebug) {
                    Log.d(TAG, "unrar entry file :" + file.getPath());
                }

                if (fh.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    fileOut = new FileOutputStream(file);
                    rarfile.extractFile(fh, fileOut);
                    fileOut.close();
                }

                if (listener != null && mWeakHandler != null && mWeakHandler.get() != null) {
                    final int current = i  + 1;
                    mWeakHandler.get().post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProgressArchiving(current, total);
                        }
                    });
                }
            }
            rarfile.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                    fileOut = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (rarfile != null) {
                try {
                    rarfile.close();
                    rarfile = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
}
