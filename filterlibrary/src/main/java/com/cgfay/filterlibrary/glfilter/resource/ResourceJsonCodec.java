package com.cgfay.filterlibrary.glfilter.resource;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerData;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerFrameData;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerNormalData;
import com.cgfay.utilslibrary.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * json解码器
 */
public class ResourceJsonCodec {

    /**
     * 读取默认动态贴纸数据
     * @param folderPath      json文件所在文件夹路径
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static DynamicSticker decodeStickerData(String folderPath)
            throws IOException, JSONException {
        File file = new File(folderPath, "json");
        String stickerJson = FileUtils.convertToString(new FileInputStream(file));

        JSONObject jsonObject = new JSONObject(stickerJson);
        DynamicSticker dynamicSticker = new DynamicSticker();
        dynamicSticker.unzipPath = folderPath;
        if (dynamicSticker.dataList == null) {
            dynamicSticker.dataList = new ArrayList<>();
        }

        JSONArray stickerList = jsonObject.getJSONArray("stickerList");
        for (int i = 0; i < stickerList.length(); i++) {
            JSONObject jsonData = stickerList.getJSONObject(i);
            String type = jsonData.getString("type");
            DynamicStickerData data;
            if ("sticker".equals(type)) {
                data = new DynamicStickerNormalData();
                JSONArray centerIndexList = jsonData.getJSONArray("centerIndexList");
                ((DynamicStickerNormalData) data).centerIndexList = new int[centerIndexList.length()];
                for (int j = 0; j < centerIndexList.length(); j++) {
                    ((DynamicStickerNormalData) data).centerIndexList[j] = centerIndexList.getInt(j);
                }
                ((DynamicStickerNormalData) data).offsetX = (float) jsonData.getDouble("offsetX");
                ((DynamicStickerNormalData) data).offsetY = (float) jsonData.getDouble("offsetY");
                ((DynamicStickerNormalData) data).baseScale = (float) jsonData.getDouble("baseScale");
                ((DynamicStickerNormalData) data).startIndex = jsonData.getInt("startIndex");
                ((DynamicStickerNormalData) data).endIndex = jsonData.getInt("endIndex");
            } else {
                // 如果不是贴纸又不是前景的话，则直接跳过
                if (!"frame".equals(type)) {
                    continue;
                }
                data = new DynamicStickerFrameData();
                ((DynamicStickerFrameData) data).alignMode = jsonData.getInt("alignMode");
            }
            DynamicStickerData stickerData = data;
            stickerData.width = jsonData.getInt("width");
            stickerData.height = jsonData.getInt("height");
            stickerData.frames = jsonData.getInt("frames");
            stickerData.action = jsonData.getInt("action");
            stickerData.stickerName = jsonData.getString("stickerName");
            stickerData.duration = jsonData.getInt("duration");
            stickerData.stickerLooping = (jsonData.getInt("stickerLooping") == 1);
            stickerData.audioPath = jsonData.optString("audioPath");
            stickerData.audioLooping = (jsonData.optInt("audioLooping", 0) == 1);
            stickerData.maxCount = jsonData.optInt("maxCount", 5);

            dynamicSticker.dataList.add(stickerData);
        }

        return dynamicSticker;
    }

    /**
     * 解码滤镜数据
     * @param folderPath
     * @return
     */
    public static DynamicColor decodeFilterData(String folderPath)
            throws IOException, JSONException {

        File file = new File(folderPath, "json");
        String filterJson = FileUtils.convertToString(new FileInputStream(file));

        JSONObject jsonObject = new JSONObject(filterJson);
        DynamicColor dynamicColor = new DynamicColor();
        dynamicColor.unzipPath = folderPath;
        if (dynamicColor.filterList == null) {
            dynamicColor.filterList = new ArrayList<>();
        }

        JSONArray filterList = jsonObject.getJSONArray("filterList");
        for (int filterIndex = 0; filterIndex < filterList.length(); filterIndex++) {
            DynamicColorData filterData = new DynamicColorData();
            JSONObject jsonData = filterList.getJSONObject(filterIndex);
            String type = jsonData.getString("type");
            // TODO 目前滤镜只做普通的filter，其他复杂的滤镜类型后续在做处理
            if ("filter".equals(type)) {
                filterData.name = jsonData.getString("name");
                filterData.vertexShader = jsonData.getString("vertexShader");
                filterData.fragmentShader = jsonData.getString("fragmentShader");
                // 获取统一变量字段
                JSONArray uniformList = jsonData.getJSONArray("uniformList");
                for (int uniformIndex = 0; uniformIndex < uniformList.length(); uniformIndex++) {
                    String uniform = uniformList.getString(uniformIndex);
                    filterData.uniformList.add(uniform);
                }

                // 获取统一变量字段绑定的图片资源
                JSONObject uniformData = jsonData.getJSONObject("uniformData");
                if (uniformData != null) {
                    Iterator<String> dataIterator = uniformData.keys();
                    while (dataIterator.hasNext()) {
                        String key = dataIterator.next();
                        String value = uniformData.getString(key);
                        filterData.uniformDataList.add(new DynamicColorData.UniformData(key, value));
                    }
                }
                filterData.strength = (float) jsonData.getDouble("strength");
                filterData.texelOffset = (jsonData.getInt("texelOffset") == 1);
                filterData.audioPath = jsonData.getString("audioPath");
                filterData.audioLooping = (jsonData.getInt("audioLooping") == 1);
            }
            dynamicColor.filterList.add(filterData);
        }

        return dynamicColor;
    }

}
