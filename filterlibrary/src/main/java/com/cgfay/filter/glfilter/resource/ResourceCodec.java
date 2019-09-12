package com.cgfay.filter.glfilter.resource;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cgfay.uitls.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 资源解码器
 */
public class ResourceCodec {

    protected static final String TAG = "ResourceCodec";

    // 索引文件路径
    private String mIndexPath;
    // 数据文件路径
    private String mDataPath;

    // 索引绑定
    protected Map<String, Pair<Integer, Integer>> mIndexMap;
    // 数据缓冲
    protected ByteBuffer mDataBuffer;

    public ResourceCodec(String indexPath, String dataPath) {
        mIndexPath = indexPath;
        mDataPath = dataPath;
    }

    /**
     * 初始化
     * @throws Exception
     */
    public void init() throws IOException {
        mIndexMap = parseIndexFile(mIndexPath);
        File file = new File(mDataPath);
        // 将资源数据读入缓存中
        mDataBuffer = ByteBuffer.allocateDirect((int)file.length());
        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[2048];
        boolean result = false;
        try {
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                mDataBuffer.put(buffer, 0, length);
            }
            result = true;
        } catch (IOException e) {
            Log.e(TAG, "init: ", e);
        } finally {
            FileUtils.safetyClose(inputStream);
        }
        if (!result) {
            throw new IOException("Failed to parse data file!");
        }
    }

    /**
     * 解码Index索引文件
     * @param indexPath
     * @return
     * @throws IOException
     */
    private static Map<String, Pair<Integer, Integer>> parseIndexFile(String indexPath) throws IOException {
        String indexString = FileUtils.convertToString(new FileInputStream(new File(indexPath)));
        HashMap<String, Pair<Integer, Integer>> map = new HashMap<>();
        // 拆分得到贴纸索引
        String[] indexArray = indexString.split(";");
        for (int i = 0; i < indexArray.length; ++i) {
            if (!TextUtils.isEmpty(indexArray[i])) {
                // ":" 分成3个，第一个是贴纸名，第二是文件起始位置，第三个是贴纸大小
                String[] subIndexArray = indexArray[i].split(":");
                if (subIndexArray.length == 3) {
                    int offset = parseInt(subIndexArray[1], -1);
                    int length = parseInt(subIndexArray[2], -1);
                    if (-1 == offset || -1 == length) {
                        throw new IOException("Failed to parse offset or length for " + indexArray[i]);
                    }
                    map.put(subIndexArray[0], new Pair<>(offset, length));
                }
            }
        }
        return map;
    }

    /**
     * 将字符串转成整型数
     * @param str
     * @param defaultValue
     * @return
     */
    private static int parseInt(String str, int defaultValue) {
        int result = defaultValue;
        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt: ", e);
        }
        return result;
    }

    /**
     * 获取资源的路径，Pair对象包括索引文件和数据文件
     * @param folder
     * @return
     */
    public static Pair<String, String> getResourceFile(String folder) {
        String index = null;
        String data = null;
        File file = new File(folder);
        String[] list = file.list();
        if (list == null) {
            return null;
        }
        for (int i = 0; i < list.length; ++i) {
            if (list[i].equals("index.idx")) {
                index = list[i];
            } else if (list[i].equals("resource.res")) {
                data = list[i];
            }
        }
        if (!TextUtils.isEmpty(index) && !TextUtils.isEmpty(data)) {
            return new Pair<>(index, data);
        } else {
            return null;
        }
    }

    /**
     * 从zip包中读入png文件列表
     * @param stream
     * @return
     */
    public static Map<String, ArrayList<FileDescription>> getFileFromZip(InputStream stream)
            throws IOException {
        HashMap<String, ArrayList<FileDescription>> map = new HashMap<>();
        ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(stream));
        try {
            byte[] buffer = new byte[8192];
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                // 找到png文件
                if (!entry.isDirectory()
                        && !entry.getName().endsWith(".DS_Store")
                        && !entry.getName().contains("__MACOSX")
                        && !FileUtils.extractFileName(entry.getName()).startsWith(".")
                        && entry.getName().endsWith(".png")) {
                    String folder = FileUtils.extractFileFolder(entry.getName());
                    ArrayList<FileDescription> folderList = map.get(folder);
                    if (folderList == null) {
                        folderList = new ArrayList<>();
                        map.put(folder, folderList);
                    }
                    int offset;
                    int length;
                    for (offset = 0; (length = inputStream.read(buffer)) != -1; offset += length) {
                        ;
                    }
                    folderList.add(new FileDescription(entry.getName(), (long) offset));
                }
            }
        } finally {
            inputStream.close();
        }
        return map;
    }

    /**
     * 将压缩包资源解码到文件中
     * @param inputStream       输入流
     * @param folder            需要写入的文件夹
     * @param dirList           文件夹列表
     * @throws IOException
     */
    public static void unzipToFolder(InputStream inputStream, File folder, Map<String, ArrayList<FileDescription>> dirList)
            throws IOException {
        HashMap<String, Object> offsetMap = new HashMap<>();
        HashMap<String, Object> sizeMap = new HashMap<>();

        Iterator iterator = dirList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            // 做一次文件名的排序
            Collections.sort((List) entry.getValue(), new Comparator<FileDescription>() {
                @Override
                public int compare(FileDescription item1, FileDescription item2) {
                    return item1.fileName.compareTo(item2.fileName);
                }
            });

            // 创建文件或文件夹
            FileUtils.makeDirectory(folder + "/" + (String)entry.getKey());
            int offset = 16;
            HashMap<String, Integer> offsetHashMap = new HashMap<>();
            HashMap<String, Integer> sizeHashMap = new HashMap<>();

            // 记录文件偏移和大小
            FileDescription fileDescription;
            for (Iterator fileDesIterator = ((ArrayList)entry.getValue()).iterator(); fileDesIterator.hasNext(); offset += (int)fileDescription.size) {
                fileDescription = (FileDescription)fileDesIterator.next();
                String fileName = FileUtils.extractFileName(fileDescription.fileName);
                offsetHashMap.put(fileName, offset);
                sizeHashMap.put(fileName, (int)fileDescription.size);
            }

            // 创建索引字符串
            sizeMap.put((String) entry.getKey(), offsetHashMap);
            StringBuilder builder = new StringBuilder();
            Iterator indexIterator = offsetHashMap.entrySet().iterator();
            while (indexIterator.hasNext()) {
                Map.Entry indexEntry = (Map.Entry)indexIterator.next();
                builder.append((String)indexEntry.getKey())
                        .append(':')
                        .append(indexEntry.getValue())
                        .append(':')
                        .append(sizeHashMap.get(indexEntry.getKey()))
                        .append(';');
            }

            // 写入索引文件
            boolean success = false;
            FileOutputStream outputStream = null;
            File file;
            try {
                file = new File(folder + "/" + (String)entry.getKey(), "index.idx");
                outputStream = new FileOutputStream(file);
                outputStream.write(builder.toString().getBytes("UTF-8"));
                success = true;
            } catch (Exception e) {
                Log.e(TAG, "writeLinesToFile failed!", e);
            } finally {
                FileUtils.safetyClose(outputStream);
            }
            if(!success) {
                throw new IOException("write index file failed!");
            }

            // 创建AccessFile用于写入data数据
            file = new File(folder + "/" + (String)entry.getKey(), "resource.res");
            RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
            // 文件头有16个0作为开头
            for (int i = 0; i < 16; ++i) {
                accessFile.write(0);
            }
            offsetMap.put((String) entry.getKey(), accessFile);
        }

        // 从zip包中读入数据并写入到folder所在的文件夹中
        ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(inputStream));
        try {
            byte[] buffer = new byte[8192];
            while (true) {
                ZipEntry zipEntry = zipStream.getNextEntry();
                if (zipEntry == null) {
                    break;
                }
                // 跳过目录、隐藏文件
                if (zipEntry.isDirectory()
                        || zipEntry.getName().endsWith(".DS_Store")
                        || zipEntry.getName().contains("__MACOSX")
                        || FileUtils.extractFileName(zipEntry.getName()).startsWith(".")) {
                    continue;
                }

                // 如果文件是png图片，则需要从png中读入数据再写入到输出文件中
                if (zipEntry.getName().endsWith(".png")) {
                    String folderName = FileUtils.extractFileFolder(zipEntry.getName());
                    RandomAccessFile accessFile = (RandomAccessFile) offsetMap.get(folderName);
                    // 找到起始位置
                    int pos = (Integer) ((Map) sizeMap.get(folderName)).get(FileUtils.extractFileName(zipEntry.getName()));
                    accessFile.seek((long) pos);
                    // 将png数据写入文件中
                    int length;
                    while ((length = zipStream.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, length);
                    }
                } else { // 如果此时已经是索引和data文件的形式，则直接写入
                    File file = new File(folder, zipEntry.getName());
                    File folderFile = zipEntry.isDirectory() ? file : file.getParentFile();
                    if (!folderFile.isDirectory() && !folderFile.mkdirs()) {
                        throw new FileNotFoundException("Failed to find directory: " +
                                folderFile.getAbsolutePath());
                    }

                    FileOutputStream outputStream = new FileOutputStream(file);
                    try {
                        int length;
                        while ((length = zipStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, length);
                        }
                    } finally {
                        outputStream.close();
                    }

                }

            }

        } finally { // 关闭输入流
            zipStream.close();
            Iterator entryIterator = offsetMap.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry entry = (Map.Entry) entryIterator.next();
                FileUtils.safetyClose((Closeable) entry.getValue());
            }
        }
    }

    /**
     * 资源文件描述
     */
    public static class FileDescription {

        // 文件名
        public String fileName;

        // 文件大小
        public long size;

        public FileDescription(String fileName, long size) {
            this.fileName = fileName;
            this.size = size;
        }
    }
}
