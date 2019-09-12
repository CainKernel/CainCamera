package com.cgfay.uitls.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作
 * Created by cain on 2017/10/4.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final boolean VERBOSE = false;

    private static final int BUFFER_SIZE = 1024 * 8;

    private FileUtils() {}

    /**
     * 检查文件是否存在
     * @param path
     * @return
     */
    public static boolean fileExists(String path) {
        if (path == null) {
            return false;
        } else {
            File file = new File(path);
            return file.exists();
        }
    }

    /**
     * 检查文件列表是否存在
     * @param paths
     * @return
     */
    public static boolean fileExists(String[] paths) {
        for (String path : paths) {
            if (!fileExists(path)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解码得到文件名
     * @param path
     * @return
     */
    public static String extractFileName(String path) {
        int index = path.lastIndexOf("/");
        return index < 0 ? path : path.substring(index + 1, path.length());
    }

    /**
     * 解码得到文件夹名
     * @param folderPath
     * @return
     */
    public static String extractFileFolder(String folderPath) {
        int length = folderPath.length();
        int index = folderPath.lastIndexOf('/');
        if ((index == -1) || (folderPath.charAt(length - 1) == '/')) {
            return folderPath;
        }
        if ((folderPath.indexOf('/') == index) &&
                (folderPath.charAt(0) == '/')) {
            return folderPath.substring(0, index + 1);
        }
        return folderPath.substring(0, index);
    }

    /**
     * 获取文件后缀
     * @param path
     * @return
     */
    public static String extractFileSuffix(String path) {
        if (path == null) {
            return "";
        }
        int index = path.lastIndexOf('.');
        if (index > -1) {
            return path.substring(index + 1);
        }
        return "";
    }



    /**
     * 从Stream中获取String
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String convertToString(InputStream inputStream)
            throws IOException {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder localStringBuilder = new StringBuilder();
        String str;
        while ((str = localBufferedReader.readLine()) != null) {
            localStringBuilder.append(str).append("\n");
        }
        return localStringBuilder.toString();
    }

    /**
     * 将多行字符串写入文件
     * @param folderPath    文件夹路径
     * @param name          文件名
     * @param stringList    字符串列表
     * @throws IOException
     */
    public static void writeToFile(String folderPath, String name, List<String> stringList)
            throws IOException {
        BufferedOutputStream outputStream = null;
        try {
            File file = createFile(folderPath, name);
            if (null == file) {
                throw new Exception("create file failed");
            }
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            for (int i = 0; i < stringList.size(); i++) {
                outputStream.write(((String) stringList.get(i)).getBytes());
                outputStream.write("\n".getBytes());
            }
        } catch (Exception localException) {
            Log.e(TAG, "writeLinesToFile failed!", localException);
        } finally {
            safetyClose(outputStream);
        }
    }

    /**
     * 从文件中读入每个的字符串
     * @param filePath
     * @return
     * @throws IOException
     */
    public static List<String> readLinesFromFile(String filePath) throws IOException {
        List<String> stringList = new ArrayList<String>();
        File localFile = new File(filePath);
        if (!localFile.exists()) {
            return stringList;
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(localFile)));
            String strLine;
            while ((strLine = bufferedReader.readLine()) != null) {
                stringList.add(strLine);
            }
        } catch (Exception e) {
            Log.e(TAG, "readLinesFromFile failed!", e);
        } finally {
            safetyClose(bufferedReader);
        }
        return stringList;
    }

    /**
     * 关闭Reader
     * @param closeable
     * @return
     */
    public static boolean safetyClose(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException var2) {
                return false;
            }
        }
        return true;
    }

    public static void closeSafely(OutputStream fos) {
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制文件或文件夹
     * @param oldPath
     * @param newPath
     */
    public static void copyFileOrFolder(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (oldFile.isFile()) {
            copyFile(oldPath, newPath);
        } else if (oldFile.isDirectory()) {
            copyFolder(oldPath, newPath);
        }
    }

    /**
     * 复制文件
     * @param oldPath
     * @param newPath
     * @return
     */
    public static void copyFile(String oldPath, String newPath) {
        InputStream is = null;
        FileOutputStream fs = null;
        int sum = 0;
        int len = 0;
        try {
            File oldFile = new File(oldPath);
            // 判断旧文件是否存在，如果存在，则将旧文件复制到新的地址
            if (oldFile.exists()) {
                is = new FileInputStream(oldPath);
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((len = is.read(buffer)) != -1) {
                    sum += len;
                    fs.write(buffer, 0, len);
                }
                fs.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建 .nomedia 文件
     * @param path
     */
    public static void createNoMediaFile(String path) {
        File file = FileUtils.createFile(path, ".nomedia");
        try {
            if (file != null) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "createNoMediaFile:  failed to create nomedia file");
        }
    }

    /**
     * 创建文件
     * @param folderPath
     * @param name
     * @return
     */
    public static File createFile(String folderPath, String name) {
        if ((folderPath == null) || (name == null)) {
            return null;
        }
        if (!makeDirectory(folderPath)) {
            Log.e(TAG, "create parent directory failed, " + folderPath);
            return null;
        }
        String str = folderPath + "/" + name;
        return new File(str);
    }

    /**
     * 创建文件名
     * @param path
     */
    public static void createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建目录/文件
     * @param path
     * @return
     */
    public static boolean makeDirectory(String path) {
        File file = new File(path);
        return file.exists() ? file.isDirectory():file.mkdirs();
    }

    /**
     * 删除文件
     * @param fileName
     */
    public static boolean deleteFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        return deleteFile(new File(fileName));
    }

    /**
     * 删除文件
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        boolean result = true;
        if (null != file) {
            result = file.delete();
        }
        return result;
    }

    /**
     * 删除目录
     * @param path
     */
    public static boolean deleteDir(File path) {
        if (path != null && path.exists() && path.isDirectory()) {
            for (File file : path.listFiles()) {
                if (file.isDirectory())
                    deleteDir(file);
                file.delete();
            }
            return path.delete();
        }
        return false;
    }

    /**
     * 删除目录
     * @param path
     */
    public static boolean deleteDir(String path) {
        if (path != null && path.length() > 0) {
            return deleteDir(new File(path));
        }
        return false;
    }

    /**
     * 复制文件夹
     * @param oldPath
     * @param newPath
     */
    public static void copyFolder(String oldPath, String newPath) {

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            (new File(newPath)).mkdirs();
            File oldFile = new File(oldPath);
            // 获取文件列表
            String[] file = oldFile.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                // 创建新文件时需要添加文件分隔符
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                // 如果是文件，则直接复制，否则递复制子文件夹
                if (temp.isFile()) {
                    input = new FileInputStream(temp);
                    output = new FileOutputStream(newPath + "/" + temp.getName());
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = input.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }
                    output.flush();
                } else if (temp.isDirectory()) {
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 剪切文件
     * @param oldPath
     * @param newPath
     * @return
     */
    public static boolean moveFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return false;
        }

        if (TextUtils.isEmpty(newPath)) {
            return false;
        }

        File file = new File(oldPath);
        return file.renameTo(new File(newPath));
    }


    /**
     * 遍历某个目录下的所有文件，包括子目录下的文件
     * @param path 某个目录的绝对路径
     * @return  返回存放文件绝对路径的列表
     */
    public static List<String> listFolder(String path) {
        List<String> result = new ArrayList<String>();
        File file = new File(path);
        File[] subFile = file.listFiles();
        for (int i = 0; i < subFile.length; i++) {
            if (!subFile[i].isDirectory()) {
                String fileName = subFile[i].getAbsolutePath();
                result.add(fileName);
            } else { // 如果是目录，则递归查找，并返回所有的
                List<String> subPath = listFolder(subFile[i].getAbsolutePath());
                result.addAll(subPath);
            }
        }
        return result;
    }

    /**
     * 从绝对路径中提取文件名
     * @param absolutePath 绝对路径
     * @return  不包含后缀的文件名
     */
    public static String getFileNameFromAbsolutePath(String absolutePath) {
        int start = absolutePath.lastIndexOf("/");
        int end = absolutePath.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return absolutePath.substring(start + 1, end);
        }
        // 防止输入当前路径时，不包含"/"符号，导致返回空字符串
        else if (start == -1 && !absolutePath.contains("/") && end != -1) {
            return absolutePath.substring(0, end);
        }
        return null;
    }


    /**
     * 递归删除文件和文件夹
     *
     */
    public static void recursionDeleteFile(File file) {
        // 文件夹则递归删除
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (File f : childFiles) {
                recursionDeleteFile(f);
            }
            file.delete();

            return;
        }

        file.delete();
    }

    /**
     * 获取某个路径下的所有文件路径
     * @param absolutePath    需要查找的绝对路径
     */
    public static List<String> getAbsolutePathlist(String absolutePath) {
        List<String> fileNames = new ArrayList<String>();
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            // 递归遍历
            for (int i = 0; i < files.length; i++) {
                List<String> names = getAbsolutePathlist(files[i].getAbsolutePath());
                fileNames.addAll(names);
            }
        } else {
            fileNames.add(file.getAbsolutePath());
        }

        return fileNames;
    }

    /**
     * 从文件中读取字符串
     * @param file 文件
     * @return  字符串
     */
    public static String readTextFromFile(File file) {
        String outStr = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr += line;
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            outStr = "";
        }
        return outStr;
    }


    /**
     * 将字符串写入到输出文件中
     * @param outputFile    输出文件
     * @param strInput      需要写入的字符串内容
     * @return
     */
    public static boolean writeTextToFile(File outputFile, String strInput) {
        boolean success = true;
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write(strInput);
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    /**
     * 是否以追加的形式写入内容
     * @param path      路径
     * @param content   内容
     * @param append    是否写入到末尾
     */
    public static void writeFile(String path, String content, boolean append) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(path, append);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据Uri获取路径, 来自aFileChooser:
     * https://github.com/iPaulPro/aFileChooser
     * @param context
     * @param uri
     * @return
     */
    public static String getUriPath(final Context context, final Uri uri) {

        if (VERBOSE)
            Log.d(TAG + " File -",
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (VERBOSE)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}