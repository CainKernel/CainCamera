package com.cgfay.filter.glfilter.resource;

import android.content.Context;
import android.os.Environment;

import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.filter.glfilter.resource.bean.ResourceType;
import com.cgfay.uitls.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 彩妆数据助手
 */
public final class MakeupHelper extends ResourceBaseHelper {

    private static final String MakeupDirectory = "Makeup";

    // 彩妆列表
    private static final List<ResourceData> mMakeupList = new ArrayList<>();

    /**
     * 获取彩妆列表
     * @return
     */
    public static List<ResourceData> getMakeupList() {
        return mMakeupList;
    }

    /**
     * 初始化Asset目录下的资源
     * @param context
     */
    public static void initAssetsMakeup(Context context) {
        FileUtils.createNoMediaFile(getMakeupDirectory(context));
        // 清空旧数据
        mMakeupList.clear();
        // 添加彩妆数据
        mMakeupList.add(new ResourceData("none", "assets://makeup/none.zip", ResourceType.NONE, "none", "assets://thumbs/makeup/none.png"));
        mMakeupList.add(new ResourceData("ls01", "assets://makeup/ls01.zip", ResourceType.MAKEUP, "ls01", "assets://thumbs/makeup/ls01.png"));

        decompressResource(context, mMakeupList);
    }

    /**
     * 解压所有资源
     * @param context
     * @param resourceList
     */
    private static void decompressResource(Context context, List<ResourceData> resourceList) {
        // 检查路径是否存在
        boolean result = checkMakeupDirectory(context);
        // 存放资源路径无法创建，则直接返回
        if (!result) {
            return;
        }

        String filterPath = getMakeupDirectory(context);
        // 解码列表中的所有资源
        for (ResourceData item : resourceList) {
            if (item.type.getIndex() >= 0) {
                if (item.zipPath.startsWith("assets://")) {
                    decompressAsset(context, item.zipPath.substring("assets://".length()), item.unzipFolder, filterPath);
                } else if (item.zipPath.startsWith("file://")) {    // 绝对目录中的资源
                    decompressFile(item.zipPath.substring("file://".length()), item.unzipFolder, filterPath);
                }
            }
        }
    }

    /**
     * 检查彩妆路径是否存在
     * @param context
     * @return
     */
    private static boolean checkMakeupDirectory(Context context) {
        String resourcePath = getMakeupDirectory(context);
        File file = new File(resourcePath);
        if (file.exists()) {
            return file.isDirectory();
        }
        return file.mkdirs();
    }

    /**
     * 获取彩妆路径
     * @param context
     * @return
     */
    public static String getMakeupDirectory(Context context) {
        String resourcePath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            resourcePath = context.getExternalFilesDir(MakeupDirectory).getAbsolutePath();
        } else { // 使用内部存储
            resourcePath = context.getFilesDir() + File.separator + MakeupDirectory;
        }
        return resourcePath;
    }

}
