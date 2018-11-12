package com.cgfay.filterlibrary.glfilter.resource;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.utilslibrary.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜数据助手
 */
public final class FilterHelper extends ResourceBaseHelper {
    // 滤镜存储路径
    private static final String FilterDirectory = "Filter";
    // 滤镜列表
    private static final List<ResourceData> mFilterList = new ArrayList<>();

    /**
     * 获取资源列表
     * @return
     */
    public static List<ResourceData> getFilterList() {
        return mFilterList;
    }

    /**
     * 初始化Asset目录下的资源
     * @param context
     */
    public static void initAssetsFilter(Context context) {
        FileUtils.createNoMediaFile(getFilterDirectory(context));
        // 清空旧数据
        mFilterList.clear();
        // 添加滤镜数据
        mFilterList.add(new ResourceData("none", "assets://filter/none.zip", ResourceType.NONE, "none", "assets://thumbs/filter/source.png"));
        mFilterList.add(new ResourceData("amaro", "assets://filter/amaro.zip", ResourceType.FILTER, "amaro", "assets://thumbs/filter/amaro.png"));
        mFilterList.add(new ResourceData("anitque", "assets://filter/anitque.zip", ResourceType.FILTER, "anitque", "assets://thumbs/filter/anitque.png"));
        mFilterList.add(new ResourceData("blackcat", "assets://filter/blackcat.zip", ResourceType.FILTER, "blackcat", "assets://thumbs/filter/blackcat.png"));
        mFilterList.add(new ResourceData("blackwhite", "assets://filter/blackwhite.zip", ResourceType.FILTER, "blackwhite", "assets://thumbs/filter/blackwhite.png"));
        mFilterList.add(new ResourceData("brooklyn", "assets://filter/brooklyn.zip", ResourceType.FILTER, "brooklyn", "assets://thumbs/filter/brooklyn.png"));
        mFilterList.add(new ResourceData("calm", "assets://filter/calm.zip", ResourceType.FILTER, "calm", "assets://thumbs/filter/calm.png"));
        mFilterList.add(new ResourceData("cool", "assets://filter/cool.zip", ResourceType.FILTER, "cool", "assets://thumbs/filter/cool.png"));
        mFilterList.add(new ResourceData("earlybird", "assets://filter/earlybird.zip", ResourceType.FILTER, "earlybird", "assets://thumbs/filter/earlybird.png"));
        mFilterList.add(new ResourceData("emerald", "assets://filter/emerald.zip", ResourceType.FILTER, "emerald", "assets://thumbs/filter/emerald.png"));
        mFilterList.add(new ResourceData("fairytale", "assets://filter/fairytale.zip", ResourceType.FILTER, "fairytale", "assets://thumbs/filter/fairytale.png"));
        mFilterList.add(new ResourceData("freud", "assets://filter/freud.zip", ResourceType.FILTER, "freud", "assets://thumbs/filter/freud.png"));
        mFilterList.add(new ResourceData("healthy", "assets://filter/healthy.zip", ResourceType.FILTER, "healthy", "assets://thumbs/filter/healthy.png"));
        mFilterList.add(new ResourceData("hefe", "assets://filter/hefe.zip", ResourceType.FILTER, "hefe", "assets://thumbs/filter/hefe.png"));
        mFilterList.add(new ResourceData("hudson", "assets://filter/hudson.zip", ResourceType.FILTER, "hudson", "assets://thumbs/filter/hudson.png"));
        mFilterList.add(new ResourceData("kevin", "assets://filter/kevin.zip", ResourceType.FILTER, "kevin", "assets://thumbs/filter/kevin.png"));
        mFilterList.add(new ResourceData("latte", "assets://filter/latte.zip", ResourceType.FILTER, "latte", "assets://thumbs/filter/latte.png"));
        mFilterList.add(new ResourceData("lomo", "assets://filter/lomo.zip", ResourceType.FILTER, "lomo", "assets://thumbs/filter/lomo.png"));
        mFilterList.add(new ResourceData("romance", "assets://filter/romance.zip", ResourceType.FILTER, "romance", "assets://thumbs/filter/romance.png"));
        mFilterList.add(new ResourceData("sakura", "assets://filter/sakura.zip", ResourceType.FILTER, "sakura", "assets://thumbs/filter/sakura.png"));
        mFilterList.add(new ResourceData("sketch", "assets://filter/sketch.zip", ResourceType.FILTER, "sketch", "assets://thumbs/filter/sketch.png"));
        mFilterList.add(new ResourceData("sunset", "assets://filter/sunset.zip", ResourceType.FILTER, "sunset", "assets://thumbs/filter/sunset.png"));
        mFilterList.add(new ResourceData("whitecat", "assets://filter/whitecat.zip", ResourceType.FILTER, "whitecat", "assets://thumbs/filter/whitecat.png"));

        decompressResource(context, mFilterList);
    }

    /**
     * 解压所有资源
     * @param context
     * @param resourceList 资源列表
     */
    public static void decompressResource(Context context, List<ResourceData> resourceList) {
        // 检查路径是否存在
        boolean result = checkFilterDirectory(context);
        // 存放资源路径无法创建，则直接返回
        if (!result) {
            return;
        }

        String filterPath = getFilterDirectory(context);
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
     * 检查滤镜路径是否存在
     * @param context
     */
    private static boolean checkFilterDirectory(Context context) {
        String resourcePath = getFilterDirectory(context);
        File file = new File(resourcePath);
        if (file.exists()) {
            return file.isDirectory();
        }
        return file.mkdirs();
    }

    /**
     * 获取滤镜路径
     * @param context
     * @return
     */
    public static String getFilterDirectory(Context context) {
        String resourcePath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            resourcePath = context.getExternalFilesDir(FilterDirectory).getAbsolutePath();
        } else { // 使用内部存储
            resourcePath = context.getFilesDir() + File.separator + FilterDirectory;
        }
        return resourcePath;
    }

    /**
     * 删除某个滤镜
     * @param context
     * @param resource  资源对象
     * @return          删除操作结果
     */
    public static boolean deleteFilter(Context context, ResourceData resource) {
        if (resource == null || TextUtils.isEmpty(resource.unzipFolder)) {
            return false;
        }
        boolean result = checkFilterDirectory(context);
        if (!result) {
            return false;
        }
        // 获取资源解压的文件夹路径
        String resourceFolder = getFilterDirectory(context) + File.separator + resource.unzipFolder;
        File file = new File(resourceFolder);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        return FileUtils.deleteDir(file);
    }

}
