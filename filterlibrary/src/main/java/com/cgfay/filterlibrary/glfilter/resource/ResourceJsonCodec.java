package com.cgfay.filterlibrary.glfilter.resource;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorBaseData;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData;
import com.cgfay.filterlibrary.glfilter.effect.bean.DynamicEffect;
import com.cgfay.filterlibrary.glfilter.effect.bean.DynamicEffectData;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupBaseData;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupLipstickData;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupMaterialData;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupNormaData;
import com.cgfay.filterlibrary.glfilter.makeup.bean.MakeupType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerData;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerFrameData;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicStickerNormalData;
import com.cgfay.filterlibrary.glfilter.stickers.bean.StaticStickerNormalData;
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
            }else if ("static".equals(type)) {//静态贴纸
                data = new StaticStickerNormalData();
                ((StaticStickerNormalData) data).alignMode = jsonData.getInt("alignMode");
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
                        filterData.uniformDataList.add(new DynamicColorBaseData.UniformData(key, value));
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

    /**
     * 解码彩妆数据列表，可以是一组彩妆数据，也可能是某个部位的彩妆数据列表
     * @param folderPath json文件所在文件夹路径
     * @return
     */
    public static DynamicMakeup decodeMakeupData(String folderPath)
            throws IOException, JSONException {
        File file = new File(folderPath, "json");
        String makeupJson = FileUtils.convertToString(new FileInputStream(file));

        DynamicMakeup dynamicMakeup = new DynamicMakeup();
        dynamicMakeup.unzipPath = folderPath;
        if (dynamicMakeup.makeupList == null) {
            dynamicMakeup.makeupList = new ArrayList<>();
        }

        JSONObject jsonObject = new JSONObject(makeupJson);
        JSONArray makeupList = jsonObject.getJSONArray("makeupList");
        for (int makeupIndex = 0; makeupIndex < makeupList.length(); makeupIndex++) {
            JSONObject jsonData = makeupList.getJSONObject(makeupIndex);
            String type = jsonData.getString("type");

            MakeupBaseData makeupData;
            if (type.equalsIgnoreCase("lipstick")) { // 唇彩素材
                makeupData = new MakeupLipstickData();
                ((MakeupLipstickData) makeupData).lookupTable = jsonData.getString("lookupTable");
            } else { // 其他彩妆素材
                makeupData = new MakeupNormaData();
                JSONObject jsonMaterial = jsonData.getJSONObject("material");

                MakeupMaterialData materialData = new MakeupMaterialData();
                materialData.name = jsonMaterial.getString("name");
                materialData.width = jsonMaterial.getInt("width");
                materialData.height = jsonMaterial.getInt("height");
                // 提取素材纹理坐标
                JSONArray textureVertices = jsonData.getJSONArray("textureVertices");
                materialData.textureVertices = new float[textureVertices.length()];
                for (int i = 0; i < textureVertices.length(); i++) {
                    materialData.textureVertices[i] = textureVertices.getLong(i);
                }
                // 提取素材索引数据
                JSONArray indices = jsonData.getJSONArray("indices");
                materialData.indices = new short[indices.length()];
                for (int i = 0; i < indices.length(); i++) {
                    materialData.indices[i] = (short) indices.getInt(i);
                }
                ((MakeupNormaData) makeupData).materialData = materialData;
            }
            makeupData.makeupType = MakeupType.getType(type);
            makeupData.name = jsonData.getString("name");
            makeupData.id = jsonData.getString("id");
            makeupData.strength = jsonData.getLong("strength");

            dynamicMakeup.makeupList.add(makeupData);
        }

        return dynamicMakeup;
    }

    /**
     * 解码单个特效数据列表
     * @param folderPath
     * @return
     */
    public static DynamicEffect decodecEffectData(String folderPath)
            throws IOException, JSONException {
        File file = new File(folderPath, "json");
        String effectJson = FileUtils.convertToString(new FileInputStream(file));

        DynamicEffect dynamicEffect = new DynamicEffect();
        dynamicEffect.unzipPath = folderPath;
        if (dynamicEffect.effectList == null) {
            dynamicEffect.effectList = new ArrayList<>();
        }

        JSONObject jsonObject = new JSONObject(effectJson);
        JSONArray effectList = jsonObject.getJSONArray("effectList");
        for (int effectIndex = 0; effectIndex < effectList.length(); effectIndex++) {
            JSONObject jsonData = effectList.getJSONObject(effectIndex);
            String type = jsonData.getString("type");
            if (type.equalsIgnoreCase("effect")) {
                DynamicEffectData effectData = new DynamicEffectData();
                effectData.name = jsonData.getString("name");
                effectData.vertexShader = jsonData.getString("vertexShader");
                effectData.fragmentShader = jsonData.getString("fragmentShader");

                // 解码统一变量和数值
                JSONObject uniformData = jsonData.getJSONObject("uniformData");
                if (uniformData != null) {
                    Iterator<String> dataIterator = uniformData.keys();
                    while (dataIterator.hasNext()) {
                        String key = dataIterator.next();
                        JSONArray effectValue = uniformData.getJSONArray(key);
                        float[] value = new float[effectValue.length()];
                        for (int i = 0; i < effectValue.length(); i++) {
                            value[i] = (float) effectValue.getDouble(i);
                        }
                        effectData.uniformDataList.add(new DynamicEffectData.UniformData(key, value));
                    }
                }
                // 解码统一变量和纹理
                JSONObject uniformSampler = jsonData.getJSONObject("uniformSampler");
                if (uniformSampler != null) {
                    Iterator<String> dataIterator = uniformSampler.keys();
                    while (dataIterator.hasNext()) {
                        String key = dataIterator.next();
                        String value = uniformSampler.getString(key);
                        effectData.uniformSamplerList.add(new DynamicEffectData.UniformSampler(key, value));
                    }
                }
                effectData.texelSize = jsonData.getInt("texelSize") == 1;
                effectData.duration = jsonData.getInt("duration");

                dynamicEffect.effectList.add(effectData);
            }
        }

        return dynamicEffect;
    }
}
