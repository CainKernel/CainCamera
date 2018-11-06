package com.cgfay.filterlibrary.glfilter.resource.bean;

/**
 * 资源数据
 */
public class ResourceData {

    public String name;         // 名称
    public String zipPath;      // 压缩包路径，绝对路径，"assets://" 或 "file://"开头
    public ResourceType type;   // 资源类型
    public String unzipFolder;  // 解压文件夹名称
    public String thumbPath;    // 缩略图路径

    // 处理文件绝对路径的zip包资源
    public ResourceData(String name, String zipPath, ResourceType type, String unzipFolder, String thumbPath) {
        this.name = name;
        this.zipPath = zipPath;
        this.type = type;
        this.unzipFolder = unzipFolder;
        this.thumbPath = thumbPath;
    }
}
