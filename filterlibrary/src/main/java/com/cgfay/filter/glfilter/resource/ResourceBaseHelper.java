package com.cgfay.filter.glfilter.resource;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * 资源助手基类
 */
public class ResourceBaseHelper {

    private static String TAG = "ResourceBaseHelper";

    /**
     * 解压Asset文件夹目录下的资源
     * @param context
     * @param assetName     assets文件夹路径
     * @param unzipFolder   解压的文件夹名称
     * @param parentFolder  解压目录
     */
    protected static void decompressAsset(Context context, String assetName, String unzipFolder, String parentFolder) {

        // 如果路径已经存在，则直接返回
        if (new File(parentFolder + "/" + unzipFolder).exists()) {
            Log.d(TAG, "decompressAsset: directory " + unzipFolder + " is existed!");
            return;
        }

        // 打开输入流
        AssetManager manager = context.getAssets();
        InputStream inputStream;
        try {
            inputStream = manager.open(assetName);
        } catch (IOException e) {
            Log.e(TAG, "decompressAsset: ", e);
            return;
        }

        // 获取所有zip包
        Map<String, ArrayList<ResourceCodec.FileDescription>> dirList = null;
        try {
            dirList = ResourceCodec.getFileFromZip(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "decompressAsset: ", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 如果zip包不存在，则不做处理
        if (dirList == null) {
            return;
        }

        // 将zip包解压到目录中
        try {
            inputStream = manager.open(assetName);
        } catch (IOException e) {
            Log.e(TAG, "decompressAsset: ", e);
            return;
        }
        try {
            if (inputStream != null) {
                ResourceCodec.unzipToFolder(inputStream, new File(parentFolder), dirList);
            }
        } catch (IOException e) {
            Log.e(TAG, "decompressAsset: ", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解压绝对路径目录下的资源
     * @param zipPath       zip绝对路径
     * @param unzipPath     解压的目录
     * @param parentFolder  解压目录
     */
    protected static void decompressFile(String zipPath, String unzipPath, String parentFolder) {
        // 如果资源路径已经存在，则直接返回
        if (new File(parentFolder + "/" + unzipPath).exists()) {
            Log.d(TAG, "decompressFile: directory " + unzipPath + "is existed!");
            return;
        }

        // 打开文件输入流
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(zipPath);
        } catch (IOException e) {
            Log.e(TAG, "decompressFile: ", e);
            return;
        }

        // 获取所有zip包
        Map<String, ArrayList<ResourceCodec.FileDescription>> dirList = null;
        try {
            dirList = ResourceCodec.getFileFromZip(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "decompressFile: ", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 如果zip包不存在，则不做处理
        if (dirList == null) {
            return;
        }

        // 将zip包解压到目录中
        try {
            inputStream = new FileInputStream(zipPath);
        } catch (IOException e) {
            Log.e(TAG, "decompressFile: ", e);
            return;
        }
        try {
            ResourceCodec.unzipToFolder(inputStream, new File(parentFolder), dirList);
        } catch (IOException e) {
            Log.e(TAG, "decompressFile: ", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
